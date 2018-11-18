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
	
	public String getSocketNumber(String routerNumber) {
		if(connectionOne != null && connectionOne[0].equals(routerNumber))
			return connectionOne[1];
		else if(connectionTwo != null && connectionTwo[0].equals(routerNumber))
			return connectionTwo[1];
		else if(connectionThree != null && connectionThree[0].equals(routerNumber))
			return connectionThree[1];
		else 
			return null;
		
	}
}
