package lang_parser;
/*******************************************************************************
*      LangFileParser class -> ok
*
* This class allows to create a multilanguage system by reading the language 
* 	file.
* ***** How to use 
* // Use the LangFileParser to support multi-language 
* lang = LangFileParser.getInstance(instance_of_main_windows);
* 				The main_window must propose a method getlang() 
* 				which return the filename of lang file to process
* 				example : "fr.xml" or "eng.xml" 
*
* // Receive the text to display
* lang.getValue(this.getClass().getName(), "tagName")
*
*
* ***** Methods:
* + static LangFileParser getInstance() -> get the current instance 
* 						of LangFileParser
* + String getValue(String className, String elementName) -> get the value to 
*						display for a specific element
*
* ***** Developper informations
* Developper :  Doume 01/11/2008
* Date : 02/07/2008
* E-Mail : domi@dambrain.homelinux.net
*******************************************************************************/
import java.io.*;
import java.util.*;


import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import main.Main_Window;		//for its method getlang()

public class LangFileParser
{
/*******************************************************************************
*		Fields
*******************************************************************************/
	private static LangFileParser instance;
	private static Main_Window main_window;
	// XML version
	static org.jdom2.Document document;
	static Element messages;	//root of XML tree element
	static List classes;		//section <classes> pointer
	
	private String filename ;
	private	File file;	
	
		
/*******************************************************************************
*		Constructor
*******************************************************************************/
	private LangFileParser()
	{
		//obtain translation filename from main window
		filename = main_window.getlang();
		//construct a file object on it
		file = new File(filename);
		//and load XML translation messages
		load_messages();
	}
	

/*******************************************************************************
*		Methods
*******************************************************************************/
	// Get the current instance of LangFileParser
	public static LangFileParser getInstance(Main_Window main_win)
	{
		if(instance == null)
		{
			main_window = main_win;
			instance = new LangFileParser();
		}
		return instance;
	}
	
	/* 
	 * This method retrieve a text string corresponding to class
	 * and tag parameters
	 * or null if not found
	 */
	public String getValue(String className, String tagName)
	{
		if(messages != null) {
			classes = messages.getChildren("class");
			@SuppressWarnings("rawtypes")
			Iterator i = classes.iterator();
			
			while(i.hasNext())	//scan classes to find the good one
		    {
		      Element current = (Element)i.next();
		      if(current.getAttributeValue("name").equals(className)){
		    	  //System.out.printf("Found class <%s>\n",className);
		    	  List tags = current.getChildren("tag");
		    	  Iterator j = tags.iterator();
		    	  while(j.hasNext()) {
		    		  Element curtag = (Element)j.next();
		    		  if(curtag.getAttributeValue("name").equals(tagName)) {
		    			  //System.out.printf("Found tag <%s>\n",curtag.getAttributeValue("name"));
				    	  return curtag.getAttributeValue("msg");
		    		  }
		    	  }
		    	  return null;
		      }
		      
		    }
		}
		return null;	//class not found
	}
	private void load_messages()
	   {
	      // Create a SAXBuilder instance
	      SAXBuilder sxb = new SAXBuilder();
	      try
	      {
	         //Create new JDOM Document in memory
	         document = sxb.build(file);
	      }
	      catch(Exception e){
	    	  System.out.printf("exception reading XML Document : %s\n",e.toString());
	    	  System.out.println("XML Document '"+filename+"' malformed");
	          return;
	      }
	      messages = document.getRootElement();
	   }
	
}
