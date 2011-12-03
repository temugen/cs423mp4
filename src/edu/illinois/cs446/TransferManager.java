package edu.illinois.cs446;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Map;


public class TransferManager extends Thread {
	private Network network;
	private JobQueue jobs;
	private ResultMap result;
	
	public TransferManager(Network network, JobQueue jobs, ResultMap result) {
		this.network = network;
		this.jobs = jobs;
		this.result = result;
	}
	
	private String getMessage() {
		try {
			return network.read();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void writeMessage(String message) {
		network.write(message);
	}
	
	private void readPixels() {
		int count = new Integer(getMessage());
		IntBuffer buffer = IntBuffer.allocate(count);
		for(int i = 0; i < count; i++)
			buffer.put(Integer.parseInt(getMessage(), Character.MAX_RADIX));
		jobs.add(buffer);
	}
	
	private void writeResult() {
		network.write(new Integer(result.size()).toString());
		for(Map.Entry<Integer, Integer> pair : result.entrySet()) {
			network.write(Integer.toString(pair.getKey(), Character.MAX_RADIX));
		}
	}
	
	private void readResult() {
		int count = new Integer(getMessage());
		for(int i = 0; i < count; i++) {
			Integer key = Integer.parseInt(getMessage(), Character.MAX_RADIX);
			Integer value = Integer.parseInt(getMessage(), Character.MAX_RADIX);
			result.increment(key, value);
		}
	}
	
	private void signalStep() {
		synchronized (this) {
			notifyAll();
		}
	}
	
	public void run() {
		while(true) {
			String line = getMessage();
			if(line == null)
				continue;
			
			if(line.equals("bootstrapped_syn")) {
				readPixels();
				network.write("bootstrapped_ack");
				signalStep();
			}
			else if(line.equals("bootstrapped_ack")) {
				signalStep();
			}
			else if(line.equals("finished_syn")) {
				network.write("finished_ack");
				writeResult();
				signalStep();
			}
			else if(line.equals("finished_ack")) {
				readResult();
				signalStep();
			}
			else if(line.equals("pixels_syn")) {
				readPixels();
				network.write("pixels_ack");
			}
			else if(line.equals("pixels_ack")) {
				
			}
		}
	}
}
