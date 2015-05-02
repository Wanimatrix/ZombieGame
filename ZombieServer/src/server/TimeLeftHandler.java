package server;

import gamestateobjects.GameStatus;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TimeLeftHandler implements HttpHandler {

	private GameStatus status;
	
	public TimeLeftHandler(GameStatus s){
		this.status = s;
	}
	@Override
	public void handle(HttpExchange t) throws IOException {
		String response = "{\"data\": \""+status.timeLeft()+"\"}";
		Sender.sendData(t, response);
	}
}
