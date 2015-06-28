package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;


public class Observer {
	
	private static Observer instance;
	
	private Measurement queryTime;
	private int currentClients;
	private int totalClients;
	private double output;
	private double input;
	private int requestsOut;
	private int requestsIn;
	
	
	private Observer() {
		this.setCurrentClients(0);
		this.setTotalClients(0);
		this.setOutput(0.0);
		this.setInput(0.0);
		this.setRequestsOut(0);
		this.setRequestsIn(0);
		this.queryTime = new Measurement();
	}
	
	public static Observer getInstance () {
		if (Observer.instance == null) {
			Observer.instance = new Observer();
		}
		return Observer.instance;
	}
	
	public synchronized int getRequestsIn() {
		return requestsIn;
	}

	public synchronized void setRequestsIn(int requestsIn) {
		this.requestsIn = requestsIn;
	}

	public synchronized int getRequestsOut() {
		return requestsOut;
	}

	public synchronized void setRequestsOut(int requestsOut) {
		this.requestsOut = requestsOut;
	}

	public synchronized double getInput() {
		return input;
	}

	public synchronized void setInput(double input) {
		this.input = input;
	}

	public synchronized double getOutput() {
		return output;
	}

	public synchronized void setOutput(double output) {
		this.output = output;
	}

	public synchronized int getTotalClients() {
		return totalClients;
	}

	public synchronized void setTotalClients(int totalClients) {
		this.totalClients = totalClients;
	}

	public synchronized int getCurrentClients() {
		return currentClients;
	}

	public synchronized void setCurrentClients(int currentClients) {
		this.currentClients = currentClients;
	}
	
	public synchronized Measurement getQueryTime() {
		return queryTime;
	}

	public synchronized void setQueryTime(Measurement queryTime) {
		this.queryTime = queryTime;
	}

	
	public synchronized void printResultsToFile() {
		
		String out = currentClients + "\t"
				+ totalClients + "\t"
				+ output / 1000000 + "\t"
				+ input / 1000000 + "\t"
				+ requestsOut + "\t"
				+ requestsIn;
		
		/*
		String out = "CurrentClients: " + currentClients 
				+ " TotalClients: " + totalClients 
				+ " Output (MB): " + output / 1000000
				+ " Input (MB): " + input / 1000000
				+ " RequestsOut: " + requestsOut
				+ " RequestsIn: " + requestsIn;
				*/
		

		try {
			String jarPath = new File(Observer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath();
			try {
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(jarPath + "/" + "server.log", true)));
				writer.println(out);
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

	}
	
	public void printQueryTime() {
		// TODO Auto-generated method stub
		
	}
	
	public void reset() {
		this.setOutput(0.0);
		this.setInput(0.0);
		this.setRequestsOut(0);
		this.setRequestsIn(0);
	}


	

}
