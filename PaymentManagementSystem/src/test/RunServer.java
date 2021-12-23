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
					// Khởi tạo màn hình server
					PaymentServer server = new PaymentServer("cert/server.keystore", 
															"88888888", 			
															9009, 					
															logger);				
															// File chứng chỉ SSL
															// Password của file chứng chỉ
															// Port của server
															// Đối tượng logger
					
					
					
					// Hiển thị màn hình server
					server.setVisible(true);
					logger.debug("Initializing server");  
					
					
					
					
				} catch (Exception e) {
					
					
					e.printStackTrace();
					
					
				}
			}
		});
	}
}
