package edu.illinois.cs446;

import java.io.IOException;
import java.util.Map;


public class Adaptor {
	private static final int jobSize = 1000;
	private static final float throttle = 0.7f;
	private static final long statePeriod = 1000;
	
	private static final JobQueue jobs = new JobQueue(jobSize);
	private static final ResultMap result = new ResultMap();
	private static TransferManager transferManager;
	private static StateManager stateManager;
	private static boolean isMaster = false;
	
	private static void initClient(String host, int port) throws IOException {
		isMaster = true;
		
		//Load all of the jobs
		ImageManager images = new ImageManager();
		images.load("/Users/temugen/Desktop/images");
		jobs.add(images.getPixels());
		
		//Send half of the data to the server
		int halfCount = jobs.size() / 2;
		transferManager = new TransferManager(new Client(host, port), jobs, result);
		transferManager.sendJobs(halfCount);
		
		stateManager = new StateManager(new Client(host, port + 1), jobs, statePeriod);
		transferManager.writeMessage("bootstrapped_syn");
	}
	
	private static void initServer(int port) throws IOException {
		transferManager = new TransferManager(new Server(port), jobs, result);
		stateManager = new StateManager(new Server(port + 1), jobs, statePeriod);
	}
	
	private static void waitForNextStep() throws InterruptedException {
		synchronized(transferManager) {
			transferManager.wait();
		}
	}
	
	private static void printResults() {
		for(Map.Entry<Integer, Integer> pair : result.entrySet())
			System.out.println("<" + Integer.toHexString(pair.getKey()) + "," + pair.getValue() + ">");
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		//Start worker threads
		Worker worker = new Worker(jobs, result, throttle);
		worker.start();
		System.out.println("Started worker threads...");
		
		//Bootstrap
		if(args.length > 1)
			initClient(args[0], new Integer(args[1]));
		else
			initServer(new Integer(args[0]));
		transferManager.start();
		waitForNextStep();
		System.out.println("Bootstrapped...");
		
		//Dynamically push or pull jobs based on local and remote state
		while(isMaster) {
			int remoteState = stateManager.getRemoteState();
			int localState = stateManager.getLocalState();
			
			if(remoteState == 0 && localState == 0) {
				transferManager.writeMessage("finished_syn");
				break;
			}
		}
		
		//Wait for results to be transferred and print results
		waitForNextStep();
		printResults();
		
		System.out.println("Finished...");
		
		//Kill all threads
		System.exit(0);
	}
}
