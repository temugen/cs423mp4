package edu.illinois.cs446;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client extends Network {
	
	public Client(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
	}
	
	@Override
	public void connect() throws IOException {
		try {
			remoteSocket = new Socket(host, port);
		} catch (UnknownHostException e) {
			System.out.println("Could not find host: " + host);
		    System.exit(-1);
		} catch (IOException e) {
			System.out.println("Could not connect to port: " + port);
		    System.exit(-1);
		}
		
		super.connect();
	}
}
