
import java.net.*;
import java.util.*;

public class WaitNotifyServiceThread extends Thread {

	private List<Socket> pool;
	String DocumentRoot;
	String ServerName;
	public WaitNotifyServiceThread(List<Socket> pool, String WWW_Root, String s) {
		this.pool = pool;
		this.DocumentRoot = WWW_Root;
		this.ServerName = s;
	}
  
	public void run() {

		while (true) {
			// get a new request connection
			Socket s = null;

			synchronized (pool) {         
				while (pool.isEmpty()) {
					try {
						Debug.DEBUG("Thread " + this + " sees empty pool.");
						pool.wait();
					}
					catch (InterruptedException ex) {
						Debug.DEBUG("Waiting for pool interrupted.");
					} // end of catch
				} // end of while
				
				// remove the first request
				s = (Socket) pool.remove(0); 
				Debug.DEBUG("Thread " + this 
								   + " process request " + s);
			} // end of extract a request

			try {
				WebRequestHandler wrh = new WebRequestHandler(s, DocumentRoot, ServerName);
				wrh.processRequest();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			
			
		} // end while
		
	} // end run



} // end ServiceThread
