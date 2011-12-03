package edu.illinois.cs446;

import java.util.concurrent.ConcurrentHashMap;


public class Worker extends Thread {
	private JobQueue jobs;
	private ConcurrentHashMap<Integer, Integer> result;
	private float throttle = 1.0f;
	
	public Worker(JobQueue jobs, ConcurrentHashMap<Integer, Integer> result, float throttle) {
		this.jobs = jobs;
		this.result = result;
		this.throttle = throttle;
	}
	
	public void run() {
		while(true) {
			long time = doWork();
			long sleepTime = (long)(time * ((1.0f-throttle) / throttle));
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
		for(Integer pixel : pixels) {
			if (result.putIfAbsent(pixel, 1) == null) {
		        break;
		    }
			
			Integer count;
			do {
				count = result.get(pixel);
			} while(!result.replace(pixel, count, count + 1));
		}
		end = System.currentTimeMillis();
		
		return (end - start);
	}
}
