package gamestateobjects.enigmas;

import gamestateobjects.MessageService;
import gamestateobjects.RoomList;

public class Enigma4 extends AEnigma{

	public Enigma4(RoomList roomlist, MessageService inbox) {
		super(roomlist, inbox);
	}


	@Override
	public boolean checkSolution(String s) {
		return s.equalsIgnoreCase("tnt") || s.equalsIgnoreCase("dynamite") || s.equalsIgnoreCase("t.n.t.") || s.equalsIgnoreCase("bomb");
	}

	@Override
	public String getContext() {
		return "/"+getRoomName();
	}

	@Override
	public String getTip() {
		return "the bomb in room 3!";
	}

	@Override
	boolean hasNextRoom() {
		return false;
	}

	@Override
	String getNextRoomName() {
		return "";
	}

	@Override
	public String getRoomName() {
		return "room4";
	}


	@Override
	String getSMS() {
		return "BROADCAST 5 OF 6: "
				+ "He took away my green eyes. "
				+ "The doctor said it will take a while to find blue eyes that will fit. "
				+ "I can’t see anymore. It hurts. "
				+ "He gave me back my teddybear... He got mad because I didn't stop crying. "
				+ "But mister teddybear feels different. There are spots without hair. "
				+ "It moves sometimes too and I can hear it making sounds.";
	}

	@Override
	String getSMSSender() {
		return "Jane D.";
	}
}
