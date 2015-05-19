package server;

import gamestateobjects.GameStatus;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StartOutroHandler implements HttpHandler {

	private GameStatus status;
	
	public StartOutroHandler(GameStatus status) {
		this.status = status;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		status.startOutro();
		String response = "{\"data\": \"true\"}";
		System.out.println("outrostarted");
		Sender.sendData(t, response);
	}

}
