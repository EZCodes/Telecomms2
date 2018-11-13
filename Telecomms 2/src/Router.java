import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

public class Router extends Machine implements Constants {
	
	private HashMap<String,String> routingTable; // Dest -> Next Router
	
	
	Router(int port){
		try {
			socket = new DatagramSocket(port);
			routingTable = new HashMap<String,String>();
			start();
		} catch (Exception e) 
		{
			if(port >= 50100)
				e.printStackTrace();
			else
			{
				port++;
				try {
					new Router(port).start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		listener.go();
	}
	
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			PacketContent recievedData = new PacketContent(recievedPacket);
			String recievedString = recievedData.toString();
			if(recievedString.contains(HELLACK_HEADER))
			{
				System.out.println("Connected to controller succesfully!");
				this.notify();
			}
			else if(recievedString.contains(INFO_HEADER))
			{
				InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
				String[] recievedInfo = recievedString.split("[|]");
				routingTable.put(recievedInfo[1], recievedInfo[4]);
				routingTable.put(recievedInfo[2], recievedInfo[3]);
				DatagramPacket ackPacket = new PacketContent(INFOACK_HEADER).toDatagramPacket();
				System.out.println("Routing Information recieived request completed!");
				sendPacket(ackPacket,destination);				 
				this.notify();
			}
			else if(recievedString.contains(SEND_HEADER))
			{
				InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
				String[] recievedInfo = recievedString.split("[|]");
				if(!routingTable.containsKey(recievedInfo[1]))
				{
					DatagramPacket infoRequest = new PacketContent(INFOREQUEST_HEADER + "|" +recievedInfo[1]+ "|" ).toDatagramPacket();
					InetSocketAddress controller = new InetSocketAddress(localHost,CONTROLLER_SOCKET);
					sendPacket(infoRequest,controller);
					this.wait();
				}
				String nextDestString = routingTable.get(recievedInfo[1]);
				int nextRouterSocket = Integer.parseInt(nextDestString);
				InetSocketAddress nextHop = new InetSocketAddress(localHost,nextRouterSocket);
				sendPacket(recievedPacket,nextHop);
				//add timers. TODO			
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
		InetSocketAddress destination = new InetSocketAddress(localHost,50000);// manually set
		sendPacket(connectPacket, destination);
		System.out.println("Connection request sent!");
		
		TimeoutTimer task = new TimeoutTimer(this,connectPacket, destination);
		timer.schedule(task, 7000,7000); // 7 sec timeout timer
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
