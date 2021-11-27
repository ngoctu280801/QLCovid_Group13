package view;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import javax.swing.JOptionPane;

import model.DbInteraction;

public class ManagerPanel extends JFrame {
	private DbInteraction dbi;
	private String usrManager;
	private static final Logger logger = Logger.getLogger(ManagerPanel.class);

	/**
	 * Create the frame.
	 */
	public ManagerPanel(DbInteraction dbi, String usrManager) {
		this.usrManager = usrManager;
		this.dbi = dbi;
		JOptionPane.showMessageDialog(null, "Successfull. This is Manager Account!");
		logger.debug("Successfull. Login Manager Account!");
		dbi.close();

		System.exit(0);
	}

}
