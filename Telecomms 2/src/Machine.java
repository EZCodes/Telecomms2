import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;


public abstract class Machine {
	final static int PACKET_SIZE = 65536; // standard packet size
	DatagramSocket socket;
	Listener listener;
	CountDownLatch latch;  // used for pausing
	
	Machine(){ 
		latch= new CountDownLatch(1);
		listener= new Listener();
		listener.setDaemon(true); // method invoked to start a thread
		listener.start();	
	}
	
	public abstract void onReceipt(DatagramPacket recievedPacket); // do something on receipt
	
	
	public abstract void sendPacket(DatagramPacket packetToSend, InetSocketAddress destination); // send packets

	
	
	class Listener extends Thread{
		
		public void go() { // indicate that server has been set up adn socket initialised
			latch.countDown();
			
		}
		
		public void run() { // needed for Thread class
			try {
				latch.await(); // method waits until latch counted down
				while(true)
				{
					DatagramPacket recievedPacket = new DatagramPacket(new byte[PACKET_SIZE],PACKET_SIZE); // byte array
					socket.receive(recievedPacket);
					
					onReceipt(recievedPacket);
				}
				
			}catch(Exception e) {
				if (!(e instanceof SocketException)) e.printStackTrace(); // if problem with socket, print it
			}
			
		}
	}
}
