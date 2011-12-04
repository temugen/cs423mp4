package edu.illinois.cs446;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class GUI extends JFrame implements Runnable, ChangeListener {
	private static final long serialVersionUID = 1L;
	private StateManager stateManager;
	private JSlider slider = new JSlider(1, 100);
	private JProgressBar bar = new JProgressBar();
	private Timer timer = new Timer();
	private int initialJobCount = 0;
	
	private class UpdateProgressTask extends TimerTask {
		private StateManager stateManager;
		private JProgressBar bar;
		
		public UpdateProgressTask(JFrame frame, StateManager stateManager, JProgressBar bar) {
			this.stateManager = stateManager;
			this.bar = bar;
		}
		
		@Override
		public void run() {
			int jobCount = stateManager.getLocalJobsLeft() + stateManager.getRemoteJobsLeft();
			if(jobCount > initialJobCount)
				initialJobCount = jobCount;
			int difference = (int)(((float)(initialJobCount - jobCount) / initialJobCount) * 100);
			bar.setValue(difference);
		}	
	}
	
	public GUI(StateManager stateManager, boolean isMaster) {
		super("MP4");
		this.stateManager = stateManager;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new GridLayout(10, 1));
		this.setSize(400, 600);
		
		slider.setPaintTicks(false);
		slider.setValue((int)(stateManager.getLocalThrottle() * 100));
		slider.addChangeListener(this);
		getContentPane().add(slider);
		
		if(isMaster) {
			bar.setMinimum(0);
			bar.setMaximum(100);
			timer.schedule(new UpdateProgressTask(this, stateManager, bar), 0, 100);
			getContentPane().add(bar);
		}
	}
	
	public void stateChanged(ChangeEvent evt) {
		JSlider slider = (JSlider) evt.getSource();
		if (!slider.getValueIsAdjusting()) {
			int value = slider.getValue();
			stateManager.setThrottle((float) value / 100.0f);
			System.out.println("> Changed throttle to " + stateManager.getLocalThrottle());
		}
	}
	
	public void displayResult(BufferedImage image) {
		getGraphics().drawImage(image, this.getWidth() / 2 - image.getWidth() / 2, this.getHeight() - image.getHeight() - 10, null);
	}

	@Override
	public void run() {
		this.setVisible(true);
	}
}
