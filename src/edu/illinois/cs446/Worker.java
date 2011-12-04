package edu.illinois.cs446;


public class Worker extends Thread {
	private JobQueue jobs;
	private ResultMap result;
	private StateManager stateManager;
	private long workTime = 0;
	
	public Worker(JobQueue jobs, ResultMap result, StateManager stateManager) {
		this.jobs = jobs;
		this.result = result;
		this.stateManager = stateManager;
	}
	
	@Override
	public void run() {
		while(true) {
			float throttle = stateManager.getThrottle();
			workTime = doWork();
			long sleepTime = calculateSleepTime(workTime, throttle);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public long doWork() {
		int[] pixels = null;
		
		try {
			pixels = jobs.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return 0;
		}
		
		long start = System.currentTimeMillis(), end;
		for(Integer pixel : pixels)
			result.increment(pixel, 1);
		end = System.currentTimeMillis();
		
		return (end - start);
	}
	
	private static long calculateSleepTime(long workTime, float throttle) {
		return (long)(workTime * ((1.0f-throttle) / throttle));
	}
	
	public static long getTotalTime(long workTime, float throttle) {
		return workTime + calculateSleepTime(workTime, throttle);
	}
	
	public long getWorkTime() {
		return workTime;
	}
}
