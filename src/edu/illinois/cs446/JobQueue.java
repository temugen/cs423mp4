package edu.illinois.cs446;

import java.nio.IntBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JobQueue {
	private int jobSize;
	private Queue<int[]> queue = new ConcurrentLinkedQueue<int[]>();
	
	public JobQueue(int jobSize) {
		this.jobSize = jobSize;
	}
	
	public void add(IntBuffer buffer) {
		buffer.rewind();
		while(buffer.hasRemaining()) {
			int[] newJob;
			newJob = new int[Math.min(jobSize, buffer.remaining())];
			buffer.get(newJob);
			queue.add(newJob);
		}
		System.out.println(queue.size());
	}
}
