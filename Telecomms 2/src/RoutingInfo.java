import java.net.InetSocketAddress;

public class RoutingInfo {
	// 3 is enough for my topology
	String[] connectionOne; // 2 cell array ->  RouterNumb|RouterSocket
	String[] connectionTwo;
	String[] connectionThree;
	InetSocketAddress endUserSocket;
	
	RoutingInfo(String[] connectionOne, String[] connectionTwo){
		this.connectionOne = connectionOne;
		this.connectionTwo = connectionTwo;
		this.connectionThree = null;
		this.endUserSocket = null;
	}
	RoutingInfo(String[] connectionOne, String[] connectionTwo, String[] connectionThree){
		this.connectionOne = connectionOne;
		this.connectionTwo = connectionTwo;
		this.connectionThree = connectionThree;
		this.endUserSocket= null;
	}
}
