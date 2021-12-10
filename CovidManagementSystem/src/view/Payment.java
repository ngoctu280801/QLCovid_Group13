package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

import java.io.File;

import javax.swing.JTextField;

import model.DbInteraction;

public class Payment extends JDialog {
	private String usrName;
	private final JPanel pnTransHis = new JPanel();
	private DefaultTableModel dtm;
	private JTable tblTransHis;
	private static JTextField txtCredit;
	private JButton btnPay;
	private static JLabel lblBalance, lblDebt;
	private final String pathToTrustStore;

	public Payment(DbInteraction dbi, String usrName, String path, String pwd, String svName, int port) {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		File f = new File(path);
		if(!f.exists()){
			//JOptionPane.showMessageDialog(null, "Không tìm thấy " + path);
			dispose();
		}
		this.usrName = usrName;
		pathToTrustStore = path;
		System.setProperty("javax.net.ssl.trustStore", pathToTrustStore);
		System.setProperty("javax.net.ssl.trustStorePassword", pwd);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		addControls();
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
	
		
	
	
}
