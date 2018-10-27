import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

public class EndUser extends Machine {

	final static int END_USER_SOCKET = 51000;
	final static String SEND_HEADER = "000|";
	final static String SENDACK_HEADER = "001|";




	EndUser(int port){
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) 
		{
			if(port >= 60000)
				e.printStackTrace();
			else
			{
				port++;
				try {
					new EndUser(port).start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		listener.go();
	}
	
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		// can do acks later of smth
	}
		
	public void sendPacket(DatagramPacket packetToSend, InetSocketAddress destination) { 
		try {
			packetToSend.setSocketAddress(destination); 
			socket.send(packetToSend);
		} catch (IOException e) {	e.printStackTrace(); }
		
	}
	
	
	public synchronized void start() throws Exception {
		DatagramPacket packetToSend;
		InetAddress localHost = InetAddress.getLocalHost();
		InetSocketAddress destination = new InetSocketAddress(localHost,50000);// manually set
		sendPacket(connectPacket, destination);
		System.out.println("Connection request sent!");
		this.wait();		
		Scanner input = new Scanner(System.in);
		String inputString = "";
		do
		{
			System.out.println("If you wish to subscribe please type 'Subscribe', if you wish to quit then type 'q'");
			inputString = input.nextLine();
			if(inputString.equals("Subscribe"))
			{
				System.out.println("Please enter a topic you want to subscribe to.");// need to set up max
				inputString = input.nextLine();
				inputString = SUBSCRIBE_HEADER + inputString + "|"; // adding '|' at the end for proper parsing at broker
				subscription = new PacketContent(inputString).toDatagramPacket();
				sendPacket(subscription, destination);
				this.wait();
			}
			else if(inputString.equals("listen"))
			{
				this.wait();
			}
			else if(!inputString.equals("q"))
				System.out.println("Invalid command, please try again.");
		}while(!inputString.equals("q"));
		input.close();
	}
	
	public static void main(String[] args) {
		try {	
			new EndUser(SUBSCRIBER_SOCKET).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
