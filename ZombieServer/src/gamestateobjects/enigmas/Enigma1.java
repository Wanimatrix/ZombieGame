package gamestateobjects.enigmas;

import gamestateobjects.RoomList;

public class Enigma1 extends AEnigma{

	public Enigma1(RoomList roomlist) {
		super(roomlist);
	}

	@Override
	public boolean checkSolution(String s) {
		return s.equalsIgnoreCase("yoursolution");
	}

	@Override
	public String getContext() {
		return "/"+getRoomName();
	}

	@Override
	public String getTip() {
		return "Tips are for fools";
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
}
