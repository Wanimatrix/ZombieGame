package server;
import gamestateobjects.GameStatus;
import gamestateobjects.MessageService;
import gamestateobjects.RoomList;
import gamestateobjects.enigmas.AEnigma;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.*;


public class ZombieServer {

	private final HttpServer server;
	private final MessageService mservice = new MessageService();
	private final RoomList roomlist = new RoomList();
	private final GameStatus status;
	
	public ZombieServer(int port, AEnigma ... enigmas) throws IOException{
		
		//Thread t = new Thread(new LockThread(roomlist));
		//t.start();
		
		status = new GameStatus(roomlist);
		server = HttpServer.create(new InetSocketAddress(8082), 300);
		server.createContext("/", new ZombieFileHandler());
		server.createContext("/connectcam", new ZombieCamConnectHandler(roomlist));
		server.createContext("/getcams", new ZombieCamListRequestHandler(roomlist));
		server.createContext("/roomstatus", new RoomStatusUpdateHandler(roomlist));
		server.createContext("/roomcount", new RoomCountRequestHandler(roomlist));
		server.createContext("/getmessages", new MessageRequestHandler(mservice));
		server.createContext("/startgame", new GameStartHandler(status));
		server.createContext("/resetgame", new GameResetHandler(status));
		server.createContext("/timeleft", new TimeLeftHandler(status));
		server.createContext("/inprogress", new InProgressHandler(status));
		server.createContext("/endgamestarted", new EndGameStartedHandler(status));
		server.createContext("/startendgame", new StartEndGameHandler(status));

		for (AEnigma ae : enigmas)
			server.createContext(ae.getContext(), ae);
		
		server.setExecutor(null);
		server.start();
		System.out.println("ZombieServer started.");
		
	}
	
	
	public static void main(String[] args) throws IOException {
		new ZombieServer(8082);
	}
}
