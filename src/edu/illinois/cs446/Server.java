package edu.illinois.cs446;

import java.io.IOException;
import java.net.ServerSocket;


public class Server extends Network {
	private ServerSocket serverSocket;
	
	public Server(int port) throws IOException {
		this.port = port;
	}
	
	@Override
	public void connect() throws IOException {
		try {
		    serverSocket = new ServerSocket(port);
		} catch (IOException e) {
		    System.out.println("Could not listen on port: " + port);
		    System.exit(-1);
		}
		
		try {
		    remoteSocket = serverSocket.accept();
		    this.host = remoteSocket.getInetAddress().getHostAddress();
		} catch (IOException e) {
		    System.out.println("Accept failed: " + port);
		    System.exit(-1);
		}
		
		super.connect();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		serverSocket.close();
	}
}
