package readers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Toolsclasses.Record_factory;

import main.Main_Window;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

/*
 * Meter class gives a common interface for different glucometers
 * It's an 'abstract' class, implementing some utilities common to all meters
 */
public abstract class Meter  {
	private String m_port = "";
	public Boolean connected = false;
	public Main_Window caller = null;
	private Meter myself = null;
	//private int reader_type = -1;
	public  static int state = -1;
	private int expected = 0;
	private int processed = 0;
	private String Logtext = "";
	
	private SerialIF serial = null;
	
	public Meter(Main_Window caller,  String reader_port)  {
		myself = this;
		this.caller = caller;
		this.m_port = reader_port;
		connected = false;
		processed = 0;
		expected = 0;
		//System.out.println("Meter class !");
		
	}
	public Main_Window get_caller() {
		return caller;
	}
	public String get_port() {
		return m_port;
	}
	public void Logger(String msg) {
		if(caller != null) {
			caller.mylog(msg);
		} else {
			System.out.println(msg);
		}
	}
	
	// The subclasses have to implement following methods 
	public abstract void connectToDevice();
	public abstract void InitiateReadRecords();
	public abstract void ReadAllRecords();
	public abstract void EndSession();
	
	public abstract void actionPerformed(ActionEvent evt);
	
	/////////////////////// Utilities ////////////////////////////////////////////
	
	public void timer(int duration) {
		try {
 			Thread.sleep(duration);
 		} catch (InterruptedException t) {
			
		}
	}
	/*
	 * This method extracts a substring from string
	 * Parameter 1 : String from which extract
	 * Parameter 2 : item index
	 * Parameter 3 : separator to use
	 * 
	 * example : ex_string("String | of | many | substrings" , 2, " | ")
	 *    will return "of"
	 */
	public String ex_string(String buff, int item, String sep) {
		String result = null;
		int ptr1 =  0;
		int ptr2 = 0;
		int cpt = 0;
		int next = 0;
		if ( item > 1)
			while ( cpt < item -1) {
				ptr1 = buff.indexOf(sep, next);	//search the tag preceeding area
				cpt++;
				next = ptr1+1;
			}
		else
			ptr1 = 0;	//1st item requested : point on buffer begin
		
		if(ptr1 == 0)
			ptr2 = buff.indexOf(sep);	    //search the 1st tag following array
		else
			ptr2 = buff.indexOf(sep, ptr1 + sep.length());	    //search the 2nd tag following array
		if((ptr1 < (buff.length() - sep.length())) && (ptr1 >= 0)) {
			//previous tag found
			if(ptr1 > 0)
				ptr1 += sep.length();	//skip the separator itself
			//1st sep found
			if( (ptr2 < (buff.length() - sep.length())) && (ptr2 > ptr1) ) { 
				//2nd found, too !
				if( (ptr2 - 1) == ptr1 ) {
					/*
					//empty result
					result = "";
					*/
					// single digit
					result = buff.substring(ptr1, ptr2);
				} else {
					result=buff.substring(ptr1, ptr2);
				}
			} else {
				//2nd not found : take the rest of line
				result = buff.substring(ptr1, buff.length());
			}
			
		} else {
			//no separator found : return empty string !
			result = "";
		}
		
		return result;
	}
	
	public void notify_main(String Command) {
		//WARNING : The part of code invoked into Main_Window by this event
		//  will be excuted by the current thread (Box_Interface)
		//  so, don't make recursive calls into event process, because it's only a unique th"Port <"+portname+"> not found !"read !
		ActionEvent actionEvent = new ActionEvent(this,
	              ActionEvent.ACTION_PERFORMED, Command);
		  caller.actionPerformed(actionEvent);	//send command to Main_Window
	}
	
	////////////////Process events routines	
	
	 ////////////////////////////////
	/*
	public void ac
}tionPerformed(ActionEvent evt) {
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
					caller.mylog(Logtext);
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
	*/
}
