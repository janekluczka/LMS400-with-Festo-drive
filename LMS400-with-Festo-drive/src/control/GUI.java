package control;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.event.*;

import sick.LMS400;

public class GUI implements ActionListener {

	private JButton lms_connection;
	private JButton lms_scan;
	private JButton lms_reset;
	private JButton lms_save;
	private JButton lms_delete;
	private JButton one_scan;
	private JButton scan_01s;
	private JButton scan_10s;
	private JButton scan_30s;
	private JButton scan_01m;
	
    private JTextField lms_filename;
		
	private int N = 1;	
	private LMS400 lms = null;
	
	//CONSTRUCTOR
	public GUI() {
		this.lms = new LMS400();
	}
	
	//WINDOW CREATOR
	public void create_window() {
		
		JFrame frame = new JFrame();
		
		JPanel communication = new JPanel();
		JPanel scan_n = new JPanel();
		JPanel status = new JPanel();
		
		lms_connection = new JButton("Connect");
		lms_scan = new JButton("Scan");
		lms_reset = new JButton("Reset");
		lms_save = new JButton("Save");
		lms_delete = new JButton("Delete");
		
		one_scan = new JButton("1 scan");
		scan_01s = new JButton("1 second");
		scan_10s = new JButton("10 seconds");
		scan_30s = new JButton("30 seconds");
		scan_01m = new JButton("1 minute");
		
		lms_filename = new JTextField(20);
		
	    lms_connection.addActionListener(this);
		lms_scan.addActionListener(this);
		lms_reset.addActionListener(this);
		
		one_scan.addActionListener(this);
		scan_01s.addActionListener(this);
		scan_10s.addActionListener(this);
		scan_30s.addActionListener(this);
		scan_01m.addActionListener(this);
		
		lms_scan.setEnabled(false);
		lms_reset.setEnabled(false);
		lms_save.setEnabled(false);
		lms_delete.setEnabled(false);
			
		//PLACE PANELS
		frame.getContentPane().add(BorderLayout.NORTH, communication);
		frame.getContentPane().add(BorderLayout.CENTER, scan_n);
		frame.getContentPane().add(BorderLayout.SOUTH, status);
		
		//SET LAYOUTS
		communication.setLayout(new GridLayout(1,3,10,0));
		scan_n.setLayout(new GridLayout(1,5,10,0));
		status.setLayout(new GridLayout(3,1,0,10));
		
		//SET BORDERS
		communication.setBorder(new EmptyBorder(10,10,10,10));
		scan_n.setBorder(new EmptyBorder(0,10,10,10));
		status.setBorder(new EmptyBorder(0,10,10,10));
		
		//ADD
		communication.add(lms_connection);
		communication.add(lms_scan);
		communication.add(lms_reset);
		
		scan_n.add(one_scan);
		scan_n.add(scan_01s);
		scan_n.add(scan_10s);
		scan_n.add(scan_30s);
		scan_n.add(scan_01m);
		
		status.add(lms_filename);
		status.add(lms_save);
		status.add(lms_delete);
		
		frame.setTitle("Menu");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);;
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);		
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		
		//CONNECTION
		if(event.getSource() == lms_connection) {
			if(this.lms.connect_or_disconnect()) {
				lms_scan.setEnabled(true);
				lms_reset.setEnabled(true);
			} else {
				lms_scan.setEnabled(false);
				lms_reset.setEnabled(false);
			}
		}
		
		//SCAN
		if(event.getSource() == lms_scan) {
			if(lms.scan(this.N)) {
				lms.process();
				lms_scan.setEnabled(false);
				lms_reset.setEnabled(false);
				lms_save.setEnabled(true);
				lms_delete.setEnabled(true);
			}
		}
		
		//RESET
		if(event.getSource() == lms_reset) {
			lms.set_reset(true);
		}
		
		//SAVE DATA
		if(event.getSource() == lms_save) {
			String name = lms_filename.getText();
			
			if(name == null)
				lms.save_data();
			else
				lms.save_data(name);
			
			lms.delete_scandata();
			lms.delete_stringdata();
			lms_scan.setEnabled(true);
			lms_reset.setEnabled(true);
			lms_save.setEnabled(false);
			lms_delete.setEnabled(false);
		}
		
		//DELETE DATA
		if(event.getSource() == lms_delete) {
			lms.delete_scandata();
			lms.delete_stringdata();
			lms_scan.setEnabled(true);
			lms_reset.setEnabled(true);
			lms_save.setEnabled(false);
			lms_delete.setEnabled(false);
		}
		
		//SET N VALUE
		if(event.getSource() == one_scan) {
			this.N = 1;
		}
		
		if(event.getSource() == scan_01s) {
			this.N = 190;
		}
		
		if(event.getSource() == scan_10s) {
			this.N = 1900;
		}
		
		if(event.getSource() == scan_30s) {
			this.N = 5700;
		}
		
		if(event.getSource() == scan_01m) {
			this.N = 11400;
		}
		
	}
	
}

