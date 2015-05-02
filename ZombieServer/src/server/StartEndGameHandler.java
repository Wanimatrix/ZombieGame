package server;

import gamestateobjects.GameStatus;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StartEndGameHandler implements HttpHandler {

	private GameStatus status;

	public StartEndGameHandler(GameStatus s){
		this.status = s;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		status.startEndGame();
		String response = "{\"data\": \""+true+"\"}";
		Sender.sendData(t, response);
	}

}