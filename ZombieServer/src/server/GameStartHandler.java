package server;

import gamestateobjects.GameStatus;
import gamestateobjects.MessageService;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GameStartHandler implements HttpHandler {

	private GameStatus status;
	private MessageService mserv;
	public GameStartHandler(GameStatus s, MessageService mserv) {
		this.status = s;
		this.mserv = mserv;
	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		status.startGame();
		Sender.sendData(t, "{\"data\": \"true\"}");
		Timer timer = new Timer();

		// scheduling the task at interval
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				mserv.publishMessage("Jane D.", 
						"Broadcast 1 of 6: "
						+ "Hello? Is anybody there? "
						+ "Uhm, I’m Jane. Have you seen my teddybear? "
						+ "The mean man took him away from me. "
						+ "He put me in a room without windows and chained me to my bed."
						+ "My mommy is here somewhere too.");					
			}
		}, new Random().nextInt(9000)+5000);      
	}

}
