import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Timer;

public class Router extends Machine implements Constants {
	
	private HashMap<String,String> routingTable; // Dest -> Next Router Socket
	private RoutingInfo neighbourList; 
	
	
	Router(int port,RoutingInfo neighbourList){
		try {
			this.neighbourList = neighbourList;
			socket = new DatagramSocket(port);
			routingTable = new HashMap<String,String>();
			start();
		} catch (Exception e) 
		{
			System.out.println("Failed to allocate sockets, try different ones.");
			e.printStackTrace();
		}
		listener.go();
	}
	
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		try {
			Timer timer = new Timer(true);			
			InetAddress localHost = InetAddress.getLocalHost();
			PacketContent recievedData = new PacketContent(recievedPacket);
			String recievedString = recievedData.toString();
			if(recievedString.contains(HELLACK_HEADER))
			{
				System.out.println("Connected to controller succesfully!");
				this.notify();
			}
			else if(recievedString.contains(SENDACK_HEADER))
			{
				System.out.println("Message forwarded succesfully!");
				this.notify();
			}
			else if(recievedString.contains(FEATURE_REQUEST_HEADER))
			{
				InetSocketAddress destination = new InetSocketAddress(localHost,CONTROLLER_SOCKET); // set manually, small security check to not send routing info to someone else
				String outputString = FEATURE_HEADER;
				if(this.neighbourList.connectionOne != null)
					outputString += this.neighbourList.connectionOne[0] +"|";
				if(this.neighbourList.connectionTwo != null)
					outputString += this.neighbourList.connectionTwo[0]+"|";
				if(this.neighbourList.connectionThree != null)
					outputString += this.neighbourList.connectionThree[0]+"|";
				if(this.neighbourList.endUserName!=null)
					outputString += this.neighbourList.endUserName+"|";
				DatagramPacket featPacket = new PacketContent(outputString).toDatagramPacket();
				sendPacket(featPacket,destination); // feature packet
				TimeoutTimer task = new TimeoutTimer(this,featPacket, destination);
				timer.schedule(task, TIMEOUT_TIME,TIMEOUT_TIME); // 7 sec timeout timer
				this.wait();
				timer.cancel();
			}
			else if(recievedString.contains(FEATACK_HEADER))
			{
				System.out.println("Feature exchange completed succesfully!");
				this.notify();
			}
			else if(recievedString.contains(INFO_HEADER))
			{
				InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
				String[] recievedInfo = recievedString.split("[|]");
				routingTable.put(recievedInfo[1], recievedInfo[2]);
				//routingTable.put(recievedInfo[2], recievedInfo[3]) // return address;
				DatagramPacket ackPacket = new PacketContent(INFOACK_HEADER).toDatagramPacket();
				System.out.println("Routing Information recieived!");
				sendPacket(ackPacket,destination);				 
				this.notify();
			}
			else if(recievedString.contains(SEND_HEADER))
			{
				InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
				if(this.neighbourList.endUserSocket == null)
					this.neighbourList.endUserSocket = destination;
				String[] recievedInfo = recievedString.split("[|]");
				if(!routingTable.containsKey(recievedInfo[1]))
				{
					DatagramPacket infoRequest = new PacketContent(INFOREQUEST_HEADER + "|" +recievedInfo[1]+ "|" ).toDatagramPacket();
					InetSocketAddress controller = new InetSocketAddress(localHost,CONTROLLER_SOCKET);
					sendPacket(infoRequest,controller);
					TimeoutTimer task = new TimeoutTimer(this,infoRequest, destination);
					timer.schedule(task, TIMEOUT_TIME,TIMEOUT_TIME); // 7 sec timeout timer
					this.wait();
					timer.cancel();
				}
				String nextDestString = routingTable.get(recievedInfo[1]);
				int nextRouterSocket = Integer.parseInt(nextDestString);
				InetSocketAddress nextHop = new InetSocketAddress(localHost,nextRouterSocket);
				sendPacket(recievedPacket,nextHop);
				TimeoutTimer task = new TimeoutTimer(this,recievedPacket, destination);
				timer.schedule(task, TIMEOUT_TIME,TIMEOUT_TIME); // 7 sec timeout timer
				this.wait();
				timer.cancel();			
				System.out.println("Send request completed!");
				DatagramPacket sendack = new PacketContent(SENDACK_HEADER).toDatagramPacket();				
				sendPacket(sendack,destination);				
			}
			else
			{
				System.out.println("Unknown Packet recieved");
				System.out.println(recievedString);
			}

		}
		catch(Exception e) {e.printStackTrace();}
	}
		
	public void sendPacket(DatagramPacket packetToSend,InetSocketAddress destination) { 
		try {
			packetToSend.setSocketAddress(destination); 
			socket.send(packetToSend);
		} catch (IOException e) {	e.printStackTrace(); }
		
	}
	
	
	public synchronized void start() throws Exception { // hardcoded address of controller
		Timer timer = new Timer(true);
		
		DatagramPacket connectPacket = new PacketContent(HELLO_HEADER).toDatagramPacket();
		InetAddress localHost;
		localHost = InetAddress.getLocalHost();
		InetSocketAddress destination = new InetSocketAddress(localHost,CONTROLLER_SOCKET);// manually set
		sendPacket(connectPacket, destination);
		System.out.println("Connection request sent!");
		
		TimeoutTimer task = new TimeoutTimer(this,connectPacket, destination);
		timer.schedule(task, TIMEOUT_TIME,TIMEOUT_TIME); // 7 sec timeout timer
		this.wait();
		task.cancel();
		this.wait();
	}
	
	public static void main(String[] args) {
		try {					
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
