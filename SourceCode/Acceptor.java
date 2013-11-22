

import java.nio.channels.*;
import java.io.IOException;

public class Acceptor implements IAcceptHandler {

    private Dispatcher dispatcher;
    private ServerSocketChannel server;
    private ISocketReadWriteHandlerFactory srwf;
    private String DocumentRoot;
    public Acceptor(ServerSocketChannel server, 
		    Dispatcher d, 
		    ISocketReadWriteHandlerFactory srwf, String DocumentRoot) {
	this.dispatcher = d;
	this.server = server;
	this.srwf = srwf;
	this.DocumentRoot = DocumentRoot;
    }

    public void handleException() {
	System.out.println("handleException(): of Acceptor");
    }

    public void handleAccept(SelectionKey key) throws IOException {
	// ServerSocketChannel server = (ServerSocketChannel ) key.channel();
	// ASSERT: this.server == server

	// extract the ready connection
	SocketChannel client = server.accept();
	Debug.DEBUG("handleAccept: Accepted connection from " + client);

	// configure the connection to be non-blocking
	client.configureBlocking(false);

	/* register the new connection with *read* events/operations
	   SelectionKey clientKey = 
	   client.register(
	   selector, SelectionKey.OP_READ);// | SelectionKey.OP_WRITE);
	*/

	IReadWriteHandler rwH = srwf.createHandler(dispatcher, client, DocumentRoot);
	int ops = rwH.getInitOps();

	SelectionKey clientKey = dispatcher.registerNewSelection(client, rwH, ops);
	//added the timeout registration
	TimeOut.register(rwH, clientKey);
    } // end of handleAccept

    
} // end of class