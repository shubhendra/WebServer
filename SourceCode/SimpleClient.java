
import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class SimpleClient {


	static long[] stats = {0,0,0,0,0};
	static String serverName;
	static int port;
	static int threadCount;
	static String fileName;
	static int timeOut;
	static ArrayList<String> requestFiles;
	static boolean blocking = true;
	static Thread threadArray[];
	static ArrayList<RequestSender> requestSenderArray;
	public SimpleClient(InetAddress serverIPAddress) throws IOException{
		requestSenderArray = new ArrayList<RequestSender>();
		threadArray = new Thread[threadCount];
		for(int i = 0; i < threadCount; i++ ){
			RequestSender rh = new RequestSender(serverIPAddress, port, requestFiles);
			requestSenderArray.add(rh);
		    threadArray[i] = new Thread( rh );
		    threadArray[i].start();
		}
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//java SHTTPTestClient -server <server> -port <server port> -parallel <# of threads> -files <file name> -T <time of test in seconds>
		String wrongMessageFormat = "Wrong message format: -server <serverName> -port <port> -parallel <threadCount> -files <fileName> -T <timeOut>";
		if (args.length != 10){
			System.out.println("1 "+ wrongMessageFormat);
			return;
		}
		/*if (!args[0].equals("-st")){
			System.out.println("2 "+wrongMessageFormat);
			return;
		}
		switch (args[1]){
		case "blocking":
			blocking = true;
			break;
		case "non" :
			blocking = false;
			break;
		}*/
		if (!args[0].equals("-server")){
			System.out.println("3 "+wrongMessageFormat);
			return;
		}
		serverName = args[1];//"localhost";//
		if (!args[2].equals("-port")){
			System.out.println("4 "+ wrongMessageFormat);
			return;
		}
		try {
			port = Integer.parseInt(args[3]);//6789;//
			if (port < 0){
				System.out.println("5 "+wrongMessageFormat);
				return;
			}
		} catch(NumberFormatException e){
			System.out.println("6 "+wrongMessageFormat);
			return;
		}
		if (!args[4].equals("-parallel")){
			System.out.println("7 "+wrongMessageFormat);
			return;
		}
		try {
			threadCount = Integer.parseInt(args[5]);
			if (threadCount < 0){
				System.out.println("8 "+wrongMessageFormat);
				return;
			}
		} catch(NumberFormatException e){
			System.out.println("9 "+wrongMessageFormat);
			return;
		}
		if (!args[6].equals("-files")){
			System.out.println("10 "+wrongMessageFormat);
			return;
		}
		fileName = args[7];//"requests.txt";
		if (!args[8].equals("-T")){
			System.out.println("11 "+wrongMessageFormat);
			return;
		}
		
		try {
			timeOut = Integer.parseInt(args[9]);//25;//
			if (timeOut < 0){
				System.out.println("12 "+wrongMessageFormat);
				return;
			}
		} catch(NumberFormatException e){
			System.out.println("13 "+wrongMessageFormat);
			return;
		}
		
		
		
		//1
		for(int i =0; i< 10; i++){
		System.out.println(args[i]);}
		
		InetAddress serverIPAddress = InetAddress.getByName(serverName);
		requestFiles = new ArrayList<String>();
		Path file = Paths.get(fileName);
		
		System.out.println(file.toAbsolutePath());
		
		InputStream in = Files.newInputStream(file);
		BufferedReader reader =
		      new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
		        Debug.DEBUG(line);
		        requestFiles.add(line); 
		}
		SimpleClient client = new SimpleClient(serverIPAddress);
		
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@SuppressWarnings("finally")
			public void run()
			{
				for (int i = 0; i< threadCount; i++) {
					try {
						Debug.DEBUG("Terminating Thread");
						
						requestSenderArray.get(i).terminate();
						threadArray[i].join(500);
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						double transactionThroughput, dataThroughput, waitTime;
						synchronized (stats) {
						System.out.println("In Simple CLient: stats 0,filesize: "+ stats[0]);
						System.out.println("In Simple CLient: stats 1,waitTime: "+ stats[1]);
						System.out.println("In Simple CLient: stats 2,transferTime: "+ stats[2]);
						System.out.println("In Simple CLient: stats 3,numFiles "+ stats[3]);
						System.out.println("In Thread: stats 4,numRequests "+ stats[4]);
						double totalTime = ((stats[1]+stats[2])*1.0)/1000.0;
						if (totalTime == 0){
							transactionThroughput = Double.MAX_VALUE;
							dataThroughput = Double.MAX_VALUE;
							waitTime = stats[1]/stats[4]*1.0;
						} else {
						transactionThroughput = (stats[3]*1.0)/totalTime; 	
						dataThroughput = stats[0]/(totalTime)*1.0;
						waitTime = stats[1]/stats[4]*1.0;
						}
				
						System.out.println("transaction Throughput: "+ transactionThroughput +" Files/second");
						System.out.println("data Throughput: "+ dataThroughput+" KB/second");
						System.out.println("average wait: "+ waitTime +" ms per file");
						timer.cancel();
						System.exit(2);
						return;
						}
					}
					
				}
			}
			} ,timeOut * 1000); 
				

	   }

 
	
	
}

