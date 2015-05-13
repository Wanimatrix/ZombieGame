package gamestateobjects;
import java.util.Collection;
import java.util.HashMap;


public class RoomList {

	private HashMap<String, Room> roomlist = new HashMap<String, Room>();

	private int roomCount = 0;
	
	
	
	public void addRoom(String name){
		if(roomlist.get(name) == null){
			roomlist.put(name, new Room(name));
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
	
	
	public void lockAllRooms(){
		for (Room r : roomlist.values())
			r.lock();
	}
	
	public Collection<Room> getAllRooms(){
		return roomlist.values();
	}
	

	public String getRoomStatusJSON() {
		
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
		Room r = c.getRoom("room1");
		r.unlock();
		System.out.println(c.getRoomStatusJSON());
	}

}
