
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class Load1 implements BaseLoad  {
	
	public static void main(String args[]){
		Load1 l= new Load1();
		l.run();
	}
	public byte[] run(){
		//String filename = "C:\\apple\\output.html";
		//FileOutputStream fileStream;
//		try {
			//fileStream = new FileOutputStream(filename);
		
		//System.out.println("Filename: "+filename);
		String result ="<html><head><title>load</title></head><body>The value is 200</body></html>";
		byte[] fileInBytes = result.getBytes();
		return fileInBytes;
			//fileStream.write(fileInBytes);
			//fileStream.close();
	//	} catch (IOException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
//		}
		
		//return null;

	}
}
