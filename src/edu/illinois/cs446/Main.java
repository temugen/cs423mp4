package edu.illinois.cs446;

import java.io.IOException;
import java.nio.IntBuffer;


public class Main {
	
	private static Network network;
	private static ImageManager images = new ImageManager();
	private static JobQueue jobs = new JobQueue();
	private static boolean done = false;
	
	private static void initClient(String host, int port) throws IOException {
		//Split out pixels in half
		images.load("/Users/temugen/Desktop/images");
		IntBuffer split[] = ImageManager.splitPixels(images.getPixels());
		jobs.add(split[0]);
		
		//Send half of the data to the server
		network = new Client(host, port);
		network.write("pixels");
		network.write(new Integer(split[1].capacity()).toString());
		split[1].position(0);
		for(int i = 0; i < split[1].capacity(); i++) {
			network.write(Integer.toHexString(split[1].get()));
		}
		
		done = true;
	}
	
	private static void initServer(int port) throws IOException {
		network = new Server(port);
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(args.length > 1)
			initClient(args[0], new Integer(args[1]));
		else
			initServer(new Integer(args[0]));

		while(!done) {
			String line = network.read();
			if(line == null)
				continue;
			
			if(line.equals("pixels")) {
				int count = new Integer(network.read());
				IntBuffer buffer = IntBuffer.allocate(count);
				for(int i = 0; i < count; i++) {
					String hex = "0x" + network.read();
					buffer.put(Integer.decode(hex));
				}
				jobs.add(buffer);
				
				done = true;
			}
		}
		
		System.out.println("done!");
	}
}
