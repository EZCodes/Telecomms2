
public interface Constants {

	final static String HELLO_HEADER = "000|";
	final static String HELLACK_HEADER = "001|";
	
	final static String INFOREQUEST_HEADER = "010|";
	final static String INFO_HEADER = "011|"; // |Source|Destination|NextHop|PrevHop|
	final static String INFOACK_HEADER = "111|";
	
	final static String SEND_HEADER = "000|";
	final static String SENDACK_HEADER = "001|";
	
	final static int STARTING_ROUTER_PORT = 50001;
	final static int STARTING_END_USER_PORT = 51000;
	final static int CONTROLLER_SOCKET = 50000;
}
