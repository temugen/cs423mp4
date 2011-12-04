package edu.illinois.cs446;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class StateManager {
	private Network network;
	private JobQueue jobs;
	private float throttle;
	private Timer timer = new Timer();
	private Lock writeLock = new ReentrantLock();
	private HardwareMonitor hardwareMonitor;
	
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
	
	public StateManager(Network network, JobQueue jobs, long period, float throttle) throws IOException {
		this.network = network;
		this.jobs = jobs;
		this.throttle = throttle;
		hardwareMonitor = new HardwareMonitor(period);
		
		if(!network.isConnected())
			network.connect();
		System.out.println("> StateManager connected");
		
		if(network instanceof Server)
			timer.scheduleAtFixedRate(new SendStateTask(this), 0, period);
	}
	
	public float getThrottle() {
		return throttle;
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
		return jobs.size() * hardwareMonitor.getCpuUsage() * (int)((1 - throttle) * 100);
	}
	
	public int getRemoteState() {
		return readInt();
	}
}
