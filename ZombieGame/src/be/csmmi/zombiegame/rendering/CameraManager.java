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
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Surface;
import be.csmmi.zombiegame.app.AppConfig;

import com.android.volley.RequestQueue;
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
	private RequestQueue queue;
	
	public CameraManager(Context context, GLSurfaceView view) {
		this.view = view;
		
		// Instantiate the RequestQueue.
		queue = Volley.newRequestQueue(view.getContext());
	}
	
	public void setSurface(Surface surface) {
		this.surface = surface;
	}
	
	public void initRoomStatus() {
		final JsonArrayRequest req = new JsonArrayRequest(AppConfig.SERVER_ADDRESS+"/roomstatus",
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
	            }, new com.android.volley.Response.ErrorListener() {
	                @Override
	                public void onErrorResponse(VolleyError error) {
	                	
	                }
	            });
		
		roomStatusThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(3000);
						Log.d("LOCKER", "Added request");
						queue.add(req);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		roomStatusThread.start();
	}
	
	public void initCameras(final OnCamerasReadyListener listener) {
		
		JsonArrayRequest req = new JsonArrayRequest(AppConfig.SERVER_ADDRESS+"/getcams",
	            new com.android.volley.Response.Listener<JSONArray>() {
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
	            }, new com.android.volley.Response.ErrorListener() {
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
	            });
		
		// Add the request to the RequestQueue.
		queue.add(req);
		
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
            	thread = new MjpegViewThread(surface, view, inStream, CameraManager.this);
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
	
	@Override
	public void onPreviewFrame(byte[] frameData, Camera camera) {
		Log.d(TAG, "Request render!");
		
		Size size = camera.getParameters().getPreviewSize();
		Mat colFrameImg = new Mat();
		Mat yuv = new Mat( (int)(size.height*1.5), size.width, CvType.CV_8UC1 );
		yuv.put( 0, 0, frameData );
		Imgproc.cvtColor( yuv, colFrameImg, Imgproc.COLOR_YUV2BGRA_NV21, 4);
//		Mat zombie = Highgui.imread("/sdcard/zbg/zombie.png",-1);
//		Mat zombieGray = new Mat();
//		Imgproc.cvtColor(zombie, zombieGray, Imgproc.COLOR_BGR2GRAY);
		
		
//		overlayImage(colFrameImg, zombie, colFrameImg);
		Mat grayFrameImg = new Mat();
		Imgproc.cvtColor(colFrameImg, grayFrameImg, Imgproc.COLOR_BGRA2GRAY);
		
		// Edit frame!
		if(torchMasks == null) {
//			torchMask = new Mat();
//			Mat mask;
			
			torchMasks = new ArrayList<Mat>();
			for (int i = 0; i < 15; i++) {
//				Mat tmp = ;
				torchMasks.add(Highgui.imread("/sdcard/masks/torchMask_"+i+".png",Highgui.CV_LOAD_IMAGE_GRAYSCALE));
			}
			
//			torchMask = Mat.zeros(colFrameImg.size(), CvType.CV_8UC1);
//			update_pixels(torchMask, 100, 300);
			
			
//			List<Mat> marr = Arrays.asList(new Mat[]{mask, mask, mask});
//			Core.merge(marr, torchMask);
		}
		
		
//		Mat saltpepper_noise = Mat.zeros(colFrameImg.rows(), colFrameImg.cols(), CvType.CV_8U);
////		(int) (rand.nextFloat()*255)
//		Core.randu(saltpepper_noise, 0, 255);
//
//		Mat black = new Mat();
//		Mat white = new Mat();
//		Core.inRange(saltpepper_noise, new Scalar(0), new Scalar(30), black);
//		Core.inRange(saltpepper_noise, new Scalar(225), new Scalar(255), black);
//
//		Mat saltpepper_img = colFrameImg.clone();
//		saltpepper_img.setTo(new Scalar(255),white);
//		saltpepper_img.setTo(new Scalar(0),black);
//		
//		colFrameImg = colFrameImg.mul(saltpepper_img, 1.0/255);
		
		Mat noised = grayFrameImg.clone();
		Core.randn(noised,128,30);
		
//		int amount1= (int) (colFrameImg.rows()*colFrameImg.cols()*0.05);
//	    int amount2= (int) (colFrameImg.rows()*colFrameImg.cols()*0.02);
//	    for(int counter = 0; counter < amount1; ++counter) {
//	    	noised.put(rand.nextInt(colFrameImg.rows()), rand.nextInt(colFrameImg.cols()), 0);
////	     srcArr.at<uchar>(rng.uniform( 0,srcArr.rows), rng.uniform(0, srcArr.cols)) =0;
//
//	    }
//	    
//	    for (int counter=0; counter<amount2; ++counter) {
//	    	noised.put(rand.nextInt(colFrameImg.rows()), rand.nextInt(colFrameImg.cols()), 255);
////	     srcArr.at<uchar>(rng.uniform(0,srcArr.rows), rng.uniform(0,srcArr.cols)) = 255;
//	    }
	    
	    Core.addWeighted(grayFrameImg, 0.8, noised, 0.2, 0, grayFrameImg);
	    
//	    if(beat) {
////	    	Mat cropped = colFrameImg.submat(new org.opencv.core.Rect(50, 50, 640-100, 480-100)).clone();
////	    	Imgproc.resize(cropped, colFrameImg, colFrameImg.size());
//	    	grayFrameImg = grayFrameImg.mul(torchMasks.get(9), 1.0/255);
//	    } else {
//	    	grayFrameImg = grayFrameImg.mul(torchMasks.get(0), 1.0/255);
//	    }
	    
	    
	    Bitmap tmp = Bitmap.createBitmap(grayFrameImg.width(), grayFrameImg.height(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(grayFrameImg, tmp);
		Bitmap overlay = BitmapFactory.decodeFile("/sdcard/zbg/zombie.png");
		Log.d(TAG, "overlay size: "+overlay.getWidth()+","+overlay.getHeight());
		Utils.bitmapToMat(overlay(tmp, overlay),grayFrameImg);
		Log.d(TAG, "overlay size: "+grayFrameImg.width()+","+grayFrameImg.height());
		
		
		Mat torchMask = new Mat(grayFrameImg.size(),grayFrameImg.type());
		Core.merge(Arrays.asList(torchMasks.get(torchCounter),torchMasks.get(torchCounter),torchMasks.get(torchCounter),new Mat()),torchMask);
	    
		Log.d(TAG, "GRAYFRAME Sizes/Channels: "+grayFrameImg.size()+"; "+grayFrameImg.channels());
		Log.d(TAG, "TORCHMASKS Sizes/Channels: "+torchMask.size()+"; "+torchMask.channels());
		
	    grayFrameImg = grayFrameImg.mul(torchMasks.get(torchCounter), 1.0/255);
	    
	    torchCounter = (torchCounter + 1) % 15;
		
		Paint p = new Paint();
        Canvas c = null;
        
		if(bmp==null){
			bmp = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
		}
		
		Utils.matToBitmap(grayFrameImg, bmp);
		
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
}
