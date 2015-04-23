package gamestateobjects;

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
		return content;
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
