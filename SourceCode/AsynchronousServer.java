



import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.io.IOException;

public class AsynchronousServer extends WebServer {

    public static int DEFAULT_PORT = 6789;

    public static ServerSocketChannel openServerChannel(int port)  {
	ServerSocketChannel serverChannel=null;
	try {

	    // open server socket for accept
	    serverChannel = ServerSocketChannel.open();
	    Debug.DEBUG(PortNumber + " " + DocumentRoot + " "
				+ ThreadPoolSize + " " + CacheSize + " "
				+ IncompleteTimeout);
	    // extract server socket of the server channel and bind the port
	    ServerSocket ss = serverChannel.socket();
	    InetSocketAddress address = new InetSocketAddress(port);
	    ss.bind(address);

	    // configure it to be non blocking
	    serverChannel.configureBlocking(false);
	    initializeCache();
	    Debug.DEBUG("Server listening for connections on port " + port);

	} catch (IOException ex) {
	    ex.printStackTrace();
	    System.exit(1);   
	} // end of catch

	return serverChannel;	
    } // end of open serverChannel

    public static void main(String[] args) {
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
						IncompleteTimeout = Integer.parseInt(args[i+1])*1000;
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
		
	// get dispatcher/selector
	Dispatcher dispatcher = new Dispatcher();
  

	//creating the new timeout thread
	TimeOut timeout = new TimeOut(IncompleteTimeout, dispatcher);
	Thread t = new Thread(timeout);
	t.start();
	// open server socket channel
	int port;
	try {
	    port = Integer.parseInt(args[0]);
	}
	catch (Exception ex) {
	    port = DEFAULT_PORT;   
	}
	ServerSocketChannel sch = openServerChannel(port);

	// create server acceptor for Echo Line ReadWrite Handler
	ISocketReadWriteHandlerFactory echoFactory = 
	    new WebLineReadWriteHandlerFactory();
	Acceptor acceptor = new Acceptor( sch, dispatcher, echoFactory, DocumentRoot );

	Thread dispatcherThread;
	// register the server channel to a selector	
	try {
	    SelectionKey key = dispatcher.registerNewSelection(sch, 
							       acceptor, 
							       SelectionKey.OP_ACCEPT);
	
	    // start dispatcher
	    dispatcherThread = new Thread(dispatcher);
	    dispatcherThread.start();
	} catch (IOException ex) {
	    System.out.println("Cannot register and start server");
	    System.exit(1);
	}
	// may need to join the dispatcher thread

    } // end of main

} // end of class
