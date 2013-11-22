
import java.util.*;
import java.net.*;

public class ShareQServer extends WebServer {
  
    private ServerSocket welcomeSocket;
	
    
    private ShareQServiceThread[] threads;
    private List<Socket> connSockPool;

    /* Constructor: starting all threads at once */
    public ShareQServer(int serverPort) {

	try {
	    // create server socket
	    welcomeSocket = new ServerSocket(serverPort);
	    System.out.println("Server started; listening at " + serverPort);

	    connSockPool = new Vector<Socket>();
	    initializeCache();

	    // create thread pool
	    threads = new ShareQServiceThread[ThreadPoolSize];

	    // start all threads
	    for (int i = 0; i < threads.length; i++) {
		threads[i] = new ShareQServiceThread(connSockPool, DocumentRoot, ServerName); 
		threads[i].start();
	    }
	} catch (Exception e) {
	    System.out.println("Server construction failed.");
	}

    } // end of Server

    public static void main(String[] args) {
	// see if we do not use default server port
		if (args.length < 2) {
			System.out.println("Wrong input: -config config.file");
			return;
		}
		if (!args[0].equals("-config")){
			System.out.println("Wrong input: -config config.file");
			return;
		}
		if (!readConfigFile(args[1])){
			return;
		}
		if (args.length > 2){
			for(int i = 2; i<args.length;i+=2){
				switch (args[i].toLowerCase()) {
				
				case "-threadpoolsize":
					try {
						ThreadPoolSize = Integer.parseInt(args[i+1]);
						if (ThreadPoolSize < 0){
							System.out.println("Incorrect -threadpoolsize");
							return;
						}
					} catch(NumberFormatException e){
						System.out.println("Incorrect -threadpoolsize");
						return;
					}
					break;
				case "-cachesize":
					try {
						CacheSize = Integer.parseInt(args[i+1]);
						if (CacheSize < 0){
							System.out.println("Incorrect -cachesize");
							return;
						}
					} catch(NumberFormatException e){
						System.out.println("Incorrect -cachesize");
						return;
					}
					break;
				case "-incompletetimeout":
					try {
						IncompleteTimeout = Integer.parseInt(args[i+1]);
						if (IncompleteTimeout < 0){
							System.out.println("Incorrect -incompletetimeout");
							return;
						}
					} catch(NumberFormatException e){
						System.out.println("Incorrect -incompletetimeout");
						return;
					}
					break;
		
				}
			}
				
				
		}
		

	ShareQServer server = new ShareQServer(PortNumber);
	server.run();

    } // end of main

    // Infinite loop to process each connection
    public void run() {
	while (true) {

	    try {
		// accept connection from connection queue
		Socket connSock = welcomeSocket.accept();
		System.out.println("Main thread retrieve connection from " 
				   + connSock);

		// how to assign to an idle thread?
		synchronized (connSockPool) {
		    connSockPool.add(connSock);
		} // end of sync
	    } catch (Exception e) {
	    }

	} // end of loop

    } // end of run

} // end of class
