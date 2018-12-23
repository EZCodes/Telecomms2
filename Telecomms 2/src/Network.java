import java.util.Timer;

// class for creating routers
public class Network implements Constants { 

	
	Network(int amountOfRouters, int amountOfEndUsers){ 
		try {
			Timer timer = new Timer(true);
			new NodeThreads(CONTROLLER).start();
			for(int i = 0; i<amountOfRouters; i++)
			{
				new NodeThreads(ROUTER).start();
				// set a delay since creation of network is in separate threads which are non-deterministic and in this case routers 
				// need to have sockets one after another
				TimeoutTimer delay = new TimeoutTimer(this,null,null); 
				timer.schedule(delay,1500);
				synchronized(this) {
					this.wait();		
				}
				delay.cancel();			
			}
			/*for(int i = 0; i<amountOfEndUsers; i++) // end user setup
			{
				new NodeThreads(END_USER).start();
			}
			*/
			
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
	 static RoutingInfo decideNeighbours(int socketNumber){
		// 'if' over 'switch' since i want to reinitialise with same name
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
		System.out.println("Network setup completed!");
	}
	
}
