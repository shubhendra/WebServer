

import java.nio.channels.SelectionKey;

public class IdleTimeOutCallback implements Runnable {

	Dispatcher disp;
	SelectionKey key;
	public IdleTimeOutCallback(SelectionKey k, Dispatcher d ){
		this.disp = d;
		this.key = k;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		disp.closeConnection(key);
	}

}
