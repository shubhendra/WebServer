
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


class WebRequestHandler {

	HashMap<String, String> requestHeaders;
	String WWW_ROOT;
	String ServerName;
	Socket connSocket;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	public final static String RFC1123_PATTERN = "EEE, dd MMM yyyyy HH:mm:ss z";
	String urlName;
	String fileName;
	File fileInfo;
	byte[] cachedFile;
	boolean mobile = false;
	Calendar modifiedDate;
	// private static Map<String, byte[]> CacheMap = new HashMap<String,
	// byte[]>();
	boolean fileInCache = false;
	boolean flagCGI = false;
	boolean flagLoad = false;

	public WebRequestHandler(Socket connectionSocket, String WWW_ROOT, String s)
			throws Exception {
		this.WWW_ROOT = WWW_ROOT;
		this.connSocket = connectionSocket;
		this.ServerName = s;

		inFromClient = new BufferedReader(new InputStreamReader(
				connSocket.getInputStream()));

		// inFromClient = new BufferedReader(new InputStreamReader(
		// connSocket.getInputStream()));

		outToClient = new DataOutputStream(connSocket.getOutputStream());

		modifiedDate = new GregorianCalendar();
	}

	public void processRequest() {

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
			// inFromClient.close();

		} catch (Exception e) {
			e.printStackTrace();
			outputError(400, "Server error");
		}
		try {
			outToClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	} // end of processARequest

	private void getRequestHeaders(InputStream is) throws IOException {
		requestHeaders = new HashMap<String, String>();
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
				is));
		StringBuffer sb = new StringBuffer();
		do {
			String line = inFromServer.readLine();
			if (line != null && line.contains(" ")) {
				StringTokenizer tk = new StringTokenizer(line);
				String method = tk.nextToken();
				String value = line.substring(method.length() + 1).trim();
				Debug.DEBUG(method + " " + value);
				requestHeaders.put(method, value);
			}
			sb.append(line + "\r\n");
		} while (!sb.subSequence(sb.length() - 4, sb.length()).equals(
				"\r\n\r\n"));

		if (!inFromServer.ready()) {
			Debug.DEBUG("End of Stream");
		}
		// System.out.print(sb.toString());
		// return;
	}

	private String getRequestMethod() {
		if (requestHeaders.containsKey("GET")) {
			return "GET";
		} else if (requestHeaders.containsKey("POST")) {
			return "POST";
		}
		return null;
	}

	private String getURLName() {
		String value = requestHeaders.get(getRequestMethod());
		if (value != null) {
			String[] split = value.split("\\s+");
			if (split.length != 2)
				return null;
			return split[0];
		}
		return null;
	}

	private String getHTTPVersion() {
		String value = requestHeaders.get(getRequestMethod());
		if (value != null) {
			String[] split = value.split("\\s+");
			if (split.length != 2)
				return null;
			return split[1];
		}
		return null;
	}

	private String getHeader(String header) {
		return requestHeaders.get(header);
	}

	private boolean haveHeader(String header) {
		if (requestHeaders.containsKey(header))
			return true;
		else
			return false;
	}

	private void mapURL2File() throws Exception {

		getRequestHeaders(connSocket.getInputStream());

		Debug.DEBUG("request line: " + getRequestMethod() + " "
				+ getHeader("GET"));

		Debug.DEBUG("Inside map2URL: " + getURLName());

		if (haveHeader("User-Agent:")) {
			if (getHeader("User-Agent:").toLowerCase().contains("iphone")
					|| getHeader("User-Agent:").toLowerCase().contains(
							"android")) {
				mobile = true;
				Debug.DEBUG(getHeader("User-Agent:"));
			} else {
				mobile = false;
			}
		} else {
			mobile = false;
		}
		if (haveHeader("If-Modified-Since:")) {
			Date thedate = new SimpleDateFormat(RFC1123_PATTERN, Locale.ENGLISH)
					.parse(getHeader("If-Modified-Since:").trim());
			modifiedDate.setTime(thedate);
		} else {
			modifiedDate = null;

		}

		Debug.DEBUG("URL name: " + getURLName());
		urlName = getURLName();

		if (urlName.equals("/") && mobile)
			urlName = "index_m.html";
		else if (urlName.equals("/"))
			urlName = "index.html";
		else if (urlName.equals("/load")) {
			// special processing for load balancer algorithm plugin
			Load l = new Load(WWW_ROOT);
			byte[] output = l.load();
			if (output == null) {
				outputError(404, "Page Not Found;");
				Debug.DEBUG("Null");
				fileInfo = null;
				fileInCache = false;
				return;
			} else {
				flagLoad = true;

				if (loadWriteResponse(output)) {
					fileInfo = null;
					fileInCache = false;
					Debug.DEBUG("Returning now");
					return;
				} else {
					fileInfo = null;
					fileInCache = false;
					outputError(404, "Page Not Found;");
					return;
				}
			}
		} else if (urlName.contains(".cgi?")) {
			String cgiFile = urlName.substring(urlName.lastIndexOf("/") + 1,
					urlName.indexOf(".cgi"));
			flagCGI = runCGI(urlName.substring(urlName.indexOf("?") + 1),
					cgiFile);
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
			Debug.DEBUG("Inside synchronized: " + fileInCache);
			if (fileInCache) {
				cachedFile = WebServer.cache.getFile(fileName);
				Debug.DEBUG(cachedFile.length);
			}
		}

		Debug.DEBUG("Before fileNotFound: " + fileInCache);

		Calendar checkCalendar = new GregorianCalendar();
		fileInfo = new File(fileName);
		if (!fileInfo.isFile()) {
			outputError(404, "Not Found");
			fileInfo = null;
			fileInCache = false;
			Debug.DEBUG("Inside fileNotFound: " + fileInCache);
			return;
		}
		Debug.DEBUG("After fileNotFound: " + fileInCache);
		checkCalendar.setTimeInMillis(fileInfo.lastModified());
		// Debug.DEBUG("File Modification Date:"+checkCalendar.toString());
		if (modifiedDate != null) {
			if (checkCalendar.compareTo(modifiedDate) > 0) {
				fileInCache = false;
				Debug.DEBUG("Inside compareTo: " + fileInCache);
			} else {
				outputError(304, "Umodified");
				fileInfo = null;
				return;
			}
		}
		Debug.DEBUG("In map2URL: fileInfo " + fileInfo.getAbsolutePath());
		Debug.DEBUG("In map2URL: fileCache " + fileInCache);
		// }
	} // end mapURL2file

	private void outputResponseHeader() throws Exception {
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat(RFC1123_PATTERN);
		outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n");
		outToClient.writeBytes("Date: " + dateFormat.format(cal.getTime())
				+ "\r\n");
		outToClient.writeBytes("Server: "+ServerName+"\r\n");
		if (urlName.endsWith(".jpg"))
			outToClient.writeBytes("Content-Type: image/jpeg\r\n");
		else if (urlName.endsWith(".gif"))
			outToClient.writeBytes("Content-Type: image/gif\r\n");
		else if (urlName.endsWith(".html") || urlName.endsWith(".htm"))
			outToClient.writeBytes("Content-Type: text/html\r\n");
		else if (urlName.contains(".cgi?"))
			outToClient.writeBytes("Content-Type: text/html\r\n");
		else if (urlName.contains("/load"))
			outToClient.writeBytes("Content-Type: text/html\r\n");
		else
			outToClient.writeBytes("Content-Type: text/plain\r\n");
		if (!urlName.contains(".cgi?") && flagLoad != true)
			outToClient.writeBytes("Last-Modified: "
					+ dateFormat.format(fileInfo.lastModified()) + "\r\n");
		//outToClient.flush();
	}

	private void outputResponseBody() {

		// send file content
		if (fileInCache == true) {
			try {
				outToClient.writeBytes("Content-Length: " + cachedFile.length
						+ "\r\n");
				outToClient.writeBytes("\r\n");
				outToClient.write(cachedFile, 0, cachedFile.length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Debug.DEBUG("File retrieved from cache");
			//outToClient.flush();

		} else {
			int numOfBytes = (int) fileInfo.length();
			try {
				outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");

				outToClient.writeBytes("\r\n");
				FileInputStream fileStream = new FileInputStream(fileName);
				Debug.DEBUG(" " + fileName);
				byte[] fileInBytes = new byte[numOfBytes];
				fileStream.read(fileInBytes);
				StringBuffer sb = new StringBuffer();
				
				Debug.DEBUG(fileInBytes.toString());
				fileStream.close();
				outToClient.write(fileInBytes, 0, numOfBytes);
				outToClient.flush();
				synchronized (WebServer.cache) {
					if (WebServer.cache.spaceLeft() > fileInBytes.length) {
						WebServer.cache.addFile(fileName, fileInBytes);
						Debug.DEBUG("File Added to Cache");
						Debug.DEBUG("Space Left in Cache: "
								+ WebServer.cache.spaceLeft());

					} else {
						Debug.DEBUG("File that cannot be added to cache has size: "
										+ fileInBytes.length);
						Debug.DEBUG("Space Left in Cache: "
								+ WebServer.cache.spaceLeft());
						Debug.DEBUG("File cannot be added to Cache. Cache is full");
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
		}

	}

	void outputError(int errCode, String errMsg) {
		try {
			outToClient.writeBytes("HTTP/1.0 " + errCode + " " + errMsg
					+ "\r\n\r\n");
			//outToClient.flush();
		} catch (Exception e) {
		}
		
	}

	private boolean runCGI(String query, String cgiFile) {
		ProcessBuilder pb = new ProcessBuilder("perl", cgiFile + ".pl");
		pb.environment().put("QUERY_STRING", query);
		pb.environment().put("REQUEST_METHOD", getRequestMethod());
		pb.environment().put("SERVER_NAME", ServerName);
		pb.environment().put("SERVER_ADMIN", "shubhendra.agrawal@yale.edu");
		pb.environment().put("SERVER_PORT", Integer.toString(WebServer.getPortNumber()));
		pb.environment().put("SERVER_SOFTWARE", "Synchronous");
		pb.environment().put("REMOTE_ADDR", connSocket.getRemoteSocketAddress().toString());
		pb.environment().put("REMOTE_HOST", connSocket.getRemoteSocketAddress().toString());
		pb.environment().put("REMOTE_PORT", Integer.toString(connSocket.getLocalPort()));
		pb.environment().put("REMOTE_USER", connSocket.getRemoteSocketAddress().toString());
		pb.directory(new File(WWW_ROOT + "/cgi/"));
		try {
			Process p = pb.start();
			String output = getOutput(p.getInputStream(), true);
			int numOfBytes = (int) output.getBytes().length;
			outputResponseHeader();
			outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
			outToClient.writeBytes("\r\n");
			outToClient.write(output.getBytes("US-ASCII"), 0, numOfBytes);
			outToClient.flush();
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
				if (print) {
					Debug.DEBUG(s);
				}
			}
			// is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
		return output;
	}

	private boolean loadWriteResponse(byte[] output) {
		try {
			Debug.DEBUG("InsideLoadWriteResponse");
			outputResponseHeader();
			int numOfBytes = (int) output.length;
			outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
			outToClient.writeBytes("\r\n");
			Debug.DEBUG("InsideLoadWriteResponse");
			Debug.DEBUG(output.toString());
			outToClient.write(output, 0, numOfBytes);
			fileInfo = null;
			fileInCache = false;
			outToClient.flush();
			return true;
		} catch (Exception e) {
			return false;
		}

	}
}
