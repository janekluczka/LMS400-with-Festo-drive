package festo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class CMMS_AS {
	
	private final String portname;
		
	private SerialPort port = null;
	private OutputStream out = null;
	private InputStream in = null;
	
	private boolean connected = false;
	
	//CONSTRUCTOR
	
	//CMMS_AS
	//DEFAULT
	public CMMS_AS() {
		this.portname = "COM3";
	}
	
	//CMMS_AS
	//SETS PORTNAME
	public CMMS_AS(String p) {
		this.portname = p;
	}
	
	//GET
	
	public boolean get_connected() {
		return this.connected;
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
		
		if(connected)
			return false;
		
		CommPortIdentifier pi = null;
		SerialPort sp = null;
		OutputStream o = null;
		InputStream i = null;
		
		try {
			
			//GET SERIAL PORT IDENTIFIER
			pi = CommPortIdentifier.getPortIdentifier(this.portname);
			//OPEN SERIAL PORT
			sp = (SerialPort) pi.open("CMMS_AS", 2000);
			//SET SERIAL PORT PARAMETERS
			sp.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			//GET OUTPUT STREAM
			//o = new PrintWriter(sp.getOutputStream());
			o = sp.getOutputStream();
			//GET INPUT STREAM
			i = sp.getInputStream();			
			
		} catch (NoSuchPortException e) {
			e.printStackTrace();
			return false;
		} catch (PortInUseException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		this.port = sp;
		this.out = o;
		this.in = i;
		this.connected = true;
		
		return true;
		
	}
	
	//DISCONNECT
	//DISCONNECTS FROM DEVICE
	//RETURN TRUE IF DONE
	//RETURN FALSE IF ALREADY DISCONNECTED 
	public boolean disconnect() {
		
		if(!connected)
			return false;
		
		//CLOSE INPUTSTREAM
		if(this.in != null) {
			try {
				this.in.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		//CLOSE OUTPUTSTREAM
		if(this.out != null) {
			try {
				this.out.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		//CLOSE SERIAL PORT
		if(this.port != null)
				this.port.close();

		this.connected = false;
		
		return true;
		
	}
	
	//SEND
	//SENDS CMD WHICH CONTAINS MI SI AND DATA
	//RETURN TRUE IF DONE
	//RETURN FALSE IF NOT CONNECTED
	public boolean send(String mi, char si, String data) {
		
		//SAFETY
		if(!connected)
			return false;
		
		//SEND EACH 2 CHARS OF MAIN INDEX
		for(int i=0; i<mi.length(); i++) {
			try{
				this.out.write(mi.charAt(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//SEND EACH CHAR OF SUB INDEX
		try {
			this.out.write(si);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//SEND EACH CHAR OF SUB INDEX
		try{
			this.out.write(':');
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//SEND EACH CHAR OF N BIT DATA
		for(int i=0; i<8; i++) {
			try{
				this.out.write(data.charAt(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	
		try{
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
		
	}
	
	//RECEIVE
	//SAVES RECEIVED DATA TO STRING
	public String receive() {
		
		//SAFETY
		if(!connected)
			return null;
		
		StringBuffer b = new StringBuffer();
		int c = 0;
		
		//SAVE RECEIVED CHAR TO STRINGBUFFER
		while(c != -1) {
			try {
				c = this.in.read();
				b.append(c);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		
		return b.toString();	
	}

}
