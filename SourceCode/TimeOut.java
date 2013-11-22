

import java.nio.channels.SelectionKey;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class TimeOut implements Runnable {

	//min priority queue to store TimeoutConnection objects
	private static PriorityBlockingQueue<TimeoutConnection> queue;
	//the timeOutValue that will be specified in the config file;
	private static long idleTimeOutMillis;
	//Since we are using just one dispatcher thread we can store 
	//an instance of that here insteading of storing it in each object
	private static Dispatcher disp;

	public TimeOut(long i, Dispatcher d) {
		Debug.DEBUG("Timeout thread instantiated");
		Comparator<TimeoutConnection> c = new CompareConnectionTimeouts();
		queue = new PriorityBlockingQueue<TimeoutConnection>(1, c);
		idleTimeOutMillis = 3000;
		disp = d;
	}

	//registers the object and notifies all threads waiting on it
	public static void register(IReadWriteHandler rwH, SelectionKey clientKey) {
		TimeoutConnection t = new TimeoutConnection(System.currentTimeMillis(),
				rwH, clientKey);
		Debug.DEBUG("Registering new connection");
		synchronized (queue) {
			queue.add(t);
			queue.notifyAll();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		while (true) {

			Debug.DEBUG("blocking before queue");
			synchronized (queue) {
				//checking for empty queue
				while (queue.isEmpty()) {
					try {
						Debug.DEBUG("Thread " + this
								+ " sees empty pool.");
						queue.wait();
					} catch (InterruptedException ex) {
						Debug.DEBUG("Waiting for pool interrupted.");
					} // end of catch
				} // end of while
			}
			TimeoutConnection t = queue.peek();
			long timeGap = System.currentTimeMillis() - t.getTimeStamp();
			//connection has timed out, invoke the callback to remove it 
			if (t != null && Math.abs(timeGap) > idleTimeOutMillis
					&& !t.getReadWriteHandler().getRequestCompleteState()) {
				synchronized (queue) {
					queue.remove();
				}
				Debug.DEBUG("Connection is timing out");
				SelectionKey k = t.getKey();
				IdleTimeOutCallback idle = new IdleTimeOutCallback(k, disp);
				disp.invokeLater(idle);
				timeGap = 0;
			} else if (t != null && Math.abs(timeGap) <= idleTimeOutMillis
					&& t.getReadWriteHandler().getRequestCompleteState()) {
				//request complete before timeout
				synchronized (queue) {
					Debug.DEBUG("Connection is complete request");
					queue.remove();
				}
			} else if (t != null && Math.abs(timeGap) <= idleTimeOutMillis
					&& !t.getReadWriteHandler().getRequestCompleteState()) {
				//if the connection isnt complete and timeout hasnt happened 
				//sleep till that time arrives
				Debug.DEBUG("waiting for connection till timeout and sleeping "
						+ (idleTimeOutMillis - timeGap));
				try {
					Thread.sleep(idleTimeOutMillis - timeGap);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else if (t != null && Math.abs(timeGap) > idleTimeOutMillis
					&& t.getReadWriteHandler().getRequestCompleteState()) {
				synchronized (queue) {
					//if the timeout thread checked it after the timeout period 
					// and the request is complete, just dequeue it
					queue.remove();
					Debug.DEBUG("Connection is complete after timeout");
				}
			
			}

		}
	}

}
