import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class RequestSender implements Runnable {

	private long[] stats = { 0, 0, 0, 0, 0 };
	private HashMap<String, String> Headers;
	InetAddress serverIPAddress;
	int serverPort;
	private Socket clientSocket;
	ArrayList<String> Files;
	public long[] startTime = { 0 };
	public long waitTime = 0;
	public long[] firstByteTime = { 0 };
	public long transferTime = 0;
	public int numFiles = 0;
	public long numBytes = 0;

	private volatile boolean running = true;

	public RequestSender(InetAddress address, int port,
			ArrayList<String> request) throws IOException {
		this.serverIPAddress = address;
		this.serverPort = port;
		this.Files = new ArrayList<String>();
		startTime = new long[request.size()];
		firstByteTime = new long[request.size()];
		for (int i = 0; i < request.size(); i++)
			Files.add(request.get(i));
	}

	//terminate the thread 
	public void terminate() {
		running = false;
	}

	private void getResponseHeader(BufferedReader inFromServer) throws IOException {
		Headers = new HashMap<String, String>();
		
		StringBuffer sb = new StringBuffer();
		do {
			String line = inFromServer.readLine();
			if (line.contains(" ")) {
				StringTokenizer tk = new StringTokenizer(line);
				String method = tk.nextToken();
				String value = line.substring(method.length() + 1).trim();
				Debug.DEBUG(method+" "+value);
				Headers.put(method, value);

			}
//			if (line!=null){ 
				sb.append(line + "\r\n");
//			}
		} while (!sb.subSequence(sb.length() - 4, sb.length()).equals(
				"\r\n\r\n"));
	}

	private String getResponseCode() {
		if (Headers.containsKey("HTTP/1.0")) {
			return Headers.get("HTTP/1.0").substring(0, 3);
		} else if (Headers.containsKey("HTTP/1.1")) {
			return Headers.get("HTTP/1.1").substring(0, 3);
		}
		return null;
	}

	public byte[] getResponseBodySynchronous(InputStream is) {
		DataInputStream inFromServer = new DataInputStream(
				new BufferedInputStream(is));
		
		//BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
			//	is));
		//StringBuffer sb = new StringBuffer();
		if (Headers.get("Content-Length:") == null) {
			return null;
		}
		int fileSize = Integer.parseInt(Headers.get("Content-Length:"));
		byte[] content = new byte[fileSize];
		char[] contentC = new char[fileSize];
		int test;
		/*for (int i = 0; i < fileSize; i++){
			try {
				test = inFromServer.read(contentC);
				if (test == -1){
					Debug.DEBUG("Value of I: "+ i);
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		try {
			Debug.DEBUG("While reading body Available bytes: "
					+ inFromServer.available());
			if (clientSocket.isClosed()) {
				Debug.DEBUG("Socket is closed");
			}
			inFromServer.readFully(content);
			Debug.DEBUG("Response body Read correctly");
		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
			Debug.DEBUG("The Response Body could not be read correctly");
			return null;
		}

		return content;

	}
	public byte[] getResponseBodyAsynchronous(InputStream is) throws IOException {
		
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
				is));
		StringBuffer sb = new StringBuffer();
		if (Headers.get("Content-Length:") == null) {
			return null;
		}
		int fileSize = Integer.parseInt(Headers.get("Content-Length:"));
		String line;
		do {
			line = inFromServer.readLine();
			sb.append(line + "\r\n");

		} while (line != null);
		
		if(inFromServer.ready()){
			Debug.DEBUG("There is more to be read");
		}
		return sb.toString().getBytes();
		
	}
	public byte[] getResponseBodyAsynchronous(BufferedReader inFromServer) throws IOException {
		

		Debug.DEBUG("inside Asynchronous read");
		StringBuffer sb = new StringBuffer();
		if (Headers.get("Content-Length:") == null) {
			return null;
		}
		int fileSize = Integer.parseInt(Headers.get("Content-Length:"));
		Debug.DEBUG("fileSize " +fileSize);
		String line;
		do {
			line = inFromServer.readLine();
			//Debug.DEBUG(line);
			sb.append(line + "\r\n");
	
			
		} while (line != null);
		Debug.DEBUG("exited from loop");
		
		if(inFromServer.ready()){
			Debug.DEBUG("There is more to be read");
		} else 
			Debug.DEBUG("exiting");
		return sb.toString().getBytes();
		
	}


	public void sendSimpleRequest(String file) throws IOException {
		DataOutputStream outToServer = new DataOutputStream(new BufferedOutputStream
				(clientSocket.getOutputStream()));
		String request = "GET /" + file + " HTTP/1.0\r\n\r\n";
		Debug.DEBUG(this.toString() + "Request Sent: " + request);
		outToServer.writeBytes(request);
		outToServer.flush();
		// outToServer.close();
	}
	//classes/cs433/web/www-root/html-small/

	public void sendSimpleDelayedRequest(String file) throws Exception {
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		String request = "GET /" + file + " HTTP/1.1\r\n\r\n";
		Debug.DEBUG(this.toString() + "Request Sent: " + request);
		outToServer.writeBytes(request.substring(0, 1));
		Thread.currentThread();
		Debug.DEBUG("Thread is going to sleep");
		Thread.sleep(3001);
		Debug.DEBUG("Thread is waking up");
		outToServer.writeBytes(request.substring(1));
		// outToServer.close();
	}

	public void sendLoadRequest(String file) throws IOException {
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		String request = "GET /load HTTP/1.1\r\n\r\n";
		Debug.DEBUG(this.toString() + "Request Sent: " + request);
		outToServer.writeBytes(request);
		// outToServer.close();
	}

	public void sendUserAgentRequest(String file) throws IOException {
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		String request = "GET /" + file
				+ " HTTP/1.1\r\nUser-Agent: iPhone\r\n\r\n";
		Debug.DEBUG(this.toString() + "Request Sent: " + request);
		outToServer.writeBytes(request);
		// outToServer.close();
	}

	public void sendIMSRequest(String file) throws IOException {
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		String request = "GET /"
				+ file
				+ " HTTP/1.0\r\nIf-Modified-Since: Tue, 22 Oct 02013 22:48:14 EST\r\n\r\n";
		Debug.DEBUG(this.toString() + "Request Sent: " + request);
		outToServer.writeBytes(request);
		// outToServer.close();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {

			Debug.DEBUG("Files Size: " + Files.size());
			Debug.DEBUG("Running " + running);
			for (int i = 0; i < Files.size() && running; i++) {
				clientSocket = new Socket(serverIPAddress, serverPort);
				sendUserAgentRequest(Files.get(i));
				// clientSocket.shutdownOutput();
				// sendUserAgentRequest(Files.get(i));
				// sendIMSRequest(Files.get(i));
				startTime[i] = System.currentTimeMillis();
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
			
				getResponseHeader(inFromServer);
				firstByteTime[i] = System.currentTimeMillis();
				waitTime = firstByteTime[i] - startTime[i];
				Debug.DEBUG("Response Code: " + getResponseCode());
				int fileSize = 0;
				if (getResponseCode().startsWith("2")
						&& getResponseCode() != null) {
					byte[] content = getResponseBodyAsynchronous(inFromServer);
					if (content == null) {
						fileSize = 0;
						transferTime = 0;
						numFiles = 0;
					} else {
						Debug.DEBUG("content.length ="+ content.length);
						fileSize = Integer.parseInt(Headers
								.get("Content-Length:"));
						transferTime = System.currentTimeMillis()
								- firstByteTime[i];
						numFiles = 1;
					}	
				} else {
					fileSize = 0;
					transferTime = 0;
					numFiles = 0;
				}



				
				Debug.DEBUG("Files Size: " + Files.size());
				Debug.DEBUG("Running " + running);
				Debug.DEBUG("Closing client socket");

				clientSocket.close();
				synchronized (SimpleClient.stats) {
					Debug.DEBUG("Stuck inside the lock");
					SimpleClient.stats[0] += fileSize;
					SimpleClient.stats[1] += waitTime;
					SimpleClient.stats[2] += transferTime;
					SimpleClient.stats[3] += numFiles;
					SimpleClient.stats[4] += 1;
				
				}
			}
/*
			synchronized (SimpleClient.stats) {
				Debug.DEBUG("Stuck inside the lock");
				SimpleClient.stats[0] += stats[0];
				SimpleClient.stats[1] += stats[1];
				SimpleClient.stats[2] += stats[2];
				SimpleClient.stats[3] += stats[3];
				SimpleClient.stats[4] += stats[4];
				Debug.DEBUG("In Thread: stats 0,filesize: "
						+ SimpleClient.stats[0]);
				Debug.DEBUG("In Thread: stats 1,waitTime: "
						+ SimpleClient.stats[1]);
				Debug.DEBUG("In Thread: stats 2,transferTime: "
						+ SimpleClient.stats[2]);
				Debug.DEBUG("In Thread: stats 3,numFiles "
						+ SimpleClient.stats[3]);
				Debug.DEBUG("In Thread: stats 4,numRequests "
						+ SimpleClient.stats[4]);

			}*/

		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}

	}

}
