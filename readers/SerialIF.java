/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package readers;

import purejavacomm.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;
import main.Main_Window;

public class SerialIF implements SerialPortEventListener
{
    //passed from main GUI
    private Main_Window window = null;
    private Meter father = null;
    //for containing the ports that will be found
    private Enumeration ports = null;
    //map the port names to CommPortIdentifiers
    private HashMap portMap = new HashMap();

    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;
    private String portname = "";
    
    //input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;
    private boolean locked = false;
    
    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;
    final static int STX = 2;
    final static int ETX = 3;

    //a string for recording what goes on in the program
    //this string is written to the GUI
    String logText = "";
    String currline = "";
    byte[] currbuf = null;
    private String error = "";
    private static int mode = 0;		// 0 = ASCII mode on interface ( Ultra)
    									// 1 = BINARY mode on interface ( Verio)
    private static int expected_len = 0;
    private static int read_len = 0;
    private boolean stx_seen = false;
    private boolean etx_seen = false;
    private boolean len_seen = false;
    private static SerialReader reader = null;
    
    public SerialIF(Main_Window window, Meter father, String portname, int pMode)
    {
    	super();
        this.window = window;
        this.father = father;
        this.portname = portname;
        this.mode = pMode;
        //RXTXCommDriver driver = new RXTXCommDriver();
        //System.out.println("Communicator started...");
    }

    //search for all the serial ports
    //pre: none
    //post: adds all the found ports to a combo box on the GUI
    /*
    public void searchForPorts()
    {
        ports = CommPortIdentifier.getPortIdentifiers();
        System.out.println(ports.toString());
        System.out.println("List size = "+ports.hasMoreElements());
        while (ports.hasMoreElements())
        {
        	System.out.println("Scanning p0orts list for  <"+portname+"> in system");
    		
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();

            System.out.println("Processing  <"+curPort.getPortType()+"> in system");
            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                //window.cboxPorts.addItem(curPort.getName());
            	System.out.println("Compare port <"+portname+"> with <"+curPort.getName()+">");
        		
            	if(curPort.getName().equals(portname)) {
            		//Found port on system
            		System.out.println("Found port <"+portname+"> in system");
            		portMap.put(curPort.getName(), curPort);
            		break;
            	}//(CommPortIdentifier)portMap.get(portname);
            }
        }
    }
	*/
    //connect to the selected port in the combo box
    //pre: ports are already found by using the searchForPor"Port <"+portname+"> not found !"ts method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
    public Boolean connect()
    {
         try {
			selectedPortIdentifier = CommPortIdentifier.getPortIdentifier( portname);
		} catch (NoSuchPortException e1) {
			setConnected(false);
			error = "Port <"+portname+"> not found !";
			trytolog(error);
			return false;
		}
		
        //Required port found in system : We can think Ultra meter is plugged on system !
        CommPort commPort = null;

        try
        {
    
            //the method below returns an object of type CommPort
        	commPort = selectedPortIdentifier.open("GlucoMeter", TIMEOUT);
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort)commPort;
            if(this.mode == 0) {
            	serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            	serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            } else {
            	System.out.println("Setting for CP2103");
            	serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            	//serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            	//serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
            	serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN + SerialPort.FLOWCONTROL_XONXOFF_OUT);

            }
            
            //for controlling GUI elements
            setConnected(true);
            //logging
            error = "Port <"+portname+"> opened successfully\n";
			trytolog(error);
			currbuf = new byte[256];
            return true;
            
        } catch (PortInUseException e) {
            error = portname + " is in use. (" + e.toString() + ")\n";
            trytolog(error);
        } catch (Exception e) {
        	e.printStackTrace();
            logText = "Failed to open " + portname + "(" + e.toString() + ")";
            trytolog(logText);
        }
        return false;
    }

    private void trytolog ( String msg) {
    	if(father != null)
			if(father.caller != null) {
				father.caller.mylog(msg);
				return;
			}
    	
    	System.out.println(msg);	//If log was'nt possible
    	return;
    }
    //open the input and output streams
    //pre: an open port
    //post: initialized intput and output streams for use to communicate data
    public boolean initIOStream() {
        //return value for whether opening the streams is successful or not
        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            return true;
        } catch (IOException e) {
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
            trytolog(logText);
            return false;
        }
    }

    //starts the event listener that knows whenever data is available to be read
    //pre: an open serial port
    //post: an event listener for the serial port that knows when data is recieved
    public void initListener() { 
    	if(mode == 0) {
	        try
	        {
	            serialPort.addEventListener(this);
	            serialPort.notifyOnDataAvailable(true);
	            serialPort.notifyOnBreakInterrupt(true);
	            serialPort.notifyOnCarrierDetect(true);
	            serialPort.notifyOnCTS(true);
	            serialPort.notifyOnDSR(true);
	            serialPort.notifyOnFramingError(true);
	            serialPort.notifyOnOutputEmpty(false); //Notified each time a byte has been sent to device !
	            serialPort.notifyOnOverrunError(true);
	            serialPort.notifyOnParityError(true);
	            serialPort.notifyOnRingIndicator(true);
	            
	        } catch (Exception e) {
	            logText = "Too many listeners. (" + e.toString() + ")";
	            trytolog(logText);
	        }
    	} else {
    		//Start a reader thread...
    		startreader();
    		
    	}
    }

    private void startreader() {
    	reader = new SerialReader( input , this);
    	 final Thread th = new Thread( reader  );
    	th.start();
        //System.out.println("Receive thread started ! ");
    }
    //disconnect the serial port
    //pre: an open serial portSystem.out.print( new String( buffer, 0, len ) );
    
    //post: clsoed serial port
    public void disconnect()
    {
        //close the serial port
        try
        {
           serialPort.removeEventListener();
           if(reader != null) {
        	   reader.askexit();
           }
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);
           
            logText = "Disconnected.";
            trytolog(logText);
        } catch (Exception e) {
            logText = "Failed to close " + serialPort.getName() + "(" + e.toString() + ")";
            trytolog(logText);
        }
    
    }

    final public boolean getConnected()
    {
        return bConnected;
    }

    public void setConnected(boolean bConnected)
    {
        this.bConnected = bConnected;
    }

    private String dump(int len, byte[] buff) {
    	String res = "";
    	if(len < 1) return "";
    	for (int i = 0; i < len; i++)
    		res += String.format("%02X ", buff[i]);
    	return res;
    }
    //what happens when data is received
    //pre: serial event is triggered
    //post: processing on the data it reads
    public void serialEvent(SerialPortEvent evt) {
    	if(this.mode != 0)
    		System.out.println("Event Received : "+evt.getEventType());
    	// Determine type of event.
        switch (evt.getEventType()) {
            // If break event notify higher level.
            case SerialPortEvent.BI:
                System.out.println("received break");
                break;
            case SerialPortEvent.CD:
                System.out.println("recievied cd");
                break;
            case SerialPortEvent.CTS:
                System.out.println("received cts");
                break;
            case SerialPortEvent.DSR:
                System.out.println("received dsr");
                break;
            case SerialPortEvent.FE:
                System.out.println("received fe");
                break;
            case SerialPortEvent.OE:
                System.out.println("received oe");
                break;
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:	//When a byte has been sent to device
                System.out.println("Output Empty");
                break;
            case SerialPortEvent.PE:
                System.out.println("received pe");
                break;
            case SerialPortEvent.RI:
                System.out.println("received ri");
                break;
            case SerialPortEvent.DATA_AVAILABLE:
            	//System.out.print("received Data : ");
                try {
	                byte singleData = (byte)input.read();
	                //System.out.printf("%02X\n", singleData);
	                if(this.mode == 0) {
	                	//We use ASCII mode (Ultra)
		                if (singleData != NEW_LINE_ASCII){
		                	currline+=new String(new byte[] {singleData});
		                } else {
		                	notify_father("answer");
		                	//System.out.println("Answer <"+currline+">");
		                }
	                } else {
	                	//We use BINARY mode (Verio)
	                	currbuf[read_len] = singleData;	//Store the byte in buffer
	                	read_len++;
	                	trytolog("Received "+read_len) ;
	                	if ( ! stx_seen && singleData == STX){
	                		stx_seen = true;
	                		System.out.println("Got STX");
	                		return;
	                	}
	                	if(stx_seen) {
	                		if( ! len_seen) {
	                			if( expected_len == -1) {
	                				//It's the len low byte
	                				expected_len = singleData;
	                				return;
	                			} else {
	                				//It's the len high byte
	                				expected_len += singleData * 256;
	                				len_seen = true;
	                				System.out.println("Got len = "+expected_len);
	                				return;
	                			}
	                			
	                		} else {
	                			// expected_len known
	                			if(read_len >= expected_len) {
	                				//Buffer complete !
	                				String temp = "";
	                				trytolog("Buffer OK = "+expected_len);
	                				trytolog("<-- ["+dump(read_len, currbuf)+"]");
	                				
	                				notify_father("answer");
	                				return;
	                			} else {
	                				//Some more bytes to process
	                				return;
	                			}
	                		}
	                	}
	                }
	            } catch (Exception e) {
	                logText = "Failed to read data. (" + e.toString() + ")";
	                trytolog(logText);
	            }
	        	break;
        }	//end switch
    }
    public String readline() {
    	String tmp = currline;
    	currline = "";
    	unlock();
    	return tmp;
    	
    }
    private void timer(int duration) {
		try {
 			Thread.sleep(duration);
 		} catch (InterruptedException t) {
			
		}
	}
    private void notify_father(String Command) {
		//WARNING : The part of code invoked into Main_Window by this event
		//  will be excuted by the current thread (Box_Interface)
		//  so, don't make recursive calls into event process, because it's only a unique thread !
		ActionEvent actionEvent = new ActionEvent(this,
	              ActionEvent.ACTION_PERFORMED, Command);
		 father.actionPerformed(actionEvent);	//send command to Meter
	}
    
    private void get_lock(){
		while(locked) {
			timer(100);
		}
		locked = true;
	}// writeData(0, 0);

    
	private void unlock() {
		locked = false;
	}
	
	
    //method that can be called to send data
    //pre: open serial port
    //post: data sent to the other device
    public void writeData(int len, byte[] to_send)
    {
    	//get_lock();
    	currline="";		//reset the receive buffer
    	for(int i = 0; i < currbuf.length; i++)
    		currbuf[i] = 0x00;
    	logText = "--> ["+ dump(len, to_send)+"]";
    	trytolog(logText);
    	//startreader();
    	this.expected_len = -1;
        this.read_len = 0;
        this.stx_seen = false;
        this.etx_seen = false;
        this.len_seen = false;
        /*
        this.serialPort.setDTR(true);
        this.serialPort.setRTS(true);
        this.serialPort.setInputBufferSize(512);
         */
    	try {
        	for(int i = 0; i< len; i++) {
        		output.write(to_send[i]);
        		output.flush();
        		timer(50);
        	}
        }catch (Exception e) {
            logText = "Failed to write data. (" + e.toString() + ")";
            trytolog(logText);
        }
    }
    /*
     * This thread will listen on port as soon something is expected
     * and will exit when response from reader is received ( \n for Ultra, or packet len for Verio)
     */
    public static class SerialReader implements Runnable {
    	 
        InputStream in;
        SerialIF notifier;
        Boolean done = false;
     
        public SerialReader( InputStream in , SerialIF notifier) {
          this.in = in;
          this.notifier = notifier;
          
        }
     
        public void run() {
        	while(! done) {
	        	try {
	        		if(notifier.mode != 0)
	        			System.out.println("Get byte(s)");
	        		/*
	        		notifier.serialPort.setDTR(true);
	        		notifier.serialPort.setRTS(true);
	        		 */
		        	byte singleData = (byte)in.read();
		            if(notifier.mode == 0) {
		            	//We use ASCII mode (Ultra)
		                if (singleData != NEW_LINE_ASCII ){
		                	if(singleData != -1)
		                		notifier.currline+=new String(new byte[] {singleData});
		                } else {
		                	notifier.notify_father("answer");
		                	//done=true;
		                }
		            } else {
		            	//Using binary mode
		            	System.out.print("1 byte read : ");
		            	System.out.printf("%02X\n", singleData);
		            }
	        	} catch (IOException e) {
	        		e.printStackTrace();
	        		done = true;	//To exit thread
	        	}
        	}
           
        	System.out.println("Reader Thread exit !");
          
        }
        public void askexit() {
        	done = true;
        	try {
				this.finalize();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
        }
      }
}
