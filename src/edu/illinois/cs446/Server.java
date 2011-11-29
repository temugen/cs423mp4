package edu.illinois.cs446;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Network {
	private ServerSocket serverSocket;
	private int port;
	
	public Server(int port) throws IOException {
		this.port = port;
		
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
}
