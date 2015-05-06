package be.csmmi.zombiegame.rendering;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import project.android.imageprocessing.FastImageProcessingPipeline;
import project.android.imageprocessing.FastImageProcessingView;
import project.android.imageprocessing.filter.GenericFilter;
import project.android.imageprocessing.input.ImageResourceInput;
import project.android.imageprocessing.output.ScreenEndpoint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import be.csmmi.zombiegame.app.AppConfig;
import be.csmmi.zombiegame.app.GameManager;
import be.csmmi.zombiegame.app.ServerCommunication;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

public class CameraManager implements PreviewCallback {
	private static final String TAG = CameraManager.class.getSimpleName();
	
	private Camera camera;
	private List<Room> rooms = new ArrayList<Room>();
	Thread cameraSwitcher;
	private int currentRoomIdx = -1;
	private int currentCameraIdx = -1;
	
	private GLSurfaceView view;
	
	private MjpegViewThread thread;
	private Thread roomStatusThread;
	private Surface surface;
	private Surface frontCamSurface;
//	private RequestQueue queue;
	
	private GameManager gm;
	
//	private Mat zombiePrep;
//	private Mat alphaInv;
	private List<List<Pair<Mat, Mat>>> overlays = new ArrayList<List<Pair<Mat,Mat>>>();
	
//	private FastImageProcessingView imgProcView;
//    private FastImageProcessingPipeline pipeline;
//    private ImageResourceInput imageIn;
//    private GenericFilter generic;
//    private ScreenEndpoint screen;
	
	
	public CameraManager(Context context, GLSurfaceView view) {
		this.view = view;
	    
		// Instantiate the RequestQueue.
//		queue = Volley.newRequestQueue(view.getContext());
	}
	
	public void setSurface(Surface surface) {
		this.surface = surface;
		
//		overlays.add(prepOverlay("/sdcard/zbg/pureSmoke.png"));
//		List<Pair<Mat,Mat>> smokeOverlays = new ArrayList<Pair<Mat,Mat>>();
		List<Pair<Mat,Mat>> zombieOverlays = new ArrayList<Pair<Mat,Mat>>();
//		List<Pair<Mat,Mat>> zombieEyesOverlays = new ArrayList<Pair<Mat,Mat>>();
		for(int i = 0; i < 8; i++) {
//			Log.d(TAG, "ZombieAlpha: "+a);
			zombieOverlays.add(prepOverlay("/sdcard/zbg/zombieScaled-"+i+".png"));
		}
//		zombieOverlays.add(prepOverlay("/sdcard/zbg/lost.png"));
//		overlays.add(smokeOverlays);
		overlays.add(zombieOverlays);
		
		this.gm = new GameManager(this.view.getContext(), overlays.get(0).size());
//		overlays.add(zombieEyesOverlays);
		
//		overlays.add(prepOverlay("/sdcard/zbg/zombieNewOnlyEyes.png"));
	}
	
	private Pair<Mat,Mat> prepOverlay(String overlayFilename) {
		Mat overlay = Highgui.imread(overlayFilename, -1);
		
		Log.d(TAG, "OVERLAY CHANNELS: "+overlay.channels()+" for "+overlayFilename);
		
		Mat alphaMat = new Mat(overlay.size(), CvType.CV_8UC(3));
//		Core.multiply(alphaMat, new Scalar(alpha), alphaMat);
		Core.mixChannels(Arrays.asList(overlay), Arrays.asList(alphaMat), new MatOfInt(3,0,3,1,3,2));
		Mat noAlphaFg = new Mat(overlay.size(), CvType.CV_8UC(3));
		Core.mixChannels(Arrays.asList(overlay), Arrays.asList(noAlphaFg), new MatOfInt(0,0,1,1,2,2));
		
		Mat one = new Mat(alphaMat.size(), alphaMat.type());
		Log.d(TAG, "ONES size: 1 "+Mat.ones(alphaMat.size(), alphaMat.type()).channels());
		Mat alphaInv = new Mat();
		Mat tmp = new Mat();
		Core.multiply(Mat.ones(alphaMat.size(), CvType.CV_8UC1), new Scalar(255), tmp);
		Core.mixChannels(Arrays.asList(tmp), Arrays.asList(one), new MatOfInt(0,0,0,1,0,2));
		Core.subtract(one, alphaMat, alphaInv);
		
		Mat prep = noAlphaFg.mul(alphaMat, 1.0/255.0);
		Imgproc.cvtColor(prep, prep, Imgproc.COLOR_BGR2RGB);
		
		return new Pair<Mat, Mat>(prep, alphaInv);
	}
	
	public void initRoomStatus() {
		final Response.Listener<JSONArray> callback = 
	            new com.android.volley.Response.Listener<JSONArray>() {
	                @Override
	                public void onResponse(JSONArray response) {
	                	try {
	                        // Parsing json array response
	                        // loop through each json object
	                        for (int i = 0; i < response.length(); i++) {
	                            JSONObject roomObj = (JSONObject) response.get(i);
	                            String roomName = (String)roomObj.get("room");
	                            String status = (String)roomObj.get("status");
	                            for (int j = 0; j < rooms.size(); j++) {
									if(rooms.get(j).getName().equals(roomName)) {
										Log.d("LOCKER", "Room "+roomName+": "+status);
										
										if(status.equals("locked")) {
											if(!rooms.get(j).isLocked()) {
												cameraSwitcher.interrupt();
												setupCamera();
											}
											rooms.get(j).setLocked(true);
											Log.d("LOCKER", "LOCKED!");
										}
										else {
											if(rooms.get(j).isLocked()) {
												pauseCamera();
												readCameraStream();
											}
											rooms.get(j).setLocked(false);
											Log.d("LOCKER", "UNLOCKED!");
										}
										
										
										
										break;
									}
								}
	                        }
	                	} catch(Exception e) {};
	                }
	            };
		
		roomStatusThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(3000);
						Log.d("LOCKER", "Added request");
						ServerCommunication.sendArrayMessage("roomstatus", callback);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		roomStatusThread.start();
	}
	
	public void initCameras(final OnCamerasReadyListener listener) {
		
	    Response.Listener<JSONArray> callback = new com.android.volley.Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.d(TAG, response.toString());
 
                    try {
                        // Parsing json array response
                    // loop through each json object
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject roomObj = (JSONObject) response.get(i);
                        Room room = new Room((String)roomObj.get("roomname"));
                        JSONArray camCluster = roomObj.getJSONArray("camcluster");
                        for (int j = 0; j < camCluster.length(); j++) {
                        	JSONObject cam = (JSONObject)camCluster.get(j);
                        	String name = cam.getString("name");
                        	String url = cam.getString("address");
                        	room.addCamera(new Cam(name, "http://"+url));
                        }
                        rooms.add(room);
                    }
                    
                    cameraSwitcher = new Thread() {
        				@Override
        				public void run() {
        					Log.d(TAG, "Switcher started");
        					
        					do {
        						try {
        							Thread.sleep(10000);
        						} catch (InterruptedException e) {
        							Log.d(TAG, "Switcher interrupted");
        							if(!stopCamSwitcher) {
        								currentCameraIdx = 0;
        								continue;
        							} else {
        								
        							}
        						}
        						while(stopCamSwitcher) {
        							Log.d(TAG, "Switcher stopped");
        							try {
        				    		 Thread.sleep(1000);
    				    		   } catch (InterruptedException e) {
    				    		      e.printStackTrace();
    				    		   }
        						}
        						
        						Log.d(TAG, "Switching...");
        						
        						currentCameraIdx = (currentCameraIdx + 1) % rooms.get(currentRoomIdx).getCameras().size();
        						readCameraStream();
        					} while(true);
        				}
        			};
                    
                    currentRoomIdx = 0;
                    currentCameraIdx = 0;
                    
                    listener.onCamerasReady();
                    if(rooms.get( currentRoomIdx).isLocked()) {
            			setupCamera();
            		} else {
            			readCameraStream();
            		}
                    
                } catch (JSONException e) {
                	Log.d("JSON", "JSON ERROR!");
                    e.printStackTrace();
                }
            }
        };
	            
	   Response.ErrorListener errorListener = new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Trying to contact the server ...");
                try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                initCameras(listener);
            }
        };
		
		// Add the request to the RequestQueue.
		ServerCommunication.sendArrayMessage("getcams", callback, errorListener);
		
		initRoomStatus();
	}
	
	private boolean stopCamSwitcher = false;
	
	public void switchRoom() {
		if(rooms.get( (currentRoomIdx + 1) % rooms.size()).isLocked()) {
			Log.d(TAG, "Front view camera...");
			setupCamera();
			currentRoomIdx = (currentRoomIdx + 1) % rooms.size();
			return;
		} else {
			pauseCamera();
		}
		currentRoomIdx = (currentRoomIdx + 1) % rooms.size();
		Log.d(TAG, "New Room: "+currentRoomIdx);
		cameraSwitcher.interrupt();
		readCameraStream();
	}
	
	public void readCameraStream() {
		stopCamSwitcher = true;
		new DoRead().execute(rooms.get(currentRoomIdx).getCameras().get(currentCameraIdx).getUrl());
	}
	
	public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            HttpResponse res = null;         
            DefaultHttpClient httpclient = new DefaultHttpClient(); 
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5*1000);
            HttpConnectionParams.setSoTimeout(httpParams, 5*1000);
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                if(res.getStatusLine().getStatusCode()==401){
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());  
            } catch (ClientProtocolException e) {
	                e.printStackTrace();
	                Log.d(TAG, "Request failed-ClientProtocolException", e);
                //Error connecting to camera
            } catch (IOException e) {
	                e.printStackTrace();
	                Log.d(TAG, "Request failed-IOException", e);
                //Error connecting to camera
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream inStream) {
        	
//        	Log.d("CONN", "Source was set to: "+source);
        	if(inStream == null) {
        		Log.d("CONNECTION", "Trying to connect");
        		try {
    		      Thread.sleep(1000);
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
        		readCameraStream();
        		return;
        	}
        	
        	Log.d("CONNECTION", "Connection succeeded.");
        	
//            source = result;
            if(inStream!=null){
            	inStream.setSkip(1);
            }
            
            if(thread==null){
            	thread = new MjpegViewThread(surface, view, inStream, CameraManager.this, gm);
            } else {
            	thread.stopDrawing();
            	thread.setSource(inStream);
            }
            Log.d("CONN", "Tread starts ... ");
            
            if(!thread.isAlive()) {
            	thread.start(); 
            }
            
            thread.startDrawing();
            thread.surfaceDone();
            if(rooms.get(currentRoomIdx).getCameras().size() > 1 && !cameraSwitcher.isAlive())
            	cameraSwitcher.start();
            stopCamSwitcher = false;
            
        }
    }
	
	/**
	 ****************************
	 * CAMERA CONTROL FUNCTIONS *
	 ****************************
	 */
	boolean beat = false;
	
	Runnable heartBeatRunnable = new Runnable() {
		
		@Override
		public void run() {
			while(true) {
				try {
					Thread.sleep(450);
					beat = true;
					Thread.sleep(600);
					beat = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	};
	Thread heartBeat;
	
	private void setupCamera() {
		stopCamSwitcher = true;
		if(thread != null)
			thread.stopDrawing();
		
		// Setup Camera Parameters
	    Camera.Parameters params = camera.getParameters();
	    params.setPreviewFormat(ImageFormat.NV21);
		params.setPreviewFpsRange(AppConfig.FPS_RANGE[0], AppConfig.FPS_RANGE[1]);
		params.setPreviewSize(AppConfig.PREVIEW_RESOLUTION[0], AppConfig.PREVIEW_RESOLUTION[1]);
		params.set("orientation", "landscape");
//		params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		camera.setParameters(params);
		camera.startPreview();
		
		heartBeat = new Thread(heartBeatRunnable);
		heartBeat.start();
		
		// Add callback buffers to camera for frame handling
		float bytesPerPix = ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8.0f;
		int frame_byteSize = (int) ((AppConfig.PREVIEW_RESOLUTION[0] * AppConfig.PREVIEW_RESOLUTION[1]) * bytesPerPix);
		
		for(int i = 0; i< AppConfig.AMOUNT_PREVIEW_BUFFERS ; i++) {
			camera.addCallbackBuffer(new byte[frame_byteSize]);
		}
		
		camera.setPreviewCallbackWithBuffer(this);
	}
	
	public void startCamera(SurfaceTexture st) {
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Starting camera...");
		
		if(camera == null) {
			camera = Camera.open();
		}
		frontCamSurface = new Surface(st);
//		try {
//			camera.setPreviewTexture(st);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

//	public void stopCamera() {
//		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Stopping camera...");
//		
//		if (camera != null) {
//			camera.stopPreview();
//			camera.setPreviewCallback(null);
//			camera.release();
//			camera = null;
//		}
//	}
	
	public void pauseCamera() {
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Stopping camera...");
		
		heartBeat.interrupt();
		
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
		}
	}
	
	/**
	 **************************
	 *     OnPreviewFrame     *
	 **************************
	 */
	
	Bitmap bmp = null;
	Bitmap bmpZombie = null;
	Mat torchMask = null;
	List<Mat> torchMasks = null;
	int torchCounter = 0;
	double torchRadius = 240;
	Random rand = new Random();
	
	int counter = 0;
	int stayCounter = 0;
	
	long time = -1;
	
	boolean flickerEnabled = false;
	boolean flicker = false;
	Random flickerRand = new Random();
	
	
	@Override
	public void onPreviewFrame(byte[] frameData, Camera camera) {
		Log.d(TAG, "Request render!");
		
		Size size = camera.getParameters().getPreviewSize();
		
		Mat result = new Mat();
		int status = gm.getGameStatus(result);
		if(gm.endGameStarted()) flickerEnabled = true;
		else flickerEnabled = false;
		
		if(status == 0) {
			Mat colFrameImg = new Mat();
			Mat yuv = new Mat( (int)(size.height*1.5), size.width, CvType.CV_8UC1 );
			yuv.put( 0, 0, frameData );
			Imgproc.cvtColor( yuv, colFrameImg, Imgproc.COLOR_YUV2BGRA_NV21, 4);
			
			
			if(!flicker && flickerEnabled) {
				if(flickerRand.nextFloat() > 0.6) flicker = true;
			} else if(flicker) {
				flicker = false;
			}
			
			
			Mat grayFrameImg = new Mat();
			if(!flicker) 
				Imgproc.cvtColor(colFrameImg, grayFrameImg, Imgproc.COLOR_BGRA2GRAY);
			else {
				Mat white = Mat.ones(colFrameImg.size(), CvType.CV_8UC1);
				Core.multiply(white, new Scalar(255), grayFrameImg);
			}
			
			// Edit frame!
			if(torchMasks == null) {
				
				torchMasks = new ArrayList<Mat>();
				for (int i = 0; i < 15; i++) {
					torchMasks.add(Highgui.imread("/sdcard/masks/torchMask_"+i+".png",Highgui.CV_LOAD_IMAGE_GRAYSCALE));
				}
				
			}
			
			if(!flicker) {
				Mat noised = grayFrameImg.clone();
				Core.randn(noised,128,30);
				Core.addWeighted(grayFrameImg, 0.8, noised, 0.2, 0, grayFrameImg);
			}
		    
		    
		    Mat torchMask = new Mat();
		    
		    Mat tmp = new Mat(grayFrameImg.size(), 3);
		    Core.merge(Arrays.asList(grayFrameImg,grayFrameImg,grayFrameImg),tmp);
		    	
		    
		    if(!flicker && gm.getZombie().isInSight()) {
		    	
		    	
		    	
	//	    	imgProcView = new FastImageProcessingView(this.view.getContext());
	//	        pipeline = new FastImageProcessingPipeline();
	//	        imgProcView.setPipeline(pipeline);
	//	        view.setContentView(imgProcView);
	//	        imageIn = new ImageResourceInput(imgProcView, this, R.drawable.wakeboard);
	//	        generic = new GenericFilter();
	//	        screen = new ScreenEndpoint(pipeline);
	//	        imageIn.addTarget(generic);
	//	        generic.addTarget(screen);
	//	        pipeline.addRootRenderer(imageIn);
	//	        pipeline.startRendering();
		    	
		    	
	//		    Bitmap tmp = Bitmap.createBitmap(grayFrameImg.width(), grayFrameImg.height(), Bitmap.Config.ARGB_8888);
	//			Utils.matToBitmap(grayFrameImg, tmp);
	//			Bitmap overlay = BitmapFactory.decodeFile("/sdcard/zbg/zombie.png");
	//			Log.d(TAG, "overlay size: "+overlay.getWidth()+","+overlay.getHeight());
	//			Utils.bitmapToMat(overlay(tmp, overlay),grayFrameImg);
	//			Log.d(TAG, "overlay size: "+grayFrameImg.width()+","+grayFrameImg.height());
		    	
		    	
		    	Log.d(TAG, "TMP Sizes/Channels/Type: "+tmp.size()+"; "+tmp.channels()+"; "+tmp.type());
	//	    	Log.d(TAG, "OverlayAlpha: "+zombieFgAlpha);
		    	tmp = overlayImage(overlays.get(0).get(gm.getZombie().getZombieScale()), tmp, 1);
	//	    	zombieFgAlpha = zombieFgAlpha == 1.0f ? 1.0f : zombieFgAlpha+1.0f/5;
	//	    	if(zombieAlphaCounter < overlays.get(0).size()-1) {
	//	    		zombieAlphaCounter++;
	//	    	}
				
				
	//	    } else if(zombieFgAlpha > 0) {
	//    		tmp = overlayImage(overlays.get(0), tmp, zombieFgAlpha);
	//    		zombieFgAlpha = zombieFgAlpha == 0.0f ? 0.0f : zombieFgAlpha-1.0f/5;
		    }
		    
		    Log.d(TAG, "TMP Sizes/Channels/Type: "+tmp.size()+"; "+tmp.channels()+"; "+tmp.type());
	//	    tmp = overlayImage(overlays.get(0), tmp);
		    Log.d(TAG, "TMP Sizes/Channels/Type: "+tmp.size()+"; "+tmp.channels()+"; "+tmp.type());
		    
		    grayFrameImg = tmp;
		    
		    Mat white = Mat.ones(torchMasks.get(torchCounter).size(), torchMasks.get(torchCounter).type())
					.mul(Mat.ones(torchMasks.get(torchCounter).size(), torchMasks.get(torchCounter).type()), 255);
			Core.merge(Arrays.asList(torchMasks.get(torchCounter),torchMasks.get(torchCounter),torchMasks.get(torchCounter)),torchMask);
		    
			Log.d(TAG, "GRAYFRAME Sizes/Channels/Type: "+grayFrameImg.size()+"; "+grayFrameImg.channels()+"; "+grayFrameImg.type());
			Log.d(TAG, "TORCHMASKS Sizes/Channels/Type: "+torchMask.size()+"; "+torchMask.channels()+"; "+torchMask.type());
			
			if(torchMask.total() == 0)
				grayFrameImg = grayFrameImg.mul(torchMasks.get(torchCounter), 1.0/255);
			else
				grayFrameImg = grayFrameImg.mul(torchMask, 1.0/255);
			
			Imgproc.cvtColor(grayFrameImg, grayFrameImg, Imgproc.COLOR_BGR2GRAY);
			Log.d(TAG, "Channels: "+grayFrameImg.channels());
			Mat black = Mat.zeros(grayFrameImg.size(), grayFrameImg.type());
			Core.merge(Arrays.asList(black,grayFrameImg,black), result);
			Core.putText(result, "["+gm.getFlashLightPercentage()+"%]", new Point(AppConfig.PREVIEW_RESOLUTION[1]-100,40), 
					Core.FONT_HERSHEY_PLAIN, 3, new Scalar(255,255,255));
		    
		    torchCounter = (torchCounter + 1) % 15;
		}
		
		Paint p = new Paint();
        Canvas c = null;
        
		if(bmp==null){
			bmp = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
		}
		
		Utils.matToBitmap(result, bmp);
		
		c = frontCamSurface.lockCanvas(null);
		c.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()), new Rect(0, 0, c.getWidth(), c.getHeight()), p);
		
    	if (c != null) {
    		frontCamSurface.unlockCanvasAndPost(c); 
    		Log.d("MJPEG", "Canvas unlocked!");
    		view.requestRender();
    	}
    	
//    	if (stayCounter > 0) {
//    		stayCounter = (stayCounter+1) % 3;
//    	} else {
//    		counter = (counter + 3) % torchMasks.size();
//    		stayCounter++;
//    	}
		
		camera.addCallbackBuffer(frameData);
		return;
	}
	
	private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }
	
	private Mat overlayImage(Pair<Mat, Mat> overlayPair, Mat background, float alphaFg){
		Mat output = new Mat(background.size(), CvType.CV_8UC(3));
		Log.d(TAG, "BG Sizes/Channels/Type: "+background.size()+"; "+background.channels()+"; "+background.type());
		Log.d(TAG, "alphaInv Sizes/Channels/Type: "+overlayPair.second.size()+"; "+overlayPair.second.channels()+"; "+overlayPair.second.type());
		Mat fg = overlayPair.first.clone();
//		Core.multiply(fg, new Scalar(alphaFg,alphaFg,alphaFg), fg);
//		Mat alhpaInv = overlayPair.second.clone();
//		Core.multiply(alhpaInv, new Scalar(1-alphaFg,1-alphaFg,1-alphaFg), alhpaInv);
		Mat bg = background.mul(overlayPair.second, 1.0/255.0);
//		Core.multiply(bg, new Scalar(1.0f-alphaFg,1.0f-alphaFg,1.0f-alphaFg), bg);
		Core.add(fg,bg, output);
		
		
		return output;
	}
}
