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
			routingInfo = new HashMap<String,ArrayList<String[]>>(); //  "Src-Dst" -> Router -> [router][input(address)][output(address)] 
			connectedRouters = new HashMap<String,InetSocketAddress>();
			initializeRoutingTable();

		} catch (SocketException e) 
		{
			e.printStackTrace();
		}
		listener.go();
	}
	void initializeRoutingTable() { // hardcoded controller configuration
		ArrayList<String[]> temp;
		temp = routingInfo.put("E1-E2", new ArrayList<String[]>()); 
		String[] rout1 = {"R2","51000","50003"};
		temp.add(rout1);
		String[] rout2 = {"R3","50002","51001"};
		temp.add(rout2);
		temp = routingInfo.put("E1-E3", new ArrayList<String[]>());
			
		temp = routingInfo.put("E1-E4", new ArrayList<String[]>());
			
		temp = routingInfo.put("E1-E5", new ArrayList<String[]>());
			
		temp = routingInfo.put("E2-E3", new ArrayList<String[]>());
			
		temp = routingInfo.put("E2-E4", new ArrayList<String[]>());
			
		temp = routingInfo.put("E2-E5", new ArrayList<String[]>());
			
		temp = routingInfo.put("E3-E4", new ArrayList<String[]>());
			
		temp = routingInfo.put("E3-E5", new ArrayList<String[]>());
			
		temp = routingInfo.put("E4-E5", new ArrayList<String[]>());	
								
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
		DatagramPacket connectPacket = new PacketContent(CONNECT_HEADER).toDatagramPacket();;
		DatagramPacket publishPacket;
		InetAddress localHost = InetAddress.getLocalHost();
		InetSocketAddress destination = new InetSocketAddress(localHost,50000); // manually set broker address	
		sendPacket(connectPacket,destination);
		System.out.println("Connection request sent!");
		this.wait();
		Scanner input = new Scanner(System.in);	
		String inputString = "";
		do
		{
			System.out.println("If you wish to publish please type 'Publish', if you wish to quit then type 'q'");
			inputString = input.nextLine();
			if(inputString.equals("Publish"))
			{
				System.out.println("Please enter a topic you want to Publish.");// need to set up max
				inputString = input.nextLine();
				inputString = PUBLISH_HEADER + inputString;
				System.out.println("Please enter a message to your topic.");// need to set up max
				String message = input.nextLine();
				inputString = inputString + "|" + message;
				publishPacket = new PacketContent(inputString).toDatagramPacket();
				sendPacket(publishPacket, destination);
				this.wait();
			}
			else if(!inputString.equals("q"))
				System.out.println("Invalid command, please try again.");
		}while(!inputString.equals("q"));
		input.close();
		System.out.println("Publishing finished");
		this.wait();
	}
	
	public static void main(String[] args) {
		try {					
			new Controller(CONTROLLER_SOCKET).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
