

import java.net.ServerSocket;
import java.net.Socket;


public class SequentialServer extends WebServer {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
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
		ServerSocket listenSocket = new ServerSocket(PortNumber);
		System.out.println("server listening at: " + listenSocket);
		System.out.println("server www root: " + DocumentRoot);
		initializeCache();
		while (true) {
			

		    try {

			// take a ready connection from the accepted queue
			Socket connectionSocket = listenSocket.accept();
			System.out.println("\nReceive request from " + connectionSocket);
		
			// process a request
			WebRequestHandler wrh = 
			    new WebRequestHandler( connectionSocket, DocumentRoot, ServerName);

			wrh.processRequest();

		    } catch (Exception e)
			{
			}
		    finally{
		    	System.out.println("Closing connection:");
		//    		listenSocket.close();
			
		    }
		    
		} // end of while (true)
		
	}
	

}
