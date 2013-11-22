

import java.net.*;

public class PerRequestServiceThread extends Thread {

    Socket connSocket;
    String DocumentRoot;
    String ServerName;
    public PerRequestServiceThread(Socket connectionSocket, String WWW_Root, String s) {
	this.connSocket = connectionSocket;
	this.DocumentRoot = WWW_Root;
	this.ServerName = s;
    }
  
    public void run() {
	System.out.println("Thread " + this + " started.");
		WebRequestHandler wrh;
		try {
			wrh = new WebRequestHandler(connSocket, DocumentRoot,ServerName);
			wrh.processRequest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
    } // end run

   
} // end ServiceThread
