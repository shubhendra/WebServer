

import java.net.*;

public class ShareWelcomeServer extends WebServer {
  
    private ServerSocket welcomeSocket;
	
    //public final static int THREAD_COUNT = 3;
    private ShareWelcomeServiceThread[] threads;

    /* Constructor: starting all threads at once */
    public ShareWelcomeServer(int serverPort) {

	try {
	    // create server socket
	    welcomeSocket = new ServerSocket(serverPort);
	    System.out.println("Server started; listening at " + serverPort);

	    // create thread pool
	    threads = new ShareWelcomeServiceThread[ThreadPoolSize];

	    // start all threads
	    for (int i = 0; i < threads.length; i++) {
		threads[i] = new ShareWelcomeServiceThread(welcomeSocket, DocumentRoot, ServerName); 
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
		
	ShareWelcomeServer server = new ShareWelcomeServer(PortNumber);
	server.run();

    } // end of main

    // Infinite loop to process each connection
    public void run() {

	try {
	    for (int i = 0; i < threads.length; i++) {
		threads[i].join();
	    }
	    System.out.println("All threads finished. Exit");

	} catch (Exception e) {
	    System.out.println("Join errors");
	}
			
    } // end of run

} // end of class
