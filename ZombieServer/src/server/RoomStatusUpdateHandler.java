package server;
import gamestateobjects.RoomList;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;
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
		Headers responseHeaders= t.getResponseHeaders();
		responseHeaders.set("Content-Type","application/json");
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}

}
