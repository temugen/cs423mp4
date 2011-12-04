package edu.illinois.cs446;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Network {
	protected PrintWriter out;
	protected BufferedReader in;
	protected Socket remoteSocket;
	private boolean connected = false;
	protected String host;
	protected int port;
	
	public void connect() throws IOException {
		out = new PrintWriter(remoteSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));
		connected = true;
	}
	
	public String read() throws IOException {
		return in.readLine();
	}
	
	public void write(String line) {
		out.println(line);
	}
	
	public void close() throws IOException {
		connected = false;
		in.close();
		out.close();
		remoteSocket.close();
	}
	
	public boolean isConnected() {
		return connected;
	}
}
