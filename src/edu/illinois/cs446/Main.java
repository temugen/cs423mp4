package edu.illinois.cs446;

import java.io.IOException;

public class Main {
	
	private static int initial_matrix1[][] = {{3, 1}, {5, 2}};
	private static int initial_matrix2[][] = {{2, 7}, {4, 6}};
	private static Network network;
	
	private static void sendMatrix(Matrix matrix, String id) {
		network.write(id);
		network.write(matrix.rows.toString());
		network.write(matrix.columns.toString());
		for(int i = 0; i < matrix.rows; i++) {
			for(int j = 0; j < matrix.columns; j++) {
				network.write(new Integer(matrix.matrix[i][j]).toString());
			}
		}
	}
	
	private static Matrix readMatrix() throws NumberFormatException, IOException {
		int rows = new Integer(network.read());
		int columns = new Integer(network.read());
		int matrix[][] = new int[rows][columns];
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				matrix[i][j] = new Integer(network.read());
			}
		}
		return new Matrix(matrix);
	}
	
	private static Matrix multiplyMatrix(Matrix matrix1, Matrix matrix2) {
		int result[][] = new int[matrix1.rows][matrix2.columns];
		for (int i = 0; i < matrix1.rows; i++)
			for (int j = 0; j < matrix2.columns; j++)
		        for (int k = 0; k < matrix1.columns; k++)
		        	result[i][j] += matrix1.matrix[i][k] * matrix2.matrix[k][j];
		return new Matrix(result);
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Matrix matrix1 = null, matrix2 = null;
		boolean done = false;
	
		if(args.length > 1) {
			network = new Client(args[0], new Integer(args[1]));
		}
		else {
			network = new Server(new Integer(args[0]));
			
			//Sending half of matrix1
			matrix1 = new Matrix(initial_matrix1);
			Matrix matrix1_remote = matrix1.splitRows();
			sendMatrix(matrix1_remote, "matrix1");
			
			//Sending matrix2
			matrix2 = new Matrix(initial_matrix2);
			Matrix matrix2_remote = matrix2;
			sendMatrix(matrix2_remote, "matrix2");
			
			done = true;
		}

		Matrix result;
		while(!done) {
			String line = network.read();
			if(line == null)
				continue;
			
			if(line.equals("matrix1")) {
				matrix1 = readMatrix();
			}
			else if(line.equals("matrix")) {
				matrix2 = readMatrix();
				done = true;
			}
		}
		
		result = multiplyMatrix(matrix1, matrix2);
		for(int i = 0; i < result.rows; i++) {
			for(int j = 0; j < result.columns; j++) {
				System.out.print(new Integer(result.matrix[i][j]).toString() + " ");
			}
			System.out.println();
		}
	}
}
