
public class NodeThreads extends Thread implements Constants{
	String typeOfNode;
	
	NodeThreads(String typeOfNode)
	{
		this.typeOfNode=typeOfNode;
	}
	
	@Override
	public synchronized void run()
	{
		try {
		if(this.typeOfNode == CONTROLLER)
			Controller.main(null);
		else if(this.typeOfNode == ROUTER)
			Router.main(null);
		else if(this.typeOfNode == END_USER)
			EndUser.main(null);
		}catch(Exception e) { e.printStackTrace(); }
	}

}
