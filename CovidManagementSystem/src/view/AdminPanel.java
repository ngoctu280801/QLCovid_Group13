package view;

import javax.swing.JFrame;
import javax.swing.JOptionPane;



import org.apache.log4j.Logger;

import model.DbInteraction;


public class AdminPanel extends JFrame {
	private DbInteraction dbi;
	private static final Logger logger = Logger.getLogger(AdminPanel.class); 
	
	/**
	 * Create the frame.
	 */
	public AdminPanel(DbInteraction dbi) {
		this.dbi = dbi;
		JOptionPane.showMessageDialog(null, 
				"Successfull. This is Admin Account!");
		logger.debug("Successfull. Login Admin Account!"); 
		dbi.close();
		
		System.exit(0);
	}
}
