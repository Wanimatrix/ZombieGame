package be.csmmi.zombiegame.app;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

public class SoundManager {
	private static final String TAG = SoundManager.class.getSimpleName();
	
//	private MediaPlayer bgMusic = null;
	private Map<String,Integer> soundFxIds = new HashMap<String, Integer>();
	private Map<String,Integer> streamIds = new HashMap<String, Integer>();
	private SoundPool soundFxPool = null;
	private Context context;
	
	public SoundManager(Context context) {
		this.context = context;
		
		soundFxPool = new SoundPool(AppConfig.MAX_SIMULTANEOUS_SOUNDS, AudioManager.STREAM_MUSIC, 0);
	}
	
//	public void setBgMusic(int newBgMusicResId, OnCompletionListener listener) {
//		if(bgMusic != null && bgMusic.isPlaying()) bgMusic.stop();
//		
//		bgMusic = MediaPlayer.create(context, newBgMusicResId);
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
	
	public void addSound(String name, int resourceId) {
		soundFxIds.put(name, soundFxPool.load(context, resourceId, 1));
		Log.d(TAG, "SoundFX "+name+" was added to the SoundManager.");
	} 
	
	public void playSoundFx(String name, float volume, int loop) {
		if(!doesSoundFxExist(name)) throw new IllegalArgumentException("SoundFX with name "+name+" was never added to the SoundManager");
		int streamId = soundFxPool.play(soundFxIds.get(name), volume, volume, 0, loop, 1);
		streamIds.put(name, streamId);
		Log.d(TAG, "SoundFX "+name+" started playing.");
	}
	
	public void stopSoundFx(String name) {
		if(!doesSoundFxExist(name)) throw new IllegalArgumentException("SoundFX with name "+name+" was never added to the SoundManager");
		if(!isSoundFxPlaying(name)) { 
			Log.d(TAG, "SoundFX was not playing.");
			return;
		}
		soundFxPool.stop(streamIds.get(name));
		streamIds.remove(name);
		Log.d(TAG, "SoundFX "+name+" stopped playing.");
	}
	
	public boolean isSoundFxPlaying(String name) {
		return streamIds.get(name) != null;
	}
	
	private boolean doesSoundFxExist(String name) {
		return soundFxIds.get(name) != null;
	}
	
}
