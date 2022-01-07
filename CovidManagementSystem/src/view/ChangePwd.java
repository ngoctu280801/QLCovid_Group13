package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPasswordField;

import java.awt.Component;

import javax.swing.SwingConstants;

import model.DbInteraction;
import model.Encrypt;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ChangePwd extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JPasswordField txtCurPwd;
	private JPasswordField txtNewPwd;
	private JPasswordField txtReNewPwd;
	private JButton btnCancel, btnSave;
	private String usrname;
	private DbInteraction dbi;
	private boolean isFirstLogin;
	private boolean changeSuccessful;
	private final String[] pwd;
	private static final Logger logger = Logger.getLogger(ChangePwd.class); 

	/**
	 * Create the dialog.
	 */
	public ChangePwd(DbInteraction dbi, String usrname, boolean isFirstLogin) {
		this.dbi = dbi;
		this.usrname = usrname;
		this.isFirstLogin = isFirstLogin;
		setResizable(false);
		changeSuccessful = false;
		pwd = new String[2];
		setTitle("Thay đổi mật khẩu");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 369, 188);
		addControls();
		addEvents();
		setLocationRelativeTo(null);
	}
	private void addControls(){
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);

		JLabel lblCurPwd = new JLabel("Mật khẩu hiện tại:");
		contentPanel.add(lblCurPwd);

		txtCurPwd = new JPasswordField();
		txtCurPwd.setColumns(12);
		contentPanel.add(txtCurPwd);

		JPanel panel = new JPanel();
		getContentPane().add(panel);

		JLabel lblNewPwd = new JLabel("Mật khẩu mới:");
		panel.add(lblNewPwd);

		txtNewPwd = new JPasswordField();
		txtNewPwd.setColumns(12);
		panel.add(txtNewPwd);

		JPanel panel1 = new JPanel();
		getContentPane().add(panel1);

		JLabel lblReNewPwd = new JLabel("Nhập lại mật khẩu mới:");
		lblNewPwd.setPreferredSize(lblReNewPwd.getPreferredSize());
		lblCurPwd.setPreferredSize(lblReNewPwd.getPreferredSize());
		panel1.add(lblReNewPwd);

		txtReNewPwd = new JPasswordField();
		txtReNewPwd.setColumns(12);
		panel1.add(txtReNewPwd);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane);

		btnSave = new JButton("Thay đổi");
		buttonPane.add(btnSave);
		btnCancel = new JButton("Huỷ");
		buttonPane.add(btnCancel);
		getRootPane().setDefaultButton(btnCancel);
	}
	private void addEvents(){

		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(isFirstLogin && !changeSuccessful){
					System.exit(ERROR);
				}
				dispose();
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent arg0) {
				if(isFirstLogin && !changeSuccessful){
					System.exit(ERROR);
				}
				dispose();
			}
		});
		
		btnSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(txtCurPwd.getText().length() < 8){
					JOptionPane.showMessageDialog(null, "Mật khẩu hiện tại không đúng");
					return;
				}
				if(txtNewPwd.getText().length() < 8 || txtReNewPwd.getText().length() < 8){
					JOptionPane.showMessageDialog(null, "Vui lòng đặt mật khẩu có 8 ký tự trở lên");
					return;
				}
				if(!txtNewPwd.getText().equals(txtReNewPwd.getText())){
					JOptionPane.showMessageDialog(null, "Mật khẩu mới không khớp nhau");
					return;
				}
				
				Runnable run = new Runnable(){
					public void run(){
						Runnable hashed_curPwd = new Runnable(){
							public void run(){
								pwd[0] = Encrypt.toPBKDF2(txtCurPwd.getText(), 8*25);
							}
						};
						Runnable hashed_newPwd = new Runnable(){
							public void run(){
								pwd[1] = Encrypt.toPBKDF2(txtNewPwd.getText(), 8*25);
							}
						};
						Thread cur_pwd = new Thread(hashed_curPwd);
						cur_pwd.start();
						
						Thread new_pwd = new Thread(hashed_newPwd);
						new_pwd.start();
						try {
							cur_pwd.join();
							new_pwd.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							logger.error("Lỗi khi hash mật khẩu");
						}
						
					}
				};
				Thread t = new Thread(run);
				t.start();
				try {
					t.join();
					changePwd(pwd[0], pwd[1]);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra");
					logger.error("Lỗi khi đổi mật khẩu");
				}
			}
		});
	}
	private void changePwd(String hashed_curPwd, String hashed_newPwd){
		Statement[] stmt1 = new Statement[] {null};
		Statement[] stmt2 = new Statement[] {null};
		ResultSet rs = dbi.query("select count(*) from accounts where usrname = '"
				+ usrname + "' and pwd = '" + hashed_curPwd + "'", stmt1);
		try {
			rs.next();
			if(rs.getString(1).equals("1")){
				int rows = dbi.insert("update accounts set pwd = '"
						+ hashed_newPwd + "' where usrname = '" + usrname + "'", stmt2);
				if(rows != 0){
					changeSuccessful = true;
					JOptionPane.showMessageDialog(null, "Đổi mật khẩu thành công");
					dispose();
				}
				else{
					JOptionPane.showMessageDialog(null, "Đổi mật khẩu thất bại");
				}
			}
			else{
				JOptionPane.showMessageDialog(null, "Mật khẩu hiện tại không đúng");
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra. Không thể thực hiện việc đổi mật khẩu");
			logger.error("Lỗi khi lấy dữ liệu từ bảng accounts");
		} finally {
			try {
				if(stmt1[0] != null){
					stmt1[0].close();
				}
				if(stmt2[0] != null){
					stmt2[0].close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("Failed!",e);
			}
		}
	}
}
