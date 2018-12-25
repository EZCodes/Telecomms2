import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Timer;
// need to manually start those when using default console
public class EndUser extends Machine implements Constants { 

	InetSocketAddress neighbouringRouter;
	private EndUserInputThread input;
	EndUser(int port, int routerPort){
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			socket = new DatagramSocket(port);
			neighbouringRouter = new InetSocketAddress(localhost, routerPort);
		} catch (Exception e) 
		{
			if(port >= 60000)
				e.printStackTrace();
			else
			{				
				try { // try next socket, i assume socket is taken because endUser on it already created, thus +2 to router(for my topology)
					new EndUser(++port,routerPort+2).start(); 
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
		{
			System.out.println("Message succesfully sent!");
			synchronized(this.input) {
				this.input.notify();
			}
		}
		else if(recievedString.contains(HELLACK_HEADER))
		{
			System.out.println("Connection with router established!");
			this.notify();
		}
		else if(recievedString.contains(SEND_HEADER)) {
			InetSocketAddress destination = (InetSocketAddress) recievedPacket.getSocketAddress();
			DatagramPacket ack = new PacketContent(SENDACK_HEADER).toDatagramPacket();
			sendPacket(ack,destination);
			String[] stringContent = recievedString.split("[|]");
			System.out.println("You recieved a message!\n"+ stringContent[2]);
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
		packetToSend = new PacketContent(HELLO_HEADER).toDatagramPacket();
		sendPacket(packetToSend, this.neighbouringRouter);
		TimeoutTimer task = new TimeoutTimer(this,packetToSend, this.neighbouringRouter);
		timer.schedule(task, TIMEOUT_TIME,TIMEOUT_TIME); // 7 sec timeout timer
		this.wait();
		timer.cancel();
		this.input = new EndUserInputThread(this);
		this.input.start();
		this.wait();
	}
	
	public static void main(String[] args) {
		try {	
			new EndUser(STARTING_END_USER_PORT,STARTING_ROUTER_PORT).start();
			System.out.println("Program completed(end user)");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
	

}
