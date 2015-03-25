package gamestateobjects;
import java.util.Collection;
import java.util.HashMap;


public class RoomList {

	private HashMap<String, Room> roomlist = new HashMap<String, Room>();
	
	public int getRoomCount(){
		return roomlist.values().size();
	}
	
	public void addRoom(String name){
		if(roomlist.get(name) == null)
			roomlist.put(name, new Room(name));
	}
	
	public Room getRoom(String name){
		return roomlist.get(name);
	}

	public synchronized void addCamToRoom(String room, String name, String address){
		Room r = getRoom(room);
		if(r == null){
			addRoom(room);
			r = getRoom(room);
		}
		r.addCam(name, address);
	}
	public Collection<Room> getAllRooms(){
		return roomlist.values();
	}
	
	public String camListJSON() {
		
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
				
		json = json.substring(0, json.length() - 1);
		json += "]";
		
		return json;
	}
	
	public String roomStatusJSON() {
		
		String json = "[";
		for (String roomname : roomlist.keySet()) {
			Room r = getRoom(roomname);
			if(r.isLocked())
				json += "{\"room\": \"" + roomname + "\", \"status\":\"locked\"}," 	;
			else
				json += "{\"room\": \"" + roomname + "\", \"status\":\"unlocked\"}," 	;
		}
				
		json = json.substring(0, json.length() - 1);
		json += "]";
		
		return json;
	}
	
	public static void main(String[] args) {
		//TEST
		RoomList c = new RoomList();
		c.addCamToRoom("room1","cam1", "::1");
		c.addCamToRoom("room1", "cam2", "::2");
		c.addCamToRoom("room2", "cam3", "::3");
		c.addCamToRoom("room2", "cam4", "::4");
		Room r = c.getRoom("room1");
		r.unlock();
		System.out.println(c.camListJSON());
		System.out.println(c.roomStatusJSON());
	}

}
