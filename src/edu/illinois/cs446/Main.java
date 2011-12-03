package edu.illinois.cs446;

import java.io.IOException;

public class Main {
	
	private static Network network;
	private static ImageManager images = new ImageManager();
	
	private static void initClient(String host, int port) throws IOException {
		images.load("/Users/temugen/Desktop/images");
		ImageManager remote = images.split();
		network = new Client(host, port);
	}
	
	private static void initServer(int port) throws IOException {
		network = new Server(port);
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		boolean done = false;
	
		if(args.length > 1)
			initClient(args[0], new Integer(args[1]));
		else
			initServer(new Integer(args[0]));

		while(!done) {
			String line = network.read();
			if(line == null)
				continue;
		}
	}
}
