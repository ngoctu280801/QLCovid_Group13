package view;



import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import model.DbInteraction;
import model.Encrypt;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LoginScr extends JFrame {
	private final DbInteraction dbi;
	private JPanel pnContent;
	private JTextField txtUsrname;
	private JPasswordField txtPwd;
	private static JButton btnLogin;
	private static ImageIcon loading;
	private static final Logger logger = Logger.getLogger(LoginScr.class); 

	/**
	 * Create the frame.
	 */
	public LoginScr(DbInteraction dbi) {
		this.dbi = dbi;
		setTitle("Hệ thống quản lý Covid");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setSize(400, 200);
		addControls();
		addEvents();
		
		setLocationRelativeTo(null);
	}
	private void addControls(){
		pnContent = new JPanel();
		pnContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(pnContent);
		pnContent.setLayout(new BorderLayout(0, 0));
		
		JPanel pnTitle = new JPanel();
		pnContent.add(pnTitle, BorderLayout.NORTH);
		
		JLabel lblngNhpH = new JLabel("\u0110\u0103ng nh\u1EADp h\u1EC7 th\u1ED1ng qu\u1EA3n l\u00FD Covid");
		lblngNhpH.setForeground(Color.RED);
		lblngNhpH.setFont(new Font("Tahoma", Font.BOLD, 16));
		pnTitle.add(lblngNhpH);
		
		JPanel pnLoginForm = new JPanel();
		pnContent.add(pnLoginForm, BorderLayout.CENTER);
		pnLoginForm.setLayout(new BoxLayout(pnLoginForm, BoxLayout.Y_AXIS));
		
		JPanel pnUsrname = new JPanel();
		pnLoginForm.add(pnUsrname);
		
		JLabel lblUsername = new JLabel("Username:");
		pnUsrname.add(lblUsername);
		
		txtUsrname = new JTextField();
		pnUsrname.add(txtUsrname);
		txtUsrname.setColumns(10);
		
		JPanel pnPwd = new JPanel();
		pnLoginForm.add(pnPwd);
		
		JLabel lblPassword = new JLabel("Password:");
		pnPwd.add(lblPassword);
		
		txtPwd = new JPasswordField();
		txtPwd.setColumns(10);
		pnPwd.add(txtPwd);
		
		JPanel pnLoginBtn = new JPanel();
		pnLoginForm.add(pnLoginBtn);
		pnLoginBtn.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		btnLogin = new JButton("Đăng nhập");
		btnLogin.setBackground(Color.WHITE);
		pnLoginBtn.add(btnLogin);
		
		loading = new ImageIcon("resources/loading.gif");
		
		pnContent.getRootPane().setDefaultButton(btnLogin);
	}
	
	private void addEvents(){
		

		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				btnLogin.setIcon(loading);
				doLogin();
				
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dbi.close();
			}
//			@Override
//			public void windowClosed(WindowEvent e) {
//				dbi.close();
//			}
		});
	}
	private void doLogin(){
		if(txtPwd.getText().length() < 8 || txtUsrname.getText().length() == 0 ||
				isContainUnicode(txtPwd.getText()) || isContainUnicode(txtUsrname.getText()) ||
				txtPwd.getText().contains(" ") || txtUsrname.getText().contains(" ")){
			JOptionPane.showMessageDialog(null, "Sai tài khoản hoặc mật khẩu");
			btnLogin.setIcon(null);
			return;
		}
		
		Runnable runLogin = new Runnable(){
			public void run(){
				login();
			}
		};
		Thread t = new Thread(runLogin);
		t.start();
	}
	private void login(){
		String hashedPwd = Encrypt.toPBKDF2(txtPwd.getText(), 8*25);
		String sql = "select id_permission, usrname, is_locked from accounts where usrname = '" +
				txtUsrname.getText() + "' and pwd = '" +
				hashedPwd + "';";
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query(sql, stmt);
		try {
			if(rs.isBeforeFirst()){
				rs.next();
				final int idPermission = Integer.parseInt(rs.getString(1));
				final String usrname = rs.getString(2);
				int is_locked = Integer.parseInt(rs.getString(3));
				if(is_locked == 1){
					JOptionPane.showMessageDialog(null, "Tài khoản của bạn đã bị khoá");
					return;
				}
				if(idPermission == 0){
					// Manager
					ManagerPanel mPanel = new ManagerPanel(dbi, usrname);
					mPanel.setVisible(true);
				}
				else if(idPermission == 1){
					// Admin
					AdminPanel aPanel = new AdminPanel(dbi);
					aPanel.setVisible(true);
				}
				else if(idPermission == 2){
					// User
					PatientPanel pPanel = new PatientPanel(dbi, usrname);
					pPanel.setVisible(true);
				}
				dispose();
				//setVisible(false);
			}
			else{
				JOptionPane.showMessageDialog(null, "Sai tài khoản hoặc mật khẩu");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Lỗi khi đăng nhập");
		} finally{
			try {
				if(stmt[0] != null){
					stmt[0].close();
				}				
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("Failed!",e);
			}
			btnLogin.setIcon(null);
		}
	}
	private boolean isContainUnicode(String s){
		for (int i = 0; i < s.length(); i++) {
			if(Character.UnicodeBlock.of(s.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN){
				return true;
			}
		}
		return false;
	}
}
