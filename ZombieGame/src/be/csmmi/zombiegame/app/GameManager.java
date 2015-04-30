package be.csmmi.zombiegame.app;

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
	
	private long flashLightTimer = 0;
	
	private Runnable flashLightTimerRunnable = new Runnable() {
		
		@Override
		public void run() {
			try {
				flashLightTimer = 0;
				while(flashLightTimer < AppConfig.MAX_FLASHLIGHT_TIME) {
					Thread.sleep(1000);
					flashLightTimer++;
				}
				
				lost = true;
			} catch (InterruptedException e) {
			}
		}
	};
	private Thread flashLightTimerThread;
	
	public GameManager(Context context, int zombieScales) {
		ServerCommunication.init(context);
		sensor = new LookatSensor(context);
		sensor.addListener(this);
		zombie = new Zombie(context, zombieScales, this, sensor);
	}
	
	public Zombie getZombie() {
		return zombie;
	}
	
	public long getFlashLightTimer() {
		return flashLightTimer;
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
				if(inControlRoom) {
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
					if(response.get("data") == "false") {
						lost = true;
					}
						
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		
		ServerCommunication.sendObjectMessage("endgamestarted", new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					if(response.get("data") == "true") {
						endgame = true;
					} else {
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
	
	private int gameOver(Mat status, boolean sendToServer) {
		Highgui.imread("/sdcard/zbg/lost.png").copyTo(status);
		Imgproc.cvtColor(status, status, Imgproc.COLOR_RGBA2BGR);
		if(sendToServer) {
			ServerCommunication.sendObjectMessage("resetgame", new Response.Listener<JSONObject>() {
	
				@Override
				public void onResponse(JSONObject response) {
					
				}
				
			});
		}
		return 1;
	}
	
}
