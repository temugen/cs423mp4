package edu.illinois.cs446;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
	private int remoteState = 1, remoteScaling = 1, remoteCpuUsage = 1, remoteJobTime = 1;
	private float remoteThrottle = 1.0f;
	private List<Worker> workers = new ArrayList<Worker>();
	
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
			stateManager.writeInt(stateManager.getCpuUsage());
			stateManager.writeInt(stateManager.getJobTime());
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
	
	public void addWorker(Worker worker) {
		workers.add(worker);
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
		return Math.max(1, hardwareMonitor.getCpuUsage()) * Math.max(1, (int)((1.0f - throttle) * 100)) * Math.max(1, getJobTime());
	}

	public int getState() {
		return jobs.size() * getScaling();
	}
	
	public int getJobTime() {
		int count = workers.size();
		if(count == 0)
			return 0;
		
		long totalTime = 0;
		for(Worker worker: workers)
			totalTime += worker.getJobTime();
		return (int)(totalTime / count);
	}
	
	public int getCpuUsage() {
		return hardwareMonitor.getCpuUsage();
	}
	
	public float getRemoteThrottle() {
		return remoteThrottle;
	}
	
	public int getRemoteScaling() {
		return remoteScaling;
	}
	
	public int getRemoteJobTime() {
		return remoteJobTime;
	}
	
	public int getRemoteCpuUsage() {
		return remoteCpuUsage;
	}
	
	public int getRemoteState() {
		remoteState = readInt();
		remoteScaling = readInt();
		remoteThrottle = readFloat();
		remoteCpuUsage = readInt();
		remoteJobTime = readInt();
		return remoteState;
	}
}
