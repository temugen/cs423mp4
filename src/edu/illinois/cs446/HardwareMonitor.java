package edu.illinois.cs446;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class HardwareMonitor {
	private Timer timer = new Timer();
	private CollectHardwareInfoTask collectHardwareInfoTask = new CollectHardwareInfoTask();
	
	private class CollectHardwareInfoTask extends TimerTask {
		protected int cpuUsage = 100;
		
		public CollectHardwareInfoTask() {
		}
		
		@Override
		public void run() {
			try {
				Runtime rt = Runtime.getRuntime();
				Process pr = rt.exec("sar -u 1 1");
				BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line;
				while((line = input.readLine()) != null);
				System.out.println(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public HardwareMonitor(long period) {
		timer.scheduleAtFixedRate(collectHardwareInfoTask, 0, period);
	}
	
	public int getCpuUsage() {
		return collectHardwareInfoTask.cpuUsage;
	}
}
