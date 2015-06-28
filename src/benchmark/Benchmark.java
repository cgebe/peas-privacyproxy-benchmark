package benchmark;

import issuer.server.IssuerServer;


import onion.node.Node;
import receiver.server.ReceiverServer;
import util.Config;


public class Benchmark {
	public static void main(String[] args) throws Exception {
		if (Config.getInstance().getValue("TYPE").equals("receiver")) {
			
			ReceiverServer receiver = new ReceiverServer(Integer.parseInt(Config.getInstance().getValue("PORT")));
			receiver.run();
					
	    } else if (Config.getInstance().getValue("TYPE").equals("issuer")) {
	    	
	    	IssuerServer issuer = new IssuerServer(Integer.parseInt(Config.getInstance().getValue("PORT")));
	    	issuer.run();
	    	
	    } else if (Config.getInstance().getValue("TYPE").equals("onion")) {
	    	
	    	Node node = new Node(Integer.parseInt(Config.getInstance().getValue("PORT")));
	    	node.run();
	    }
	}
}
