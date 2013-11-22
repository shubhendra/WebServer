

import java.io.*;
import java.net.*;


public class ShareWelcomeServiceThread extends Thread {

    ServerSocket welcomeSocket;
    String DocumentRoot;
    String ServerName;

    public ShareWelcomeServiceThread(ServerSocket welcomeSocket, String WWW_Root, String s) {
	this.welcomeSocket = welcomeSocket;
	this.DocumentRoot = WWW_Root;
	this.ServerName = s;
    }
  
    public void run() {

	System.out.println("Thread " + this + " started.");
	while (true) {
	    // get a new request connection
	    Socket s = null;

	    synchronized (welcomeSocket) {         
		try {
		    s = welcomeSocket.accept();
		    System.out.println("Thread " + this 
				       + " process request " + s);
		} catch (IOException e) {
		}
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
