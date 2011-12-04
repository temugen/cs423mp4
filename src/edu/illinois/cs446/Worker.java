package edu.illinois.cs446;


public class Worker extends Thread {
	private JobQueue jobs;
	private ResultMap result;
	private StateManager stateManager;
	private long jobTime = 0;
	
	public Worker(JobQueue jobs, ResultMap result, StateManager stateManager) {
		this.jobs = jobs;
		this.result = result;
		this.stateManager = stateManager;
	}
	
	@Override
	public void run() {
		while(true) {
			float throttle = stateManager.getLocalThrottle();
			long workTime = doWork();
			long sleepTime = (long)(workTime * ((1.0f - throttle) / throttle));
			jobTime = workTime + sleepTime;
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Read in all of the pixels in a job and increment their result values
	 * @return the time it takes to process the job
	 */
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
	
	public long getJobTime() {
		return jobTime;
	}
}
