package be.csmmi.zombiegame.app;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class ServerCommunication {
	private static RequestQueue queue;
	
	public static void init(Context ctx) {
		queue = Volley.newRequestQueue(ctx);
	}

	public static void sendArrayMessage(String message, Response.Listener<JSONArray> callback) {
		JsonArrayRequest req = new JsonArrayRequest(AppConfig.SERVER_ADDRESS+"/"+message, callback, null);		
		queue.add(req);
	}
	
	public static void sendArrayMessage(String message, Response.Listener<JSONArray> callback, Response.ErrorListener errorListener) {
		JsonArrayRequest req = new JsonArrayRequest(AppConfig.SERVER_ADDRESS+"/"+message, callback, errorListener);	
		queue.add(req);
	}
	
	public static void sendObjectMessage(String message, Response.Listener<JSONObject> callback) {
		JsonObjectRequest req = new JsonObjectRequest(AppConfig.SERVER_ADDRESS+"/"+message, null, callback, null);
		queue.add(req);
	}
	
	public static void sendObjectMessage(String message, Response.Listener<JSONObject> callback, Response.ErrorListener errorListener) {
		JsonObjectRequest req = new JsonObjectRequest(AppConfig.SERVER_ADDRESS+"/"+message, null, callback, errorListener);
		queue.add(req);
	}
}
