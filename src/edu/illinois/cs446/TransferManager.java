package edu.illinois.cs446;

import java.io.IOException;
import java.nio.IntBuffer;


public class TransferManager extends Thread {
	private Network network;
	private JobQueue jobs;
	
	public TransferManager(Network network, JobQueue jobs) {
		this.network = network;
		this.jobs = jobs;
	}
	
	private String getMessage() {
		try {
			return network.read();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void run() {
		while(true) {
			String line = getMessage();
			if(line == null)
				continue;
			
			if(line.equals("pixels")) {
				int count = new Integer(getMessage());
				IntBuffer buffer = IntBuffer.allocate(count);
				for(int i = 0; i < count; i++)
					buffer.put(Integer.parseInt(getMessage(), Character.MAX_RADIX));
				jobs.add(buffer);
			}
		}
	}
}
