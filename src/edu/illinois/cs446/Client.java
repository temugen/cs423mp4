package edu.illinois.cs446;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Network {
	private int port;
	private String host;
	
	public Client(String host, int port) throws IOException {
		this.port = port;
		this.host = host;
		
		try {
			remoteSocket = new Socket(host, port);
		} catch (UnknownHostException e) {
			System.out.println("Could not find host: " + host);
		    System.exit(-1);
		} catch (IOException e) {
			System.out.println("Could not connect to port: " + port);
		    System.exit(-1);
		}
		
		connect();
	}
}
