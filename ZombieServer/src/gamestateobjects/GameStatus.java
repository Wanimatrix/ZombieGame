package gamestateobjects;

public class GameStatus {

	private boolean inprogress = false;
	private boolean endgamestarted = false;
	
	public void startGame(){
		this.inprogress = true;
		//TODO 
	}
	public boolean isInProgress(){
		return inprogress;
	}
	
	public boolean hasEndgameStarted(){
		return endgamestarted;
	}
}

