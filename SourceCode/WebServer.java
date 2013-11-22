import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebServer {

	protected static int PortNumber = 80;
	protected static int CacheSize = 50000;
	protected static String DocumentRoot = "C:\\Test\\";
	protected static int ThreadPoolSize = 10;
	protected static int IncompleteTimeout = 3000;
	protected static String ServerName = "localhost";
	public static Cache cache;
	public static Cache cacheByteBuffer;

	public static int getPortNumber() {
		return PortNumber;
	}

	public WebServer() {
		initializeCache();
	}

	public static boolean readConfigFile(String filename) {
		//read the config File
		Path file = Paths.get(filename);
		System.out.println(file.toAbsolutePath());
		try (InputStream in = Files.newInputStream(file);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in))) {
			String line = null;
			String text = "";
			while ((line = reader.readLine()) != null) {
				Debug.DEBUG(line);
				text += line + " ";
			}
			Debug.DEBUG(text.trim());
			String words[] = text.trim().split("\\s+");
			for (int i = 0; i < words.length; i++) {
				Debug.DEBUG(words[i]);
				switch (words[i].toLowerCase()) {
				case "listen":
					PortNumber = Integer.parseInt(words[i + 1]);
					break;
				case "documentroot":
					DocumentRoot = words[i + 1];
					break;
				case "threadpoolsize":
					ThreadPoolSize = Integer.parseInt(words[i + 1]);
					break;
				case "cachesize":
					CacheSize = Integer.parseInt(words[i + 1]);
					break;
				case "incompletetimeout":
					IncompleteTimeout = Integer.parseInt(words[i + 1])*1000;
					break;
				case "servername":
					ServerName = words[i+1];
					break;
				}
			}
			Debug.DEBUG(PortNumber + " " + DocumentRoot + " "
					+ ThreadPoolSize + " " + CacheSize + " "
					+ IncompleteTimeout);
			return true;
		} catch (IOException x) {
			System.err.println(x);
			return false;
		}
	}

	public static void initializeCache() {
		cache = new Cache(CacheSize);
	}

	/*
	 * public static void initializeCacheByteBuffer(){ cacheByteBuffer = new
	 * Cache(CacheSize); }
	 */

}
