package server;

import gamestateobjects.RoomList;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RoomCountRequestHandler implements HttpHandler {

	public RoomList r;
	public RoomCountRequestHandler(RoomList r) {
		this.r = r;
	}
	@Override
	public void handle(HttpExchange t) throws IOException {
		int rcount = r.getRoomCount();
		String response = "{\"roomcount\":\""+rcount+"\"}";
		Sender.sendData(t, response);
	}

}
