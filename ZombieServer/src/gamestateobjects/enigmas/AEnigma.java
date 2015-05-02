package gamestateobjects.enigmas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import server.Sender;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class AEnigma implements HttpHandler{

	abstract boolean checkSolution(String s);
		
	public abstract String getContext();
	
	abstract String getTip();
	
	abstract boolean hasNextRoom();
	
	abstract String getNextRoomName();
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		String uri = t.getRequestURI().toString();
		if(uri.contains("/gettip"))
			Sender.sendData(t, "{\"data\": \""+getTip()+"\"}");
		else if(uri.contains("/checksolution")){
			BufferedReader input = new BufferedReader(new InputStreamReader(t.getRequestBody()));
			String sol = input.readLine();
			
			if(checkSolution(sol))
				Sender.sendData(t, "{\"data\": \"true\"}");
			else
				Sender.sendData(t, "{\"data\": \"false\"}");
		}
		else if(uri.contains("/getnextroom")){
			if(hasNextRoom())
				Sender.sendData(t, "{\"data\": \""+getNextRoomName()+"\"}");
			else
				Sender.sendData(t, "{\"data\": \"none\"}");
		}	
	}
}


