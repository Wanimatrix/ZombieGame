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
		return "BROADCAST 3 OF 6: "
				+ "Yesterday he took me to a white room and strapped me to a bed. "
				+ "He put a needle in my arm and said that this was going to make me stronger. "
				+ "It hurts. I asked him where my teddybear was, but he didn’t answer. "
				+ "I asked him why he took my mother away. "
				+ "He brushed my hair while he explained that my mommy wasn’t strong enough to be an Übermensch.";
	}

	@Override
	String getSMSSender() {
		return "Jane D.";
	}
}
