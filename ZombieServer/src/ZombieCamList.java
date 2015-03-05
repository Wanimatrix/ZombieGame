import java.util.HashMap;


public class ZombieCamList {

	private HashMap<String, String> camlist = new HashMap<String, String>();
	
	public ZombieCamList(){
		
	}
	
	public void addCam(String name, String address){
		camlist.put(name, address);
	}
	
	
	public String toJSON() {
		
		String json = "[";
		for (String camname : camlist.keySet()) {
			json += "{\"name\" : \"" + camname + "\", \"address\" : \""+camlist.get(camname)+"\"},";
		}
		json = json.substring(0, json.length() - 1);
		json += "]";
		
		return json;
	}
	
	public static void main(String[] args) {
		//TEST
		ZombieCamList c = new ZombieCamList();
		c.addCam("room1", "::1");
		c.addCam("room2", "::2");
		c.addCam("room3", "::3");
		System.out.println(c.toJSON());
	}

}
