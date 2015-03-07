package be.csmmi.zombiegame.rendering;

import java.util.ArrayList;
import java.util.List;

public class Room {
	private String name;
	private List<Cam> cameras = new ArrayList<Cam>();
	
	public Room(String name) {
		this.name = name;
	}
	
	public void addCamera(Cam camera) {
		cameras.add(camera);
	}
	
	public List<Cam> getCameras() {
		return cameras;
	}
	
	public String getName() {
		return name;
	}
}
