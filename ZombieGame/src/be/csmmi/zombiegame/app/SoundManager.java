package be.csmmi.zombiegame.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import be.csmmi.zombiegame.R;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

public class SoundManager {
	private static final String TAG = SoundManager.class.getSimpleName();
	
//	private MediaPlayer bgMusic = null;
	public static final int MINIMAL_WAIT = 500;
	
//	private Map<String,Integer> soundFxIds = new HashMap<String, Integer>();
//	private Map<String,Integer> streamIds = new HashMap<String, Integer>();
	private MediaPlayer mediaPlayer;
	private Context context;
	
	private String[] scares = new String[]{"scare1","scare2","scare3"};
	private String[] atmo = new String[]{"atmosphere1","atmosphere2"};
	
	public enum RANDOM_SFX {
		SCARES, ATMOSPHERE;
	}
	
	public SoundManager(Context context) {
		this.context = context;
		mediaPlayer = new MediaPlayer();
		
//		addSound("atmo1", R.raw.atmosphere1);
//		addSound("atmo2", R.raw.atmosphere2);
//		addSound("scare1", R.raw.scare1);
//		addSound("scare2", R.raw.scare2);
//		addSound("scare3", R.raw.scare3);
//		addSound("heartbeat", R.raw.heartbeat);
	}
	
//	public void setBgMusic(int newBgMusicResId, OnCompletionListener listener) {
//		if(bgMusic != null && bgMusic.isPlaying()) bgMusic.stop();
//		
		
//		bgMusic.setLooping(true);
//		bgMusic.setVolume(1, 1);
//		if(listener != null) bgMusic.setOnCompletionListener(listener);
//		bgMusic.start();
//		
//		Log.d(TAG, "Background music started playing.");
//	}
//	
//	public void stopBgMusic() {
//		if(bgMusic != null && bgMusic.isPlaying()) { 
//			bgMusic.stop();
//			Log.d(TAG, "Background music stopped playing");
//		}
//	}
//	
//	public void startBgMusic() {
//		if(bgMusic != null && !bgMusic.isPlaying()) { 
//			bgMusic.start();
//			Log.d(TAG, "Background music started playing");
//		}
//	}
	
//	public void addSound(String name, int resourceId) {
//		soundFxIds.put(name, soundFxPool.load(context, resourceId, 1));
//		Log.d(TAG, "SoundFX "+name+" was added to the SoundManager.");
//	} 
	
	private Random rand = new Random();
	private long lastSoundStarted;
	
	public String playRandomSoundFx(RANDOM_SFX sfxType, float volume) {
		String[] soundIds = null;
		switch (sfxType) {
		case SCARES:
			soundIds = scares;
			break;
		case ATMOSPHERE:
			soundIds = atmo;
			break;
		}
		float randomNumber = rand.nextFloat();
		int index = (int) Math.floor(randomNumber*soundIds.length);
		playSoundFx(soundIds[index], 0.8f, false);
		return soundIds[index];
	}
	
	public void playSoundFx(String name, float volume, boolean loop) {
		if((System.nanoTime()-lastSoundStarted)/1000000.0 < MINIMAL_WAIT) return;
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource("/sdcard/arbg/sounds/"+name+".mp3");
			mediaPlayer.setVolume(volume, volume);
			mediaPlayer.setLooping(loop);
			mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.start();
				}
			});
			mediaPlayer.prepareAsync();
			
		} catch (Exception e) {
			Log.e(TAG, "ERROR ", e);
		}
		
//		if(!doesSoundFxExist(name)) throw new IllegalArgumentException("SoundFX with name "+name+" was never added to the SoundManager");
//		stopAllSoundFxs();
//		int streamId = soundFxPool.play(soundFxIds.get(name), volume, volume, 0, loop, 1);
//		streamIds.put(name, streamId);
//		lastSoundStarted = System.nanoTime();
//		Log.d(TAG, "SoundFX "+name+" started playing.");
	}
	
	public void stopSoundFx() {
		if(isPlaying()){
			mediaPlayer.stop();
		}
	}
	
	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}
	
//	public void stopAllSoundFxs() {
//		
//		String[] currentPlayingSounds = streamIds.keySet().toArray(new String[streamIds.size()]);
//		for (int i = 0; i < currentPlayingSounds.length; i++) {
//			stopSoundFx(currentPlayingSounds[i]);
//		}
//	}
//	
//	public boolean isSoundFxPlaying(String name) {
//		return streamIds.get(name) != null;
//	}
//	
//	private boolean doesSoundFxExist(String name) {
//		return soundFxIds.get(name) != null;
//	}
	
}
