package server;

import gamestateobjects.Message;
import gamestateobjects.MessageService;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MessageRequestHandler implements HttpHandler {

	private MessageService mservice;
	SecureRandom random = new SecureRandom();
	public MessageRequestHandler(MessageService mservice){
		this.mservice = mservice;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		int last_received = Integer.parseInt(t.getRequestURI().toString().split("=")[1]);
		//if(random.nextBoolean() && random.nextBoolean() )
		//mservice.publishMessage(new BigInteger(40, random).toString(32), new BigInteger(130, random).toString(32));
		ArrayList<Message> list = mservice.getAllMessageSince(last_received);
		
		String json = "[";
		for (Message m : list)
			json += m.toJSON() + ",";
		if(json.length() > 1)
			json = json.substring(0, json.length() - 1) + "]";
		if(!json.endsWith("]"))
			json += "]";	
		System.out.println(json);
		Sender.sendData(t, json);
	}

}
