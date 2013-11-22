

import java.util.Comparator;
//comparator for connecting timeoutConnection objects
public class CompareConnectionTimeouts implements Comparator<TimeoutConnection> {

	@Override
	public int compare(TimeoutConnection o1, TimeoutConnection o2) {
		// TODO Auto-generated method stub
		if(o1.getTimeStamp() < o2.getTimeStamp())
			return -1;
		else if (o1.getTimeStamp() > o2.getTimeStamp())
			return 1;
		return 0;
	}

}
