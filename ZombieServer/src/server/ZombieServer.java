package server;
import gamestateobjects.RoomList;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.*;


public class ZombieServer {

	private final HttpServer server;
	private final RoomList roomlist = new RoomList();
	
	public ZombieServer(int port) throws IOException{
		
		Thread t = new Thread(new LockThread(roomlist));
		t.start();
		
		server = HttpServer.create(new InetSocketAddress(8082), 300);
		server.createContext("/", new ZombieFileHandler());
		server.createContext("/connectcam", new ZombieCamConnectHandler(roomlist));
		server.createContext("/getcams", new ZombieCamListRequestHandler(roomlist));
		server.createContext("/roomstatus", new RoomStatusUpdateHandler(roomlist));
		server.createContext("/roomcount", new RoomCountRequestHandler(roomlist));

		server.setExecutor(null);
		server.start();
	}
	
	
	public static void main(String[] args) throws IOException {
		new ZombieServer(8082);
	}
}
