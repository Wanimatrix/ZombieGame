package be.csmmi.zombiegame.app;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class LookatSensor implements SensorEventListener{
	
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private float[] mGravity;
	private float[] mGeomagnetic;
	
	private float[] orientation = new float[3];
	
	private List<LookatSensorListener> listeners;
	
	public LookatSensor(Context context) {
		listeners = new ArrayList<LookatSensorListener>();
		
		mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	    accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
		
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
	    mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
	}
	
	public void addListener(LookatSensorListener listener) {
		this.listeners.add(listener);
	}
	
	public float[] getOrientation() {
		return orientation;
	}
	
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR)
			mGeomagnetic = event.values;
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				SensorManager.getOrientation(R, orientation);
				
				if(orientation[0] < 0)
					orientation[0] += Math.PI;
				orientation[0] *= 4.0f;
				orientation[0] %= Math.PI*2;
				
				for(LookatSensorListener listener : listeners) listener.onLookatSensorChanged(orientation);
//				azimut = orientation[0]; // orientation contains: azimut, pitch and roll
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
