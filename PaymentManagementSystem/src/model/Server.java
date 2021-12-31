package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server {
	public static boolean stop;
	public static SSLServerSocket serverSocket;
	public Server(){
		stop = false;
		serverSocket = null;
	}
	public void start(int port) throws IOException{
		SSLServerSocketFactory ssf = 
				(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
		System.out.println("Đã bật server");
		while(!stop){
			SSLSocket sslSocket = (SSLSocket) serverSocket.accept();
			ClientConnection client = new ClientConnection(sslSocket, "localhost", "qlcovid", "root", "");
			client.run();
		}
		serverSocket.close();
		serverSocket = null;
		System.out.println("Đã tắt server");
	}
}
