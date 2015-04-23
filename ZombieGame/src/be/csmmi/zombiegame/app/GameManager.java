package be.csmmi.zombiegame.app;

import org.json.JSONArray;
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
	
	public GameManager(Context context, int zombieScales) {
		ServerCommunication.init(context);
		sensor = new LookatSensor(context);
		sensor.addListener(this);
		zombie = new Zombie(context, zombieScales, this, sensor);
	}
	
	public Zombie getZombie() {
		return zombie;
	}
	
	public boolean isGameOver() {
		return lost;
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
				inControlRoom = true;
				zombie.disable();
			} else {
				inControlRoom = false;
				zombie.enable();
			}
		}
	}
	
	public int getGameStatus(Mat status) {
		if(isInControlRoom() && !isGameOver()) {
			Highgui.imread("/sdcard/zbg/ctrlRoom.png").copyTo(status);;
			return 1;
		}
		else if(isGameOver()) {
			Highgui.imread("/sdcard/zbg/lost.png").copyTo(status);
			Imgproc.cvtColor(status, status, Imgproc.COLOR_RGBA2BGR);
			ServerCommunication.sendMessage("resetGame", new Response.Listener<JSONArray>() {

				@Override
				public void onResponse(JSONArray response) {
					
				}
				
			});
			return 1;
		}
		return 0;
	}
	
}
