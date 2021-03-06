package model;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

public class ClientConnection extends Thread {
	private SSLSocket sslSocket;
	private DbInteraction dbi = null;
	private final Logger logger;
	
	
	public ClientConnection(SSLSocket sock, String pHostname,
							String pDbName, String pUsrName, 
							String pPwd, Logger logger){
		this.logger = logger;
		
		sslSocket = sock;
		try {
			dbi = new DbInteraction(pHostname, pDbName, pUsrName, pPwd);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.error("Lỗi không thể kết nối tới CSDL");
			
			System.out.println("Không thể kết nối đến CSDL");
		}
	}
	public void run(){
		try{
			InputStream inStream = sslSocket.getInputStream();
			//OutputStream outStream = sslSocket.getOutputStream();
			BufferedReader bfr = new BufferedReader(new InputStreamReader(inStream));
			String msg = bfr.readLine();
			if(msg != null){
				System.out.println(msg);
				//System.out.flush();
			}
			String[] data = msg.split(" ");
			String code = doTrans(data[0], Integer.parseInt(data[1]));
			dbi.close();
			OutputStreamWriter os = new OutputStreamWriter(sslSocket.getOutputStream());
			PrintWriter out = new PrintWriter(os);
			out.println(code);
			out.flush();
			bfr.close();
			sslSocket.close();
		} catch(IOException e){
			
			
			logger.debug("Ngắt kết nối đột ngột");
			
			e.printStackTrace();
		}
	}
	public String doTrans(String usrName, int credit){
		int code = -1;
		try {
			CallableStatement st = dbi.getStatement("{call doTransaction(?, ?, ?)}");
			st.registerOutParameter(3, Types.INTEGER);
			st.setString(1, usrName);
			st.setString(2, credit + "");
			st.execute();
			code  = st.getInt("code");
			dbi.getConnection().commit();
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				dbi.getConnection().rollback();
				logger.debug("Rollback giao dịch thành công");
			} catch (SQLException e1) {
				
				// TODO Auto-generated catch block
				
				e1.printStackTrace();
				
				logger.error(e);
			}
			
			e.printStackTrace();
			
			logger.error(e);
			System.out.println("Không thể kết nối đến CSDL");
		}
		return code + "";
	}
}
