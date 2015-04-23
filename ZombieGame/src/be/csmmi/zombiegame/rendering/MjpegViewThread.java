package be.csmmi.zombiegame.rendering;

import java.io.IOException;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import be.csmmi.zombiegame.app.AppConfig;
import be.csmmi.zombiegame.app.GameManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;
import android.view.View;

public class MjpegViewThread extends Thread {
    private Surface surface;
    private boolean mRun = false;
    private boolean surfaceDone = false;  
    private Bitmap bmp = null;
	private GLSurfaceView view;
	private MjpegInputStream source;
	private CameraManager camManager;
	private GameManager gm;
     
    public MjpegViewThread(Surface surface, GLSurfaceView view, MjpegInputStream source, CameraManager camManager, GameManager gm) { 
        this.surface = surface; 
        this.view = view;
        this.source = source;
        this.camManager = camManager;
        this.gm = gm;
    }
    
    public void stopDrawing() {
    	mRun = false;
    }
    
    public void startDrawing() {
    	mRun = true;
    }
    
    public void surfaceDone() {
    	this.surfaceDone = true;
    }
    
    public void setSource(MjpegInputStream source) {
    	this.source = source;
    }

    public void run() {
    	long start = System.currentTimeMillis();
    	
        Log.d("MJPEG","Reading begins ... ");
        Paint p = new Paint();
        while(true) {
        	Log.d("CONNECTION", "Trying to run: "+mRun);
            while (mRun) {
            	
                Canvas c = null;

                if(surfaceDone) {   
                	try {
                		if(bmp==null){
                			bmp = Bitmap.createBitmap(AppConfig.PREVIEW_RESOLUTION[0], AppConfig.PREVIEW_RESOLUTION[1], Bitmap.Config.ARGB_8888);
                		}
                		
                		Mat status = new Mat();
                		int gameStatus = gm.getGameStatus(status);
                		
                		if(gameStatus == 1) {
                			Utils.matToBitmap(status, bmp);
                		} else {
	                		int ret = source.readMjpegFrame(bmp);
	
	                		if(ret == -1)
	                		{
	                			Log.d("MJPEG", "Error while reading frame");
	                			camManager.readCameraStream();
	                			mRun = false;
	                			break;
	                		}
                		}
                        
                		synchronized (surface) {
                			c = surface.lockCanvas(null);
                			c.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()), new Rect(0, 0, c.getWidth(), c.getHeight()), p);
                        }

                    }catch (IOException e){ 
                }finally { 
                    	if (c != null) {
                    		surface.unlockCanvasAndPost(c); 
                    		Log.d("MJPEG", "Canvas unlocked!");
                    		view.requestRender();
                    	}
                    }
                }
            }
	        try {
	        	sleep(1000);
			} catch (InterruptedException e) { }
        }
    }
}
