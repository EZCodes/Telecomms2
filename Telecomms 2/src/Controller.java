import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class Controller extends Machine implements Constants { 
	
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
	 * @param destination
	 * @return HashMap where Router->Next hop socket number(from that router)
	 */
	HashMap<String,String> calculateRout(String destination,String startRouter) { // the method calculating rout using BFS(modified)
		HashMap<String,String> map = new HashMap<String,String>(); // doing it in a map so it can support other types if needed
		
		HashMap<String,String> precedessors = new HashMap<String,String>();
		ArrayList<String> queue = new ArrayList<String>();
		
		
		
		ArrayList<String> neighbours = routingInfo.get(startRouter);
		for(int i=0;neighbours != null && i<neighbours.size(); i++ ) 
		{
			if(!precedessors.containsKey(neighbours.get(i)))
				precedessors.put(neighbours.get(i), startRouter);
			queue.add(neighbours.get(i));
		}
		String nextMachine;
		do {
		nextMachine = queue.remove(0);
		}while(precedessors.containsValue(nextMachine)); // if visited go next
	
		
		
		precedessors = calculateRoutRecursive(destination,startRouter, precedessors, queue);
		
		String value;
		String key = destination;
		
		while(!key.equals(startRouter))
		{
			value = precedessors.get(key);
			map.put(value, key);
			key = value;
		}
		return map;
	}
	HashMap<String,String> calculateRoutRecursive( String destination, String currentMachine, HashMap<String,String> precedessor, ArrayList<String> queue){ //stack may overflow in extreme cases
		if(!currentMachine.equals(destination) && !queue.isEmpty())
		{
			ArrayList<String> neighbours = routingInfo.get(currentMachine);
			// sometime a current machine will be EndUser, so additional check for null is needed
			for(int i=0;neighbours != null && i<neighbours.size(); i++ ) 
			{
				if(!precedessor.containsKey(neighbours.get(i)))
					precedessor.put(neighbours.get(i), currentMachine);
				queue.add(neighbours.get(i));
			}
			String nextMachine;
			do {
			nextMachine = queue.remove(0);
			}while(precedessor.containsValue(nextMachine)); // if visited go next
			return calculateRoutRecursive(destination,nextMachine, precedessor,queue);
		}
		else if(currentMachine.equals(destination))
		{
			return precedessor;
		}
		else
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
				int routerPort = routerAddress.getPort();
				String routerNumber = "R" + Integer.toString((routerPort%STARTING_ROUTER_PORT)+1); // getting number of router
				connectedRouters.put(routerNumber, routerAddress);
				System.out.println("Connection with router: "+ routerNumber + ", established!");
				DatagramPacket featureRequest = new PacketContent(FEATURE_REQUEST_HEADER).toDatagramPacket();// feat request works as ack in this case
				sendPacket(featureRequest,routerAddress);
				System.out.println("Feature request sent!");
				
			}
			else if(recievedString.contains(INFOREQUEST_HEADER))
			{
				InetSocketAddress routerAddress = (InetSocketAddress) recievedPacket.getSocketAddress();
				int routerPort = routerAddress.getPort();
				String routerNumber = "R" + Integer.toString((routerPort%STARTING_ROUTER_PORT)+1);
				if(routerPort == connectedRouters.get(routerNumber).getPort()) // small protection if external or dead router will want the info
				{
					String finalDestination = packetInformation[1];
					HashMap<String,String> map = calculateRout(finalDestination, routerNumber); 
					if(map == null)// if end user is not in topology, send order to drop packet
					{
						DatagramPacket packetToSend = new PacketContent(INFO_HEADER+"0|").toDatagramPacket();		
						sendPacket(packetToSend,routerAddress);	
					}
					else {
						String[] routers = map.keySet().toArray(new String[map.size()]); // getting keys out of map to iterate through them
						for(int i =0; i<routers.length ; i++)
						{
							InetSocketAddress destination = connectedRouters.get(routers[i]);
							String nextHop = map.get(routers[i]);
							DatagramPacket packetToSend = new PacketContent(INFO_HEADER+finalDestination+"|"+nextHop+"|").toDatagramPacket();		
							sendPacket(packetToSend,destination);	
						}
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
			}
			else
			{
				System.out.println("Unknown Packet recieved on controller");
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
	
	public static void main(String[] args) {
		try {		
			new Controller(CONTROLLER_SOCKET).start();
			System.out.println("Program completed(controller)");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
	

}
