package edu.illinois.cs446;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class TransferManager extends Thread {
	private Network network;
	private JobQueue jobs;
	private ResultMap result;
	private Lock writeLock = new ReentrantLock();
	
	public TransferManager(Network network, JobQueue jobs, ResultMap result) {
		this.network = network;
		this.jobs = jobs;
		this.result = result;
	}
	
	private String readMessage() {
		try {
			return network.read();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void writeInt(int num) {
		writeMessage(Integer.toString(num, Character.MAX_RADIX));
	}
	
	public int readInt() {
		return Integer.parseInt(readMessage(), Character.MAX_RADIX);
	}
	
	public void writeMessage(String message) {
		writeLock.lock();
		network.write(message);
		writeLock.unlock();
	}
	
	public int sendJobs(int count) {
		for(int i = 0; i < count; i++) {
			int[] pixels = jobs.poll();
			if(pixels == null)
				return i;
			
			writeMessage("job");
			writeInt(pixels.length);
			for(int pixel : pixels)
				writeInt(pixel);
		}
		
		return count;
	}
	
	private void readJob() {
		int count = readInt();
		IntBuffer buffer = IntBuffer.allocate(count);
		for(int i = 0; i < count; i++)
			buffer.put(readInt());
		jobs.add(buffer);
	}
	
	private void writeResult() {
		writeInt(result.size());
		for(Map.Entry<Integer, Integer> pair : result.entrySet()) {
			writeInt(pair.getKey());
			writeInt(pair.getValue());
		}
	}
	
	private void readResult() {
		int count = readInt();
		for(int i = 0; i < count; i++) {
			Integer pixel = readInt(), num = readInt();
			result.increment(pixel, num);
		}
	}
	
	private void signalStep() {
		synchronized (this) {
			notifyAll();
		}
	}
	
	@Override
	public void run() {
		while(true) {
			String line = readMessage();
			if(line == null)
				continue;
			
			if(line.equals("bootstrap_syn")) {
				writeMessage("bootstrap_ack");
				signalStep();
			}
			else if(line.equals("bootstrap_ack")) {
				signalStep();
			}
			else if(line.equals("result_syn")) {
				writeMessage("result_ack");
				writeResult();
				signalStep();
			}
			else if(line.equals("result_ack")) {
				readResult();
				signalStep();
			}
			else if(line.equals("job")) {
				readJob();
			}
		}
	}
}
