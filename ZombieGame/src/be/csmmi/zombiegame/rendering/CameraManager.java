package be.csmmi.zombiegame.rendering;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
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
	private Surface surface;
	private RequestQueue queue;
	
	public CameraManager(Context context, GLSurfaceView view) {
		this.view = view;
		
		rooms.add(new Room("Room0"));
		
		// Instantiate the RequestQueue.
		queue = Volley.newRequestQueue(view.getContext());
	}
	
	public void setSurface(Surface surface) {
		this.surface = surface;
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
	                        if(rooms.get( currentRoomIdx).getCameras().size() == 0) {
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
	}
	
	private boolean stopCamSwitcher = false;
	
	public void switchRoom() {
		if(rooms.get( (currentRoomIdx + 1) % rooms.size()).getCameras().size() == 0) {
			Log.d(TAG, "Front view camera...");
			setupCamera();
			currentRoomIdx = (currentRoomIdx + 1) % rooms.size();
			return;
		} else if(rooms.get( currentRoomIdx).getCameras().size() == 0) {
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
		try {
			camera.setPreviewTexture(st);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	@Override
	public void onPreviewFrame(byte[] frameData, Camera camera) {
		Log.d(TAG, "Request render!");
		
		view.requestRender();
		camera.addCallbackBuffer(frameData);
		return;
	}
}
