package gamestateobjects;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageService {
	
	private HashMap<Integer, Message> mlist = new HashMap<Integer, Message>();
	private int nxtID = 0;
	
	public void publishMessage(String sender, String content){
		Message m = new Message(sender, content, nxtID++);
		mlist.put(m.getId(), m);
	}
	
	public ArrayList<Message> getAllMessageSince(int lastID){
		ArrayList<Message> list = new ArrayList<Message>();
		if(lastID < nxtID-1)
			for (int i = lastID+1; i < nxtID; i++)
				list.add(mlist.get(i));
					
		return list;
	}
	
	public void reset() {
		mlist.clear();
		nxtID = 0;
	}
}
