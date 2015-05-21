package server;

import gamestateobjects.GameStatus;
import gamestateobjects.MessageService;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StartOutroHandler implements HttpHandler {

	private GameStatus status;
	private MessageService mserv;
	
	public StartOutroHandler(GameStatus status, MessageService mserv) {
		this.status = status;
		this.mserv = mserv;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		status.startOutro();
		String response = "{\"data\": \"true\"}";
		System.out.println("outrostarted");
		Sender.sendData(t, response);
		
		Timer timer = new Timer();

		// scheduling the task at interval
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				mserv.publishMessage("Jane D.", 
						"BROADCAST 6 OF 6: "
						+ "The doctor said he can’t find blue eyes for me and that I won’t be an Übermensch after all. "
						+ "He said I’ve become sick and that I’m not strong enough, just like my mommy. "
						+ "He said he won’t come back anymore and that I’ll die. "
						+ "He chained me to my bed and left. "
						+ "I used the secret transmitter my mommy fixed to broadcast this message in a loop. "
						+ "Please come help me and please find my mommy."
						+ "July 6th 1942.");
			}
		}, 68*1000);
	}

}
