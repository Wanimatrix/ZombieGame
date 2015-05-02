package server;

import gamestateobjects.GameStatus;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class InProgressHandler implements HttpHandler {

	private GameStatus status;

	public InProgressHandler(GameStatus s){
		this.status = s;
	}
	@Override
	public void handle(HttpExchange t) throws IOException {
		String response = "{\"data\": \""+status.isInProgress()+"\"}";
		Sender.sendData(t, response);
	}

}
