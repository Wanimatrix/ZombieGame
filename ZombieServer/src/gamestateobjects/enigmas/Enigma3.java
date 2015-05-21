package gamestateobjects.enigmas;

import gamestateobjects.MessageService;
import gamestateobjects.RoomList;

public class Enigma3 extends AEnigma{

	public Enigma3(RoomList roomlist, MessageService inbox) {
		super(roomlist, inbox);
	}


	@Override
	public boolean checkSolution(String s) {
		return s.equalsIgnoreCase("except for");
	}

	@Override
	public String getContext() {
		return "/"+getRoomName();
	}

	@Override
	public String getTip() {
		return ", except for";
	}

	@Override
	boolean hasNextRoom() {
		return true;
	}

	@Override
	String getNextRoomName() {
		return "room4";
	}

	@Override
	public String getRoomName() {
		return "room3";
	}


	@Override
	String getSMS() {
		// TODO Auto-generated method stub
		return "Hello Ich bin Jane nicht. Ich bin da sister. We family. Yes.";
	}

	@Override
	String getSMSSender() {
		return "Jano D.";
	}
}
