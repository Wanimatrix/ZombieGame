package gamestateobjects;

public class ZombieCam {

	private String name;
	private String address;
	
	public ZombieCam(String name, String address){
		this.name = name;
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getName() {
		return name;
	}
}
