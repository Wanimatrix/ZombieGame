package gamestateobjects.enigmas;

import gamestateobjects.MessageService;
import gamestateobjects.RoomList;

public class Enigma1 extends AEnigma{

	public Enigma1(RoomList roomlist, MessageService inbox) {
		super(roomlist, inbox);
	}

	@Override
	public boolean checkSolution(String s) {
		return s.equalsIgnoreCase("there's");
	}

	@Override
	public String getContext() {
		return "/"+getRoomName();
	}

	@Override
	public String getTip() {
		return "There's";
	}

	@Override
	boolean hasNextRoom() {
		return true;
	}

	@Override
	String getNextRoomName() {
		return "room2";
	}

	@Override
	public String getRoomName() {
		return "room1";
	}

	@Override
	String getSMS() {
		// TODO Auto-generated method stub
		return "Hello I am Jane. I Help You. Yes.";
	}

	@Override
	String getSMSSender() {
		return "Jane D.";
	}
}
