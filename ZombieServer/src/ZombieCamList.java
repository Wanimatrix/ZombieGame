import java.util.ArrayList;
import java.util.HashMap;


public class ZombieCamList {

	private HashMap<String, ArrayList<String>> camclusters = new HashMap<String, ArrayList<String>>();
	private HashMap<String, String> camlist = new HashMap<String, String>();

	public synchronized void addCam(String room, String name, String address){
		if(camclusters.get(room) == null)
			camclusters.put(room, new ArrayList<String>());
		camclusters.get(room).add(name);
		camlist.put(name, address);
	}
	
	public String toJSON() {
		
		String json = "[";
		for (String cluster : camclusters.keySet()) {
			json += "{\"roomname\": \"" + cluster + "\", \"camcluster\":[";
				
			for (String cam : camclusters.get(cluster)) {
				json += "{\"name\" : \"" + cam + "\", \"address\" : \""+camlist.get(cam)+"\"},";
			}
			json = json.substring(0, json.length() - 1);
			json += "]},";
		}
		
		json = json.substring(0, json.length() - 1);
		json += "]";
		
		return json;
	}
	
	public static void main(String[] args) {
		//TEST
		ZombieCamList c = new ZombieCamList();
		c.addCam("room1","cam1", "::1");
		c.addCam("room1", "cam2", "::2");
		c.addCam("room2", "cam3", "::3");
		c.addCam("room2", "cam4", "::4");
		System.out.println(c.toJSON());
	}

}
