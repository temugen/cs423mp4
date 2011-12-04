package edu.illinois.cs446;

import java.nio.IntBuffer;
import java.util.concurrent.LinkedBlockingQueue;


public class JobQueue extends LinkedBlockingQueue<int[]> {
	private static final long serialVersionUID = 1L;
	private int jobSize;
	
	public JobQueue(int jobSize) {
		super();
		this.jobSize = jobSize;
	}
	
	/**
	 * Chunk a buffer into jobs of jobSize and add them to our queue
	 * @param buffer
	 */
	public void add(IntBuffer buffer) {
		buffer.rewind();
		while(buffer.hasRemaining()) {
			int[] newJob = new int[Math.min(jobSize, buffer.remaining())];
			buffer.get(newJob);
			add(newJob);
		}
	}
}
