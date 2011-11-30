package edu.illinois.cs446;

public class Matrix {
	public Integer columns, rows;
	public int matrix[][];
	
	public Matrix(int[][] matrix) {
		this.matrix = matrix;
		rows = matrix.length;
		columns = matrix[0].length;
	}
	
	public Matrix splitRows() {
		int halfRows = rows / 2;
		int[][] firstHalf = new int[halfRows][columns], secondHalf = new int[rows - halfRows][columns];
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				if(i < halfRows) {
					firstHalf[i][j] = matrix[i][j];
				}
				else {
					secondHalf[i - halfRows][j] = matrix[i][j];
				}
			}
		}
		matrix = firstHalf;
		rows = rows / 2;
		return new Matrix(secondHalf);
	}
	
	public Matrix splitColumns() {
		int halfColumns = columns / 2;
		int[][] firstHalf = new int[rows][halfColumns], secondHalf = new int[rows][columns - halfColumns];
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				if(j < halfColumns) {
					firstHalf[i][j] = matrix[i][j];
				}
				else {
					secondHalf[i][j - halfColumns] = matrix[i][j];
				}
			}
		}
		matrix = firstHalf;
		columns = columns / 2;
		return new Matrix(secondHalf);
	}
}
