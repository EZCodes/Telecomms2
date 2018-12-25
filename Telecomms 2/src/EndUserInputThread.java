import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.Timer;

public class EndUserInputThread extends Thread implements Constants {
	
	EndUser endUser;
	
	EndUserInputThread(Object endUser){
		super();
		if(endUser instanceof EndUser)
			this.endUser=(EndUser) endUser;

	}
	@Override 
	public synchronized void run() {
		try {
		Timer timer = new Timer(true);
		TimeoutTimer task;
		DatagramPacket packetToSend;
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
				InetSocketAddress destination = this.endUser.neighbouringRouter; 
				this.endUser.sendPacket(packetToSend, destination);
				
				task = new TimeoutTimer(this,packetToSend, destination);
				timer.schedule(task, TIMEOUT_TIME,TIMEOUT_TIME); // 7 sec timeout timer
				this.wait();
				task.cancel();	
			}
		}while(!inputString.equals("q"));
		input.close();
		timer.cancel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		synchronized(this.endUser) { //lock on subscriber to wake it up
			endUser.notify();
		}
		
	}
	
	
	
	
	
}
