package edu.illinois.cs446;

import java.io.IOException;

public class Main {
	
	private static int initial_matrix[][] = {{3, 1}, {5, 2}};
	private static int initial_vector[] = {2, 7};

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		boolean done = false;
		
		int matrix[][] = null;
		int vector[] = null;
	
		Network network;
		if(args.length > 1) {
			network = new Client(args[0], new Integer(args[1]));
		}
		else {
			network = new Server(new Integer(args[0]));
			
			vector = initial_vector;
			
			//Sending initial data
			network.write("vector");
			network.write(new Integer(initial_vector.length).toString());
			for(Integer num : initial_vector) {
				network.write(num.toString());
			}
			
			done = true;
		}

		while(!done) {
			String line = network.read();
			if(line == null)
				continue;
			System.out.println(line);
			
			if(line.equals("vector")) {
				int count = new Integer(network.read());
				vector = new int[count];
				for(int i = 0; i < count; i++) {
					vector[i] = new Integer(network.read());
				}
			}
		}
		
		for(int i = 0; i < vector.length; i++) {
			System.out.println(new Integer(vector[i]).toString());
		}
	}
}
