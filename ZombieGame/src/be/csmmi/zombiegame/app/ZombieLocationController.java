package be.csmmi.zombiegame.app;

import java.util.Random;

import android.content.Context;
import android.util.Log;

public class ZombieLocationController implements LookatSensorListener {
	private static final String TAG = ZombieLocationController.class.getSimpleName();
	private static final int TIME_FOR_NEW_LOCATION = 20000;
	
	private boolean enabled = false;
	
	
	private int timeToWaitForNewZombie = TIME_FOR_NEW_LOCATION;
//	private boolean waitingForNextZombieLocation = false;
	private Thread waitForZombieLocThread;
	private Random zombieRand = new Random();
	private Object locationLock = new Object();
	private float randomAzimuth = (float) (zombieRand.nextFloat()*Math.PI);
	
	private boolean lookingAtZombie = false;
	private ZombieInSightListener inSightListener;
	private LookatSensor sensor;
	
	public ZombieLocationController(Context context, ZombieInSightListener inSightListener, LookatSensor sensor) {
	    this.inSightListener = inSightListener;
	    this.sensor = sensor;
	    sensor.addListener(this);
	}
	
	public void setEnabled(boolean enabled) {
		
		
		
		if(enabled && this.enabled != enabled) {
			if(lookingAtZombie == true) {
				inSightListener.onZombieInSight();
			} else {
				inSightListener.onZombieOutOfSight();
			}
		} else if(!enabled && this.enabled != enabled) {
			this.stopWaitForChangeZombieLoc();
		}
		
		this.enabled = enabled;
	}
	
	Runnable zombieLocationGenerator = new Runnable() {
		
		@Override
		public void run() {
			long started = System.nanoTime();
			try {
				
				Log.d(TAG, "Started sleeping ...");
				Thread.sleep(timeToWaitForNewZombie);
				Log.d(TAG, "sleeping done ...");
				timeToWaitForNewZombie = TIME_FOR_NEW_LOCATION;
				synchronized(locationLock) {
					randomAzimuth = (float) (zombieRand.nextFloat()*Math.PI);
					Log.d(TAG, "Zombie has new azimuth: "+randomAzimuth);
				}
				
				if(!lookingAtZombie()) {
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							startWaitForChangeZombieLoc();
						}
					}).start();
				}
			} catch (InterruptedException e) {
				timeToWaitForNewZombie = timeToWaitForNewZombie - (int)((System.nanoTime()-started)/1000000.0);
			}
		}
	};
	
	public boolean lookingAtZombie() {
		float[] orientation = sensor.getOrientation();
		float currentAzimuth = orientation[0];
		
		Log.d(TAG, "Orientation: ("+currentAzimuth+","+orientation[1]+","+orientation[2]+"); "+randomAzimuth);
		
		if(Math.abs(currentAzimuth-randomAzimuth) < (45/2.0f)*(Math.PI/180.0f) && Math.abs(-orientation[2]-Math.PI/2) < Math.PI/4) {
			Log.d(TAG, "ZOMBIE!");
			return true;
		}
		return false;
	}
	
	boolean waiting = false;
	Object waitingLock = new Object();
	public void startWaitForChangeZombieLoc() {
		if(!enabled) return;
		synchronized (waitingLock) {
			if(waiting) return;
		}
		while(waitForZombieLocThread != null && waitForZombieLocThread.isAlive()) {
			synchronized (waitingLock) {
				waiting = true;
			}
			Log.d(TAG, "Zombie waits for starting location change!");
		}
		if(waitForZombieLocThread == null || !waitForZombieLocThread.isAlive()) {
    		waitForZombieLocThread = new Thread(zombieLocationGenerator);
    		waitForZombieLocThread.start();
    	}
		synchronized (waitingLock) {
			waiting = false;
		}
	}
	
	public void stopWaitForChangeZombieLoc() {
		if(waitForZombieLocThread != null && waitForZombieLocThread.isAlive()) {
    		waitForZombieLocThread.interrupt();
    	}
	}

	@Override
	public void onLookatSensorChanged(float[] newOrientation) {
		if(this.lookingAtZombie() != lookingAtZombie) {
			if(lookingAtZombie == false) {
				inSightListener.onZombieInSight();
				lookingAtZombie = true;
			} else {
				inSightListener.onZombieOutOfSight();
				lookingAtZombie = false;
			}
		}
	}
}
