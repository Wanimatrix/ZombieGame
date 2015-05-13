package server;
import gamestateobjects.CamList;
import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class ZombieCamListRequestHandler implements HttpHandler {

	private final CamList camlist;
	
	public ZombieCamListRequestHandler(CamList camlist) {
		this.camlist = camlist;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		String response = camlist.getCamListJSON();
		Sender.sendData(t, response);
	}

}
