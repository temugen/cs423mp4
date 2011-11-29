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
	
	protected void connect() throws IOException {
		out = new PrintWriter(remoteSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));
	}
	
	public String read() throws IOException {
		return in.readLine();
	}
	
	public void write(String line) throws IOException {
		out.println(line);
	}
	
	public void close() throws IOException {
		in.close();
		out.close();
		remoteSocket.close();
	}
}
