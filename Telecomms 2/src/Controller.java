import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

public class Controller extends Machine {
	final static int PUBLISHER_SOCKET = 50100;
	final static String CONNECT_HEADER = "000MQTT|";
	final static String CONNACK_HEADER = "001MQTT|";
	final static String PUBLISH_HEADER = "010MQTT|";
	final static String PUBACK_HEADER = "011MQTT|";
	Publisher(int port){
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) 
		{
			if(port >= 51000)
				e.printStackTrace();
			else
			{
				port++;
				try {
					new Publisher(port).start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		listener.go();
	}
	
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		try {
			PacketContent recievedData = new PacketContent(recievedPacket);
			String recievedString = recievedData.toString();
			if(recievedString.contains(CONNACK_HEADER))
			{
				System.out.println("Connection with broker established!");
				this.notify();
			}
			else if(recievedString.contains(PUBACK_HEADER))
			{
				System.out.println("Publishing successfull");
				this.notify();
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
	
	public int decideDestinationSocket() // TODO
	{
		return 0;
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
			new Publisher(PUBLISHER_SOCKET).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
