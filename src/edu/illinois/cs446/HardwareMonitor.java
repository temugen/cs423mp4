package edu.illinois.cs446;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;


public class HardwareMonitor {
	private Timer timer = new Timer();
	private CollectHardwareInfoTask collectHardwareInfoTask = new CollectHardwareInfoTask();
	
	/**
	 * This timer task reads the cpu idle from sar and inverts it
	 * @author temugen
	 *
	 */
	private class CollectHardwareInfoTask extends TimerTask {
		protected int cpuUsage = 100;
		
		@Override
		public void run() {
			try {
				Runtime rt = Runtime.getRuntime();
				Process pr = rt.exec("sar -u 1 1");
				BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String lastLine = null, line;
				while((line = input.readLine()) != null)
					lastLine = line;
				lastLine = lastLine.trim();
				int start = lastLine.lastIndexOf(" ") + 1;
				cpuUsage = 100 - (int)Float.parseFloat(lastLine.substring(start));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	/**
	 * Sets a timer with a given period to collect cpu usage
	 * @param period
	 */
	public HardwareMonitor(long period) {
		timer.scheduleAtFixedRate(collectHardwareInfoTask, 0, period);
	}
	
	public int getCpuUsage() {
		return collectHardwareInfoTask.cpuUsage;
	}
}
