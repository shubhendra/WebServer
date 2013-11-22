
import java.net.ServerSocket;
import java.net.Socket;


public class PerRequestThreadServer extends WebServer{

    //public final static int THREAD_COUNT = 3;


    @SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
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
	
	
		ServerSocket welcomeSocket;
		initializeCache();
		
			welcomeSocket = new ServerSocket(PortNumber);
			System.out.println("Server started; listening at " + PortNumber);
		
	    
		// accept connection from connection queue
		while(true){
			
		try{
		Socket connectionSocket = welcomeSocket.accept();
	    System.out.println("accepted connection from " + connectionSocket);
		// how to assign to an idle thread?
		
	    Thread PerRequestServiceThread = new PerRequestServiceThread(connectionSocket, DocumentRoot, ServerName);
	    PerRequestServiceThread.start();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		}

	} // end of loop

    } // end of run


