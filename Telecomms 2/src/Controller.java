import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Controller extends Machine implements Constants {

	private HashMap<String, RoutingInfo> routingInfo; // Router -> it's surroundings
	private HashMap<String,InetSocketAddress> connectedRouters;
	Controller(int port){
		try {
			socket = new DatagramSocket(port);
			connectedRouters = new HashMap<String,InetSocketAddress>();
			routingInfo = new HashMap<String,RoutingInfo>();
		} catch (SocketException e) 
		{
			e.printStackTrace();
		}
		listener.go();
	}
	HashMap<String,String> calculateRout() { // the method calculating rout
		
		
		return null;
	}
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		try {
			PacketContent recievedData = new PacketContent(recievedPacket);
			String recievedString = recievedData.toString();
			String[] packetInformation = recievedString.split("[|]");
			if(recievedString.contains(HELLO_HEADER))
			{
				InetSocketAddress routerAddress = (InetSocketAddress) recievedPacket.getSocketAddress();
				connectedRouters.put(packetInformation[1], routerAddress);
				System.out.println("Connection with router: "+ packetInformation[1] + "established!");
				
			}
			else if(recievedString.contains(INFOREQUEST_HEADER))
			{
				InetSocketAddress routerAddress = (InetSocketAddress) recievedPacket.getSocketAddress();
				if(routerAddress.equals(connectedRouters.get(packetInformation[1]))) // small protection if external or dead router will want the info
				{
					ArrayList<String[]> table = routingInfo.get(packetInformation[2]);
					PacketContent routAsString = new PacketContent(rout[0] +","+rout[1]);
					DatagramPacket packetToSend = routAsString.toDatagramPacket();		
					sendPacket(packetToSend,routerAddress);					
				}
				System.out.println("Routing information sent!");
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}
		
	public void sendPacket(DatagramPacket packetToSend, InetSocketAddress destination) { // look into what it does
		try {
			packetToSend.setSocketAddress(destination); // set address yourself
			socket.send(packetToSend);
		} catch (IOException e) {	e.printStackTrace(); }
		
	}
	
	
	public synchronized void start() throws Exception {
		System.out.println("Controller online!");
		this.wait();
		System.out.println("Controller going offline");
	}
	

}
