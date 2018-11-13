// class for creating routers
public class Network implements Constants {


	
	Network(int amountOfRouters, int amountOfEndUsers){
		for(int i = 0; i<amountOfRouters; i++)
		{
			new Router(STARTING_ROUTER_PORT + i);
		}
		for(int i = 0; i<amountOfEndUsers; i++)
		{
			int offset = 0;
			new EndUser(STARTING_END_USER_PORT, STARTING_ROUTER_PORT + offset);
			offset += 2;
		}
		
		
	}
	
}
