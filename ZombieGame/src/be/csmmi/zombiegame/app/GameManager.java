package be.csmmi.zombiegame.app;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.lang.model.SourceVersion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import be.csmmi.zombiegame.R;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;

public class GameManager implements ZombieScaleChangeListener, LookatSensorListener{
	private static final String TAG = GameManager.class.getSimpleName();
	
	private Zombie zombie;
	private boolean lost = false;
	private boolean lostFromServer = false;
	private boolean lostFromGame = false;
	private Object lostLock = new Object();
	private LookatSensor sensor;
	private float originalLocation = -1;
	private boolean inControlRoom = true;
	private boolean endgame = false;
	private boolean inLockedRoom = false;
	private long lostTimeStamp = 0;
	private boolean outroStarted = false;
	
	private Mat status = new Mat();
	private int statusValue;
	private SoundManager sndMan;
	
	private long flashLightTimerCount = AppConfig.MAX_FLASHLIGHT_TIME;
	private long flashTimerStarted;
	private double flashTimerPrevious = 0;
	
	private Runnable flashLightTimerRunnable = new Runnable() {
		
		@Override
		public void run() {
			flashTimerStarted = System.nanoTime();
			try {
				while(flashLightTimerCount > 0) {
					Thread.sleep(200);
					if((System.nanoTime()-flashTimerStarted)/1000000.0+flashTimerPrevious >= 1000) {
						flashLightTimerCount--;
						flashTimerPrevious = (System.nanoTime()-flashTimerStarted)/1000000.0+flashTimerPrevious-1000;
						flashTimerStarted = System.nanoTime();
					}
				}
				Log.d(TAG, "Lost == TRUE");
				synchronized (lostLock) {
					lostFromGame = true;
				}
				Log.d(TAG, "LOST BY FLASHLIGHT");
				Log.d(TAG, "LOST: "+lost);
			} catch (InterruptedException e) {
				flashTimerPrevious = (System.nanoTime()-flashTimerStarted)/1000000.0;
			}
			
		}
	};
	private Thread flashLightTimerThread;
	private boolean flashLightTimerRunning = false;
	
	private Thread pollingThread = new Thread(new Runnable() {
		
		private boolean previousInProgressDone = true;
		private boolean previousOutroStartedDone = true;
		private boolean previousEndGameStartedDone = true;
		
		@Override
		public void run() {
			while(true) {
				try {
					Thread.sleep(500);
					
					if(previousInProgressDone) {
						previousInProgressDone = false;
						ServerCommunication.sendObjectMessage("inprogress", new Response.Listener<JSONObject>() {
							@Override
							public void onResponse(JSONObject response) {
								try {
									synchronized (lostLock) {
										Log.d(TAG, "LOST: "+lost+"; Lost from server: "+lostFromServer+"; Lost from game: "+lostFromGame);
				//						Log.d(TAG, "INPROGRESS RESPONSE:"+response.get("data"));
										if(response.get("data").equals("false") && inprogress) {
				//							Log.d(TAG, "INPROGRESS == FALSE");
											Log.d(TAG, "INPROGRESS == FALSE");
											lostFromServer = true;
//											inprogress = false;
											Log.d(TAG, "LOST BY SERVER");
											if(sendResetGame) sendResetGame = false; // The server said we are lost, so do not send reset to the server
//										} else if(response.get("data").equals("true") && !inprogress) {
//											Log.d(TAG, "INPROGRESS == TRUE");
//											
//											
										} else if(response.get("data").equals("true") && (lost && !lostFromServer && !lostFromGame)) {
											Log.d(TAG, "INPROGRESS == TRUE2");
											lost = false;
											if(!sendResetGame) sendResetGame = true; // We are in the game if we lose the game we need to send a reset
											lostTimeStamp = 0;
											zombie.reset();
											outroDone = false;
											ServerCommunication.sendObjectMessage("outroended", new Response.Listener<JSONObject>() {
												@Override
												public void onResponse(JSONObject response) {
													outroStarted = false;
												}
												
											});
											turnOnSounds();
											outroFrameCounter = 0;
											resetFlashLightTimer();
											inprogress = true;
										}
										
										if(isInControlRoom() && !isGameOver()) { // IN CONTROL ROOM
											synchronized (status) {
												status = Highgui.imread("/sdcard/zbg/ctrlRoom.png");
												statusValue = 1;
											}
											return;
										}
										else if(isGameOver()) { // GAME OVER
											if(lostFromServer || lostFromGame) {
												sndMan.playSoundFx("dead", 1, false);
												lostFromServer = false;
												lostFromGame = false;
												lost = true;
												inprogress = false;
											}
											
											synchronized (status) {
												statusValue = gameOver(true);
											}
											return;
										}
										synchronized (status) {
											statusValue =  0;
										}
									}
								} catch (JSONException e) {
									e.printStackTrace();
								} finally {
									previousInProgressDone = true;
								}
							}
						});
					}
					
					if(previousOutroStartedDone) {
						previousOutroStartedDone = false;
						ServerCommunication.sendObjectMessage("outrostarted", new Response.Listener<JSONObject>() {
							public void onResponse(JSONObject response) {
								try {
									if(response.get("data").equals("true")) {
										stopFlashLightTimer();
										zombie.disable();
										outroStarted = true;
									} else {
										outroStarted = false;
									}
								} catch (JSONException e) {
									e.printStackTrace();
								} finally {
									previousOutroStartedDone = true;
								}
							};
						});
					}
					
					if(previousEndGameStartedDone) {
						previousEndGameStartedDone = false;
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
								} finally {
									previousEndGameStartedDone = true;
								}
							}
						});
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	});
	
	public GameManager(Context context, int zombieScales) {
		sensor = new LookatSensor(context);
		sensor.addListener(this);
		sndMan = new SoundManager(context);
		zombie = new Zombie(context, zombieScales, this, sensor, sndMan);
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
		synchronized (lostLock) {
			return lost || lostFromGame || lostFromServer;
		}
	}
	
	private boolean outroDone = false;
	
	public void outroDone(){
		outroDone = true;
	}
	
	public boolean isAfterOutro() {
		return outroDone;
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

	public boolean scaleChange = false;
	
	@Override
	public void onZombieScaleChange() {
		if(zombie.hasKilledPlayer()) {
			Log.d(TAG, "LOST BY ZOMBIE");
			synchronized (lostLock) {
				lostFromGame = true;
			}
		} else {
			scaleChange = true; 
		}
	}

	private Thread zombieEnableThread = null;
	private Runnable zombieEnableRunnable = new Runnable() {
		
		@Override
		public void run() {
			try {
				Thread.sleep(2000);
				zombie.enable();
			} catch (InterruptedException e) {
			}
		}
	};
	
	@Override
	public void onLookatSensorChanged(float[] newOrientation) {
//		Log.d(TAG, "Orientation: "+newOrientation[2]);
		if(originalLocation == -1) {
			Log.d(TAG, "ORIGINAL: "+originalLocation);
			originalLocation = newOrientation[2];
		} else { 
			Log.d(TAG, "Diff: "+Math.abs(originalLocation-newOrientation[2])+" < "+((5/2.0f)*(Math.PI/180.0f)));
			Log.d(TAG, "CONTROLROOM: "+inControlRoom);
			
			int hysteresisBuffer = 0;
			if(inControlRoom) {
				hysteresisBuffer = 5;
			}
			
			if(Math.abs(originalLocation-newOrientation[2]) < (10/2.0f+hysteresisBuffer)*(Math.PI/180.0f)) {
				if(!inControlRoom) {
					Log.d("SENSORCHANGECHECK", "INTO CTRLROOM");
					Log.d("SENSORCHANGECHECK", "Diff: "+Math.abs(originalLocation-newOrientation[2])+" < "+((10/2.0f)*(Math.PI/180.0f)));
					stopFlashLightTimer();
					zombie.disable();
				}
				inControlRoom = true;
			} else  {
				if(inControlRoom && inLockedRoom) {
					Log.d("SENSORCHANGECHECK", "OUT OF CTRLROOM");
					Log.d("SENSORCHANGECHECK", "Diff: "+Math.abs(originalLocation-newOrientation[2])+" < "+((10/2.0f+4)*(Math.PI/180.0f)));
					startFlashLightTimer();
					zombie.enable();
				}
				inControlRoom = false;
			}
		}
	}
	
	private boolean inprogress = true;
	private int outroFrameCounter = 0;
	
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
	
	public void onZombieEntryScreen() {
		sndMan.playRandomSoundFx(SoundManager.RANDOM_SFX.SCARES, 1);
		toWait = -1;
	}
	
	public void onZombieOnScreen() {
		if(scaleChange) {
			sndMan.playRandomSoundFx(SoundManager.RANDOM_SFX.SCARES, 1);
			scaleChange = false;
		}
	}
	
	public boolean isOutroStarted() {
		return outroStarted;
	}
	
	public void turnOffSounds() {
		sndMan.turnOffSound();
	}
	
	public void turnOnSounds() {
		sndMan.turnOnSound();
	}
	
	private Random randomWait = new Random();
	private int toWait = -1;
	private long startWait;
	private String currentSound = null;
	
	public void onNoZombieOnScreen() {
		Log.d(TAG, "NO ZOMBIE ON SCREEN!");
		if(toWait == -1) {
			toWait = (int) (randomWait.nextInt(3))*1000;
			startWait = System.nanoTime();
		} else if((System.nanoTime()-startWait)/1000000.0 >= toWait) {
			Log.d(TAG, "ATMOSPHERE SOUND!");
			sndMan.playRandomSoundFx(SoundManager.RANDOM_SFX.ATMOSPHERE, 1);
			toWait = (int) (randomWait.nextInt(3)+15)*1000;
			startWait = System.nanoTime();
		} else {
			Log.d(TAG, "ATMOSPHERE SOUND STILL WAITING!");
		}
	}
	
	private void resetFlashLightTimer() {
		if(flashLightTimerThread!= null && flashLightTimerThread.isAlive()) {
			flashLightTimerThread.interrupt();
		}
		flashLightTimerThread = null;
		flashLightTimerCount = AppConfig.MAX_FLASHLIGHT_TIME;
		flashTimerPrevious = 0;
	}
	
}
