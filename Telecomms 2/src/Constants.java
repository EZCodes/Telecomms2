
public interface Constants {

	final static String HELLO_HEADER = "0000|";
	//final static String HELLACK_HEADER = "1|";
	
	final static String FEATURE_REQUEST_HEADER = "0001|";
	final static String FEATURE_HEADER = "0010|";
	final static String FEATACK_HEADER = "0100|";
	
	final static String INFOREQUEST_HEADER = "1000|";
	final static String INFO_HEADER = "0011|"; // |Destination|NextHop|
	final static String INFOACK_HEADER = "0110|";
	
	final static String SEND_HEADER = "1100|";
	final static String SENDACK_HEADER = "0111|";
		
	final static int STARTING_ROUTER_PORT = 50001;
	final static int STARTING_END_USER_PORT = 51000;
	final static int CONTROLLER_SOCKET = 50000;
	
	final static int NUMBER_OF_ROUTERS = 7;
	final static int NUMBER_OF_END_USERS = 4;
	
	final static int TIMEOUT_TIME = 7000;
	
	final static String END_USER = "E";
	final static String ROUTER = "R";
	final static String CONTROLLER = "C";
}
