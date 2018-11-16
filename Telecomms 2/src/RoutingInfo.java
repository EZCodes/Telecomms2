import java.net.InetSocketAddress;

public class RoutingInfo {
	// 3 is enough for my topology
	String[] connectionOne; // 2 cell array ->  RouterNumb|RouterSocket
	String[] connectionTwo;
	String[] connectionThree;
	String endUserName;
	InetSocketAddress endUserSocket;
	
	RoutingInfo(String[] connectionOne, String[] connectionTwo, String[] connectionThree,  String endUserName){
		this.connectionOne = connectionOne;
		this.connectionTwo = connectionTwo;
		this.connectionThree = connectionThree;
		this.endUserSocket= null;
		this.endUserName = endUserName;
	}
	RoutingInfo(String[] connectionOne, String[] connectionTwo, String endUserName){
		this.connectionOne = connectionOne;
		this.connectionTwo = connectionTwo;
		this.connectionThree = null;
		this.endUserSocket= null;
		this.endUserName = endUserName;
	}
}
