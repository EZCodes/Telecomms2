import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

public class EndUser extends Machine implements Constants {

	InetSocketAddress neighbouringRouter;
	EndUser(int port, int routerPort){
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			socket = new DatagramSocket(port);
			neighbouringRouter = new InetSocketAddress(localhost, routerPort);
			start();
		} catch (Exception e) 
		{
			if(port >= 60000)
				e.printStackTrace();
			else
			{
				port++;
				try {
					new EndUser(port, routerPort).start();
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
		if(recievedString.contains(SENDACK_HEADER))
			System.out.println("Message succesfully sent!");
		else if(recievedString.contains(FORWARD_HEADER)) {
			InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
			DatagramPacket ack = new PacketContent(FORACK_HEADER).toDatagramPacket();
			sendPacket(ack,destination);
			String[] stringContent = recievedString.split("[|]");
			System.out.println("You recieved a message!\n"+ stringContent[3]);
		}
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
		Scanner input = new Scanner(System.in);
		String inputString = "";
		do
		{
			System.out.println("If you wish to send a message please type it here, if you wish to quit then type 'q'");
			inputString = input.nextLine();
			if(!inputString.equals("q")) {
				System.out.println("Which user you wih to send this to?");
				String destinationString = input.nextLine();
				inputString = SEND_HEADER +destinationString + "|" + inputString + "|"; // adding '|' at the end for proper parsing
				packetToSend = new PacketContent(inputString).toDatagramPacket();
				InetSocketAddress destination = neighbouringRouter; 
				sendPacket(packetToSend, destination);
				this.wait();
			}
		}while(!inputString.equals("q"));
		input.close();
	}
	
	public static void main(String[] args) {
		try {	
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
