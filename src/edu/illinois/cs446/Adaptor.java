package edu.illinois.cs446;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;


public class Adaptor {
	private static final int jobSize = 10000;
	private static float throttle = 0.7f;
	private static final long statePeriod = 100;
	private static final int threshold = 100;
	private static final int workers = 2;
	
	private static final JobQueue jobs = new JobQueue(jobSize);
	private static final ResultMap result = new ResultMap();
	private static TransferManager transferManager;
	private static StateManager stateManager;
	private static boolean isMaster = false;
	private static GUI gui;
	
	/**
	 * Initialize a client (master) to connect to server (slave) at host on port
	 * @param host
	 * @param port
	 * @throws IOException
	 */
	private static void initClient(String host, int port) throws IOException {
		isMaster = true;
	
		transferManager = new TransferManager(new Client(host, port), jobs, result);
		transferManager.start();
		stateManager = new StateManager(new Client(host, port + 1), jobs, statePeriod, throttle);
		stateManager.start();
		gui = new GUI(stateManager, isMaster);
		new Thread(gui).start();
		
		//Load all of the jobs
		ImageManager images = new ImageManager();
		images.load("/Users/temugen/Desktop/images");
		jobs.add(images.getPixels());
		
		//Send half of the data to the server
		int halfCount = jobs.size() / 2;
		transferManager.pushJobs(halfCount);
		transferManager.writeMessage("bootstrap_syn");
	}
	
	/**
	 * Initialize a server (slave) on port
	 * @param port
	 * @throws IOException
	 */
	private static void initServer(int port) throws IOException {
		transferManager = new TransferManager(new Server(port), jobs, result);
		transferManager.start();
		stateManager = new StateManager(new Server(port + 1), jobs, statePeriod, throttle);
		stateManager.start();
		gui = new GUI(stateManager, isMaster);
		new Thread(gui).start();
	}
	
	/**
	 * Create multiple workers to exploit multiple cores/CPUs
	 */
	private static void initWorkers() {
		for(int i = 0; i < workers; i++) {
			Worker worker = new Worker(jobs, result, stateManager);
			stateManager.addWorker(worker);
			worker.start();
		}
	}
	
	/**
	 * Wait for an ACK from the transferManager
	 * @throws InterruptedException
	 */
	private static void waitForNextStep() throws InterruptedException {
		synchronized(transferManager) {
			transferManager.wait();
		}
	}
	
	/**
	 * Prints out the key value pairs of our ResultMap and attempts to 
	 * make an image of the pairs which is drawn on the GUI
	 */
	private static void displayResult() {
		int width = 300, height = 400;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int x = 0, y = 0;
		
		for(Map.Entry<Integer, Integer> pair : result.entrySet()) {
			System.out.println("<" + Integer.toHexString(pair.getKey()) + "," + pair.getValue() + ">");
			
			if(y == height)
				continue;
			for(int i = 0; i < Math.min(pair.getValue() / 10, width * 10); i++) {
				image.setRGB(x, y, pair.getKey());
				x++;
				if(x == width) {
					x = 0;
					y++;
				}
				if(y == height)
					break;
			}
		}
				
		gui.displayResult(image);
	}
	
	/**
	 * 
	 * @return whether the local nor remote nodes have any jobs left 
	 */
	private static boolean isComplete() {
		int remoteState = stateManager.getRemoteState();
		int state = stateManager.getLocalState();
		return (remoteState == 0 && state == 0);
	}
	
	/**
	 * 
	 * @return the number of jobs that will balance the nodes given instant network speed
	 */
	private static int getIdealTransferCount() {
		int remoteState = stateManager.getRemoteState();
		int state = stateManager.getLocalState();
		int remoteScaling = stateManager.getRemoteScaling();
		int scaling = stateManager.getLocalScaling();
		int transferCount = (state - remoteState) / (scaling + remoteScaling);
		return transferCount;
	}
	
	/**
	 * 
	 * @param transferCount
	 * @return adjust the number of jobs to transfer based on network and job processing speed
	 */
	private static int smoothTransferCount(int transferCount) {
		boolean negative = transferCount < 0;
		transferCount = Math.abs(transferCount);
		long jobTime = stateManager.getLocalJobTime();
		long remoteJobTime = stateManager.getRemoteJobTime();
		long transferTime = transferManager.getTransferTime();
		
		if(negative) {
			if(transferTime != 0 && remoteJobTime != 0)
				transferCount = (int)Math.floor(transferCount / ((transferTime / remoteJobTime) + 1));
			
			if(transferCount > threshold)
				return -transferCount;
		}
		else {
			if(transferTime != 0 && jobTime != 0)
				transferCount = (int)Math.floor(transferCount / ((transferTime / jobTime) + 1));
			
			if(transferCount > threshold)
				return transferCount;
		}
		
		return 0;
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
		waitForNextStep();
		System.out.println("> Bootstrapped");
		
		//Start workers
		initWorkers();
		System.out.println("> Started Worker threads");
		
		//Dynamically push or pull jobs based on local and remote state
		while(isMaster) {
			
			//We don't have valid state yet
			if(!stateManager.isValid()) {
				Thread.sleep(statePeriod);
				continue;
			}
			
			//There is no more work left
			if(isComplete()) {
				transferManager.writeMessage("result_syn");
				break;
			}
			
			//Calculate the ideal number of jobs to transfer in order to balance
			int transferCount = getIdealTransferCount();
			//Adjust transferCount to minimize the combined transfer and processing time
			transferCount = smoothTransferCount(transferCount);
			if(transferCount == 0)
				continue;
			
			if(transferCount > 0) {
				transferManager.pushJobs(transferCount);
				System.out.println("> Pushed " + transferCount + " jobs");
			}
			else {
				transferManager.pullJobs(-transferCount);
				System.out.println("> Pulled " + -transferCount + " jobs");
			}
			
			Thread.sleep(statePeriod);
		}
		
		//Wait for results to be transferred and print them
		waitForNextStep();
		if(isMaster)
			displayResult();
		System.out.println("> Complete");
		
		//Kill the non-master node
		if(!isMaster)
			System.exit(0);
	}
}
