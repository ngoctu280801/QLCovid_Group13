package test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import view.PaymentServer;

public class RunServer {
	
	public static void main(String[] args) {
		final Logger logger = Logger.getLogger(RunServer.class);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					PaymentServer server = new PaymentServer("cert/server.keystore", "88888888", 9009);
					server.setVisible(true);
					logger.debug("Màn hình server");  
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
