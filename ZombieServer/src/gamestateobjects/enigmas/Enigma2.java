package gamestateobjects.enigmas;

import gamestateobjects.MessageService;
import gamestateobjects.RoomList;

public class Enigma2 extends AEnigma{

	public Enigma2(RoomList roomlist, MessageService inbox) {
		super(roomlist, inbox);
	}


	@Override
	public boolean checkSolution(String s) {
		return s.equalsIgnoreCase("prison") || s.equalsIgnoreCase("trapped");
	}

	@Override
	public String getContext() {
		return "/"+getRoomName();
	}

	@Override
	public String getTip() {
		return "no escape";
	}

	@Override
	boolean hasNextRoom() {
		return true;
	}

	@Override
	String getNextRoomName() {
		return "room3";
	}

	@Override
	public String getRoomName() {
		return "room2";
	}


	@Override
	String getSMS() {
		// TODO Auto-generated method stub
		return "Hello I am Jane again. I Help You again. Yes.";
	}

	@Override
	String getSMSSender() {
		return "Jane D.";
	}
}
