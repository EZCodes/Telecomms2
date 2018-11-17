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
import java.util.Set;
import java.util.Timer;

public class Controller extends Machine implements Constants { // TODO add timers

	private HashMap<String, ArrayList<String>> routingInfo; // Router -> it's surroundings
	private HashMap<String,InetSocketAddress> connectedRouters;
	Controller(int port){
		try {
			socket = new DatagramSocket(port);
			connectedRouters = new HashMap<String,InetSocketAddress>();
			routingInfo = new HashMap<String,ArrayList<String>>();
		} catch (SocketException e) 
		{
			e.printStackTrace();
		}
		listener.go();
	}
	/**
	 * 
	 * @param destination
	 * @return HashMap where Router->Next hop socket number(from that router)
	 */
	HashMap<String,String> calculateRout(String destination,String startRouter) { // the method calculating rout
		HashMap<String,String> map = new HashMap<String,String>();
		
		return map;
	}
	HashMap<String,String> calculateRoutRecursive(HashMap<String,String> map, String destination, String currentMachine){ 
		
		return null;
	}
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		try {
			Timer timer = new Timer(true);			
			InetAddress localHost = InetAddress.getLocalHost();
			PacketContent recievedData = new PacketContent(recievedPacket);
			String recievedString = recievedData.toString();
			String[] packetInformation = recievedString.split("[|]");
			if(recievedString.contains(HELLO_HEADER))
			{
				InetSocketAddress routerAddress = (InetSocketAddress) recievedPacket.getSocketAddress();
				int routerPort = routerAddress.getPort();
				String routerNumber = "R" + Integer.toString((routerPort%STARTING_ROUTER_PORT)+1);
				connectedRouters.put(routerNumber, routerAddress);
				System.out.println("Connection with router: "+ routerNumber + "established!");
				
			}
			else if(recievedString.contains(INFOREQUEST_HEADER))
			{
				InetSocketAddress routerAddress = (InetSocketAddress) recievedPacket.getSocketAddress();
				int routerPort = routerAddress.getPort();
				String routerNumber = "R" + Integer.toString((routerPort%STARTING_ROUTER_PORT)+1);
				if(routerAddress.equals(connectedRouters.get(routerNumber))) // small protection if external or dead router will want the info
				{
					String finalDestination = packetInformation[1];
					HashMap<String,String> map = calculateRout(finalDestination, routerNumber);
					String[] routers = map.keySet().toArray(new String[map.size()]); // getting keys out of map to iterate through them
					for(int i =0; i<routers.length ; i++)
					{
						InetSocketAddress destination = connectedRouters.get(routers[i]);
						String nextHopSocket = map.get(routers[i]);
						DatagramPacket packetToSend = new PacketContent(INFO_HEADER+finalDestination+"|"+nextHopSocket+"|").toDatagramPacket();		
						sendPacket(packetToSend,destination);	
						this.wait();
					}
				}
				else
					System.out.println("Request from unknown router, packet dropped!");
				System.out.println("Routing procedure completed!");
			}
			else if(recievedString.contains(FEATURE_HEADER))
			{
				InetSocketAddress routerAddress = (InetSocketAddress) recievedPacket.getSocketAddress();
				int routerPort = routerAddress.getPort();
				String routerNumber = "R" + Integer.toString((routerPort%STARTING_ROUTER_PORT)+1);
				if(routerAddress.equals(connectedRouters.get(routerNumber))) // small protection if external or dead router will want the info
				{
					ArrayList<String> neighbours = new ArrayList<String>();
					for(int i=1; i<packetInformation.length-1; i++)
					{
						neighbours.add(packetInformation[i]);
					}
					routingInfo.put(routerNumber, neighbours);
				}	
				else
					System.out.println("Feature from unknown router, packet dropped!");
				System.out.println("Feature from a router recieved!");
				DatagramPacket ack = new PacketContent(FEATACK_HEADER).toDatagramPacket();
				sendPacket(ack,routerAddress);
			}
			else if(recievedString.contains(INFOACK_HEADER))
			{
				System.out.println("Routing information successfully sent!");
				this.notify();
			}
			else
			{
				System.out.println("Unknown Packet recieved");
				System.out.println(recievedString);
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}
		
	public void sendPacket(DatagramPacket packetToSend, InetSocketAddress destination) { 
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
