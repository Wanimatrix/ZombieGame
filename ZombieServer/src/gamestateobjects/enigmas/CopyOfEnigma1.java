package gamestateobjects.enigmas;

import gamestateobjects.RoomList;

public class CopyOfEnigma1 extends AEnigma{

	public CopyOfEnigma1(RoomList roomlist) {
		super(roomlist);
	}

	@Override
	public boolean checkSolution(String s) {
		return s.equalsIgnoreCase("mysolution");
	}

	@Override
	public String getContext() {
		return "/"+getRoomName();
	}

	@Override
	public String getTip() {
		return "Tips snul";
	}

	@Override
	boolean hasNextRoom() {
		return false;
	}

	@Override
	String getNextRoomName() {
		return "room2";
	}

	@Override
	public String getRoomName() {
		return "room2";
	}
}
