package sick;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class LMS400 extends Thread {

	private final String hostname;
	private final int port;
	
	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;

	private boolean connected = false;
	private boolean working = false;
	private boolean reset = false;
	
	private ArrayList<String> stringdata;
	private ArrayList<ArrayList<Point>> scandata;
	
	//CONSTRUCTOR
	
	//LMS400
	//DEFAULT
	public LMS400() {
		this.hostname = "192.168.0.1";
		this.port = 2111;
		
		stringdata = new ArrayList<String>();
		scandata = new ArrayList<ArrayList<Point>>();
	}
	
	//LMS400
	//SETS IP AND PORT
	public LMS400(String h, int p) {
		this.hostname = h;
		this.port = p;
		
		stringdata = new ArrayList<String>();
		scandata = new ArrayList<ArrayList<Point>>();
	}
	
	//SET
	
	public void set_reset(boolean r) {
		this.reset = r;
	}
	
	//GET
	
	public boolean get_connected() {
		return this.connected;
	}
		
	public boolean get_working() {
		return this.working;
	}
	
	public boolean get_reset() {
		return this.reset;
	}
	
	public ArrayList<Point> get_scandata(int index){
		return scandata.get(index);
	}
	
	//DELETE
	
	public boolean delete_stringdata() {
		stringdata.clear();
		return true;
	}
	
	public boolean delete_scandata() {
		scandata.clear();
		return true;
	}
	
	//CONNECT_OR_DISCONNECT
	//CONNECTS OR DISCONNECTS DEPENDING ON CONNECTION STATUS
	//COMBINES CONNECT AND DISCONNECT FUNCTIONS
	//RETURNS CONNECTED VALUE
	public boolean connect_or_disconnect() {
		if(this.connected) {
			disconnect();
		} else {
			connect();	
		}
		
		return this.connected;
	}
	
	//CONNECT
	//CONNECTS TO DEVICE
	//RETURN TRUE IF DONE
	//RETURN FALSE IF ALREADY CONNECTED
	public boolean connect() {
		
		//SAFETY
		if(connected)
			return false;
		
		Socket s = null;
		PrintWriter o = null;
		BufferedReader i = null;
		
		//TRY CONNECTING
		try {
			s = new Socket(this.hostname, this.port);
			o = new PrintWriter(s.getOutputStream(), true);
			i = new BufferedReader(new InputStreamReader(s.getInputStream()));			
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {			
			return false;
		}
		
		this.socket = s;
		this.out = o;
		this.in = i;
		this.connected = true;
		
		return true;
		
	}
	
	//DISCONNECT
	//DISCONNECTS FROM DEVICE
	//RETURN TRUE IF DONE
	//RETURN FALSE IF ALREADY DISCONNECTED
	public boolean disconnect () {
		
		//SAFETY
		if(!connected)
			return false;
		
		//CLOSE IN
		if(this.in != null) {
			try {
				this.in.close();
			} catch (final IOException e) {
				return false;
			}
		}

		//CLOSE OUT
		if(this.out != null)
			this.out.close();

		//CLOSE SOCKET
		if(this.socket != null) {
			try {
				this.socket.close();
			} catch (final IOException e) {
				return false;
			}
		}

		//SET IN, OUT AND SOCKET AS NULL AND CONNECTED AS FALSE
		this.in = null;
		this.out = null;
		this.socket = null;
		this.connected = false;
	
		return true;
		
	}
	
	//SCAN
	//PERFORMS STATED NUMBER OF SCANS
	//USES COMMAND AND MEASURE FUNCTIONS
	//RETURN TRUE IF SCANNED SUCCESFULLY
	//RETURN FALSE IF NOT CONNECTED OR IF RESET WAS USED
	public boolean scan(int N) {
		
		//CHECK CONNECTION
		if(!connected) return false;
		
		//SEND COMMAND TO ACTIVATE 
		scan_start();
		
		//PERFORM REQUIRED NUMBER OF SCANS
		measure(N);
		
		//SEND COMMAND TO DEACTIVATE
		scan_stop();
		
		if(reset) {
			reset = false;
			return false;
		}
		
		return true;
		
	}
	
	//SCAN_START
	//STARTS SCANNING BY SENDING COMMAND AND CHECKING LMS400 ASWERS
	//RETURN TRUE IF STARTED
	//RETURN FALSE IF ERROR
	public boolean scan_start() {
		if(command("sMN mLRreqdata 0021", "sMA mLRreqdata", "sAN mLRreqdata 00000000")) { 
			this.working = true;
			return true;
		} else 
			return false;
	}
	
	//SCAN_STOP
	//STOPS SCANNING BY SENDING COMMAND AND CHECKING LMS400 ASWERS
	//RETURN TRUE IF STOPPED
	//RETURN FALSE IF ERROR
	public boolean scan_stop() {
		if(command("sMN mLRstopdata", "sMA mLRstopdata", "sAN mLRstopdata 00000000")) {
			this.working = false;
			return true;
		} else 
			return false;
	}
	
	//COMMAND	
	//SEND SOPAS METHOD BY NAME "sMN" AND RECEIVE SOPAS METHOD ACKNOWLEDGED AND SOPAS ANSWER "sAN"
	//USES SEND RECEIVE AND CHECK FUNCTIONS
	//RETURN TRUE IF RECEIVED SOPAS SOPAS METHOD ACKNOWLEDGED AND SPOAS ANSWER
	//RETURN FALSE IF RECEIVED SOPAS FAULT ANSWER "sFA" OR DEVICE IS DISCONNECTED
	public boolean command(String cmd, String a1, String a2) {
		
		//CHECK CONNECTION
		if(!connected) return false;
		
		send(cmd);
		
		while (true) {
			
			String ans1 = receive();
				
			if(check(ans1, a1) == 1) {
				
				while (true) {
					
					String ans2 = receive();
					
					if(check(ans2, a2) == 1)
						return true;
					else if (check(ans1, a1) == -1)
						return false;
					
				}
			} else if (check(ans1, a1) == -1)
				return false;
		}
	}

	//SEND
	//SENDS CMD IN FRAME
	//FRAME BEGINING 4 TIMES STX, 4 BYTES OF LENGTH IN HEX
	//FRAME END CHECKSUM
	private void send (String cmd) {
		
		char checksum = 0;
		int len = cmd.length();
		
		//SEND STX
		this.out.write(0x02);
		this.out.write(0x02);
		this.out.write(0x02);
		this.out.write(0x02);
		
		//SEND LENGTH AS HEX
		for(int i=0;i<4;i++) {
			int temp = len / (int) Math.pow(16, (6 - 2*i));
			this.out.write(temp);
			len = len % (int) Math.pow(16, (6 - 2*i));	
		}
		
		//SEND TELEGRAM AND CALCULLATE CHECKSUM
		for(int i = 0; i < cmd.length(); i++) {
			this.out.write(cmd.charAt(i));
			checksum = (char) (checksum ^ cmd.charAt(i));
		}
		
		//SEND CHECKSUM
		this.out.write(checksum);
		this.out.flush();
		
	}
	
	//RECEIVE
	//RECEIVES ANSWER
	//RETURNS LENGTH AND TELEGRAM AS STRING	
	public String receive() {
		
		//CHECK CONNECTION
		if(!connected)
			return null;
		
		StringBuffer b = new StringBuffer();
		int c = 0;
		int len = 0;
		
		//SKIP STX
		for(int i = 0; i < 4; i++) {
			try {
				c = this.in.read();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		
		//COUNT LENGTH AND SAVE
		for(int i = 0; i < 4; i++) {
			try {
				c = this.in.read();
				b.append((char)c);
				len = len + (c * (int) Math.pow(16, (6 - 2*i)));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		
		//SAVE TELEGRAM
		for(int i = 0; i < len;i++) {
			try {
				c = this.in.read();
				b.append((char)c);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		
		//GET CHECKSUM
		try {
			c = this.in.read();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		
		//RETURN RECEIVED MESSAGE AS STRING
		return b.toString();
		
	}
	
	//CHECK
	//CHECKS RECEIVED ANSWER WITH EXPECTED ANSWER
	//RETURN 1 IF EXPECTED
	//RETURN 0 IF UNEXPECTED
	//RETURN -1 IF ERROR	
	private int check(String rec, String ans) {
		
		if(rec.contains(ans))
			return 1;
		else if(rec.contains("sFA FF"))
			return -1;
		else {
			stringdata.add(rec);
			return 0;
		}
		
	}
	
	//MEASURE
	//GET AND SAVE ONE SCAN DATA
	//RETURN TRUE IF DONE
	public boolean measure() {
		
		//RECEIVE STRING
		String ans = receive();
		
		//ADD RECEIVED STRING TO STRINGDATA ARRAYLIST
		stringdata.add(ans);
		
		return true;
		
	}
	
	//MEASURE
	//GET AND SAVE ONE SCAN DATA
	//RETURN TRUE IF DONE
	//RETRUN FALSE IF RESET WAS USED
	public boolean measure(int N) {
		
		for(int i = 0; i < N; i++) {
		
			//RECEIVE STRING
			String ans = receive();
			
			//ADD RECEIVED STRING TO STRINGDATA ARRAYLIST
			stringdata.add(ans);
			
			if(reset) {
				stringdata.clear();
				return false;
			}
		
		}
		
		return true;
		
	}
	
	//PROCESS
	//USES PROCESS_STRINGDATA FOR ALL STRINGDATA RECEIVED THEN CLEARS STRINGDATA
	//RETURN TRUE IF DONE
	public boolean process() {
		
		for(int i = 0; i < stringdata.size(); i++)
			process_stringdata(stringdata.get(i));
		
		return true;
		
	}

	//PROCESS_STRINGDATA
	//PROCESS DATA FROM STRING TO ARRAYLIST OF POINTS FOR ONE SCAN
	private void process_stringdata(String rec) {
		
		ArrayList<Point> data= new ArrayList<Point>();
				
		int c = 0; 		//CHAR
		int i = 0;		//ITERATOR FOR RECEIVED STRING
		int n = 0;		//ITERATOR FOR POINTS DATA
		int len = 0;	//TELEGRAM LENGTH
		int SA = 0;		//STARTING ANGLE
		int AR = 0;		//ANGULAR RESOLUTION
		int N = 0;		//NUMBER OF POINTS
		
		//COUNT LENGHT "len"
		while(i < 4) {
			
			c = rec.charAt(i);
			len = len + (c * ((int) Math.pow(16, 6 - 2*i)));
			i++;
			
		}
		
		//SKIP FORMAT AND SCALING
		i = i + 4;
		
		//COUNT STARTING ANGLE "SA"
		while(i < 12) {
			
			int j = i - 8;
			c = rec.charAt(i);
			SA = SA + (c * ((int) Math.pow(16, 2*j)));
			i++;
			
		}
		
		//COUNT ANGULAR RESOLUTION "AR"
		while(i < 14) {
			
			int j = i - 12;
			c = rec.charAt(i);
			AR = AR + (c * ((int) Math.pow(16, 2*j)));
			i++;
			
		}
		
		//COUNT NUMBER OF POINTS "N"
		while(i < 16) {
			
			int j = i - 14;
			c = rec.charAt(i);
			N = N + (c * ((int) Math.pow(16, 2*j)));
			i++;
			j++;
			
		}
		
		//SKIP SCANNING FREQUENCY, REEMISION SCALING, REEMISION START AND END VALUES
		i = i + 8;
		
		//COUNT POINTS
		while(n < N) {
			
			int temp1 = rec.charAt(i);
			i++;
			
			int temp2 = rec.charAt(i);
			i++;
			
			int dis = temp2 * 256 + temp1; 
			
			if(dis > 3000 || dis < 700)
				dis = 0;
			
			int ang = SA + (AR * n);
			
			//ADD NEW POINT TO ARRAYLIST OF POINTS
			data.add(new Point(dis, ang));
			n++;
			
		}
		
		//ADD ARRAYLIST OF POINTS TO ARRAYLIST OF ARRAYLISTS OF POINTS
		scandata.add(data);
		
	}
		
	//PRINT_DATA 
	//PRINTS DATA FOR ALL SCANDATA PROCESSED
	//RETURN TRUE IF DONE
	//RETURN FALSE IF NO SCANDATA AVAILABLE
	public boolean print_data() {
		
		if(scandata.size() == 0)
			return false;
		else {
			
			for(int i = 0; i < scandata.size(); i++) {
				System.out.print("Scan " + i + " ");
				
				for(int j = 0; j < scandata.get(i).size(); j++) {
					System.out.print("("+ (int) scandata.get(i).get(j).getX() + ","+ (int) scandata.get(i).get(j).getX() + ") ");
				}
				
				System.out.println();
			}
			
			return true;
			
		}
	}
	
	//SAVE_DATA
	//SAVES DATA TO LMS400_SCANDATA.TXT FILE
	//RETURN TRUE IF DONE
	//RETURN FALSE IF NO DATA OR IOEXCEPTION
	public boolean save_data() {
		
		if(scandata.size() == 0)
			return false;
		else {
			
			try {
				
				PrintStream ps = new PrintStream(new FileOutputStream("D://Ja//Studia//Projekty//Eclipse//MEASUREMENT_SYSTEM//LMS400_scandata.txt"));
				
				for(int i = 0; i < scandata.size(); i++)
					for(int j = 0; j < scandata.get(i).size(); j++)
						ps.println(scandata.get(i).get(j).getX() + " " + (double) i + " " + (0 - scandata.get(i).get(j).getY()));
				
				ps.close();
						
			} catch(IOException e) {
				return false;
			} 
			
			return true;
			
		}
	}
	
	
	//SAVE_DATA
	//SAVES DATA TO FILENAME.TXT FILE
	//RETURN TRUE IF DONE
	//RETURN FALSE IF NO DATA OR IOEXCEPTION
	public boolean save_data(String filename) {
		
		if(scandata.size() == 0)
			return false;
		else {
			
			String filepath = "D://Ja//Studia//Projekty//Eclipse//MEASUREMENT_SYSTEM//" + filename + ".txt";
			
			try {
				
				PrintStream ps = new PrintStream(new FileOutputStream(filepath));
				
				for(int i = 0; i < scandata.size(); i++)
					for(int j = 0; j < scandata.get(i).size(); j++)
						ps.println(scandata.get(i).get(j).getX() + " " + (double) i + " " + (0 - scandata.get(i).get(j).getY()));
				
				ps.close();
						
			} catch(IOException e) {
				return false;
			} 
			
			return true;
			
		}
	}

	//SIMULATE_SCAN
	//SIMULATES N NUMBER OF SCANS, EACH CONTAINS LEN NUMBER OF POINTS
	//RETURN TRUE IF DONE
	public boolean simulate_scan(int N) {
		
		for(int i=0; i<N; i++) {
			
			ArrayList<Point> data= new ArrayList<Point>();
				
			for(int j=0; j<280; j++) {
				int dis = 1500;
				int ang = 550000 + (2500*j);
				data.add(new Point(dis,ang));
			}
			
			if(this.reset == true) break;
			
			scandata.add(data);
			
		}
		
		return true;
		
	}
	
}

