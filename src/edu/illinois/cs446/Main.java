package edu.illinois.cs446;

import java.io.IOException;
import java.nio.IntBuffer;


public class Main {
	
	private static Network network;
	private static ImageManager images = new ImageManager();
	private static JobQueue jobs = new JobQueue();
	
	private static IntBuffer splitPixels(IntBuffer pixels) {
		int halfCapacity = pixels.capacity() / 2;
		IntBuffer split1 = IntBuffer.allocate(halfCapacity);
		IntBuffer split2 = IntBuffer.allocate(pixels.capacity() - halfCapacity);
		
		pixels.position(0);
		for(int i = 0; i < pixels.capacity(); i++) {
			if(i < halfCapacity)
				split1.put(pixels.get());
			else
				split2.put(pixels.get());
		}
		pixels = split1;
		return split2;
	}
	
	private static void initClient(String host, int port) throws IOException {
		//Split out pixels in half
		images.load("/Users/temugen/Desktop/images");
		IntBuffer pixels = images.getPixels();
		IntBuffer remote = splitPixels(pixels);
		
		//Send half of the data to the server
		network = new Client(host, port);
		network.write("pixels");
		network.write(new Integer(remote.capacity()).toString());
		remote.position(0);
		for(int i = 0; i < remote.capacity(); i++) {
			network.write(Integer.toHexString(remote.get()));
		}
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
			
			if(line.equals("pixels")) {
				int count = new Integer(network.read());
				IntBuffer buffer = IntBuffer.allocate(count);
				for(int i = 0; i < count; i++)
					buffer.put(Integer.decode(network.read()));
				jobs.add(buffer);
			}
		}
	}
}
