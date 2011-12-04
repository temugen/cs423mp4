package edu.illinois.cs446;

import java.io.IOException;
import java.util.Map;


public class Adaptor {
	private static final int jobSize = 1000;
	private static float throttle = 0.01f;
	private static final long statePeriod = 100;
	private static final int threshold = 100;
	
	private static final JobQueue jobs = new JobQueue(jobSize);
	private static final ResultMap result = new ResultMap();
	private static TransferManager transferManager;
	private static StateManager stateManager;
	private static boolean isMaster = false;
	
	private static void initClient(String host, int port) throws IOException {
		isMaster = true;
	
		transferManager = new TransferManager(new Client(host, port), jobs, result);
		stateManager = new StateManager(new Client(host, port + 1), jobs, statePeriod, throttle);
		
		//Load all of the jobs
		ImageManager images = new ImageManager();
		images.load("/Users/temugen/Desktop/images");
		jobs.add(images.getPixels());
		
		//Send half of the data to the server
		int halfCount = jobs.size() / 2;
		transferManager.pushJobs(halfCount);
		transferManager.writeMessage("bootstrap_syn");
	}
	
	private static void initServer(int port) throws IOException {
		transferManager = new TransferManager(new Server(port), jobs, result);
		stateManager = new StateManager(new Server(port + 1), jobs, statePeriod, throttle);
	}
	
	private static void waitForNextStep() throws InterruptedException {
		synchronized(transferManager) {
			transferManager.wait();
		}
	}
	
	private static void printResult() {
		for(Map.Entry<Integer, Integer> pair : result.entrySet())
			System.out.println("<" + Integer.toHexString(pair.getKey()) + "," + pair.getValue() + ">");
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("> Adaptor loaded");
		
		//Bootstrap
		if(args.length > 1)
			initClient(args[0], new Integer(args[1]));
		else
			initServer(new Integer(args[0]));
		transferManager.start();
		waitForNextStep();
		System.out.println("> Bootstrapped");
		
		//Start worker threads
		Worker worker = new Worker(jobs, result, stateManager);
		stateManager.addWorker(worker);
		worker.start();
		System.out.println("> Started Worker threads");
		
		//Dynamically push or pull jobs based on local and remote state
		stateManager.start();
		while(isMaster) {
			
			//We don't have valid state yet
			if(!stateManager.isValid()) {
				Thread.sleep(statePeriod);
				continue;
			}
			
			int remoteState = stateManager.getRemoteState();
			int state = stateManager.getLocalState();
			
			System.out.println("remoteState: " + remoteState + ", state: " + state);
			
			//There is no more work left
			if(remoteState == 0 && state == 0) {
				transferManager.writeMessage("result_syn");
				break;
			}
			
			//Calculate the ideal number of jobs to transfer in order to balance
			int remoteScaling = stateManager.getRemoteScaling();
			int scaling = stateManager.getLocalScaling();
			int transferCount = (state - remoteState) / (scaling + remoteScaling);
			System.out.println("remoteScaling: " + remoteScaling + ", scaling: " + scaling);
			System.out.println("remoteCpu: " + stateManager.getRemoteCpuUsage() + " Cpu: " + stateManager.getLocalCpuUsage());
			if(transferCount == 0)
				continue;
			
			//Adjust transferCount to minimize the combined transfer and processing time
			boolean negative = transferCount < 0;
			transferCount = Math.abs(transferCount);
			long jobTime = stateManager.getLocalJobTime();
			long remoteJobTime = stateManager.getRemoteJobTime();
			long transferTime = transferManager.getTransferTime();
			//System.out.println("remoteJobTime: " + remoteJobTime + ", jobTime: " + jobTime);
			//System.out.println("transferTime: " + transferTime);
			if(negative) {
				if(transferTime != 0 && remoteJobTime != 0)
					transferCount = (int)Math.floor(transferCount / ((transferTime / remoteJobTime) + 1));
				
				if(transferCount > threshold) {
					transferManager.pullJobs(transferCount);
					System.out.println("> Pulled " + transferCount + " jobs");
				}
			}
			else {
				if(transferTime != 0 && jobTime != 0)
					transferCount = (int)Math.floor(transferCount / ((transferTime / jobTime) + 1));
				
				if(transferCount > threshold) {
					transferManager.pushJobs(transferCount);
					System.out.println("> Pushed " + transferCount + " jobs");
				}
			}
			
			Thread.sleep(statePeriod);
		}
		
		//Wait for results to be transferred, print them, and kill all threads
		waitForNextStep();
		/*if(isMaster)
			printResult();*/
		System.out.println("> Complete");
		System.exit(0);
	}
}
