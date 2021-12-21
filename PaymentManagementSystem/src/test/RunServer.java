package test;

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
