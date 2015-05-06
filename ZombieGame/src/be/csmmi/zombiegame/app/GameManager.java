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

import android.content.Context;
import android.util.Log;

public class GameManager implements ZombieScaleChangeListener, LookatSensorListener{
	private static final String TAG = GameManager.class.getSimpleName();
	
	private Zombie zombie;
	private boolean lost = false;
	private LookatSensor sensor;
	private float originalLocation = -1;
	private boolean inControlRoom = true;
	private boolean endgame = false;
	
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
				lost = true;
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
	
	public GameManager(Context context, int zombieScales) {
		sensor = new LookatSensor(context);
		sensor.addListener(this);
		zombie = new Zombie(context, zombieScales, this, sensor);
	}
	
	public Zombie getZombie() {
		return zombie;
	}
	
	public long getFlashLightPercentage() {
		double timePassed = (AppConfig.MAX_FLASHLIGHT_TIME-flashLightTimerCount)/(double)AppConfig.MAX_FLASHLIGHT_TIME;
		long percentage = (long)(timePassed*100);
//		Log.d(TAG, "FLASH: "+timePassed+", "+flashLightTimer);
		return percentage;
	}
	
	public boolean isGameOver() {
		return lost;
	}
	
	public boolean endGameStarted() {
		return endgame;
	}
	
	public boolean isInControlRoom() {
		return inControlRoom;
	}

	@Override
	public void onZombieScaleChange() {
		if(zombie.hasKilledPlayer())
			lost = true;
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
					flashLightTimerThread.interrupt();
				}
				inControlRoom = true;
				zombie.disable();
			} else {
				if(inControlRoom && !flashLightTimerThread.isAlive()) {
					Log.d(TAG, "Going out of controlroom");
					flashLightTimerThread = new Thread(flashLightTimerRunnable);
				}
				inControlRoom = false;
				zombie.enable();
			}
		}
	}
	
	public int getGameStatus(final Mat status) {
		
		ServerCommunication.sendObjectMessage("inprogress", new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					Log.d(TAG, "INPROGRESS RESPONSE:"+response.get("data"));
					if(response.get("data").equals("false")) {
						Log.d(TAG, "INPROGRESS == FALSE");
						lost = true;
						if(sendResetGame) sendResetGame = false; // The server said we are lost, so do not send reset to the server
					} else {
						if(!sendResetGame) sendResetGame = true; // We are in the game if we lose the game we need to send a reset
						lost = false;
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
		
		if(isInControlRoom() && !isGameOver()) { // IN CONTROL ROOM
			Highgui.imread("/sdcard/zbg/ctrlRoom.png").copyTo(status);;
			return 1;
		}
		else if(isGameOver()) { // GAME OVER
			return gameOver(status, true);
		}
		return 0;
	}
	
	boolean sendResetGame = true;
	
	private int gameOver(Mat status, boolean sendToServer) {
		Highgui.imread("/sdcard/zbg/lost.png").copyTo(status);
		Imgproc.cvtColor(status, status, Imgproc.COLOR_RGBA2BGR);
		if(sendResetGame && sendToServer) {
			ServerCommunication.sendObjectMessage("resetgame", new Response.Listener<JSONObject>() {
	
				@Override
				public void onResponse(JSONObject response) {
					sendResetGame = false; // Reset is sent and arrived, so do not send it again
				}
				
			});
		}
		return 1;
	}
	
}
