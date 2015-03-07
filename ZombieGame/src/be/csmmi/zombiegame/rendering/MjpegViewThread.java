package be.csmmi.zombiegame.rendering;

import java.io.IOException;

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
    public int IMG_WIDTH=640;
	public int IMG_HEIGHT=480;
	private GLSurfaceView view;
	private MjpegInputStream source;
	private CameraManager camManager;
     
    public MjpegViewThread(Surface surface, GLSurfaceView view, MjpegInputStream source, CameraManager camManager) { 
        this.surface = surface; 
        this.view = view;
        this.source = source;
        this.camManager = camManager;
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
                			bmp = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
                		}
                		int ret = source.readMjpegFrame(bmp);

                		if(ret == -1)
                		{
                			Log.d("MJPEG", "Error while reading frame");
                			camManager.readCameraStream();
                			mRun = false;
                			break;
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
