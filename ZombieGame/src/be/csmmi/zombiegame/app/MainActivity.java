package be.csmmi.zombiegame.app;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import be.csmmi.zombiegame.R;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;

public class MainActivity extends CardboardActivity implements OnSharedPreferenceChangeListener {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	private Vibrator v;
	/**
	 * Setup app:
	 * 	- Set defined layout in content view;
	 * 	- Get the Surface holder from the Surface View;
	 *  - Add the Surface Callback to the Surface Holder;
	 *  - Register sensorManager
	 *  - Get default Accelerometer and Magnetometer
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//		if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
//	        return;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
	}
	
	/**
	 * On resume:
	 * 	- Get the camera;
	 * 	- Register event listeners on the sensorManager for 
	 * 		the Accelerometer and Magnetometer.
	 */
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	/**
	 * On pause:
	 * 	- Stop the camera when in preview and release the camera,
	 * 		so it can be used by other apps;
	 * 	- Unregister listener on the sensorManager.
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
	
	@Override
	public void onCardboardTrigger() {
		Log.d(TAG, "Switching rooms...");
		
		CameraView v = (CameraView) this.findViewById(R.id.arView);
		v.getRenderer().getArRenderer().getCameraManager().switchRoom();
		
		this.v.vibrate(50);
		
		super.onCardboardTrigger();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {	
	}
}
