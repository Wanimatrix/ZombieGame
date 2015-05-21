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
		return "BROADCAST 2 OF 6: "
				+ "Last week the mean man took my mommy. "
				+ "He said that he has almost solved the formula for creating the Übermensch. "
				+ "My mommy didn’t like him. She was always yelling at him and said he was insane. "
				+ "Herr Dr. Hÿnkell became really mad and yelled that this task was trusted to him by his Führer himself."
				+ "Then he dragged my mother away. I haven’t seen her since. "
				+ "Do you know where my mommy is? I miss her… It’s cold here...";
	}

	@Override
	String getSMSSender() {
		return "Jane D.";
	}
}
