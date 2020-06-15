package control;

import sick.LMS400;

public class Console {

	public static void main(String [] args) {
		
		LMS400 lms = new LMS400();
		
		System.out.println("CONNECT: " + java.time.LocalTime.now());
		
		lms.connect();
		
		System.out.println("START: " + java.time.LocalTime.now());  
		
		lms.scan_start();
			
		System.out.println("MEASURE: " + java.time.LocalTime.now());
		
		lms.measure(1);
			
		System.out.println("STOP: " + java.time.LocalTime.now());
			
		lms.scan_stop();
			
		System.out.println("DISCONECT: " + java.time.LocalTime.now());  
				
		lms.process();
			
		String data = "BOXANDDOC";
			
		if(lms.save_data(data)) System.out.println("ZAPISANO: " + data);
			
		lms.disconnect();
		
	}
	

	


	
	
}
