package gamestateobjects.enigmas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;
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
			sendData(t, "{\"data\": \""+getTip()+"\"}", false);
		else if(uri.contains("/checksolution")){
			BufferedReader input = new BufferedReader(new InputStreamReader(t.getRequestBody()));
			String sol = input.readLine();
			
			if(checkSolution(sol))
				sendData(t, "{\"data\": \"true\"}", false);
			else
				sendData(t, "{\"data\": \"false\"}", false);
		}
		else if(uri.contains("/getnextroom")){
			if(hasNextRoom())
				sendData(t, "{\"data\": \""+getNextRoomName()+"\"}", false);
			else
				sendData(t, "{\"data\": \"none\"}", false);
		}	
	}
	
	void sendData(HttpExchange t, String data, boolean isJson) throws IOException{
		Headers responseHeaders = t.getResponseHeaders();
		if(isJson)
			responseHeaders.set("Content-Type","application/json");
        t.sendResponseHeaders(200, data.length());
        OutputStream os = t.getResponseBody();
        os.write(data.getBytes());
        os.close();
	}
}


