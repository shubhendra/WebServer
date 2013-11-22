

import java.nio.channels.SelectionKey;
//class whose objects are queued in the min priority queue
public class TimeoutConnection {

	private IReadWriteHandler rwh;
	private SelectionKey key;
	private long timeStamp;
	
	public TimeoutConnection(long t, IReadWriteHandler r, SelectionKey k){
		this.rwh = r;
		this.key = k;
		this.timeStamp = t;

	}
	
	public long getTimeStamp(){
		return timeStamp;
	}
	
	public IReadWriteHandler getReadWriteHandler(){
		return rwh;
	}
	
	public SelectionKey getKey(){
		return key;
	}
}
