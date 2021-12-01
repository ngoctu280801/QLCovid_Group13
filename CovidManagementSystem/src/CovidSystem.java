import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import view.LoginScr;
import model.DbInteraction;

public class CovidSystem {
	private static final Logger logger = Logger.getLogger(CovidSystem.class); 
	public static void main(String[] args) {
		Statement[] stmt = new Statement[] {null};
		try {
			final DbInteraction dbi = new DbInteraction("localhost", "qlcovid", "root", "");
			
			
			ResultSet res = dbi.query("call isFirstInit();", stmt);
			res.next();
			
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					LoginScr lgScr = new LoginScr(dbi);
					lgScr.setVisible(true);
					logger.debug("Bắt đầu đăng nhập.");  
				}
			});

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Không thể kết nối đến CSDL.");  
			JOptionPane.showMessageDialog(null,
					"Kết nối tới cơ sở dữ liệu thất bại. Vui lòng thử lại sau!");
		} finally {
			try {
				if(stmt[0] != null){
					stmt[0].close();
				}				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		
		
		
	}
}
