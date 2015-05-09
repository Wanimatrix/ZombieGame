package server;

import gamestateobjects.GameStatus;
import gamestateobjects.MessageService;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GameResetHandler implements HttpHandler{

	private GameStatus status = null;
	private MessageService mService = null;
	
	public GameResetHandler(GameStatus s, MessageService mService) {
		this.status = s;
		this.mService = mService;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		status.resetGame();
		mService.reset();
		Sender.sendData(t, "{\"data\": \"true\"}");
	}

}
