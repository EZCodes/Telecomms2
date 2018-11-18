
// class for creating routers
public class Network implements Constants {

	
	Network(int amountOfRouters, int amountOfEndUsers){ 
		try {
			new Controller(CONTROLLER_SOCKET).start();
			
			for(int i = 0; i<amountOfRouters; i++)
			{
				RoutingInfo neighbours = decideNeighbours(STARTING_ROUTER_PORT + i);
				new Router(STARTING_ROUTER_PORT + i, neighbours).start();;
			}
			for(int i = 0; i<amountOfEndUsers; i++)
			{
				int offset = 0;
				new EndUser(STARTING_END_USER_PORT, STARTING_ROUTER_PORT + offset).start();
				offset += 2;
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		};
	}
	/**
	 * This method defines the network topology. Instead of hardcoding every rout in the controller
	 * I'm using similar approach to the Distance vector routing and define neighbour of every router
	 * out of which controller can calculate the best rout. With this, instead of hardcoding N+N-1+..+1 routes
	 * where N is number of End users, we hardcode M routers.
	 * It's inefficient if we have low amount of End Users but a lot of routers, but very efficient if we have
	 * a lot of EndUsers and less routers(more probable imo).
	 */
	private RoutingInfo decideNeighbours(int socketNumber){
		// if over switch since i want to reinitialise with same name
		if(socketNumber==STARTING_ROUTER_PORT) {
			String[] connectionOne = {"R2", Integer.toString(STARTING_ROUTER_PORT+1)};
			String[] connectionTwo = {"R6", Integer.toString(STARTING_ROUTER_PORT+5)};
			String[] connectionThree = {"R7", Integer.toString(STARTING_ROUTER_PORT+6)};
			String endUserName = "E1";
			return new RoutingInfo(connectionOne,connectionTwo,connectionThree,endUserName);
		}
		if(socketNumber==STARTING_ROUTER_PORT+1) {
			String[] connectionOne = {"R1", Integer.toString(STARTING_ROUTER_PORT+1)};
			String[] connectionTwo = {"R3", Integer.toString(STARTING_ROUTER_PORT+2)};
			return new RoutingInfo(connectionOne,connectionTwo,null);
		}
		if(socketNumber==STARTING_ROUTER_PORT+2) {
			String[] connectionOne = {"R2", Integer.toString(STARTING_ROUTER_PORT+1)};
			String[] connectionTwo = {"R4", Integer.toString(STARTING_ROUTER_PORT+3)};
			String endUserName = "E2";
			return new RoutingInfo(connectionOne,connectionTwo, endUserName);		
		}
		if(socketNumber==STARTING_ROUTER_PORT+3) {
			String[] connectionOne = {"R3", Integer.toString(STARTING_ROUTER_PORT+2)};
			String[] connectionTwo = {"R7", Integer.toString(STARTING_ROUTER_PORT+6)};
			String[] connectionThree = {"R5", Integer.toString(STARTING_ROUTER_PORT+4)};
			return new RoutingInfo(connectionOne,connectionTwo,connectionThree,null);
		}
		if(socketNumber==STARTING_ROUTER_PORT+4) {
			String[] connectionOne = {"R4", Integer.toString(STARTING_ROUTER_PORT+1)};
			String[] connectionTwo = {"R6", Integer.toString(STARTING_ROUTER_PORT+5)};
			String endUserName = "E3";
			return new RoutingInfo(connectionOne,connectionTwo, endUserName);
		}
		if(socketNumber==STARTING_ROUTER_PORT+5) {
			String[] connectionOne = {"R1", Integer.toString(STARTING_ROUTER_PORT)};
			String[] connectionTwo = {"R5", Integer.toString(STARTING_ROUTER_PORT+4)};
			return new RoutingInfo(connectionOne,connectionTwo,null);
		}
		else { //(socketNumber==STARTING_ROUTER_PORT+6) so java wont complain
			String[] connectionOne = {"R1", Integer.toString(STARTING_ROUTER_PORT)};
			String[] connectionTwo = {"R4", Integer.toString(STARTING_ROUTER_PORT+3)};
			String endUserName = "E4";
			return new RoutingInfo(connectionOne,connectionTwo, endUserName);
		}	
	}	
	
	
	public static void main(String args[])
	{
		new Network(NUMBER_OF_ROUTERS,NUMBER_OF_END_USERS);
	}
	
}
