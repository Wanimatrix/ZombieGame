package server;

import gamestateobjects.RoomList;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;
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
		Headers responseHeaders = t.getResponseHeaders();
		responseHeaders.set("Content-Type","application/json");
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();

	}

}
