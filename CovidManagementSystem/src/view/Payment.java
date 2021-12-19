package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.SwingConstants;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.JTextField;

import model.DbInteraction;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Payment extends JDialog {
	private DbInteraction dbi;
	private String usrName;
	private final JPanel pnTransHis = new JPanel();
	private DefaultTableModel dtm;
	private JTable tblTransHis;
	private static JTextField txtCredit;
	private JButton btnPay;
	private static JLabel lblBalance, lblDebt;
	private final String pathToTrustStore;
	private final String pwd;
	private final String serverName;
	private final int port;

	public Payment(DbInteraction dbi, String usrName, String path, String pwd, String svName, int port) {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		File f = new File(path);
		if(!f.exists()){
			JOptionPane.showMessageDialog(null, "Không tìm thấy " + path);
			dispose();
		}
		this.dbi = dbi;
		this.usrName = usrName;
		pathToTrustStore = path;
		this.pwd = pwd;
		this.serverName = svName;
		this.port = port;
		System.setProperty("javax.net.ssl.trustStore", pathToTrustStore);
		System.setProperty("javax.net.ssl.trustStorePassword", pwd);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		addControls();
		addEvents();
		reloadData();
		setLocationRelativeTo(null);
	}
	private void addControls(){
		setTitle("Lịch sử thanh toán của bạn");
		setBounds(100, 100, 879, 500);
		getContentPane().setLayout(new BorderLayout());
		
		dtm = new DefaultTableModel();
		dtm.addColumn("Ngày giao dịch");
		dtm.addColumn("Số tiền giao dịch");
		dtm.addColumn("Dư nợ còn lại");
		dtm.addColumn("Số dư tài khoản còn lại");
		tblTransHis = new JTable(dtm);
		// Prevent edit this table
		tblTransHis.setDefaultEditor(Object.class, null);
		JScrollPane scrollPane = new JScrollPane(
				tblTransHis,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		pnTransHis.setLayout(new BorderLayout());
		pnTransHis.add(scrollPane, BorderLayout.CENTER);
		
		pnTransHis.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(pnTransHis, BorderLayout.CENTER);

		JPanel pnPay = new JPanel();
		pnPay.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(pnPay, BorderLayout.SOUTH);
		
		JLabel lblCredit = new JLabel("Nhập số tiền: (Hạn mức tối thiểu 20,000 VNĐ)");
		pnPay.add(lblCredit);
		
		txtCredit = new JTextField();
		pnPay.add(txtCredit);
		txtCredit.setColumns(10);

		btnPay = new JButton("Thanh toán");
		btnPay.setActionCommand("OK");
		pnPay.add(btnPay);
		getRootPane().setDefaultButton(btnPay);

		JPanel pnMoney = new JPanel();
		getContentPane().add(pnMoney, BorderLayout.NORTH);
		pnMoney.setLayout(new BoxLayout(pnMoney, BoxLayout.Y_AXIS));
		
		
		//new
		JPanel pnUsr = new JPanel();
		FlowLayout flowLayout2 = (FlowLayout) pnUsr.getLayout();
		flowLayout2.setAlignment(FlowLayout.LEFT);
		pnMoney.add(pnUsr);
		JLabel lblUrsTitle = new JLabel("User:");
		lblUrsTitle.setHorizontalAlignment(SwingConstants.TRAILING);
		lblUrsTitle.setFont(new Font("Tahoma", Font.ITALIC, 14));
		//lblUrsTitle.setPreferredSize(lblBalanceTitle.getPreferredSize());
		pnUsr.add(lblUrsTitle);
		JLabel lbUrs = new JLabel(this.usrName);
		lbUrs.setHorizontalAlignment(SwingConstants.TRAILING);
		lbUrs.setFont(new Font("Tahoma", Font.BOLD, 14));
		pnUsr.add(lbUrs);

		JPanel pnBalance = new JPanel();
		pnMoney.add(pnBalance);
		FlowLayout flowLayout = (FlowLayout) pnBalance.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		
		JLabel lblBalanceTitle = new JLabel("Số dư hiện tại:");
		lblBalanceTitle.setHorizontalAlignment(SwingConstants.TRAILING);
		lblBalanceTitle.setFont(new Font("Tahoma", Font.ITALIC, 14));
		pnBalance.add(lblBalanceTitle);
		lblBalance = new JLabel("10,000,000 (VNĐ)");
		lblBalance.setHorizontalAlignment(SwingConstants.TRAILING);
		lblBalance.setFont(new Font("Tahoma", Font.BOLD, 15));
		pnBalance.add(lblBalance);

		JPanel pnDebt = new JPanel();
		FlowLayout flowLayout1 = (FlowLayout) pnDebt.getLayout();
		flowLayout1.setAlignment(FlowLayout.LEFT);
		pnMoney.add(pnDebt);

		JLabel lblDebtTitle = new JLabel("Dư nợ:");
		//lblDebtTitle.setHorizontalAlignment(SwingConstants.TRAILING);
		lblDebtTitle.setFont(new Font("Tahoma", Font.ITALIC, 14));
		lblDebtTitle.setPreferredSize(lblBalanceTitle.getPreferredSize());
		pnDebt.add(lblDebtTitle);
		
		
		
		lblDebt = new JLabel("0 (VNĐ)");
		lblDebt.setHorizontalAlignment(SwingConstants.TRAILING);
		lblDebt.setFont(new Font("Tahoma", Font.BOLD, 14));
		pnDebt.add(lblDebt);
		
	}
	private void addEvents(){
		
		btnPay.addMouseListener(new MouseAdapter() {
			@Override
			
			public void mouseClicked(MouseEvent arg0) {
				if(btnPay.isEnabled()) { 
				Runnable connect = new Runnable(){
					public void run(){
						connectToServer();
					}
				};
				Thread t = new Thread(connect);
				t.start();
				reloadData();
				}
			}
		});
		
		
		txtCredit.addKeyListener(new KeyAdapter() {
			@Override
			
			public void keyReleased(KeyEvent arg0) {	
				
				txtCredit.setText(validateNum(new StringBuilder(txtCredit.getText())));
				String credit = txtCredit.getText();
				if(credit.equals("") || Integer.parseInt(credit.replace(",","")) < 20000 || 
						isCreditLargerThanBalance() || isCreditLargerThanDebt() || isDebtSmallerThanCreditMin()) {
					btnPay.setEnabled(false);
				}
				else {btnPay.setEnabled(true);}
			}
			
		});
		
	}
	private void connectToServer(){
		btnPay.setEnabled(false);
		txtCredit.setEnabled(false);
		SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocket sslSocket = null;
		try {
			sslSocket = (SSLSocket) ssf.createSocket(serverName, port);
			OutputStreamWriter os = new OutputStreamWriter(sslSocket.getOutputStream());
			PrintWriter out = new PrintWriter(os);
			out.println(usrName + " " + txtCredit.getText().replace(",", ""));
			os.flush();
			
			BufferedReader bfr = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
			String code = bfr.readLine();
			sslSocket.close();
			codeToMessage(Integer.parseInt(code));
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Vui lòng xem lại địa chỉ server");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Server đang offline");
		} 
		
		finally{
			reloadData();
		}
	}
	private void codeToMessage(int code){
		if(code == 1){
			txtCredit.setText("");
			JOptionPane.showMessageDialog(null, "Thanh toán thành công");
		}
		else if(code == -1){
			JOptionPane.showMessageDialog(null, "Thanh toán thất bại");
		}
	}
	private void onOffPaymentBtn(){
		if(lblDebt.getText().equals("0 (VNĐ)")){
			txtCredit.setEnabled(false);
			btnPay.setEnabled(false);
		}
		else{
			txtCredit.setEnabled(true);
			btnPay.setEnabled(true);
		}
		if(txtCredit.getText().equals("")){
			btnPay.setEnabled(false);
		}
	}
	private String validateNum(StringBuilder s){
		for(int i = 0; i < s.length(); i++){
			if(!Character.isDigit(s.charAt(i))){
				s.deleteCharAt(i);
				i--;
			}
		}
		for(int i = s.length() - 3; i > 0; i-=3){
			s.insert(i, ',');
		}
		return s.toString();
	}
	private void reloadData(){
		Runnable getData = new Runnable(){
			public void run(){
				dtm.setRowCount(0);
				getDataFromDb();
			}
		};
		Thread t = new Thread(getData);
		t.start();
		
		Runnable getDebtAndBalance = new Runnable(){
			public void run(){
				lblBalance.setText("");
				lblDebt.setText("");
				getDebtFromDb();
				getBalanceFromDb();
				lblDebt.setPreferredSize(lblBalance.getPreferredSize());
			}
		};
		Thread t1 = new Thread(getDebtAndBalance);
		t1.start();
		
		try {
			t.join();
			t1.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			onOffPaymentBtn();
		}
	}
	private void getDataFromDb(){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select th.date_trans, th.credit, th.remaining_debt, th.remaining_balance "
				+ "from transaction_history th join accounts a on a.id = th.from_id_acc "
				+ "where a.usrname = '" + usrName + "'", stmt);
		try {
			while(rs.next()){
				Vector<String> rowData = new Vector<String>();
				rowData.add(rs.getString(1));
				rowData.add(validateNum(new StringBuilder(rs.getString(2))));
				rowData.add(validateNum(new StringBuilder(rs.getString(3))));
				rowData.add(validateNum(new StringBuilder(rs.getString(4))));
				dtm.addRow(rowData);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Không thể kết nối tới CSDL");
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
	private void getDebtFromDb(){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select d.debt from debt d "
				+ "join accounts a on d.id_patient = a.id "
				+ " where a.usrname = '" + usrName + "'", stmt);
		try {
			rs.next();
			lblDebt.setText(validateNum(new StringBuilder(rs.getString(1))) + " (VNĐ)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	private void getBalanceFromDb(){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select pa.balance from payment_acc pa "
				+ "join accounts a on pa.id_acc = a.id "
				+ " where a.usrname = '" + usrName + "'", stmt);
		try {
			rs.next();
			lblBalance.setText(validateNum(new StringBuilder(rs.getString(1))) + " (VNĐ)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	private boolean isCreditLargerThanBalance(){
		if(Integer.parseInt(txtCredit.getText().replace(",", "")) > lblToInt(lblBalance.getText())){
			return true;
		}
		return false;
	}
	private boolean isCreditLargerThanDebt(){
		if(Integer.parseInt(txtCredit.getText().replace(",", "")) > lblToInt(lblDebt.getText())){
			return true;
		}
		return false;
	}
	private boolean isDebtSmallerThanCreditMin(){
		if(20000 > lblToInt(lblDebt.getText())){
			return true;
		}
		return false;
	}
	private int lblToInt(String s){
		return Integer.parseInt(s.substring(0, s.length() - 6).replace(",", ""));
	}
}
