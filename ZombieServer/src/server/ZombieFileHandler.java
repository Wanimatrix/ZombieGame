package server;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class ZombieFileHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {
		String root = "www";
	    URI uri = t.getRequestURI();
	    System.out.println(root +uri.getPath());
	    File file = null;
	    if(uri.getPath().equals("/"))
	    	file = new File(root + uri.getPath()+"index.html").getCanonicalFile();
	    else
	    	file = new File(root + uri.getPath()).getCanonicalFile();
	    
	    if (!file.isFile()) {
	      // Object does not exist or is not a file: reject with 404 error.
	      String response = "404 (Not Found)\n";
	      t.sendResponseHeaders(404, response.length());
	      OutputStream os = t.getResponseBody();
	      os.write(response.getBytes());
	      os.close();
	    } else {
	      // Object exists and is a file: accept with response code 200.
	      t.sendResponseHeaders(200, 0);
	      OutputStream os = t.getResponseBody();
	      FileInputStream fs = new FileInputStream(file);
	      final byte[] buffer = new byte[0x10000];
	      int count = 0;
	      while ((count = fs.read(buffer)) >= 0) {
	        os.write(buffer,0,count);
	      }
	      fs.close();
	      os.close();
	    }
	}

	
}
