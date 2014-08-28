package main;

import javax.swing.*;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.lang.String;
import java.awt.Font;
import javax.swing.JTextField;

import javax.swing.SwingConstants;
import javax.swing.JPanel;

import java.awt.Dimension;
import javax.swing.border.LineBorder;
import javax.swing.border.EtchedBorder;

import readers.Meter;
import readers.OTUltra;
import readers.OTVerioIQ;

import Toolsclasses.Reader_factory;
import Toolsclasses.Record_factory;
import Toolsclasses.get_settings; 

import lang_parser.LangFileParser;
import main.DrawImage;

public class Main_Window extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private JLabel lblSelectedReader ;
	private JPanel panel_reader = null;
	private JComboBox sel_reader = null;
	public  JTextField reader_port;
	private JTextField Reader_serial_ID;
	private JTextArea logtext;
	private JTextField user_id;
	private JTextField user_name;
	private JButton btnReadResults = null;
	private JButton BtErase = null;
	private JPanel panel_picture = null;
	private JButton Bt_process;
	private String version = "0.0";
	private String myclass = "main_win";  	// String to use for Translator
	public get_settings settings;
	private String session_language;
	private int session_meter = 0;
	private Meter reader = null;
	public String meter_id = "";
	public String unit;
	public int toprocess = 0;
	public int processed = 0;
	
	private JTextField itemsread;
	private JTextField textField;
	
	public Record_factory Record = null;
	private ArrayList<Record_factory> record_list = null;
	
	public Main_Window() {
		setResizable(false);
		setSize(600,350);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		settings = new get_settings(); 	//Parser object
	    settings.load_settings();		// load XML settings
	    session_language = settings.get_last_lang();
	    
	    setTitle(Translate("Main_title")); //"Onetouch Glucometers reader");
	    build_frame();
	    if(settings != null) {
	    	set_curr_reader(settings);		//set variables about last selected glucometer
	    									// ! ! ! ! must be AFTER build_frame ! ! ! ! !
	    }
	    
		
		
		this.setVisible(true);
	}
	/*
	 * Translator for texts
	 */	
	private String Translate(String token) {
		return LangFileParser.getInstance(this).getValue(myclass, token);
	}
	
	void build_frame() {
		getContentPane().setLayout(null);
		
		panel_reader = new JPanel();
		panel_reader.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_reader.setBounds(12, 0, 268, 309);
		panel_reader.setLayout(null);
		getContentPane().add(panel_reader);
		
		lblSelectedReader = new JLabel(Translate("Select"));
		lblSelectedReader.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectedReader.setFont(new Font("Dialog", Font.ITALIC, 10));
		lblSelectedReader.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		lblSelectedReader.setBounds(32, 0, 121, 24);
		panel_reader.add(lblSelectedReader);
		
		sel_reader = new JComboBox();
		sel_reader.setBounds(2, 29, 254, 22);
		sel_reader.setActionCommand("sel_reader");
		sel_reader.addItem("Onetouch Ultra");
		sel_reader.addItem("Onetouch Verio IQ");
		sel_reader.addActionListener(this);
		
		panel_reader.add(sel_reader);
		
		panel_picture = new JPanel();
		panel_picture.setBounds(53, 109, 100, 169);
		panel_reader.add(panel_picture);
		
		JPanel panel_work = new JPanel();
		panel_work.setBounds(282, 0, 316, 309);
		getContentPane().add(panel_work);
		panel_work.setLayout(null);
		
		JScrollPane sp = new JScrollPane();
		logtext = new JTextArea();
		logtext.setFont(new Font("Dialog", Font.BOLD, 10));
		logtext.setBounds(0, 158, 316, 150);
		sp.setViewportView(logtext);
		sp.setBounds(0, 158, 316, 150);
		panel_work.add(sp);
		
		JLabel lblDeviceLog = new JLabel(Translate("device_log"));
		lblDeviceLog.setBounds(12, 141, 204, 15);
		panel_work.add(lblDeviceLog);
		
		JButton btnCheckReader = new JButton(Translate("Read_Memory"));
		btnCheckReader.setBounds(12, 5, 141, 25);
		btnCheckReader.addActionListener(this);
		btnCheckReader.setActionCommand("check_reader");
		panel_work.add(btnCheckReader);
		
		BtErase= new JButton(Translate("Erase_Memory"));
		BtErase.setActionCommand("erase");
		BtErase.addActionListener(this);
		BtErase.setBounds(12, 104, 141, 25);
		panel_work.add(BtErase);
		
		JLabel lblId = new JLabel("ID");
		lblId.setHorizontalAlignment(SwingConstants.CENTER);
		lblId.setHorizontalTextPosition(SwingConstants.CENTER);
		lblId.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblId.setBounds(156, 10, 27, 15);
		panel_work.add(lblId);
		
		Reader_serial_ID = new JTextField();
		Reader_serial_ID.setHorizontalAlignment(SwingConstants.CENTER);
		Reader_serial_ID.setFont(new Font("Dialog", Font.BOLD, 12));
		Reader_serial_ID.setBounds(188, 8, 128, 19);
		panel_work.add(Reader_serial_ID);
		Reader_serial_ID.setColumns(10);
		
		JLabel lblItemsRead = new JLabel(Translate("read"));
		lblItemsRead.setBounds(99, 42, 84, 15);
		panel_work.add(lblItemsRead);
		
		itemsread = new JTextField();
		itemsread.setHorizontalAlignment(SwingConstants.CENTER);
		itemsread.setFont(new Font("Dialog", Font.BOLD, 12));
		itemsread.setBounds(188, 39, 41, 19);
		panel_work.add(itemsread);
		itemsread.setColumns(10);
		
		JLabel label = new JLabel("/");
		label.setFont(new Font("Dialog", Font.BOLD, 14));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(227, 42, 17, 15);
		panel_work.add(label);
		
		textField = new JTextField();
		textField.setFont(new Font("Dialog", Font.BOLD, 12));
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setBounds(241, 39, 50, 19);
		panel_work.add(textField);
		textField.setColumns(10);
		
		Bt_process = new JButton(Translate("Process"));
		Bt_process.setFont(new Font("Dialog", Font.BOLD, 10));
		Bt_process.setBounds(12, 67, 141, 25);
		Bt_process.setActionCommand("process_records");
		Bt_process.addActionListener(this);
		Bt_process.setEnabled(false);
		panel_work.add(Bt_process);
		
		
		 
		 JLabel lblPort = new JLabel(Translate("Port"));
		 lblPort.setFont(new Font("Dialog", Font.PLAIN, 12));
		 lblPort.setBounds(2, 54, 42, 18);
		 panel_reader.add(lblPort);
		 
		 reader_port = new JTextField();
		 reader_port.setBounds(54, 54, 202, 18);
		 panel_reader.add(reader_port);
		 reader_port.setColumns(10);
		 
		 JLabel lblUser = new JLabel(Translate("User"));
		 lblUser.setFont(new Font("Dialog", Font.PLAIN, 12));
		 lblUser.setBounds(2, 78, 52, 18);
		 panel_reader.add(lblUser);
		 
		 user_id = new JTextField();
		 user_id.setBounds(53, 78, 24, 18);
		 panel_reader.add(user_id);
		 user_id.setColumns(10);
		 
		 user_name = new JTextField();
		 user_name.setBounds(82, 78, 174, 18);
		 panel_reader.add(user_name);
		 user_name.setColumns(10);
		 
	}
	public String getlang() {
		return session_language;
	}
	public void mylog( String s) {
		logtext.append(s+"\n");
		logtext.selectAll();	//To see bottom of list
		
	}
	
	private void set_curr_reader(get_settings settings){
		if(settings == null) return;
		session_language = null;
    	session_language = settings.get_last_lang();
    	session_meter = settings.get_last_meter();
    	if(session_meter > 0 && session_meter < 3)
			sel_reader.setSelectedIndex(session_meter-1);
		else
			sel_reader.setSelectedIndex(0);
    	
    	Reader_factory reader = settings.get_reader(session_meter);
    	
    	if(reader != null) {
    		reader_port.setText(reader.port);
    		user_id.setText(Integer.toString(reader.userid));
    		user_name.setText(settings.get_username(reader.userid));
    		DrawImage dimg = new DrawImage(reader.imagefile);
   		 	// create the image using the toolkit
    		panel_picture.removeAll();
	   		Dimension picdim = new Dimension(panel_picture.getWidth(),panel_picture.getHeight());
	   		dimg.setSize(picdim);
	   		dimg.setPreferredSize(picdim);
	   		panel_picture.add(dimg);
	   		repaint();
    	}
		
	}

	public void timer(int duration) {
		try {
 			Thread.sleep(duration);
 		} catch (InterruptedException t) {
			
		}
	}
	private void process_records() {
		String line = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Record_factory rec;
		if(record_list != null) {
			int i = record_list.size();
			for(int j = 0; j < i; j++) {
				/////////// for tests : check if all records are in list....
				rec = record_list.get(j);
				line = sdf.format(rec.event_date);
				line += " Value : ";
				line += Integer.toString(rec.valuemg);
				mylog(line);
				////////////////////////////////////////////////////////////
				
			}
		}
	}
	////////////////Process events routines ////////////////////////////////
	public void actionPerformed(ActionEvent evt) {
		String Action = evt.getActionCommand();
		if (Action.equals("sel_reader")) {
			//Clic on combo box readers list
			int selected = sel_reader.getSelectedIndex();
			if(selected == session_meter - 1)
				return;		//No modifs
			//System.out.println("reader changed to : "+(selected+1));
			session_meter = selected+1;
			if(settings != null) {
				settings.set_last_meter(session_meter);
				settings.save_settings();
				set_curr_reader(settings);
			}
		} else if (Action.equals("erase")) {
			//Clic button erase memory
			
		} else if (Action.equals("check_reader")) {
			//Clic to connect to reader and get SN
			int selected = sel_reader.getSelectedIndex();
			if(reader != null) {
				reader = null;		//Free existing interface
			}
			
			if(selected == 0) {
				//Ultra
				reader = new OTUltra(this, reader_port.getText());
			} else if (selected == 1) {
				// OTVerioIQ
				reader = new OTVerioIQ(this, reader_port.getText());
			}
			
		} else if (Action.equals("meter_id")) {
			//The meter ID has been detected by 'Meter' method : display it
			this.Reader_serial_ID.setText(meter_id);
			repaint();
			return;
		} else if (Action.equals("items")) {
			//The meter ID has been detected by 'Meter' method : display it
			this.itemsread.setText(Integer.toString(processed));
			this.textField.setText(Integer.toString(toprocess));
			repaint();
			return;
		} else if (Action.equals("record")) {
			//A record has been detected by 'Meter' method : store it !
			if(record_list == null) {
				record_list = new ArrayList<Record_factory>();
			}
			record_list.add(Record);
			Bt_process.setEnabled(true);	
			Record = null;
			this.itemsread.setText(Integer.toString(processed));
			this.textField.setText(Integer.toString(toprocess));
			repaint();
			return;
			
		} else if (Action.equals("readfinished")) {
			//all records from meter have been downloaded
			mylog("Read meter memory complete...");
			repaint();
			/*
			reader.EndSession();
			reader = null;
			*/
			repaint();
			return;
			
		} else if (Action.equals("process_records")) {
			//Some records has to be processed
			if(record_list == null || record_list.size() < 1) {
				mylog("Records list empty, nothing to process....");
				Bt_process.setEnabled(false);
				return;
			}
			mylog("Processing records....");
			process_records();
			
			return;
		}
	
	}
}  
