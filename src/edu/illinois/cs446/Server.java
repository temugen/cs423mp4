package edu.illinois.cs446;

import java.io.IOException;
import java.net.ServerSocket;


public class Server extends Network {
	private ServerSocket serverSocket;
	
	public Server(int port) throws IOException {
		try {
		    serverSocket = new ServerSocket(port);
		} catch (IOException e) {
		    System.out.println("Could not listen on port: " + port);
		    System.exit(-1);
		}
		
		try {
		    remoteSocket = serverSocket.accept();
		} catch (IOException e) {
		    System.out.println("Accept failed: " + port);
		    System.exit(-1);
		}
		
		connect();
	}
	
	public void close() throws IOException {
		super.close();
		serverSocket.close();
	}
}
