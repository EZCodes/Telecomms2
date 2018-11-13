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
		
		System.out.println("Connection timeout! Trying to resend.");
		Router router;

		if(machine instanceof Router)
		{
			router = (Router) machine;
			router.sendPacket(packet, address);
		}

					
	}

	
}
