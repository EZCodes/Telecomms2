import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class Router extends Machine {

	final static int ROUTER_SOCKET = 50001;
	final static String CONNECT_HEADER = "000|";
	final static String CONNACK_HEADER = "001|";
	final static String SUBSCRIBE_HEADER = "100|";
	final static String SUBACK_HEADER = "101|";
	final static String PUBLISH_HEADER = "010|";
	final static String PUBACK_HEADER = "011|";
	private HashMap<String,ArrayList<InetSocketAddress>> subscribersByTopics;
	
	
	Broker(int port){
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) 
		{
			if(port >= 50100)
				e.printStackTrace();
			else
			{
				port++;
				try {
					new Broker(port).start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		subscribersByTopics = new HashMap<String,ArrayList<InetSocketAddress>>();
		listener.go();
	}
	
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			PacketContent recievedData = new PacketContent(recievedPacket);
			String recievedString = recievedData.toString();
			if(recievedString.contains(CONNECT_HEADER))
			{
				InetSocketAddress destination =  (InetSocketAddress) recievedPacket.getSocketAddress();
				DatagramPacket ack = new PacketContent(CONNACK_HEADER).toDatagramPacket();
				sendPacket(ack,destination);
				System.out.println("Connection request accepted!");
			}
			else if(recievedString.contains(SUBSCRIBE_HEADER))
			{
				InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
				String[] recievedTopic = recievedString.split("[|]");
				if(subscribersByTopics.containsKey(recievedTopic[1]))
				{
					subscribersByTopics.get(recievedTopic[1]).add(destination);
				}
				else
				{
					subscribersByTopics.put(recievedTopic[1], new ArrayList<InetSocketAddress>());
					subscribersByTopics.get(recievedTopic[1]).add(destination);				
				}
				DatagramPacket ackPacket = new PacketContent(SUBACK_HEADER).toDatagramPacket();
				System.out.println("Subscription request completed!");
				sendPacket(ackPacket,destination);				 
			}
			else if(recievedString.contains(PUBLISH_HEADER))
			{
				InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
				String[] recievedPublication = recievedString.split("[|]");
				if(subscribersByTopics.containsKey(recievedPublication[1]))
				{
					ArrayList<InetSocketAddress> recipientAddresses = subscribersByTopics.get(recievedPublication[1]);
					for(int i=0; i<recipientAddresses.size(); i++)
					{
						InetSocketAddress address = recipientAddresses.get(i);
						sendPacket(recievedPacket,address);
					}
				}
				else
					System.out.println("No such key found");
				
				System.out.println("Publish request completed!");
				DatagramPacket puback = new PacketContent(PUBACK_HEADER).toDatagramPacket();				
				sendPacket(puback,destination);				
			}
			else
			{
				System.out.println("Unknown Packet recieved");
				System.out.println(recievedString);
			}

		}
		catch(Exception e) {e.printStackTrace();}
	}
		
	public void sendPacket(DatagramPacket packetToSend,InetSocketAddress destination) { // look into what it does
		try {
			packetToSend.setSocketAddress(destination); // set address yourself
			socket.send(packetToSend);
		} catch (IOException e) {	e.printStackTrace(); }
		
	}
	
	public int decideDestinationSocket() // TODO
	{
		return 0;
	}
	
	public synchronized void start() throws Exception {
		System.out.println("Waiting for contact");
		this.wait();
	}
	
	public static void main(String[] args) {
		try {					
			new Broker(BROKER_SOCKET).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
