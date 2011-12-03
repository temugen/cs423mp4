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
	
	public void add(IntBuffer buffer) {
		buffer.rewind();
		while(buffer.hasRemaining()) {
			int[] newJob = new int[Math.min(jobSize, buffer.remaining())];
			buffer.get(newJob);
			this.add(newJob);
		}
	}
}
