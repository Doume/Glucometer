package readers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Toolsclasses.Record_factory;

import main.Main_Window;


/*
 * Onetouch Verio IQ class
 * assumes : - connect to device
 * 			 - read serial ID and got it to main window
 * 			 - read all records from device memory, and return them to main window
 */
public class OTVerioIQ extends Meter implements ActionListener {
	private String m_port = "";
	public Boolean connected = false;
	public Main_Window caller = null;
	private Meter myself = null;
	private int reader_type = -1;
	public  int state = -1;
	private int expected = 0;
	private int processed = 0;
	private String Logtext = "";
	
	private SerialIF serial = null;
	
	public OTVerioIQ(Main_Window caller, String reader_port)  {
		super(caller,reader_port);
		
		myself = this;
		this.caller = get_caller();
		this.m_port = get_port();
		connected = false;
		processed = 0;
		expected = 0;
		Logger("VerioIQ opening !");
		serial = new SerialIF(this.caller, myself, m_port, 1);	//Configure serial in Binary mode
		
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
		
	}
	// 1st step : wake up meter by sending 'Disconnect' command ?
	// Does'nt work for now : no answer from meter .... ???? 
	public void connectToDevice(){
		// Try to wake up the reader
		int len = 0;
		state = 0;		//Attempt to wake up the device
		//byte[] msg = new byte[]{0x02, 0x0A, 0x00, 0x03, 0x0B, 0x01, 0x02, 0x03, 0x2A, (byte)0xDC }; //Get serial ID ?
		byte[] msg = new byte[]{0x02, 0x06, 0x08, 0x03,  (byte)0xC2, (byte)0x62  }; //Disconnect ?
		Logtext = "Port <"+m_port+"> --> wake up ";
		Logger(Logtext);
		len+=msg[1];
		serial.writeData(len, msg);
		
		// This Meter will be notified by communicator when response received....or timeout !
	}
	
	//2nd step : Serial ID received... Try to know if records available, and how many
	public void InitiateReadRecords(){
	
		state = 1;		//Attempt to read BG records header
		Logtext = "Port <"+m_port+"> --> Get records count";
		Logger(Logtext);
	
		//serial.writeData(4, "DMP\n".getBytes());
		
		//Next response should be header
	}
	// 3rd step : process records while it has....
	// 
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
	
	
	
	////////////////Process events routines	
	
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
				// read BG records sent to meter : We should receive records count
				
				break;
				
			case 2:
				// Process record sent by meter 
				
				processed++;
				caller.processed = processed;
				
					//caller.Record = new Record_factory(caller.meter_id, caller.unit, itemdate, itemtime, res, itemflag);
					notify_main("record");
				
				if(processed >= expected) {
					notify_main("readfinished");
					state = -1;	//Nothing else expected
				}
						
				
				break;
			}
		}
	}
}
