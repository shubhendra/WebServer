
import java.util.HashMap;


public class Cache {
	
	private HashMap<String, byte[]> cacheMap;
	private long cacheSize; 
	private long currentSize;
	
	public Cache(int size){
		this.cacheSize = size*1000;//because size is specified in KB
		cacheMap = new HashMap<String, byte[]>();
		
	}
	public Cache(){
		this.cacheSize = 100000000;//defaultcachesize
		cacheMap = new HashMap<String, byte[]>();
	}
	
	public boolean addFile(String fileName, byte[] fileInBytes){
		if (currentSize + fileInBytes.length < cacheSize) {
			currentSize += fileInBytes.length;
			//System.out.println("Size of file added to cache: "+fileInBytes.length);
			if(findFile(fileName)){return true;}
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
	
	public byte[] getFile(String fileName){
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
