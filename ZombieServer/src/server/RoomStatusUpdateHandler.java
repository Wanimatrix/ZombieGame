package server;
import gamestateobjects.RoomList;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class RoomStatusUpdateHandler implements HttpHandler {

	private final RoomList rooms;
	
	public RoomStatusUpdateHandler(RoomList rooms) {
		this.rooms = rooms;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		String response = rooms.getRoomStatusJSON();
		Sender.sendData(t, response);
	}

}
