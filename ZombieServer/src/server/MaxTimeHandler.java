package server;

import gamestateobjects.GameStatus;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MaxTimeHandler implements HttpHandler {

	private GameStatus status;
	
	public MaxTimeHandler(GameStatus s){
		this.status = s;
	}
	@Override
	public void handle(HttpExchange t) throws IOException {
		String response = "{\"data\": \""+status.getMaxTime()+"\"}";
		Sender.sendData(t, response);
	}
}
