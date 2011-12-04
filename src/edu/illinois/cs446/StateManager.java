package edu.illinois.cs446;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class StateManager {
	private Network network;
	private JobQueue jobs;
	private Timer timer;
	private Lock writeLock = new ReentrantLock();
	private static final HardwareMonitor hardwareMonitor = new HardwareMonitor();
	
	private class SendStateTask extends TimerTask {
		private StateManager stateManager;
		
		public SendStateTask(StateManager stateManager) {
			this.stateManager = stateManager;
		}
		
		@Override
		public void run() {
			stateManager.writeInt(stateManager.getLocalState());
		}
		
	}
	
	public StateManager(Network network, JobQueue jobs, long period) {
		this.network = network;
		this.jobs = jobs;
		
		if(network instanceof Server)
			timer.scheduleAtFixedRate(new SendStateTask(this), 0, period);
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
	
	public int getLocalState() {
		return jobs.size();
	}
	
	public int getRemoteState() {
		return readInt();
	}
}
