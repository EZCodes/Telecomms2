import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.Timer;
// need to manually start those when using default console
public class EndUser extends Machine implements Constants {

	InetSocketAddress neighbouringRouter;
	static int currentEndUserSocket = STARTING_END_USER_PORT;
	private static int offset = 0; // variable to indicate a neighbouring router when creating is required
	EndUser(int port){
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			socket = new DatagramSocket(port);
			neighbouringRouter = new InetSocketAddress(localhost, STARTING_ROUTER_PORT + offset);
			System.out.println(currentEndUserSocket);
		} catch (Exception e) 
		{
			if(port >= 60000)
				e.printStackTrace();
			else
			{				
				try {
					new EndUser(++currentEndUserSocket).start(); // try next socket
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		listener.go();
		offset += 2;
		currentEndUserSocket++;
	}
	
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		PacketContent recievedData = new PacketContent(recievedPacket);
		String recievedString = recievedData.toString();
		if(recievedString.contains(SENDACK_HEADER))
		{
			System.out.println("Message succesfully sent!");
			this.notify();
		}
		else if(recievedString.contains(SEND_HEADER)) {
			InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
			DatagramPacket ack = new PacketContent(SENDACK_HEADER).toDatagramPacket();
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
		Timer timer = new Timer(true);
		Scanner input = new Scanner(System.in);
		String inputString = "";
		do
		{
			System.out.println("If you wish to send a message please type it here, if you wish to quit then type 'q'");
			inputString = input.nextLine();
			if(!inputString.equals("q")) {
				System.out.println("Which user you wish to send this to?");
				String destinationString = input.nextLine();
				inputString = SEND_HEADER +destinationString + "|" + inputString + "|"; // adding '|' at the end for proper parsing
				packetToSend = new PacketContent(inputString).toDatagramPacket();
				InetSocketAddress destination = neighbouringRouter; 
				sendPacket(packetToSend, destination);
			//	TimeoutTimer task = new TimeoutTimer(this,packetToSend, destination);
			//	timer.schedule(task, TIMEOUT_TIME,TIMEOUT_TIME); // 7 sec timeout timer
			//	this.wait();
			//	timer.cancel();	
			}
		}while(!inputString.equals("q"));
		input.close();
	}
	
	public static void main(String[] args) {
		try {	
			new EndUser(currentEndUserSocket).start();
			System.out.println("Program completed(end user)");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
	

}
