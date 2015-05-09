package be.csmmi.zombiegame.app;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import android.content.Context;
import android.util.Log;

public class GameManager implements ZombieScaleChangeListener, LookatSensorListener{
	private static final String TAG = GameManager.class.getSimpleName();
	
	private Zombie zombie;
	private boolean lost = false;
	private boolean lostFromServer = false;
	private boolean lostFromGame = false;
	private LookatSensor sensor;
	private float originalLocation = -1;
	private boolean inControlRoom = true;
	private boolean endgame = false;
	private boolean inLockedRoom = false;
	private boolean showOutro = false;
	private long lostTimeStamp = 0;;
	
	private Mat status = new Mat();
	private int statusValue;
	
	private long flashLightTimerCount = 0;
	
	private Runnable flashLightTimerRunnable = new Runnable() {
		
		@Override
		public void run() {
			flashLightTimerCount = AppConfig.MAX_FLASHLIGHT_TIME;
			try {
				while(flashLightTimerCount > 0) {
					Thread.sleep(1000);
					flashLightTimerCount--;
				}
				Log.d(TAG, "Lost == TRUE");
				lostFromGame = true;
				Log.d(TAG, "LOST BY FLASHLIGHT");
				Log.d(TAG, "LOST: "+lost);
			} catch (InterruptedException e) {
			}
			
		}

//		@Override
//		public void run() {
//			if(flashLightTimerCount++ >= AppConfig.MAX_FLASHLIGHT_TIME) {
//				flashLightTimer.cancel();
//				lost = true;
//			}
//		}
	};
	private Thread flashLightTimerThread;
	private boolean flashLightTimerRunning = false;
	
	private Thread pollingThread = new Thread(new Runnable() {
		
		@Override
		public void run() {
			while(true) {
				try {
					Thread.sleep(100);
				
					ServerCommunication.sendObjectMessage("inprogress", new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							try {
								Log.d(TAG, "LOST: "+lost+"; Lost from server: "+lostFromServer+"; Lost from game: "+lostFromGame);
		//						Log.d(TAG, "INPROGRESS RESPONSE:"+response.get("data"));
								if(response.get("data").equals("false") && inprogress) {
		//							Log.d(TAG, "INPROGRESS == FALSE");
									Log.d(TAG, "INPROGRESS == FALSE");
									lostFromServer = true;
									inprogress = false;
									Log.d(TAG, "LOST BY SERVER");
									if(sendResetGame) sendResetGame = false; // The server said we are lost, so do not send reset to the server
								} else if(!inprogress && response.get("data").equals("true")) {
									Log.d(TAG, "INPROGRESS == TRUE");
									inprogress = true;
									lostFromServer = false;
								} else if(response.get("data").equals("true") && (lost && !lostFromServer && !lostFromGame)) {
									Log.d(TAG, "INPROGRESS == TRUE2");
									lost = false;
									if(!sendResetGame) sendResetGame = true; // We are in the game if we lose the game we need to send a reset
									lostTimeStamp = 0;
									zombie.reset();
									flashLightTimerThread = null;
									flashLightTimerCount = AppConfig.MAX_FLASHLIGHT_TIME;
								}
								
								if(isInControlRoom() && !isGameOver()) { // IN CONTROL ROOM
									synchronized (status) {
										status = Highgui.imread("/sdcard/zbg/ctrlRoom.png");
										statusValue = 1;
									}
									return;
								}
								else if(isGameOver()) { // GAME OVER
									lostFromServer = false;
									lostFromGame = false;
									lost = true;
									synchronized (status) {
										statusValue = gameOver(true);
									}
									return;
								}
								synchronized (status) {
									statusValue =  0;
								}
									
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
					
					
					ServerCommunication.sendObjectMessage("endgamestarted", new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							Log.d(TAG, "ENDGAME RESPONSE:");
							try {
								Log.d(TAG, "ENDGAME RESPONSE:"+response.get("data"));
								if(response.get("data").equals("true")) {
									Log.d(TAG, "ENDGAME STARTED");
									endgame = true;
								} else {
									Log.d(TAG, "ENDGAME STOPPED");
									endgame = false;
								}
									
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	});
	
	public GameManager(Context context, int zombieScales) {
		sensor = new LookatSensor(context);
		sensor.addListener(this);
		zombie = new Zombie(context, zombieScales, this, sensor);
		pollingThread.start();
		
	}
	
	public Zombie getZombie() {
		return zombie;
	}
	
	public long getFlashLightPercentage() {
		double timePassed = (flashLightTimerCount)/(double)AppConfig.MAX_FLASHLIGHT_TIME;
		long percentage = (long)(timePassed*100);
		Log.d(TAG, "FLASH: "+timePassed);
		return percentage;
	}
	
	public void gotoLockedRoom() {
		this.inLockedRoom = true;
		startFlashLightTimer();
		zombie.enable();
	}
	
	public void gotoUnlockedRoom() {
		this.inLockedRoom = false;
		stopFlashLightTimer();
		zombie.disable();
	}
	
	public boolean isGameOver() {
		Log.d(TAG, "LOST: "+lost);
		return lost || lostFromGame || lostFromServer;
	}
	
	public boolean endGameStarted() {
		return endgame;
	}
	
	public boolean isInControlRoom() {
		return inControlRoom;
	}
	
	public void stopFlashLightTimer() {
		if(flashLightTimerThread != null && flashLightTimerThread.isAlive())
			flashLightTimerThread.interrupt();
	}
	
	public void startFlashLightTimer() {
		if(flashLightTimerThread == null || !flashLightTimerThread.isAlive()) {
			flashLightTimerThread = new Thread(flashLightTimerRunnable);
			flashLightTimerThread.start();
		}
	}

	@Override
	public void onZombieScaleChange() {
		if(zombie.hasKilledPlayer()) {
			Log.d(TAG, "LOST BY ZOMBIE");
			lostFromGame = true;
		}
	}

	@Override
	public void onLookatSensorChanged(float[] newOrientation) {
		if(originalLocation == -1) {
			Log.d(TAG, "ORIGINAL: "+originalLocation);
			originalLocation = newOrientation[2];
		} else { 
			Log.d(TAG, "Diff: "+Math.abs(originalLocation-newOrientation[2])+" < "+((5/2.0f)*(Math.PI/180.0f)));
			Log.d(TAG, "CONTROLROOM: "+inControlRoom);
			if(Math.abs(originalLocation-newOrientation[2]) < (10/2.0f)*(Math.PI/180.0f)) {
				if(!inControlRoom) {
					stopFlashLightTimer();
					zombie.disable();
				}
				inControlRoom = true;
			} else {
				if(inControlRoom && inLockedRoom) {
					Log.d(TAG, "Going out of controlroom");
					startFlashLightTimer();
					zombie.enable();
				}
				inControlRoom = false;
			}
		}
	}
	
	private boolean inprogress = true;
	
	public int getGameStatus(final Mat status) {
		synchronized (this.status) {
			this.status.copyTo(status);
			return statusValue;
		}
	}
	
	boolean sendResetGame = true;
	
	private int gameOver(boolean sendToServer) {
		if(lostTimeStamp == 0) lostTimeStamp = System.nanoTime();
		if((System.nanoTime()-lostTimeStamp)/1000000.0f < 2000) status = Highgui.imread("/sdcard/zbg/zombieLost.png");
		else status = Highgui.imread("/sdcard/zbg/lost.png");
		Imgproc.cvtColor(status, status, Imgproc.COLOR_RGBA2BGR);
		if(sendResetGame && sendToServer) {
			sendResetGame = false; // Reset is sent, so do not send it again
			Log.d(TAG, "SEND RESET!");
			ServerCommunication.sendObjectMessage("resetgame", new Response.Listener<JSONObject>() {
	
				@Override
				public void onResponse(JSONObject response) {
					
				}
				
			}, new Response.ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					sendResetGame = true; // Sending reset has failed, so send it again!
				}
				
			});
		}
		return 1;
	}
	
}
