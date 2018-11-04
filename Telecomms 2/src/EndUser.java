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
		PacketContent recievedData = new PacketContent(recievedPacket);
		String recievedString = recievedData.toString();
		//String[] packetInformation = recievedString.split("[|]");
		if(recievedString.contains(SENDACK_HEADER))
			System.out.println("Message succesfully sent!");
		else
			System.out.println("Unknown packet recieved " + recievedString );
			
	}
		
	public void sendPacket(DatagramPacket packetToSend, InetSocketAddress destination) { 
		try {
			packetToSend.setSocketAddress(destination); 
			socket.send(packetToSend);
		} catch (IOException e) {	e.printStackTrace(); }
		
	}
	
	
	public synchronized void start() throws Exception {
		DatagramPacket packetToSend;
		InetAddress localhost = InetAddress.getLocalHost();	
		Scanner input = new Scanner(System.in);
		String inputString = "";
		do
		{
			System.out.println("If you wish to send a message please type it here, if you wish to quit then type 'q'");
			inputString = input.nextLine();
			if(!inputString.equals("q")) {
				inputString = SEND_HEADER + inputString + "|"; // adding '|' at the end for proper parsing
				packetToSend = new PacketContent(inputString).toDatagramPacket();
				InetSocketAddress destination = new InetSocketAddress(localhost,50002); //??? how to know where to send if multiple created???
				sendPacket(packetToSend, destination);
				this.wait();
			}
		}while(!inputString.equals("q"));
		input.close();
	}
	
	public static void main(String[] args) {
		try {	
			new EndUser(END_USER_SOCKET).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
