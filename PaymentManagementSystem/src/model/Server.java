package model;

import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

public class Server {
	public static boolean stop;
	public static SSLServerSocket serverSocket;
	private static Logger logger;
	
	
	public Server(Logger logger){
		
		stop = false;
		
		this.logger = logger;
		
		serverSocket = null;
	}
	public void start(int port) throws IOException{
		
		// Khởi tạo SSL server socket factory
		
		
		SSLServerSocketFactory ssf = 
							(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		
		
		// Tạo server socket SSL
		serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
		
		
		System.out.println("Đã bật server");
		
		logger.debug("Server đang chạy");
		
		while(!stop){
			// Luồng hiện tại accept kết nối
			SSLSocket sslSocket = (SSLSocket) serverSocket.accept();
			
			// Các kết nối đã được accept đưa qua luồng khác để xử lí
			ClientConnection client = new ClientConnection(sslSocket, 
										"localhost", "qlcovid", "root", "", logger);
			client.run();
		}
		
		serverSocket.close();
		
		serverSocket = null;
		
		logger.debug("Đã tắt server");
		
		System.out.println("Đã tắt server");
	}
}
