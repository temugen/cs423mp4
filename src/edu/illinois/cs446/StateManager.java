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
	private int remoteState = 1, remoteScaling = 1;
	private float remoteThrottle = 1.0f;
	
	private class SendStateTask extends TimerTask {
		private StateManager stateManager;
		
		public SendStateTask(StateManager stateManager) {
			this.stateManager = stateManager;
		}
		
		@Override
		public void run() {
			stateManager.writeInt(stateManager.getState());
			stateManager.writeInt(stateManager.getScaling());
			stateManager.writeFloat(stateManager.getThrottle());
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
	
	public void writeFloat(float num) {
		writeMessage(Float.toString(num));
	}
	
	public float readFloat() {
		return Float.parseFloat(readMessage());
	}
	
	public void writeMessage(String message) {
		writeLock.lock();
		network.write(message);
		writeLock.unlock();
	}
	
	public float getThrottle() {
		return throttle;
	}
	
	public int getScaling() {
		return Math.max(1, hardwareMonitor.getCpuUsage()) * Math.max(1, (int)((1 - throttle) * 100));
	}

	public int getState() {
		return jobs.size() * getScaling();
	}
	
	public float getRemoteThrottle() {
		return remoteThrottle;
	}
	
	public int getRemoteScaling() {
		return remoteScaling;
	}
	
	public int getRemoteState() {
		remoteState = readInt();
		remoteScaling = readInt();
		remoteThrottle = readFloat();
		return remoteState;
	}
}
