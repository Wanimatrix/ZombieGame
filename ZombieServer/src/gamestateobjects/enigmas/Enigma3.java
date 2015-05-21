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
		return "BROADCAST 4 OF 6: "
				+ "The doctor said that my green eyes aren’t pretty enough. "
				+ "He is going to get rid of them and give me pretty blue eyes instead. "
				+ "He said that I will be the first Übermensch and that my mommy would be proud of me. "
				+ "I saw other people wandering by my room. "
				+ "But they didn’t look like people anymore. "
				+ "Dr. Hÿnkell said that they were failed experiments and that they weren’t strong enough to be an Übermensch, just like my mommy. "
				+ "I hear him approaching. Time to get my pretty blue eyes.";
	}

	@Override
	String getSMSSender() {
		return "Jane D.";
	}
}
