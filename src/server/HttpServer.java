package server;

import java.io.*;
import java.net.*;

public class HttpServer {
	int port;
	int cnt;
	ServerSocket svr = null;

	public HttpServer(int p) {
		this.port=p;
	}

	public static void main(String a[]) {
		int port = -1;
		try {
			port = Integer.parseInt(a[0]);
		} catch (Exception err) {
			System.err.println("Usage: <port>");
			System.exit(-1);
		}

		new HttpServer(port).run();
	}

	public void run() {

		try {
			svr = new ServerSocket(port);
		} catch (IOException err) {
			System.err.println("Socket invalid or in use");
			System.exit(-1);
		}

		while (true) {
			try {
				Socket client = svr.accept();
				cnt++;
				new HttpHandler(client,cnt).start();
			} catch (IOException err) {}
		}
	}
}

