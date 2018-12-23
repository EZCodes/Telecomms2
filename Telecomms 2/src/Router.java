import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Timer;

public class Router extends Machine implements Constants { // TODO send feature after hellack fix timers(another thread) 
	
	static int currentRouterSocket = STARTING_ROUTER_PORT;
	
	private HashMap<String,String> routingTable; // Dest -> Next Router Socket 
	private RoutingInfo neighbourList; 
	
	
	Router(int port){
		try {
			this.neighbourList = Network.decideNeighbours(port);
			socket = new DatagramSocket(port);
			routingTable = new HashMap<String,String>();
		} catch (Exception e) 
		{
			System.out.println("Failed to allocate sockets, try different ones.");
			e.printStackTrace();
		}
		listener.go();
		currentRouterSocket++;
	}
	
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		try {
			Timer timer = new Timer(true);			
			InetAddress localHost = InetAddress.getLocalHost();
			PacketContent recievedData = new PacketContent(recievedPacket);
			String recievedString = recievedData.toString();
			/*if(recievedString.contains(HELLACK_HEADER))
			{
				System.out.println("Connected to controller succesfully!");
				this.notify();
			} */
			if(recievedString.contains(SENDACK_HEADER))
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
				this.notify();
			/*	TimeoutTimer task = new TimeoutTimer(this,featPacket, destination);
				timer.schedule(task, TIMEOUT_TIME,TIMEOUT_TIME); // 7 sec timeout timer
				this.wait();
				timer.cancel(); */
			}
			else if(recievedString.contains(FEATACK_HEADER))
			{
				System.out.println("Feature exchange completed succesfully!");
	//			this.notify();
			}
			else if(recievedString.contains(INFO_HEADER))
			{
				InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
				String[] recievedInfo = recievedString.split("[|]");
				if(recievedInfo[1].equals("0"))
					System.out.println("Information for given user is not found");
				else {
					routingTable.put(recievedInfo[1], recievedInfo[2]); 
					//routingTable.put(recievedInfo[2], recievedInfo[3]) // return address;
				}
				DatagramPacket ackPacket = new PacketContent(INFOACK_HEADER).toDatagramPacket();
				System.out.println("Routing Information recieived!");
				sendPacket(ackPacket,destination);				 
				this.notify();
			}
			else if(recievedString.contains(SEND_HEADER))
			{
				InetSocketAddress source = (InetSocketAddress) recievedPacket.getSocketAddress();
				if(this.neighbourList.endUserSocket == null)		// TODO gets user socket if any, flawed method
					this.neighbourList.endUserSocket = source; 
				String[] recievedInfo = recievedString.split("[|]");
				if(!routingTable.containsKey(recievedInfo[1]))
				{
					DatagramPacket infoRequest = new PacketContent(INFOREQUEST_HEADER+recievedInfo[1]+ "|" ).toDatagramPacket();
					InetSocketAddress controller = new InetSocketAddress(localHost,CONTROLLER_SOCKET);
					sendPacket(infoRequest,controller);
				//	TimeoutTimer task = new TimeoutTimer(this,infoRequest, destination);
				//	timer.schedule(task, TIMEOUT_TIME,TIMEOUT_TIME); // 7 sec timeout timer
				//	this.wait();
				//	timer.cancel();
				}
				String nextDestRouter = routingTable.get(recievedInfo[1]); 
				if(nextDestRouter == null)
				{
					System.out.println("Destination not found, dropping packet.");
				}
				else {
					String nextDestSocket = neighbourList.getSocketNumber(nextDestRouter);
					int nextRouterSocket = Integer.parseInt(nextDestSocket);
					InetSocketAddress nextHop = new InetSocketAddress(localHost,nextRouterSocket);
					sendPacket(recievedPacket,nextHop);
				}
		//		TimeoutTimer task = new TimeoutTimer(this,recievedPacket, destination);
		//		timer.schedule(task, TIMEOUT_TIME,TIMEOUT_TIME); // 7 sec timeout timer
		//		this.wait();
		//		timer.cancel();			
				System.out.println("Send request completed!");
				DatagramPacket sendack = new PacketContent(SENDACK_HEADER).toDatagramPacket();				
				sendPacket(sendack,source);
				
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
			new Router(currentRouterSocket).start();
			System.out.println("Program completed(router)");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
