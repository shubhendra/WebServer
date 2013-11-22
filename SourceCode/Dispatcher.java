
import java.nio.channels.*;
import java.io.IOException;
import java.util.*; // for Set and Iterator

public class Dispatcher implements Runnable {

    private Selector selector;
    
    private final List<Runnable> pendingInvocations = new ArrayList<Runnable>();

	private boolean closeRequested = false;
    
    

	public Dispatcher() {
		// create selector
		try {
			selector = Selector.open();
		} catch (IOException ex) {
			Debug.DEBUG("Cannot create selector.");
			ex.printStackTrace();
			System.exit(1);
		} // end of catch
	} // end of Disptacher

//All the three methods below have been adopted from the another version 
//	of the java code posted on the class homepage
    /**
     * Raises an internal flag that will result on this thread dying
     * the next time it goes through the dispatch loop. The thread 
     * executes all pending tasks before dying.
     */
    public void requestClose() {
      closeRequested  = true;
      // Nudges the selector.
      selector.wakeup();
    }
    
    /**
     * Executes the given task in the selector thread. This method returns
     * as soon as the task is scheduled, without waiting for it to be 
     * executed.
     * 
     * @param run The task to be executed.
     */
    public void invokeLater(Runnable run) {
      synchronized (pendingInvocations) {
        pendingInvocations.add(run);
      }
      selector.wakeup();
    }
    
    /**
     * Executes all tasks queued for execution on the selector's thread.
     * 
     * Should be called holding the lock to <code>pendingInvocations</code>.
     *
     */
    private void doInvocations() {
      synchronized (pendingInvocations) {
        for (int i = 0; i < pendingInvocations.size(); i++) {
          Runnable task = (Runnable) pendingInvocations.get(i);
          task.run();
        }
        pendingInvocations.clear();
      }
    }

    public void closeConnection(SelectionKey key){
    	try {
			deregisterSelection(key);
			key.channel().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    public SelectionKey registerNewSelection(SelectableChannel channel, 
					     IChannelHandler handler, 
					     int ops) throws ClosedChannelException
    {
	SelectionKey key = channel.register(selector, ops);
	key.attach(handler);
	return key;
    } // end of registerNewChannel

    public SelectionKey keyFor(SelectableChannel channel) {
	return channel.keyFor(selector);
    }

    public void deregisterSelection(SelectionKey key) throws IOException {
	key.cancel();
    }

    public void updateInterests(SelectionKey sk, int newOps) {
	sk.interestOps(newOps);
    }

    public void run() {

	while (true) {
		
		Debug.DEBUG("Enter Dispatcher");
		
		 doInvocations();
	      
	      // Time to terminate? 
	      if (closeRequested) {
	        return;
	      }
	      
	     
	    Debug.DEBUG("Enter selection");
	    try {
		// check to see if any events
		selector.select();
	    }
	    catch (IOException ex) {
		ex.printStackTrace();
		break;
	    }
        
	    // readKeys is a set of ready events
	    Set<SelectionKey> readyKeys = selector.selectedKeys();

	    // create an iterator for the set
	    Iterator<SelectionKey> iterator = readyKeys.iterator();
			
	    // iterate over all events
	    while (iterator.hasNext()) {
        
		SelectionKey key = (SelectionKey) iterator.next();
		iterator.remove();

		try {
		    if (key.isAcceptable()) { // a new connection is ready to be accepted
			IAcceptHandler aH = (IAcceptHandler)key.attachment();
			aH.handleAccept(key);
		    } // end of isAcceptable

		    if ( key.isReadable() || key.isWritable() ) {
			IReadWriteHandler rwH = (IReadWriteHandler)key.attachment();

			if (key.isReadable()) {
			    rwH.handleRead(key);
			} // end of if isReadable

			if (key.isWritable()) {
			    rwH.handleWrite(key);
			} // end of if isWritable
		    } // end of readwrite
		}
		catch (IOException ex) {
		    Debug.DEBUG("Exception when handling key " + key);
		    key.cancel();
		    try {
			key.channel().close(); 
			// in a more general design, call have a handleException
		    }
		    catch (IOException cex) {}
		} // end of catch
				
	    } // end of while (iterator.hasNext()) {

	} // end of while (true)
    } // end of run
}