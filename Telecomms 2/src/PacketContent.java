import java.net.DatagramPacket;

public class PacketContent implements PacketConversion {
	
	String string;
	DatagramPacket packet;
	
	PacketContent(String string)
	{
		this.string = string;
	}
	PacketContent(DatagramPacket packet)
	{
		this.packet = packet;
	}
	
	public DatagramPacket toDatagramPacket() {
		DatagramPacket packet= null;
		try {
			byte[] data= string.getBytes();
			packet= new DatagramPacket(data, data.length);
		}
		catch(Exception e) {e.printStackTrace();}
		return packet;
	}
	public String toString() {
		byte[] data;
		
		data= packet.getData();
		string = new String(data);
		return string;
	}
}
