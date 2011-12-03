package edu.illinois.cs446;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Map;


public class Adaptor {
	private static final JobQueue jobs = new JobQueue(1000);
	private static final ResultMap result = new ResultMap();
	private static TransferManager transferManager;
	private static StateManager stateManager;
	private static final HardwareMonitor hardwareMonitor = new HardwareMonitor();
	
	private static void initClient(String host, int port) throws IOException {
		//Split pixels in half
		ImageManager images = new ImageManager();
		images.load("/Users/temugen/Desktop/images");
		IntBuffer split[] = ImageManager.splitPixels(images.getPixels());
		jobs.add(split[0]);
		
		//Send half of the data to the server
		transferManager = new TransferManager(new Client(host, port), jobs, result);
		transferManager.writeMessage("bootstrapped_syn");
		transferManager.writeMessage(new Integer(split[1].capacity()).toString());
		split[1].rewind();
		while(split[1].hasRemaining())
			transferManager.writeMessage(Integer.toString(split[1].get(), Character.MAX_RADIX));
	}
	
	private static void initServer(int port) throws IOException {
		transferManager = new TransferManager(new Server(port), jobs, result);
	}
	
	private static void waitForNextStep() throws InterruptedException {
		synchronized(transferManager) {
			transferManager.wait();
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		boolean isClient = false;
		
		//Start worker threads
		Worker worker = new Worker(jobs, result, 0.7f);
		worker.start();
		System.out.println("Started worker threads...");
		
		//Bootstrap
		if(args.length > 1) {
			initClient(args[0], new Integer(args[1]));
			isClient = true;
		}
		else {
			initServer(new Integer(args[0]));
		}
		transferManager.start();
		waitForNextStep();
		System.out.println("Bootstrapped...");
		
		//Commander loop
		if(isClient) {
			//Wait for jobs to complete
			while(!jobs.isEmpty())
				Thread.sleep(100);
			
			transferManager.writeMessage("finished_syn");
			
			//Wait for results to be transferred and print results
			waitForNextStep();
			for(Map.Entry<Integer, Integer> pair : result.entrySet())
				System.out.println("<" + Integer.toHexString(pair.getKey()) + "," + pair.getValue() + ">");
		}
		else {
			waitForNextStep();
		}
		
		System.out.println("Finished...");
		
		//Kill all threads
		System.exit(0);
	}
}
