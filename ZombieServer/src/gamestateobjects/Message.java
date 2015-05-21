package gamestateobjects;

import java.util.Base64;

public class Message {

	private String sender;
	private String content;
	private int id;
	
	public Message(String sender, String content, int id){
		this.sender = sender;
		this.content = content;
		this.id = id;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getContent() {
		String converted = Base64.getEncoder(). encodeToString(content.getBytes());
		return converted;
	}
	
	public int getId() {
		return id;
	}
	
	public String toJSON(){
		String json = "{";
		json += "\"id\": \"" + getId() + "\",";
		json += "\"sender\": \"" + getSender() + "\",";
		json += "\"content\": \"" + getContent() + "\"}";
		return json;
	}
}
