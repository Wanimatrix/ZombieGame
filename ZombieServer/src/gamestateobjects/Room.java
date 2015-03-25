package gamestateobjects;

import java.util.ArrayList;
import java.util.List;

public class Room {
	
	private String name;
	private List<ZombieCam> camlist = new ArrayList<>();
	private boolean locked = true;
	
	public Room(String name){
		this.name = name;
	}
	
	public void addCam(String name, String address){
		this.camlist.add(new ZombieCam(name, address));
	}
	
	public String getName() {
		return name;
	}
	
	public List<ZombieCam> getAllCams(){
		return camlist;
	}
	
	public void lock(){
		this.locked = true;
	}
	
	public void unlock(){
		this.locked = false;
	}
	
	public boolean isLocked(){
		return locked;
	}


}
