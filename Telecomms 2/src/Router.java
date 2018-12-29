import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Timer;

public class Router extends Machine implements Constants { 
	
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
			InetAddress localHost = InetAddress.getLocalHost();
			PacketContent recievedData = new PacketContent(recievedPacket);
			String recievedString = recievedData.toString();
			if(recievedString.contains(HELLO_HEADER)) // should recieve hello only from end users
			{
				InetSocketAddress endUser = (InetSocketAddress) recievedPacket.getSocketAddress();
				DatagramPacket ack = new PacketContent(HELLACK_HEADER).toDatagramPacket();
				this.neighbourList.endUserSocket = endUser;
				sendPacket(ack,endUser);
				System.out.println("Connection with end user established!");
			} 
			else if(recievedString.contains(SENDACK_HEADER))
			{
				System.out.println("Message forwarded succesfully!");
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
			}
			else if(recievedString.contains(FEATACK_HEADER))
			{
				System.out.println("Feature exchange completed succesfully!");
			}
			else if(recievedString.contains(INFO_HEADER))
			{
				InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
				String[] recievedInfo = recievedString.split("[|]");
				if(recievedInfo[1].equals("0"))
					System.out.println("Information for given user is not found");
				else {
					routingTable.put(recievedInfo[1], recievedInfo[2]); 
				}
				DatagramPacket ackPacket = new PacketContent(INFOACK_HEADER).toDatagramPacket();
				System.out.println("Routing Information recieived!");
				sendPacket(ackPacket,destination);				 
			}
			else if(recievedString.contains(SEND_HEADER))
			{
				InetSocketAddress source = (InetSocketAddress) recievedPacket.getSocketAddress();
				String[] recievedInfo = recievedString.split("[|]");
				if(!routingTable.containsKey(recievedInfo[1]))
				{
					DatagramPacket infoRequest = new PacketContent(INFOREQUEST_HEADER+recievedInfo[1]+ "|" ).toDatagramPacket();
					InetSocketAddress controller = new InetSocketAddress(localHost,CONTROLLER_SOCKET);
					sendPacket(infoRequest,controller);
				}
				String nextDestRouter = routingTable.get(recievedInfo[1]); 
				if(nextDestRouter == null)
				{
					System.out.println("Destination not found, dropping packet.");
				}
				else {
					InetSocketAddress nextHop;
					if(nextDestRouter.equals(neighbourList.endUserName))
						nextHop = neighbourList.endUserSocket;
					else 
					{
						String nextDestSocket = neighbourList.getSocketNumber(nextDestRouter);
						int nextRouterSocket = Integer.parseInt(nextDestSocket);
						nextHop = new InetSocketAddress(localHost,nextRouterSocket);
					}
					sendPacket(recievedPacket,nextHop);
					System.out.println("Send request completed!");
					DatagramPacket sendack = new PacketContent(SENDACK_HEADER).toDatagramPacket();				
					sendPacket(sendack,source); // ack here, so that if no routing info initially
					// then user will re-send and it will go through
				}		
		
			}
			else
			{
				System.out.println("Unknown Packet recieved on router");
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
	
	
	public synchronized void start() throws Exception {
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
