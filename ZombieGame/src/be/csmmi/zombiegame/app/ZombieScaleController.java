package be.csmmi.zombiegame.app;

import android.util.Log;

public class ZombieScaleController {
	private final String TAG = ZombieScaleController.class.getSimpleName();
	
	private int zombieScale = 0;
	private Object scaleLock = new Object();
	private int amountOfScales = 0;
	private Thread waitForNextScale = null;
	private int timeToWait = 12000;
	private ZombieScaleChangeListener scaleChangeListener;
	
	private boolean enabled = false;
	
	private Runnable waitingForNextScaleRunnable = new Runnable() {
		
		@Override
		public void run() {
			long started = System.nanoTime();
			try {
				while((System.nanoTime()-started)/1000000.0 < timeToWait) {
					Thread.sleep(200);
				}
				timeToWait = 12000 - (int)((System.nanoTime()-started)/1000000.0-timeToWait);
				synchronized(scaleLock) {
					zombieScale = zombieScale+1;
				}
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						scaleChangeListener.onZombieScaleChange();
						
					}
				}).start();
				
			} catch (InterruptedException e) {
				timeToWait = timeToWait - (int)((System.nanoTime()-started)/1000000.0);
			}
		}
	};
	
	public ZombieScaleController(int zombieScales, ZombieScaleChangeListener scaleChangeListener) {
		amountOfScales = zombieScales;
		this.scaleChangeListener = scaleChangeListener;
	}
	
	public void setEnabled(boolean enabled) {
		
		if(!enabled && this.enabled != enabled) {
			this.stopWaitForScaleChange();
		}
		
		this.enabled = enabled;
	}
	
	public int getZombieScale() {
		synchronized(scaleLock) {
			if(zombieScale > amountOfScales-1)
				return amountOfScales-1;
			else return zombieScale;
		}
		
	}
	
	public boolean isFinalScale() {
		synchronized(scaleLock) {
			return zombieScale > amountOfScales-1;
		}
	}
	
	boolean waiting = false;
	Object waitingLock = new Object();
	public void startWaitForScaleChange() {
		Log.d(TAG, "Start scale change waiting....");
		if(!enabled) return;
//		if(waitForNextScale != null)
//			Log.d(TAG, "Zombie isNotWaiting: "+(waitForNextScale == null || !waitForNextScale.isAlive())+" isFinalScale: "+isFinalScale());
		synchronized (waitingLock) {
			if(waiting) return;
		}
		while(waitForNextScale != null && waitForNextScale.isAlive()) {
			Log.d(TAG, "Zombie waits for starting scale change!");
			synchronized (waitingLock) {
				waiting = true;
			}
		}
		if((waitForNextScale == null || !waitForNextScale.isAlive()) && !isFinalScale()) {
    		waitForNextScale = new Thread(waitingForNextScaleRunnable);
    		waitForNextScale.start();
    	}
		synchronized (waitingLock) {
			waiting = false;
		}
	}
	
	public void stopWaitForScaleChange() {
		if(waitForNextScale != null && waitForNextScale.isAlive())
    		waitForNextScale.interrupt();
	}
}
