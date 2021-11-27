package view;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import model.DbInteraction;

public class PatientPanel extends JFrame {
	private DbInteraction dbi;
	private String usrname;

	/**
	 * Create the frame.
	 */
	private static final Logger logger = Logger.getLogger(PatientPanel.class);

	public PatientPanel(DbInteraction dbi, String usrname) {

		this.dbi = dbi;
		this.usrname = usrname;
		JOptionPane.showMessageDialog(null, "Successfull. This is Patient Account!");
		logger.debug("Successfull. Login Patient Account!");
		dbi.close();

		System.exit(0);
	}

}
