package edu.illinois.cs446;

import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class JobQueue {
	Queue<IntBuffer> queue = new LinkedList<IntBuffer>();
	
	public void add(IntBuffer buffer) {
		queue.add(buffer);
	}
}
