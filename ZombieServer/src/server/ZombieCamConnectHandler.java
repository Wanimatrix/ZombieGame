package server;
import gamestateobjects.CamList;
import gamestateobjects.RoomList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class ZombieCamConnectHandler implements HttpHandler {

	private final CamList rooms;
	
	public ZombieCamConnectHandler(CamList rooms){
		this.rooms = rooms;
	}
	
	public void handle(HttpExchange t) throws IOException {

		BufferedReader input = new BufferedReader(new InputStreamReader(t.getRequestBody()));
		String[] cam = input.readLine().split(Pattern.quote("$$$"));
		System.out.println("----------------------");
		System.out.println("Incoming ZombieCam:");
		System.out.println("room: "+cam[0]);
		System.out.println("cam: "+cam[0]);
		System.out.println("address: "+ cam[1]);
		System.out.println("----------------------");
		rooms.addCamToRoom(cam[0], cam[1], cam[2]);
		
		t.sendResponseHeaders(200, 0);
		input.close();
		t.close();
		//System.out.println(camlist.toJSON());
	}
}
