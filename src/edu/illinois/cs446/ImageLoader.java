package edu.illinois.cs446;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;


public class ImageLoader {
	private String directory;
	private List<BufferedImage> images = new ArrayList<BufferedImage>();
	
	public ImageLoader(String directory) {
		this.directory = directory;
		
		File dir = new File(directory);
		File[] files = dir.listFiles();
		for(File file : files) {
			
			String filename = file.getName();
			//only attempt to read jpg or png files
			if(!file.isFile() || !(filename.endsWith("jpg") || filename.endsWith("png")))
				continue;
			
			System.out.println(filename);
			
			try {
				BufferedImage image = ImageIO.read(file);
				images.add(image);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
