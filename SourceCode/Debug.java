

public class Debug {

    private static boolean DEBUG = false;
    public static void DEBUG(String s) {
	if (DEBUG)
	    System.out.println(s);
    }
    
    public static void DEBUG(int s) {
    	if (DEBUG)
    	    System.out.println(s);
        }
}
