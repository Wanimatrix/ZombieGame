package server;

import gamestateobjects.GameStatus;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class EndGameStartedHandler implements HttpHandler {

	private GameStatus status;

	public EndGameStartedHandler(GameStatus s){
		this.status = s;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		String response = "{\"data\": \""+status.hasEndgameStarted()+"\"}";
		Sender.sendData(t, response);
	}

}