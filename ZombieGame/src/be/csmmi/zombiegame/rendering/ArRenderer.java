package be.csmmi.zombiegame.rendering;

import java.nio.Buffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;
import be.csmmi.zombiegame.app.AppConfig;
import be.csmmi.zombiegame.app.SoundManager;
import be.csmmi.zombiegame.rendering.meshes.FullSquadMesh;
import be.csmmi.zombiegame.utilities.RenderingUtils;

public class ArRenderer implements Renderer, OnCamerasReadyListener {
	
	private final static String TAG = ArRenderer.class.getSimpleName();
	private GLSurfaceView view;
	
	private Camera camera;
	private int[] tex;
	private SurfaceTexture st;
	private SurfaceTexture st2;
	private Buffer pTexCoordVBG;
	private Buffer pVertexVBG;
	private Context context;
	
    private float[] mvp = new float[16];
    
    // RENDER TO TEXTURE VARIABLES
 	int[] fb, depthRb, renderTex; // the framebuffer, the renderbuffer and the texture to render
 	int texW = AppConfig.PREVIEW_RESOLUTION[0];           // the texture's width
 	int texH = AppConfig.PREVIEW_RESOLUTION[1];           // the texture's height
 	IntBuffer texBuffer;          //  Buffer to store the texture
 	
 	// RENDERING HANDLERS
 	private int[] programId;
 	private int vTexCoordHandler;
	private int sTextureHandler;
	private int[] vPositionHandler;
    private int[] mvpHandler;
    
    
    private CameraManager camManager;
	
    // CAMERA SHADERS
	private final String vssCamera =
		"attribute vec2 vPosition;\n" +
		"attribute vec2 vTexCoord;\n" +
		"uniform mat4 u_MVP;\n" +
		"varying vec2 texCoord;\n" +
		"void main() {\n" +
		"  texCoord = vTexCoord;\n" +
		"  gl_Position = u_MVP * vec4( vPosition.x, vPosition.y, 0.0, 1.0 );\n" +
		"}";
 
	private final String fssCamera =
		"#extension GL_OES_EGL_image_external : require\n" +
		"precision mediump float;\n" +
		"uniform samplerExternalOES sTexture;\n" +
		"varying vec2 texCoord;\n" +
		"void main() {\n" +
		"  gl_FragColor = texture2D(sTexture,texCoord);\n" +
		"}";
	
	public ArRenderer(GLSurfaceView view, Context context) {
		this.view = view;
		this.context = context;
		camManager = new CameraManager(context, view);
	}
	
	/**
	 ***********
	 * GETTERS *
	 ***********
	 */
	
	public Camera getCamera() {
		return camera;
	}
	
	public int getTextureId() {
		return renderTex[0];
	}

	

	/**
	 **************************
	 * SURFACE INIT FUNCTIONS *
	 **************************
	 */
	
	private Surface s;
	
	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		initFrameBuffer();
		
		setupCameraTex();
		
		GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		
		setupRenderHandlers();
		
		camManager.initCameras(this);
    }

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Starting vertices, texcoords init...");	
		
		FullSquadMesh squad = new FullSquadMesh();
		
		pTexCoordVBG = squad.getTexCoords();
		pVertexVBG = squad.getVertices();
		
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Vertices, texcoords init done...");
		
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Starting camera setup...");
		
//	    setupCamera();
		
	    if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Camera setup done...");
	}
	
	@Override
	public void onCamerasReady() {
		s = new Surface(st);
		camManager.setSurface(s);
	}

	/**
	 ******************************
	 * RENDERER CONTROL FUNCTIONS *
	 ******************************
	 */
	
	@Override
	public void onDrawFrame(GL10 unused) {
//		synchronized(this) {
//            if (updateSurface) {
//                st.updateTexImage();
////                st.getTransformMatrix(mSTMatrix);
//                updateSurface = false;
//            }
//        }
		Log.d("MJPEG", "Drawing...");
		
		st.updateTexImage();
		st2.updateTexImage();
		
		GLES20.glViewport(0, 0, this.texW, this.texH);
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb[0]);
     	GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRb[0]);
     	GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		renderCamera();
	    
	    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
	}

	private void renderCamera() {
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Starting rendering camera to FrameBuffer...");
		
		// Render camera to texture
		GLES20.glUseProgram(programId[0]);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
	    GLES20.glUniform1i(sTextureHandler, 0);
	    
	    GLES20.glVertexAttribPointer(vPositionHandler[0], 2, GLES20.GL_FLOAT, false, 4*2, pVertexVBG);
	    GLES20.glEnableVertexAttribArray(vPositionHandler[0]);
	    GLES20.glVertexAttribPointer(vTexCoordHandler, 2, GLES20.GL_FLOAT, false, 4*2, pTexCoordVBG );
	    GLES20.glEnableVertexAttribArray(vTexCoordHandler);
	    
	    Matrix.setIdentityM(mvp, 0);
	    Matrix.scaleM(mvp, 0, 1, -1, 1);
	    
	    GLES20.glUniformMatrix4fv(mvpHandler[0], 1, false, mvp, 0);
	
	    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	    GLES20.glFlush();
	    
	    GLES20.glDisableVertexAttribArray(vPositionHandler[0]);
	    GLES20.glDisableVertexAttribArray(vTexCoordHandler);
	    
	    if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Rendering camera to FrameBuffer done...");
	}

	
	
	/**
	 **************************
	 * SETUP HELPER FUNCTIONS *
	 **************************
	 */

	private void setupRenderHandlers() {
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Starting renderhandlers init...");
		
		programId = new int[1];
		vPositionHandler = new int[1];
		mvpHandler = new int[1];
		
		programId[0] = RenderingUtils.createProgramFromShaderSrc(vssCamera,fssCamera);
		
		// Camera handlers
		vPositionHandler[0] = GLES20.glGetAttribLocation(programId[0], "vPosition");
	    vTexCoordHandler = GLES20.glGetAttribLocation ( programId[0], "vTexCoord" );
	    sTextureHandler = GLES20.glGetUniformLocation ( programId[0], "sTexture" );
	    mvpHandler[0] = GLES20.glGetUniformLocation(programId[0], "u_MVP");
	    
	    if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Renderhandlers init done...");
	}

	private void setupCameraTex() {
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Starting camera texture setup...");
		
		tex = new int[2];
		GLES20.glGenTextures(1, tex, 0);
	    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
	    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
	    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
	    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
		
		st2 = new SurfaceTexture(tex[0]);
		st2.setDefaultBufferSize(1920, 1080);
//		st.setOnFrameAvailableListener(camManager);
		st = new SurfaceTexture(tex[0]);
		st.setDefaultBufferSize(1920, 1080);
		
		camManager.startCamera(st2);
		
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Camera texture setup done...");
	}

	private void initFrameBuffer() {
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "Starting FrameBuffer init...");
		
		// create the ints for the framebuffer, depth render buffer and texture
        fb = new int[1];
        depthRb = new int[1];
        renderTex = new int[1];
         
        // generate
        GLES20.glGenFramebuffers(1, fb, 0);
        GLES20.glGenRenderbuffers(1, depthRb, 0); // the depth buffer
        GLES20.glGenTextures(1, renderTex, 0);
         
        // generate texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTex[0]);
         
        // parameters - we have to make sure we clamp the textures to the edges
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        
        // generate the textures
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, texW, texH, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
         
        // create render buffer and bind 16-bit depth buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRb[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, texW, texH);
        
        // Bind the framebuffer
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb[0]);
		 
		// specify texture as color attachment
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTex[0], 0);
		 
		// attach render buffer as depth buffer
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRb[0]);
		 
		// check status
		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		
		if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
		    Log.e(TAG, "FRAMEBUFFER INCOMPLETE"); 
		    System.exit(1);
		}
		
		// Bind screen as framebuffer
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
		
		if(AppConfig.DEBUG_LOGGING) Log.d(TAG, "FrameBuffer init successful...");
	}
	
	public CameraManager getCameraManager() {
		return camManager;
	}
}
