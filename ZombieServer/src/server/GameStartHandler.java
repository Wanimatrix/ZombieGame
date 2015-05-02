package server;

import gamestateobjects.GameStatus;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GameStartHandler implements HttpHandler {

	private GameStatus status;
	
	public GameStartHandler(GameStatus s) {
		this.status = s;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		status.startGame();
		Sender.sendData(t, "{\"data\": \"true\"}");
	}
}
