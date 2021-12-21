package model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.mysql.cj.jdbc.Driver;

public class DbInteraction {
	private Connection con = null;
	private String usrName, dbName, hostname, pwd;
	public DbInteraction(String pHostname,
			String pDbName, String pUsrName, String pPwd) throws SQLException{
		dbName = pDbName;
		hostname = pHostname;
		usrName = pUsrName;
		pwd = pPwd;
		String strCon = "jdbc:mysql://" + hostname + "/" + dbName +
				"?useUnicode=true&characterEncoding=utf-8";
		Properties prop = new Properties();
		prop.put("user", usrName);
		prop.put("password", pwd);
		Driver driver;
		driver = new Driver();
		con = driver.connect(strCon, prop);
		con.setAutoCommit(false);

	}
	public void close(){
		if(con != null){
			try {
				con.close();
				//JOptionPane.showMessageDialog(null, "Ngắt kết nối với cơ sở dữ liệu thành công");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//JOptionPane.showMessageDialog(null, "Có lỗi khi ngắt kết nối với cơ sở dữ liệu");
			}
		}
	}
	public CallableStatement getStatement(String s){
		CallableStatement statement = null;
		try {
			statement = con.prepareCall(s);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Không thể tạo statement");
		}
		return statement;
	}
	public Connection getConnection(){
		return con;
	}
}
