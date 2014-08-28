package Toolsclasses;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Record_factory {
	public String reader_id;
	public Date event_date;
	public int valuemg;
	public float valuemmol;
	public String flag;
	
	
	public Record_factory(String pID, String pUnit, String pDate, String pTime, String pValue, String pFlag) {
		String datetime = pDate+" "+pTime.trim();
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
		reader_id = pID;
		try {
			event_date = df.parse(datetime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(pUnit.startsWith("MG/DL")) {
			valuemg = Integer.parseInt(pValue.trim());
			valuemmol = 0;
		} else {
			valuemg = 0;
			valuemmol = Float.parseFloat(pValue.trim());
		}
		flag = pFlag;
		//System.out.println("Record processed : "+event_date.toString()+" Value : "+valuemg+" Flag <"+flag+">");
		
	}
}
