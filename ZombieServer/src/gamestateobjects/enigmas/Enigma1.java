package gamestateobjects.enigmas;

public class Enigma1 extends AEnigma{

	@Override
	public boolean checkSolution(String s) {
		return s.equalsIgnoreCase("yoursolution");
	}

	@Override
	public String getContext() {
		return "/room1";
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
}
