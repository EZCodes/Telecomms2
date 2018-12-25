import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.TimerTask;

public class TimeoutTimer extends TimerTask {
	
	Object machine;
	DatagramPacket packet;
	InetSocketAddress address;
	TimeoutTimer(Object machine, DatagramPacket packet, InetSocketAddress address)
	{
		super();
		this.machine = machine;
		this.packet = packet;
		this.address = address;
	}

	@Override
	public void run() {
		if(!(machine instanceof Network))
				System.out.println("Connection timeout! Trying to resend.");
		Router router;
		Controller controller;
		EndUser endUser;
		Network network;

		if(machine instanceof Router)
		{		
			router = (Router) machine;	
			router.sendPacket(packet, address);
		}
		else if(machine instanceof Controller)
		{
			controller = (Controller) machine;
			controller.sendPacket(packet, address);
		}
		else if(machine instanceof EndUser)
		{
			endUser = (EndUser) machine;
			endUser.sendPacket(packet, address);
		}
		else if(machine instanceof Network)
		{
			network = (Network) machine;
			synchronized(network)
			{
				network.notify();
			}
		}

					
	}

	
}
