package be.csmmi.zombiegame.app;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

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
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
	
	private static final String TAG = MainActivity.class.getSimpleName();
	private Vibrator v;
	/**
	 * Setup app
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ServerCommunication.init(MainActivity.this);
		setContentView(R.layout.activity_main);
		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
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
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
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
	
	long cardboardTriggerTimestamp = 0;
	
	@Override
	public void onCardboardTrigger() {
		Log.d(TAG, "Switching rooms...");
		
		long newTimestamp = System.nanoTime();
		if(cardboardTriggerTimestamp == 0 || (newTimestamp-cardboardTriggerTimestamp)/1000000.0f > 1000) {
			cardboardTriggerTimestamp = newTimestamp;
			CameraView v = (CameraView) this.findViewById(R.id.arView);
			v.getRenderer().getArRenderer().getCameraManager().switchRoom();
			this.v.vibrate(50);
		}
		
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
