package edu.illinois.cs446;

import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JobQueue extends ConcurrentLinkedQueue<int[]> {
	private static final long serialVersionUID = 1L;
	private int jobSize;
	
	public JobQueue(int jobSize) {
		super();
		this.jobSize = jobSize;
	}
	
	public void add(IntBuffer buffer) {
		buffer.rewind();
		while(buffer.hasRemaining()) {
			int[] newJob;
			newJob = new int[Math.min(jobSize, buffer.remaining())];
			buffer.get(newJob);
			super.add(newJob);
		}
	}
}
