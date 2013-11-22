
import java.nio.ByteBuffer;
import java.util.HashMap;


public class CacheByteBuffer {
	
	private HashMap<String, ByteBuffer> cacheMap;
	private long cacheSize; 
	private long currentSize;
	
	public CacheByteBuffer(int size){
		this.cacheSize = size*1000;
		cacheMap = new HashMap<String, ByteBuffer>();
		
	}
	public CacheByteBuffer(){
		this.cacheSize = 16000;
		cacheMap = new HashMap<String, ByteBuffer>();
	}
	
	public boolean addFile(String fileName, ByteBuffer fileInBytes){
		if (currentSize + fileInBytes.position() < cacheSize) {
			currentSize += fileInBytes.position();
			//System.out.println("Size of file added to cache: "+fileInBytes.length);
			cacheMap.put(fileName, fileInBytes);
			return true;
		}
		else return false;
	}
	
	public boolean findFile(String fileName){
		if (cacheMap.containsKey(fileName))
			return true;
		else 
			return false;
	}
	
	public ByteBuffer getFile(String fileName){
		if(cacheMap.containsKey(fileName))
			return cacheMap.get(fileName);
		return null;
	}
	
	public double spaceLeft(){
		if (cacheSize - currentSize>0){
			return (cacheSize-currentSize);
		}
		else
			return -1;
	}

}
