package readers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Toolsclasses.Record_factory;

import main.Main_Window;



/*
 * Onetouch Ultra class
 * assumes : - connect to device
 * 			 - read serial ID and got it to main window
 * 			 - read all records from device memory, and return them to main window
 */
public class OTUltra extends Meter implements ActionListener {
	private String m_port = "";
	public Boolean connected = false;
	public Main_Window caller = null;
	private Meter myself = null;
	public  int state = -1;
	private int expected = 0;
	private int processed = 0;
	private String Logtext = "";
	
	private SerialIF serial = null;
	
	//It's a subclass of Meter, implementing specifics methods for Onetouch Ultra reader
	public OTUltra(Main_Window caller, String reader_port)  {
		super(caller, reader_port);
		
		myself = this;
		this.caller = get_caller();
		this.m_port = get_port();
		connected = false;
		processed = 0;
		expected = 0;
		Logger("Ultra opening !");
		serial = new SerialIF(this.caller, myself, m_port, 0);	//Configure serial in ascii mode
		
		if(serial != null){
			if(serial.connect()){
				caller.mylog("Port <"+m_port+"> connected");
				if(serial.initIOStream()) {
					serial.initListener();
					
					this.connectToDevice(); //Try to obtain an answer from meter (serial_id)
					timer(2000);
					if(state == 0) {
						//Meter does'nt answer : Probably due to wake up ! redo command
						this.connectToDevice();
					}
				}
			}
		}
		
	}	//Constructor end //////////////////////////////////
	
	// 1st step : wake up meter by sending 'DM@' command
	public void connectToDevice(){
		// Try to wake up the reader
		
		state = 0;		//Attempt to wake up the device
		Logtext = "Port <"+m_port+"> --> wake up with DM@";
		Logger(Logtext);
		serial.writeData(3, "DM@".getBytes());
		
		// This Meter will be notified by communicator when response received....or timeout !
	}
	//2nd step : Serial ID received... Try to know if records available
	public void InitiateReadRecords(){
		state = 1;		//Attempt to read BG records header
		Logtext = "Port <"+m_port+"> --> DMP";
		Logger(Logtext);
		serial.writeData(3, "DMP".getBytes());
		
		//Next response should be header
	}
	// 3rd step : process BG records while it has....
	// It's implicit after header : Nothing to do !
	public void ReadAllRecords(){
		state = 2;		// BG records processing
		
	}
	//Final step : disconnect from reader
	// invoked by main, on 'readfinished' notification
	public void EndSession(){
		state = -1;		// BG records processing ended
		serial.disconnect();
		serial = null;
	}
	
	
	////////////////Process events routines	, specific for this kind of meter
	
	 ////////////////////////////////
	public void actionPerformed(ActionEvent evt) {
		String Action = evt.getActionCommand();
		String last_line = null;
		String tmp = "";
		if (Action.equals("answer")) {
			if(state == -1) {
				return;		//Unexpected notification
			}
			last_line = serial.readline();
			Logtext = "<-- ["+last_line+"]";
			caller.mylog(Logtext);
			switch (state) {
			case 0:
				//Wake up sent to meter (DM@ sent : we should receive @ at 1st position)
				//Check if answer or not yet
				if(last_line.equals("") || ! last_line.substring(0, 1).equals("@")) {
					//No expected answer : redo !
					state = 0;
					connectToDevice();
					return;
				} else {
					
					tmp = ex_string(last_line, 2 ,  "\"");
					Logtext = "Meter ready, serial <"+tmp+"> received ";
					caller.meter_id = tmp;
					Logger(Logtext);
					notify_main("meter_id");
					state = 1;	//Wake up success !
					InitiateReadRecords();
				}
				break;
			case 1:
				// read BG records sent to meter : We should receive header
				if(! last_line.equals("") &&  last_line.substring(0, 1).equals("P")) {
					tmp = ex_string(last_line, 1 ,  ",");
					tmp = ex_string(tmp, 2 ,  " ");
					String unit = ex_string(last_line, 4 ,  "\"");
					expected = 0;
					expected = Integer.parseInt(tmp);
					caller.processed = 0;
					caller.toprocess = expected;
					
					caller.unit = unit;
					Logtext = "Records to process = "+tmp+" Unit = "+unit;
					caller.mylog(Logtext);
					
					notify_main("items");
					
					if(expected == 0) {
						notify_main("readfinished");
						state = -1;	//Nothing else expected
					} else {
						state = 2;
					}
				}
				break;
			case 2:
				// Process BG records sent by meter 
				if(! last_line.equals("") &&  last_line.substring(0, 1).equals("P")) {
					Boolean getit = true;
					String itemdate = ex_string(last_line, 4 ,  "\"");
					String itemtime = ex_string(last_line, 6 ,  "\"");
					String res = ex_string(last_line, 8 ,  "\"").trim();
					String itemflag = ex_string(last_line, 5 ,  ",").trim();
					itemflag = ex_string(itemflag, 1 ,  " ");
					if ((res.startsWith("C")) ||  // control solution
			                (res.startsWith("!")) ||  // check strip
			                (res.startsWith("I")) ||  // insulin or other data
			                (res.startsWith("K")))    // control solution (on OT2 when language is SVENS or DEUTSCH)
			         {
							getit = false;	//Ignore this entry
			         } else if(res.contains("HIGH")) {
			            	res = "600";
			            	
		             }
					processed++;
					caller.processed = processed;
					//Logtext = "Records processed = "+processed;
					//caller.mylog(Logtext);
					
					if(getit) {
						//Build a 'Record' object for main's arraylist
						caller.Record = new Record_factory(caller.meter_id, caller.unit, itemdate, itemtime, res, itemflag);
						notify_main("record");
					}
					if(processed >= expected) {
						notify_main("readfinished");
						state = -1;	//Nothing else expected
					}
						
				}
				break;
			}
		}
	}
}
