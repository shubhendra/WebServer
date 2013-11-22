


import java.nio.*;
import java.nio.channels.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class WebLineReadWriteHandler implements IReadWriteHandler {
	
    private ByteBuffer inBuffer;
    private ByteBuffer outBuffer;

    private Dispatcher dispatcher;
    private SocketChannel client;

    private boolean requestComplete;
    private boolean responseReady;
    private boolean responseSent;
    private boolean channelClosed;

    
    private StringBuffer request;
    
    HashMap<String,String> requestHeaders;
	String WWW_ROOT;
	public final static String RFC1123_PATTERN = "EEE, dd MMM yyyyy HH:mm:ss z";
	String urlName;
	String fileName;
	File fileInfo;
	byte[] cachedFile;
	boolean mobile = false;
	Calendar modifiedDate;
	//private static Map<String, byte[]> CacheMap = new HashMap<String, byte[]>();
	boolean fileInCache = false;
	boolean flagCGI = false;
	boolean flagLoad = false;

    //private enum State { 
    //	READ_REQUEST, REQUEST_COMPLETE, GENERATING_RESPONSE, RESPONSE_READY, RESPONSE_SENT
    //}
    //private State state;

    public WebLineReadWriteHandler(Dispatcher dispatcher, SocketChannel client, String D) {
	this.WWW_ROOT = D;
	inBuffer = ByteBuffer.allocate(4096);
	outBuffer = ByteBuffer.allocate(4096);

	this.dispatcher = dispatcher;
	this.client = client;

	// initial state
	requestComplete = false;
	responseReady = false;
	responseSent = false;
	channelClosed = false;

	request = new StringBuffer();
	modifiedDate = new GregorianCalendar();
    }

    public int getInitOps() {
	return SelectionKey.OP_READ;
    }

    public void handleException() {
    }
    
    public boolean getRequestCompleteState(){
    	return requestComplete;
    }

    public void handleRead(SelectionKey key) throws IOException {



    	// assert: t
    	// a connection is ready to be read
    	Debug.DEBUG("->handleRead");

    	if (requestComplete) { // this call should not happen, ignore
    	    return;
    	}
     	int readBytes = client.read(inBuffer);
     	if (readBytes == -1) {
            // End of stream. Closing channel...
            requestComplete = true;
    	    Debug.DEBUG("handleRead: readBytes == -1");
          }
          // Nothing else to be read?
     	else if (readBytes == 0) {        
            // There was nothing to read. Shouldn't happen often, but 
            // it is not an error, we can deal with it. Ignore this event
            // and reactivate reading.
            //reactivateReading();
            return;
          }

         
          // There is some data in the buffer. Processes it.
          int position = inBuffer.position();
          Debug.DEBUG("Number of bytes in inBuffer: "+ inBuffer.position());
          inBuffer.flip();
          byte[] ch = new byte[position];
          inBuffer.get(ch);
          String str = new String(ch);
          request.append(str);
          
          Debug.DEBUG("Request String:" + request.toString());
          
          if (request.length()>4 && request.subSequence(request.length()-4, request.length()).equals("\r\n\r\n")){
        	  requestComplete = true;
  		    Debug.DEBUG("handleRead: find terminating chars");
          }
          
          inBuffer.clear(); // we do not keep things in the inBuffer

          if (requestComplete) {
      	    generateResponse();
          }
    	
          updateDispatcher();
          
      	Debug.DEBUG("handleRead->");
        
 
    } // end of handleRead

    private void updateDispatcher() throws IOException {

	Debug.DEBUG("->Update dispatcher.");

	if (channelClosed) 
	    return;
		
	// get registration key; as an optimization, may save it locally
	SelectionKey sk = dispatcher.keyFor(client);

	if (responseSent) {
	    Debug.DEBUG("***Response sent; connection closed");
	    dispatcher.deregisterSelection(sk);
	    client.close();
	    channelClosed = true;
	    return;
	}

	int nextState = 0; //sk.interestOps();
	if (requestComplete) {
	    nextState = nextState & ~SelectionKey.OP_READ;
	    Debug.DEBUG("New state: -Read since request parsed complete");
	} else {
	    nextState = nextState | SelectionKey.OP_READ;
	    Debug.DEBUG("New state: +Read to continue to read");
	}

	if (responseReady) {
	    nextState = SelectionKey.OP_WRITE;
	    Debug.DEBUG("New state: +Write since response ready but not done sent");
	} 

	dispatcher.updateInterests(sk, nextState);
    }
	
    public void handleWrite(SelectionKey key) throws IOException {
	Debug.DEBUG("->handleWrite");
	
	// process data
	//SocketChannel client = (SocketChannel) key.channel();
	Debug.DEBUG("handleWrite: Write data to connection " + client  
		    + "; from buffer " + outBuffer);
	int writeBytes = client.write(outBuffer);
	Debug.DEBUG("handleWrite: after write " + outBuffer);
	Debug.DEBUG("outbuffer remaining: "+ outBuffer.remaining());
	if ( responseReady && (outBuffer.remaining() == 0) ) {
	    outBuffer.clear();
		responseSent = true;
	}

	// update state
	updateDispatcher();
			
	//try {Thread.sleep(5000);} catch (InterruptedException e) {}
	Debug.DEBUG("handleWrite->");
    } // end of handleWrite


    private void generateResponse() {

    	try {
			mapURL2File();

			if (fileInfo != null) // found the file and
															// knows its info
			{
				Debug.DEBUG("Outputting Response Header");
				outputResponseHeader();
				Debug.DEBUG("Outputting Response Body");
				outputResponseBody();
			}
			//inFromClient.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			outputError(400, "PR:Server error");
		} finally{
			Debug.DEBUG("before Outbuffer.flip");
			Debug.DEBUG(outBuffer.position());
			Debug.DEBUG(outBuffer.limit());
			outBuffer.flip(); 
			responseReady = true;
			Debug.DEBUG("Outbuffer.flip");
			Debug.DEBUG(outBuffer.position());
			Debug.DEBUG(outBuffer.limit());
		}

    } // end of generate response
    
    private void getRequestHeaders() throws IOException{
		requestHeaders = new HashMap<String,String>();
		String[] lines = request.toString().split("\\r+\\n");
		Debug.DEBUG("Request Split.length: " + lines.length + lines[0]);
		for(int i = 0; i< (lines.length); i++ ){
			Debug.DEBUG("Lines " + i + ": " + lines[i]);
			if(lines[i]!=null && lines[i].contains(" ")){
				StringTokenizer tk = new StringTokenizer(lines[i]);
				String method = tk.nextToken();
				String value = lines[i].substring(method.length()+1).trim();
				Debug.DEBUG(method+" "+value);
				requestHeaders.put(method, value);
			}
		}
		
	
	}
    

	private String getRequestMethod(){
		if(requestHeaders.containsKey("GET")){
			return "GET";
		}else if (requestHeaders.containsKey("POST")){
			return "POST";
		}
			return null;
	}
	
	private String getURLName(){
		String value = requestHeaders.get(getRequestMethod());
		if (value !=null) {
			String[] split = value.split("\\s+");
			if (split.length!=2)
				return null;
			return split[0];
		}
		return null;
	}
	private String getHTTPVersion(){
		String value = requestHeaders.get(getRequestMethod());
		if (value!=null){
			String[] split = value.split("\\s+");
			if (split.length!=2)
				return null;
			return split[1];
		}
		return null;
	}
	
	private String getHeader( String header){
		return requestHeaders.get(header);
	}
	
	private void mapURL2File() throws Exception {

		getRequestHeaders();

		Debug.DEBUG("request line: " + getRequestMethod() + " "+ getHeader("GET") );

		// process the request

		Debug.DEBUG("Inside map2URL: "+getURLName());
		
		//Debug.DEBUG("Modified Date: "+modifiedDate.toString());
			if (haveHeader("User-Agent:")) {
				if (getHeader("User-Agent:").toLowerCase().contains("iphone")
						|| getHeader("User-Agent:").toLowerCase().contains("android")) {
					mobile = true;
					Debug.DEBUG(getHeader("User-Agent:"));
				} else {
					mobile = false;
				}
			} else {
				mobile = false;
			}
			if (haveHeader("If-Modified-Since:")) {
				Date thedate = new SimpleDateFormat(RFC1123_PATTERN,
						Locale.ENGLISH).parse(getHeader("If-Modified-Since:").trim());
				modifiedDate.setTime(thedate);
			} else {
				modifiedDate = null;
				//modifiedDate.setTime(new SimpleDateFormat("MMM/dd/yyyy",
			//			Locale.ENGLISH).parse("JAN/01/1970"));
				
			}

		
		// parse URL to retrieve file name
		Debug.DEBUG("URL name: "+getURLName());
		urlName = getURLName();

		if (urlName.equals("/") && mobile)
			urlName = "index_m.html";
		else if (urlName.equals("/"))
			urlName = "index.html";
		else if (urlName.equals("/load")){
			Load l = new Load(WWW_ROOT);
			byte[] output = l.load();
			if (output == null) {
				outputError(404, "Page Not Found;");
				Debug.DEBUG("Null");
				fileInfo = null;
				fileInCache = false;
				return;
			} else {

				if (loadWriteResponse(output)) {
					return;
				} else {
					fileInfo = null;
					fileInCache = false;
					outputError(404, "Page Not Found;");
					return;
				}
			}
		}
		else if (urlName.contains(".cgi?")) {
			String cgiFile = urlName.substring(urlName.lastIndexOf("/")+1,urlName.indexOf(".cgi"));
			flagCGI = runCGI(urlName.substring(urlName.indexOf("?")+1), cgiFile);
			return;
		} else if (urlName.startsWith("/") == true)
			urlName = urlName.substring(1);

		fileName = WWW_ROOT + urlName;
		if(urlName.equals("index_m.html")) {
			fileInfo = new File(fileName);
			if (!fileInfo.isFile()) {
				fileName = WWW_ROOT+"index.html";
			}
		}

		Debug.DEBUG("Map to File name: " + fileName);

		synchronized (WebServer.cache) {
				fileInCache = WebServer.cache.findFile(fileName);
				Debug.DEBUG("Inside synchronized: "+ fileInCache);
				if (fileInCache) {
					cachedFile = WebServer.cache.getFile(fileName);
				}
		}

		Debug.DEBUG("Before fileNotFound: "+ fileInCache);
		//if (fileInCache = false) {
		//Debug.DEBUG("After checking cache " );
		Calendar checkCalendar = new GregorianCalendar();
		fileInfo = new File(fileName);
		if (!fileInfo.isFile()) {
			outputError(404, "Not Found");
			fileInfo = null;
			fileInCache = false;
			Debug.DEBUG("Inside fileNotFound: "+ fileInCache);
			return;
		}
		Debug.DEBUG("After fileNotFound: "+ fileInCache);
		checkCalendar.setTimeInMillis(fileInfo.lastModified());
		//Debug.DEBUG("File Modification Date:"+checkCalendar.toString());
		if (modifiedDate!=null){
			if (checkCalendar.compareTo(modifiedDate)>0){
				fileInCache = false;
				Debug.DEBUG("Inside compareTo: "+ fileInCache);
			} else {
				outputError(304, "Umodified");
				fileInfo = null;
				return;
			}
		}
		Debug.DEBUG("In map2URL: fileInfo " + fileInfo.getAbsolutePath());
		Debug.DEBUG("In map2URL: fileCache " + fileInCache);
		//}
	}
	
	private boolean haveHeader(String header){
		if (requestHeaders.containsKey(header))
			return true;
		else return false;
	}
	
	private void outputResponseHeader() throws Exception {
		if(fileInCache){
			outBuffer = ByteBuffer.allocate(cachedFile.length + 500);
		} else
			outBuffer = ByteBuffer.allocate((int)fileInfo.length() + 500);
		Debug.DEBUG("Capacity of outbuffer"+outBuffer.capacity());
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat(RFC1123_PATTERN);
		StringBuffer sb = new StringBuffer();
		
		sb.append("HTTP/1.0 200 Document Follows\r\n");
		sb.append("Date: " + dateFormat.format(cal.getTime())
				+ "\r\n");
		sb.append("Server: localhost\r\n");
		if (urlName.endsWith(".jpg"))
			sb.append("Content-Type: image/jpeg\r\n");
		else if (urlName.endsWith(".gif"))
			sb.append("Content-Type: image/gif\r\n");
		else if (urlName.endsWith(".html") || urlName.endsWith(".htm"))
			sb.append("Content-Type: text/html\r\n");
		else if (urlName.contains(".cgi?"))
			sb.append("Content-Type: text/html\r\n");
		else
			sb.append("Content-Type: text/plain\r\n");
		if (!urlName.contains(".cgi?"))
			sb.append("Last-Modified: "
				+ dateFormat.format(fileInfo.lastModified())+"\r\n");
		
		outBuffer.put(sb.toString().getBytes());
	}

	private void outputResponseBody() throws Exception {

		StringBuffer sb = new StringBuffer();
		// send file content
		if (fileInCache == true) {
			sb.append("Content-Length: " + cachedFile.length + "\r\n");
			sb.append("\r\n");
			outBuffer.put(sb.toString().getBytes());
			outBuffer.put(cachedFile, 0, cachedFile.length);
			Debug.DEBUG("File retrieved from cache");		
		}
		else {
			int numOfBytes = (int) fileInfo.length();
			sb.append("Content-Length: " + numOfBytes + "\r\n");
			sb.append("\r\n");
			outBuffer.put(sb.toString().getBytes());
			Debug.DEBUG("Filename: "+fileName);
			FileInputStream fin = new FileInputStream(fileName);
			FileChannel in = fin.getChannel();
			ByteBuffer input = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
			Debug.DEBUG("Size from in.size()"+ in.size());
			byte[] file = new byte[(int) in.size()];
			Debug.DEBUG("File[] size"+file.length);
			input.get(file);
			Debug.DEBUG("File Size: " + in.size());
			
			synchronized(WebServer.cache) {
				if (WebServer.cache.spaceLeft()> in.size()) {
					WebServer.cache.addFile(fileName, file);
					Debug.DEBUG("File Added to Cache");
					Debug.DEBUG("Space Left in Cache: "+WebServer.cache.spaceLeft());
					
				} else {
					Debug.DEBUG("File that cannot be added to cache has size: "+in.size());
				    Debug.DEBUG("Space Left in Cache: "+WebServer.cache.spaceLeft());
					Debug.DEBUG("File cannot be added to Cache. Cache is full");
				}
			}
			outBuffer.put(file);	
			in.close();
			fin.close();
			
		}
	}

	void outputError(int errCode, String errMsg) {
		try {
			StringBuffer sb = new StringBuffer(); 
			sb.append("HTTP/1.0 " + errCode + " " + errMsg
					+ "\r\n\r\n");
			outBuffer.put(sb.toString().getBytes());	
		} catch (Exception e) {
		}
	}
	
	private void outputHTMLResponseHeader() throws Exception {
		
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat(RFC1123_PATTERN);
		StringBuffer sb = new StringBuffer();
		
		sb.append("HTTP/1.0 200 Document Follows\r\n");
		sb.append("Date: " + dateFormat.format(cal.getTime())
				+ "\r\n");
		sb.append("Server: localhost\r\n");
		sb.append("Content-Type: text/html\r\n");
		outBuffer.put(sb.toString().getBytes());
	}
	private boolean runCGI(String query, String cgiFile) throws IOException{
		ProcessBuilder pb = new ProcessBuilder("perl", cgiFile+".pl");
		pb.environment().put("QUERY_STRING", query);
		pb.environment().put("REQUEST_METHOD", getRequestMethod());
		pb.environment().put("SERVER_NAME", "test.com");
		pb.environment().put("SERVER_ADMIN", "shubhendra.agrawal@yale.edu");
		pb.environment().put("SERVER_PORT", Integer.toString(WebServer.getPortNumber()));
		pb.environment().put("SERVER_SOFTWARE", "Asynchronous");
		pb.environment().put("REMOTE_ADDR", client.getRemoteAddress().toString());
		pb.environment().put("REMOTE_HOST", client.getRemoteAddress().toString());
		pb.environment().put("REMOTE_PORT", Integer.toString(client.socket().getLocalPort()));
		pb.environment().put("REMOTE_USER", client.socket().getRemoteSocketAddress().toString());

	    pb.directory(new File(WWW_ROOT+"/cgi/"));
	    try {
	        Process p = pb.start();
	        String output = getOutput(p.getInputStream(), true);
	        int numOfBytes = (int) output.getBytes().length;
	        outBuffer = ByteBuffer.allocate(numOfBytes+500);
	        outputHTMLResponseHeader();
	        StringBuffer sb = new StringBuffer();
			sb.append("Content-Length: " + numOfBytes + "\r\n");
			sb.append("\r\n");
			outBuffer.put(sb.toString().getBytes());
			outBuffer.put(output.getBytes("US-ASCII"), 0, numOfBytes);
			try {
		            int exitValue = p.waitFor();
		            Debug.DEBUG("\n\nExit Value is " + exitValue);
		            return true;
		        } catch (InterruptedException e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		        	outputError(400, "Server Error");
		            return false;
		        }
			
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	    	outputError(400, "Server Error");
	        return false;
	    }
	}
	private String getOutput(InputStream is, boolean print) {
	    String output = "";
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    String s = null;
	    try {
	        while ((s = reader.readLine()) != null) {
	            output += s;
	            if(print){
	                Debug.DEBUG(s);
	            }
	        }
	        is.close();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        //e.printStackTrace();
	        return null;
	    }
	    return output;
	}

	private boolean loadWriteResponse(byte[] output) {
		try {
			
	        int numOfBytes = output.length;
	        outBuffer = ByteBuffer.allocate(numOfBytes+500);
	        outputHTMLResponseHeader();
		    StringBuffer sb = new StringBuffer();
			sb.append("Content-Length: " + numOfBytes + "\r\n");
			sb.append("\r\n");
			outBuffer.put(sb.toString().getBytes());
			Debug.DEBUG(output.toString());
			outBuffer.put(output, 0, numOfBytes);
			fileInfo = null;
			fileInCache = false;
			return true;
		} catch (Exception e) {
			return false;
		}

	}
}
