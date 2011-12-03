package edu.illinois.cs446;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;


public class ImageManager {
	private String directory;
	private List<BufferedImage> images = new ArrayList<BufferedImage>();
	
	public void load(String directory) {
		this.directory = directory;
		
		File dir = new File(directory);
		File[] files = dir.listFiles();
		for(File file : files) {
			
			String filename = file.getName();
			//only attempt to read jpg or png files
			if(!file.isFile() || !(filename.endsWith("jpg") || filename.endsWith("png")))
				continue;
			
			try {
				BufferedImage image = ImageIO.read(file);
				images.add(image);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ImageManager split() {
		ImageManager man = new ImageManager();
		int totalSize = getSize();
		int currentSize = 0;
		while(currentSize < totalSize / 2)
			man.add(images.remove(images.size() - 1));
		return man;
	}
	
	private int getSize() {
		int size = 0;
		for(BufferedImage image : images) {
			size += image.getHeight() * image.getWidth();
		}
		return size;
	}
	
	public void add(BufferedImage image) {
		images.add(image);
	}
}
