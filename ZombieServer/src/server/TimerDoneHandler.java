package server;

import gamestateobjects.GameStatus;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TimerDoneHandler implements HttpHandler {

	private GameStatus status;
	
	public TimerDoneHandler(GameStatus s){
		this.status = s;
	}
	@Override
	public void handle(HttpExchange t) throws IOException {
		status.setInProgress(false);
		String response = "{\"data\": \"true\"}";
		Sender.sendData(t, response);
	}
}
