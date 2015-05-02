package server;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class Sender {
	
	public static void sendData(HttpExchange t, String response) throws IOException {
		Headers responseHeaders= t.getResponseHeaders();
		responseHeaders.set("Content-Type","application/json");
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}
}
