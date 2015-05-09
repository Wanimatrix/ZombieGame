package gamestateobjects;

import java.util.Timer;
import java.util.TimerTask;

public class GameStatus {

	private boolean inprogress = false;
	private boolean endgamestarted = false;
	private RoomList rooms;
	
	private Timer t;
	private int secondsLeft = 0;
	private int period = 1000;
	private int maxSeconds = 60*20; // 20 minutes
	
	public GameStatus(RoomList r){
		this.rooms = r;
	}
	
	public void startGame(){
		this.inprogress = true;
		startTimer();
	}
	
	public void startEndGame(){
		this.endgamestarted = true;
	}
	
	public void resetGame(){
		this.inprogress = false;
		this.endgamestarted = false;
		if(t != null) t.cancel();
		this.rooms.lockAllRooms();
	}
	
	private void startTimer() {
		TimerTask theTimertask = new TimerTask() {

	        public void run() {
	        	if (secondsLeft-- == 1)
	                t.cancel();
	        }
	    };
	    t = new Timer();
	    secondsLeft = maxSeconds;
	    t.scheduleAtFixedRate(theTimertask, period, period);
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

