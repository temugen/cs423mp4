package edu.illinois.cs446;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class StateManager extends Thread {
	private Network network;
	private JobQueue jobs;
	private float throttle;
	private Timer timer = new Timer();
	private Lock writeLock = new ReentrantLock();
	private HardwareMonitor hardwareMonitor;
	private int remoteState = 1, remoteScaling = 1, remoteCpuUsage = 1, remoteJobTime = 1;
	private float remoteThrottle = 1.0f;
	private List<Worker> workers = new ArrayList<Worker>();
	private long period;
	
	private class SendStateTask extends TimerTask {
		private StateManager stateManager;
		
		public SendStateTask(StateManager stateManager) {
			this.stateManager = stateManager;
		}
		
		@Override
		public void run() {
			stateManager.writeMessage("state");
			stateManager.writeInt(stateManager.getLocalState());
			stateManager.writeInt(stateManager.getLocalScaling());
			stateManager.writeFloat(stateManager.getLocalThrottle());
			stateManager.writeInt(stateManager.getLocalCpuUsage());
			stateManager.writeInt(stateManager.getLocalJobTime());
		}	
	}
	
	public StateManager(Network network, JobQueue jobs, long period, float throttle) throws IOException {
		this.network = network;
		this.jobs = jobs;
		this.throttle = throttle;
		this.period = period;
		hardwareMonitor = new HardwareMonitor(period);
		
		if(!network.isConnected())
			network.connect();
		System.out.println("> StateManager connected");
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
	
	public float getLocalThrottle() {
		return throttle;
	}
	
	public int getLocalScaling() {
		return Math.max(1, hardwareMonitor.getCpuUsage() / 10) * Math.max(1, (int)((1.0f - throttle) * 100) / 10) * Math.max(1, getLocalJobTime() / 10);
	}

	public int getLocalState() {
		return jobs.size() * getLocalScaling();
	}
	
	public int getLocalJobTime() {
		int count = workers.size();
		if(count == 0)
			return 0;
		
		long totalTime = 0;
		for(Worker worker: workers)
			totalTime += worker.getJobTime();
		return (int)(totalTime / count);
	}
	
	public int getLocalCpuUsage() {
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
		return remoteState;
	}
	
	@Override
	public void run() {
		if(network instanceof Server)
			timer.scheduleAtFixedRate(new SendStateTask(this), 0, period);
		
		while(true) {
			String line = readMessage();
			if(line == null)
				continue;
			
			if(line.equals("state")) {
				remoteState = readInt();
				remoteScaling = readInt();
				remoteThrottle = readFloat();
				remoteCpuUsage = readInt();
				remoteJobTime = readInt();
			}
		}
	}
}
