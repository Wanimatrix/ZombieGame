package gamestateobjects;

import java.util.Timer;
import java.util.TimerTask;

public class GameStatus {

	private boolean inprogress = false;
	private boolean endgamestarted = false;
	private RoomList rooms;
	
	private Timer t;
	private int secondsLeft = 0;
	
	public GameStatus(RoomList r){
		this.rooms = r;
	}
	
	public void startGame(){
		this.inprogress = true;
		
	    int period = 1000;
	    t = new Timer();
	    secondsLeft = 60*20; //20 minutes
	    t.scheduleAtFixedRate(new TimerTask() {

	        public void run() {
	        	if (secondsLeft-- == 1)
	                t.cancel();
	        }
	    }, period, period);
	}
	
	public void startEndGame(){
		this.endgamestarted = true;
	}
	
	public void resetGame(){
		this.inprogress = false;
		this.endgamestarted = false;
		secondsLeft = 60*20;
		this.rooms.lockAllRooms();
	}
	
	public boolean isInProgress(){
		return inprogress;
	}
	
	public boolean hasEndgameStarted(){
		return endgamestarted;
	}
	
	public int timeLeft(){
		return secondsLeft;
	}
}

