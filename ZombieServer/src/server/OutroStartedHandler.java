package server;

import gamestateobjects.GameStatus;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class OutroStartedHandler implements HttpHandler {

	private GameStatus status;
	
	public OutroStartedHandler(GameStatus status) {
		this.status = status;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		String response = "{\"data\": \""+status.getStartOutro()+"\"}";
		Sender.sendData(t, response);
	}

}
