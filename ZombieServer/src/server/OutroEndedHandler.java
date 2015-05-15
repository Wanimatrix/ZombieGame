package server;

import gamestateobjects.GameStatus;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class OutroEndedHandler implements HttpHandler {

	private GameStatus status;
	
	public OutroEndedHandler(GameStatus status) {
		this.status = status;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		status.stopOutro();
		String response = "{\"data\": \"true\"}";
		Sender.sendData(t, response);
	}

}
