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
		// TODO Auto-generated method stub
		return "PfanzerGewehr!!!!!!! KRIEGGG!!!! DIEE!!!!";
	}

	@Override
	String getSMSSender() {
		return "Dr. Hynkell";
	}
}
