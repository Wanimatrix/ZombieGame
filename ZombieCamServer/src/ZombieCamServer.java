import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamStreamer;


public class ZombieCamServer {
	Webcam webcam;
	
	public ZombieCamServer(){
		System.out.println("Amount wbcams: "+Webcam.getWebcams().size());
		webcam = Webcam.getWebcams().get(CONFIG.webcamId);
	}
	
	public void start() throws UnknownHostException, UnsupportedEncodingException, IOException{
		webcam.setViewSize(new Dimension(640, 480));
		new WebcamStreamer(CONFIG.localport, webcam, 100, true);
		sendData("connectcam", CONFIG.room + "$$$" + CONFIG.cameraname + "$$$" + CONFIG.localip+":"+CONFIG.localport);
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}
	
	private static void sendData(String endpoint, String data)	throws UnknownHostException, IOException, UnsupportedEncodingException {

		Socket socket = new Socket(CONFIG.zombieserverip, CONFIG.zombieserverport);

		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
		wr.write("POST " + "/" + endpoint + " HTTP/1.0\r\n");
		wr.write("Content-Length: " + data.length() + "\r\n");
		wr.write("Content-Type: text/html");
		wr.write("\r\n");
		wr.write("\r\n");
		wr.write(data);
		wr.write("\r\n");
		wr.write("\r\n");

		wr.flush();
		wr.close();

		socket.close();
	}

	public static void main(String[] args) throws UnknownHostException, UnsupportedEncodingException, IOException {
		ZombieCamServer zcs = new ZombieCamServer();
		zcs.start();
	}

}