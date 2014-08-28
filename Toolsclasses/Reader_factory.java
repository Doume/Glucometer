package Toolsclasses;

public class Reader_factory {
	public int ID;
	public String Manufacturer;
	public String name;
	public String imagefile;
	public String port;
	public int userid;
	
	public Reader_factory(int pID, String pManufacturer, String pname, String pimagefile, String pport, int puserid) {
		ID = pID;
		Manufacturer = pManufacturer;
		name = pname;
		imagefile=pimagefile;
		port=pport;
		userid=puserid;
	}
}
