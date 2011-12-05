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
	private long transferStart, transferTime = 0;
	
	public TransferManager(Network network, JobQueue jobs, ResultMap result) throws IOException {
		this.network = network;
		this.jobs = jobs;
		this.result = result;
		
		if(!network.isConnected())
			network.connect();
		System.out.println("> TransferManager connected");
	}
	
	public long getTransferTime() {
		return transferTime;
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
	
	/**
	 * Write out count number of jobs to the network
	 * @param count
	 * @return the number of jobs actually sent
	 */
	public int pushJobs(int count) {
		writeLock.lock();
		for(int i = 0; i < count; i++) {
			int[] pixels = jobs.poll();
			if(pixels == null)
				return i;
			
			transferStart = System.currentTimeMillis();
			writeMessage("job_syn");
			writeInt(pixels.length);
			for(int pixel : pixels)
				writeInt(pixel);
		}
		writeLock.unlock();
		
		return count;
	}
	
	/**
	 * Request count jobs to be sent
	 * @param count
	 */
	public void pullJobs(int count) {
		writeLock.lock();
		writeMessage("job_pull");
		writeInt(count);
		writeLock.unlock();
	}
	
	/**
	 * Add a job from the network into the local queue
	 */
	private void readJob() {
		int count = readInt();
		IntBuffer buffer = IntBuffer.allocate(count);
		for(int i = 0; i < count; i++)
			buffer.put(readInt());
		jobs.add(buffer);
	}
	
	/**
	 * Send our result map over the network
	 */
	private void writeResult() {
		writeLock.lock();
		writeMessage("result_ack");
		writeInt(result.size());
		for(Map.Entry<Integer, Integer> pair : result.entrySet()) {
			writeInt(pair.getKey());
			writeInt(pair.getValue());
		}
		writeLock.unlock();
	}
	
	/**
	 * Add a result map to our local map
	 */
	private void readResult() {
		int count = readInt();
		for(int i = 0; i < count; i++) {
			Integer pixel = readInt(), num = readInt();
			result.increment(pixel, num);
		}
	}
	
	/**
	 * Notify all threads that the transfer manager has received
	 * an important ACK
	 */
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
				writeResult();
				signalStep();
			}
			else if(line.equals("result_ack")) {
				readResult();
				signalStep();
			}
			else if(line.equals("job_syn")) {
				readJob();
				writeMessage("job_ack");
			}
			else if(line.equals("job_ack")) {
				transferTime = System.currentTimeMillis() - transferStart;
			}
			else if(line.equals("job_pull")) {
				pushJobs(readInt());
			}
		}
	}
}
