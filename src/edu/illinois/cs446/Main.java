package edu.illinois.cs446;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Main {
	private static Network network;
	private static final JobQueue jobs = new JobQueue(1000);
	private static final ResultMap result = new ResultMap();
	private static final Lock bootstrapped = new ReentrantLock(), finished = new ReentrantLock();
	
	private static void initClient(String host, int port) throws IOException {
		//Split pixels in half
		ImageManager images = new ImageManager();
		images.load("/Users/temugen/Desktop/images");
		IntBuffer split[] = ImageManager.splitPixels(images.getPixels());
		jobs.add(split[0]);
		
		//Send half of the data to the server
		network = new Client(host, port);
		network.write("bootstrapped_syn");
		network.write(new Integer(split[1].capacity()).toString());
		split[1].rewind();
		while(split[1].hasRemaining())
			network.write(Integer.toString(split[1].get(), Character.MAX_RADIX));
	}
	
	private static void initServer(int port) throws IOException {
		network = new Server(port);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		//Start transfer manager
		TransferManager transferManager = new TransferManager(network, jobs, result, bootstrapped, finished);
		transferManager.start();
		
		//Start worker threads
		Worker worker = new Worker(jobs, result, 0.7f);
		worker.start();
		
		//Bootstrap
		if(args.length > 1)
			initClient(args[0], new Integer(args[1]));
		else
			initServer(new Integer(args[0]));
		//Wait for bootstrap process to finish
		bootstrapped.lock();
		
		//Commander loop
		if(network instanceof Client) {
			//Wait for jobs to complete
			while(!jobs.isEmpty())
				Thread.sleep(100);
			
			network.write("finished_syn");
			
			//Wait for results to be transferred and print results
			finished.lock();
			for(Map.Entry<Integer, Integer> pair : result.entrySet())
				System.out.println("<" + Integer.toHexString(pair.getKey()) + "," + pair.getValue() + ">");
		}
		else {
			finished.lock();
		}
		
		//Kill all threads
		System.exit(0);
	}
}
