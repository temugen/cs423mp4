package edu.illinois.cs446;

import java.io.IOException;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Network network;
		if(args.length > 1)
			network = new Client(args[0], new Integer(args[1]));
		else
			network = new Server(new Integer(args[0]));
	
		network.write("Hello");
		System.out.println(network.read());
	}
}
