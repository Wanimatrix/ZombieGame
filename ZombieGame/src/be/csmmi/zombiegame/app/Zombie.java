package be.csmmi.zombiegame.app;

import android.content.Context;
import android.util.Log;

public class Zombie implements ZombieInSightListener,  ZombieScaleChangeListener{
	private final String TAG = Zombie.class.getSimpleName();
	
	private Context context;
	private LookatSensor sensor;
	private int zombieScales;
	
	private ZombieLocationController zombieLocationController;
	private ZombieScaleController zombieScaleController;
	private ZombieScaleChangeListener externalScaleChangeListener;
	private boolean inSight = false;
	
	public Zombie(Context context, int zombieScales, ZombieScaleChangeListener scaleChangeListener, LookatSensor sensor) {
		this.context = context;
		this.sensor = sensor;
		this.zombieScales = zombieScales;
		zombieLocationController = new ZombieLocationController(context, this, sensor);
		zombieScaleController = new ZombieScaleController(zombieScales, this);
		externalScaleChangeListener = scaleChangeListener;
	}
	
	public boolean isInSight() {
		return zombieLocationController.lookingAtZombie();
	}
	
	public int getZombieScale() {
		return zombieScaleController.getZombieScale();
	}
	
	public boolean hasKilledPlayer() {
		return zombieScaleController.isFinalScale();
	}

	@Override
	public void onZombieInSight() {
		Log.d(TAG, "Zombie in sight!");
		inSight = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				zombieLocationController.stopWaitForChangeZombieLoc();
				zombieScaleController.startWaitForScaleChange();
			}
			
		}).start();
		
	}

	@Override
	public void onZombieOutOfSight() {
		Log.d(TAG, "Zombie out of sight!");
		inSight = false;
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				zombieLocationController.startWaitForChangeZombieLoc();
				zombieScaleController.stopWaitForScaleChange();
			}
			
		}).start();
	}

	@Override
	public void onZombieScaleChange() {
		Log.d(TAG, "Zombie scale change! InSight: "+inSight);
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(inSight) zombieScaleController.startWaitForScaleChange();
				externalScaleChangeListener.onZombieScaleChange();
			}
		}).start();
	}
	
	public void enable() {
		Log.d(TAG, "Zombie enabled!");
		zombieScaleController.setEnabled(true);
		zombieLocationController.setEnabled(true);
	}
	
	public void disable() {
		Log.d(TAG, "Zombie disabled!");
		zombieScaleController.setEnabled(false);
		zombieLocationController.setEnabled(false);
	}
	
	public void reset() {
		this.disable();
		zombieLocationController = new ZombieLocationController(context, this, sensor);
		zombieScaleController = new ZombieScaleController(zombieScales, this);
	}
}
