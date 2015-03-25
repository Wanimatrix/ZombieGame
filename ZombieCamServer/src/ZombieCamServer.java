import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.imgscalr.Scalr.*;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.WebcamStreamer;


public class ZombieCamServer implements WebcamImageTransformer {
	Webcam webcam;
	
	public ZombieCamServer(){
		System.out.println("Amount wbcams: "+Webcam.getWebcams().size());
		webcam = Webcam.getWebcams().get(CONFIG.webcamId);
		webcam.setImageTransformer(this);
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

	@Override
	public BufferedImage transform(BufferedImage old) {
		old = apply(old, OP_GRAYSCALE);

		//		BufferedImage img = new BufferedImage(
//                w, h, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = old.createGraphics();
//        g2d.drawImage(old, 0, 0, null);
        
        g2d.setFont(new Font("Serif", Font.BOLD, 40));
        String s = CONFIG.room+" - "+CONFIG.cameraname;
        FontMetrics fm = g2d.getFontMetrics();
        int x = old.getWidth() - fm.stringWidth(s) - 5;
        int y = fm.getHeight() + 5;
        g2d.setColor(new Color(0, 0, 0, 128));
        g2d.fillRect(x-10, 0, fm.stringWidth(s)+20, fm.getHeight()+15);
        g2d.setPaint(Color.white);
        g2d.drawString(s, x, y);
        
        g2d.dispose();
		
		return old;
	}

}