package server;

import gamestateobjects.Room;
import gamestateobjects.RoomList;


public class LockThread implements Runnable {
	private RoomList l;
	
	public LockThread(RoomList l) {
		this.l = l;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(5000);
				for (Room room : l.getAllRooms()) {
					if(room.isLocked())
						room.unlock();
					else
						room.lock();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
