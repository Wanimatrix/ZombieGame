package be.csmmi.zombiegame.app;

public class AppConfig {
	
	public final static String SERVER_ADDRESS = "http://192.168.1.254:8082"; // 23.3
	
	public final static int AMOUNT_PREVIEW_BUFFERS = 1;
	public static boolean TOUCH_EVENT = false;

	public static final long MAX_FLASHLIGHT_TIME = 60*7; // 7 minutes
	public static final int MAX_SIMULTANEOUS_SOUNDS = 10;
	
	public final static boolean DEBUG_LOGGING = true;
	public final static boolean DEBUG_TIMING = true;
	
	/**
	 * Supported Resolutions:<p/>
	 * 
	 * 1600,1200<br/>
	 * 1280,720<br/>
	 * 960,720<br/>
	 * 720,480<br/>
	 * 704,576<br/>
	 * 640,480<br/>
	 * 480,320<br/>
	 * 320,240<br/>
	 * 176,144
	 */
	public final static int[] PREVIEW_RESOLUTION = new int[]{640,480};
	public final static float ASPECT_RATIO = AppConfig.PREVIEW_RESOLUTION[1]/((float)AppConfig.PREVIEW_RESOLUTION[0]);
	
	/**
	 * Supported FPS ranges (multiplied by 1000)
	 * 
	 * 1000,15000 (1-15)
	 * 1000,30000 (1-30)
	 * 1000,45000 (1-45)
	 * 1000,60000 (1-60)
	 */
	
	public final static int[] FPS_RANGE = new int[]{1000,15000};

}
