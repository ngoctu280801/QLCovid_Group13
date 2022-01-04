package view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JButton;

import model.DbInteraction;
import model.MyComparator;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;

import java.awt.Color;

public class PatientPanel extends JFrame {
	private DbInteraction dbi;
	private JPanel contentPane;
	private DefaultTableModel dtm;
	private JTable tblBoughtPkg;
	private String usrname, idCard;
	JButton btnInfo, btnMngmHis, btnPkg, btnPayment;
	private JButton btnChangePwd;
	private TableRowSorter<TableModel> sorter;

	/**
	 * Create the frame.
	 */
	public PatientPanel(DbInteraction dbi, String usrname) {
		
		this.dbi = dbi;
		this.usrname = usrname;
		isFirstLogin();
		getIdCard(usrname);
		setTitle("Phân hệ người dùng");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 500);
		addControls();
		addEvents();
		getDataFromDb();
		setLocationRelativeTo(null);
	}
	private void addControls(){
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel pnUtils = new JPanel();
		contentPane.add(pnUtils, BorderLayout.NORTH);
		
		btnInfo = new JButton("Thông tin cá nhân");
		pnUtils.add(btnInfo);
		
		btnChangePwd = new JButton("Đổi mật khẩu");
		pnUtils.add(btnChangePwd);
		
		btnMngmHis = new JButton("Lịch sử được quản lý");
		pnUtils.add(btnMngmHis);
		
		btnPkg = new JButton("Các gói nhu yếu phẩm");
		pnUtils.add(btnPkg);
		
		btnPayment = new JButton("Thanh toán dư nợ");
		pnUtils.add(btnPayment);
		
		JPanel pnBoughtPkg = new JPanel();
		pnBoughtPkg.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "L\u1ECBch s\u1EED ti\u00EAu th\u1EE5 g\u00F3i nhu y\u1EBFu ph\u1EA9m", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		contentPane.add(pnBoughtPkg, BorderLayout.CENTER);
		
		dtm = new DefaultTableModel();
		dtm.addColumn("Ngày");
		dtm.addColumn("Tên gói");
		dtm.addColumn("Số lượng");
		dtm.addColumn("Giá");
		tblBoughtPkg = new JTable(dtm);
		// Prevent manager edit this table
		tblBoughtPkg.setDefaultEditor(Object.class, null);
		
		sorter = new TableRowSorter<TableModel>(dtm);
		tblBoughtPkg.setRowSorter(sorter);
		for (int i = 0; i < dtm.getColumnCount(); i++) {
			sorter.setComparator(i, new MyComparator<String>());
		}
		
		JScrollPane scrollPane = new JScrollPane(
				tblBoughtPkg,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnBoughtPkg.setLayout(new BorderLayout());
		pnBoughtPkg.add(scrollPane, BorderLayout.CENTER);
	}
	private void addEvents(){
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				dbi.close();
			}
		});
		btnInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				PatientInfo pInfo = new PatientInfo(dbi, null, null, idCard, 1);
				pInfo.setModal(true);
				pInfo.setVisible(true);
			}
		});
		btnMngmHis.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ManagementHistory mngmHis = new ManagementHistory(dbi, idCard);
				mngmHis.setModal(true);
				mngmHis.setVisible(true);
			}
		});
		btnPkg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Packages pkg = new Packages(dbi, "", idCard, dtm);
				pkg.setModal(true);
				pkg.setVisible(true);
			}
		});
		btnPayment.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Payment paymentScr = new Payment(dbi, usrname, "cert/truststore", "88888888", "localhost", 9009);
				paymentScr.setModal(true);
				paymentScr.setVisible(true);
			}
		});
		btnChangePwd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				ChangePwd cPwd = new ChangePwd(dbi, usrname, false);
				cPwd.setModal(true);
				cPwd.setVisible(true);
			}
		});
	}
	private void getDataFromDb(){
		String sql = "select bph.date, p.pkg_name, bph.quantity, bph.price from bought_pkg_history bph "
				+ "join accounts a on a.id = bph.id_patient "
				+ "join necessary_packages p on p.id = bph.id_pkg "
				+ "where a.usrname = '" + this.usrname + "' and a.id_permission = 2";
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query(sql, stmt);
		try {
			if(rs.isBeforeFirst()){
				
				while(rs.next()){
					Vector<String> rowData = new Vector<String>();
					rowData.add(rs.getString(1));
					rowData.add(rs.getString(2));
					rowData.add(validateNum(new StringBuilder(rs.getString(3))));
					rowData.add(validateNum(new StringBuilder(rs.getString(4))));
					dtm.addRow(rowData);
				}
			}
			else{
				//JOptionPane.showMessageDialog(null, "Bạn chưa mua gói nhu yếu phẩm nào");
			}
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
	private void getIdCard(String usrPatient){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select p.id_card from accounts a join patients p on "
				+ "a.id = p.id where a.usrname = '" + usrPatient + "' limit 1;", stmt);
		try {
			if(rs.isBeforeFirst()){
				rs.next();
				this.idCard = rs.getString(1);
			}
			else{
				JOptionPane.showMessageDialog(null, "Lỗi không tìm thấy số CMND/ CCCD của người này");
				dbi.close();
				dispose();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			JOptionPane.showMessageDialog(null, "Lỗi kết nối tới CSDL");
			dbi.close();
			dispose();
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
	private void isFirstLogin(){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select count(*) from logon l "
				+ "join accounts a on a.id = l.id_patient "
				+ "where a.usrname = '" + usrname + "'", stmt);
		try {
			rs.next();
			if(rs.getString(1).equals("0")){
				ChangePwd cPwd = new ChangePwd(dbi, usrname, true);
				cPwd.setModal(true);
				cPwd.setVisible(true);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Đã có lỗi xảy ra");
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
