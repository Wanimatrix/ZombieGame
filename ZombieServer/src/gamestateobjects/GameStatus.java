package gamestateobjects;

import java.util.Timer;
import java.util.TimerTask;

public class GameStatus {

	private boolean inprogress = false;
	private boolean endgamestarted = false;
	private boolean startOutro = false;
	private RoomList rooms;
	
	private Thread timer;
	private long startTime = 0;
	private int period = 1000;
	private int maxSeconds = 60*60*4; // 20 minutes
	
	public GameStatus(RoomList r){
		this.rooms = r;
	}
	
	public void startGame(){
		this.inprogress = true;
	}
	
	public void startEndGame(){
		this.endgamestarted = true;
	}
	
	public void resetGame(){
		this.inprogress = false;
		this.endgamestarted = false;
		this.startOutro = false;
		this.rooms.lockAllRooms();
	}
	
	public boolean isInProgress(){
		return inprogress;
	}
	
	public boolean hasEndgameStarted(){
		return endgamestarted;
	}
	
	public void startOutro() {
		startOutro = true;
	}
	
	public void stopOutro() {
		startOutro = false;
	}
	
	public int getMaxTime(){
		return maxSeconds;
	}
	
	public void setInProgress(boolean inProgress) {
		this.inprogress = inProgress;
	}
	
	public boolean getStartOutro() {
		return startOutro;
	}
}

