package edu.illinois.cs446;

import javax.imageio.ImageIO;
import java.nio.IntBuffer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;


public class ImageManager {
	private List<BufferedImage> images = new ArrayList<BufferedImage>();
	
	/**
	 * Load all of the images in a directory into a list
	 * @param directory
	 */
	public void load(String directory) {
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
	
	/**
	 *
	 * @return the pixels corresponding to all of the images in our list
	 */
	public IntBuffer getPixels() {
		int size = getSize();
		IntBuffer buff = IntBuffer.allocate(size);
		for(BufferedImage image : images) {
			int w = image.getWidth(), h = image.getHeight();
			buff.put(image.getRGB(0, 0, w, h, null, 0, w));
		}
		return buff;
	}
	
	/**
	 * 
	 * @return the total number of pixels all of our images have
	 */
	private int getSize() {
		int size = 0;
		for(BufferedImage image : images) {
			size += image.getHeight() * image.getWidth();
		}
		return size;
	}
}
