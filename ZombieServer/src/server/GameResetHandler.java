package server;

import gamestateobjects.RoomList;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GameResetHandler implements HttpHandler{

	private RoomList rlist = null;
	
	public GameResetHandler(RoomList rlist) {
		this.rlist = rlist;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		rlist.lockAllRooms();
	}

}
