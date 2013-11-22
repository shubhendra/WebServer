
import java.net.*;
import java.util.*;

public class ShareQServiceThread extends Thread {

    private List<Socket> pool;
    String DocumentRoot;
    String ServerName;
    public ShareQServiceThread(List<Socket> pool, String WWW_Root, String s) {
	this.pool = pool;
	this.DocumentRoot = WWW_Root;
	this.ServerName = s;
    }
  
    public void run() {

	while (true) {
	    // get a new request connection
	    Socket s = null;

	    while (s == null) {
		synchronized (pool) {         
		    if (!pool.isEmpty()) {
			// remove the first request
			s = (Socket) pool.remove(0); 
			System.out.println("Thread " + this 
					   + " process request " + s);
		    } // end if
		} // end of sync
	    } // end while
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
