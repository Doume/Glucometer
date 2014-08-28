package Toolsclasses;

import java.io.*;
import org.jdom2.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jdom2.input.*;
import org.jdom2.output.*;
import java.util.List;
import java.util.Iterator;

public class get_settings {
   static org.jdom2.Document document;
   static Element settings;
   static Element last_section;
   static Element readers;
   static Element users;
   static Element XML_Path;
   static Element GGC_Path;
   static Element Start_Date;
   static Element End_Date;
   static Element last_meter;
   static Element reader_elem;
   static String lang = "";
   
   
  public Element load_settings()
   {
      //On cr�e une instance de SAXBuilder
	  //System.out.println("load_settings constructor");
      SAXBuilder sxb = new SAXBuilder();
      try
      {
         //On cr�e un nouveau document JDOM avec en argument le fichier XML
         //Le parsing est termin� ;)
         document = sxb.build(new File("./settings.xml"));
      }
      catch(Exception e){
    	  System.out.println("exception reading XML Document :\n" + e.toString() );
    	  System.out.println("XML Document 'settings.xml' malformed");
          return null;
      }
      if(load_last()  )
    	  return settings;
      else
    	  return null;	//Failure
   }
  
   
   
private Boolean load_last() {
	   // We initialize pointer to root of document
	   try {
		   settings = document.getRootElement();
		   List listsections = settings.getChildren();
		   if (listsections != null){
			   Iterator i = listsections.iterator();
			   while(i.hasNext()){
				   Element current = (Element)i.next();
				   String currname = current.getName();
				   //System.out.println("Trouvé : "+currname);
				   if(currname.equals("last")) {
					   last_section = current;
				   } else if(currname.equals("readers")) {
					   readers = current;
				   } else if(currname.equals("users")) {
					   users = current;
				   } 
			   }
		   }
		   if(last_section != null) {
		   		//Point elements Path, etc...
		   		XML_Path = last_section.getChild("XML_Path");
		   		GGC_Path = last_section.getChild("GGC_Path");
		   		Start_Date = last_section.getChild("Start_Date");
		   		End_Date = last_section.getChild("End_Date");
		   		last_meter = last_section.getChild("Meter");
		   		lang = last_section.getChild("lang").getValue();
		   } else {
			   System.out.println("XML Document 'settings.xml' 'last' section not found");
		       return false;  
		   }
		   
	   }
	   catch(Exception e){
	    	  System.out.println("'settings.xml' does'nt have expected structure !");
	          return false;
	   }
	   return true;
}
   /*
    * This method rewrites on disk the current settings configuration
    */
   public boolean save_settings() {
	   try
	   {
	      XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
	      sortie.output(document, new FileOutputStream("settings.xml"));
	   }
	   catch (java.io.IOException e){
		   System.out.println("exception re-writing settings XML Document :\n" + e.toString());
		   return false;
	   }
	   return true;
   }
   /*
    * get parameters values
    */
   public String get_last_lang() {
	   return lang;
   }
   
   public String get_XML_path() {
	   if(XML_Path != null) {
		   return XML_Path.getValue();
	   } else
		   return "";   
   }
   public String get_GGC_path() {
	   if(GGC_Path != null) {
		   return GGC_Path.getValue();
	   } else
		   return "";   
   }
   public String get_Start_Date() {
	   if(Start_Date != null) {
		   return Start_Date.getValue();
	   } else
		   return "";   
   }
   public String get_End_Date() {
	   if(End_Date != null) {
		   return End_Date.getValue();
	   } else
		   return "";   
   }
   public int get_last_meter() {
	   if(last_meter != null) {
		   return Integer.parseInt(last_meter.getValue());
	   } else
		   return -1;   
   }
   public Reader_factory get_reader(int id) {
	   Reader_factory reader = null;
	   if(readers == null) return null;
	   List listreaders = readers.getChildren();
	   if (listreaders != null){
		   Iterator i = listreaders.iterator();
		   while(i.hasNext()){
			   Element current = (Element)i.next();
			   int currid = Integer.parseInt(current.getAttributeValue("id"));
			   if(currid == id) {
				   String user = current.getAttributeValue("userid");
				   int userid = 0;
				   if(user != null)
					   userid = Integer.parseInt(user);
				   
				   
				   reader = new Reader_factory(currid, current.getAttributeValue("manufacturer"),
						   current.getAttributeValue("name"),
						   current.getAttributeValue("picture"),
						   current.getAttributeValue("port"),
						   userid );
				   //System.out.println("Reader found "+reader.name);
				   return reader;
			   }
		   }
	   }
	   return null;	//Not found
   }
   public String get_username(int id) {
	   
	   if(users == null) return "";
	   List listusers = users.getChildren();
	   if (listusers != null){
		   Iterator i = listusers.iterator();
		   while(i.hasNext()){
			   Element current = (Element)i.next();
			   int currid = Integer.parseInt(current.getAttributeValue("id"));
			   if(currid == id) {
				   return current.getAttributeValue("name");
			   }
		   }
	   }
	   return "";
   }
   /*
    * set parameters values
    */
   public void set_XML_path(String new_path) {
	   if(XML_Path != null) {
		   XML_Path.setText(new_path);
	   } 
   }
   public void set_GGC_path(String new_path) {
	   if(GGC_Path != null) {
		   GGC_Path.setText(new_path);
	   } 
   }
   public void set_Start_Date(String new_date) {
	   if(Start_Date != null) {
		   Start_Date.setText(new_date);
	   } 
   }
   public void set_End_Date(String new_date) {
	   if(End_Date != null) {
		   End_Date.setText(new_date);
	   } 
   }
   public void set_last_meter(int item) {
	   if(item != -1) {
		   last_meter.setText(Integer.toString(item));
	   } 
   }
}
