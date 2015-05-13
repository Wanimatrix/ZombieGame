package gamestateobjects;
import java.util.Collection;
import java.util.HashMap;


public class CamList {

	private HashMap<String, Room> roomlist = new HashMap<String, Room>();
	private HashMap<String, Room> camlist = new HashMap<String, Room>();

	private String camListJSON = "[]";
	private int roomCount = 0;
	
	
	
	public void addRoom(String name){
		if(roomlist.get(name) == null){
			roomlist.put(name, new Room(name));
			updateCamListJSON();
			roomCount++;
		}
	}
	
	public Room getRoom(String name){
		return roomlist.get(name);
	}
	
	public int getRoomCount(){
		return 4;
		//return roomCount;
	}
	
	public synchronized void addCamToRoom(String room, String name, String address){
		Room r = getRoom(room);
		if(r == null){
			addRoom(room);
			r = getRoom(room);
		}
		r.addCam(name, address);
		updateCamListJSON();
	}
	
	public void lockAllRooms(){
		for (Room r : roomlist.values())
			r.lock();
	}
	
	public Collection<Room> getAllRooms(){
		return roomlist.values();
	}
	
	public String getCamListJSON() {
		return camListJSON;
	}
	public String getRoomStatusJSON() {
		
		String json = "[";
		for (String roomname : roomlist.keySet()) {
			Room r = getRoom(roomname);
			if(r.isLocked())
				json += "{\"room\": \"" + roomname + "\", \"status\":\"unlocked\"}," 	;
			else
				json += "{\"room\": \"" + roomname + "\", \"status\":\"unlocked\"}," 	;
		}
				
		json = json.substring(0, json.length() - 1);
		json += "]";
		
		return json;
	}
	
	private void updateCamListJSON() {
		
		String json = "[";
		for (String roomname : roomlist.keySet()) {
			Room r = getRoom(roomname);
			json += "{\"roomname\": \"" + roomname + "\", \"camcluster\":[";
			
			for (ZombieCam cam : r.getAllCams()){
				json += "{\"name\" : \"" + cam.getName() + "\", \"address\" : \""+cam.getAddress()+"\"},";
			}
			json = json.substring(0, json.length() - 1);
			json += "]},";
		}
		
		if(json.length() > 1)		
			json = json.substring(0, json.length() - 1);
		json += "]";
		
		this.camListJSON = json;
	}

	public static void main(String[] args) {
		//TEST
		CamList c = new CamList();
		c.addCamToRoom("room1","cam1", "::1");
		c.addCamToRoom("room1", "cam2", "::2");
		c.addCamToRoom("room2", "cam3", "::3");
		c.addCamToRoom("room2", "cam4", "::4");
		Room r = c.getRoom("room1");
		r.unlock();
		System.out.println(c.getCamListJSON());
		System.out.println(c.getRoomStatusJSON());
	}

}
