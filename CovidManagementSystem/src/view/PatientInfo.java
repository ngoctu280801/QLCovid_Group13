package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import model.DbInteraction;
import model.Utils;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class PatientInfo extends JDialog {
	private int mode;
	// mode = 	0: Them nguoi nay vao ds lien quan
	//			1: Xem thong tin ca nhan cua nguoi lien quan
	private DefaultTableModel dtm;
	private DbInteraction dbi;
	private final JPanel pnContent = new JPanel();
	private JTextField txtFName;
	private JTextField txtIdCard;
	private JTextField txtDOB;
	private JTextField txtVlg;
	private JTextField txtTown;
	private JTextField txtProvince;
	private JTextField txtState;
	private JTextField txtQrtPos;
	private JButton btnAddToRelatedPer, btnCancel;
	private String id;
	private JTable tblRelatedPer;
	private static final Logger logger = Logger.getLogger(PatientInfo.class);
	/**
	 * Create the dialog.
	 */
	public PatientInfo(DbInteraction dbi, DefaultTableModel dtm, JTable rp, String idCard, int mode) {
		this.dtm = dtm;
		this.dbi = dbi;
		this.mode = mode;
		tblRelatedPer = rp;
		setTitle("Thông tin chi tiết");
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 720, 237);
		getContentPane().setLayout(new BorderLayout());
		pnContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(pnContent, BorderLayout.CENTER);
		addControls();
		getInfoFromDb(idCard);
		addEvents();
		setMode(mode);
		setLocationRelativeTo(null);
	}
	
	private void addControls(){
		pnContent.setLayout(new BoxLayout(pnContent, BoxLayout.Y_AXIS));
		{
			JPanel pnFName_IdCard_DOB = new JPanel();
			FlowLayout flowLayout = (FlowLayout) pnFName_IdCard_DOB.getLayout();
			flowLayout.setAlignment(FlowLayout.LEADING);
			pnContent.add(pnFName_IdCard_DOB);
			{
				JLabel lblFName = new JLabel("Họ Tên:");
				pnFName_IdCard_DOB.add(lblFName);
			}
			{
				txtFName = new JTextField();
				pnFName_IdCard_DOB.add(txtFName);
				txtFName.setColumns(20);
			}
			{
				JLabel lblIdCard = new JLabel("CMND/ CCCD:");
				pnFName_IdCard_DOB.add(lblIdCard);
			}
			{
				txtIdCard = new JTextField();
				pnFName_IdCard_DOB.add(txtIdCard);
				txtIdCard.setColumns(12);
			}
			{
				JLabel lblDOB = new JLabel("Ngày sinh:");
				pnFName_IdCard_DOB.add(lblDOB);
			}
			{
				txtDOB = new JTextField();
				pnFName_IdCard_DOB.add(txtDOB);
				txtDOB.setColumns(10);
			}
		}
		{
			JPanel pnAddr = new JPanel();
			FlowLayout flowLayout = (FlowLayout) pnAddr.getLayout();
			flowLayout.setAlignment(FlowLayout.LEADING);
			pnContent.add(pnAddr);
			{
				JLabel lblVlg = new JLabel("Xã/ Phường:");
				pnAddr.add(lblVlg);
			}
			{
				txtVlg = new JTextField();
				pnAddr.add(txtVlg);
				txtVlg.setColumns(15);
			}
			{
				JLabel lblTown = new JLabel("Quận/ Huyện:");
				pnAddr.add(lblTown);
			}
			{
				txtTown = new JTextField();
				pnAddr.add(txtTown);
				txtTown.setColumns(10);
			}
			{
				JLabel lblProvince = new JLabel("Tỉnh/ Thành phố:");
				pnAddr.add(lblProvince);
			}
			{
				txtProvince = new JTextField();
				pnAddr.add(txtProvince);
				txtProvince.setColumns(11);
			}
		}
		{
			JPanel pnState_QrtPos = new JPanel();
			FlowLayout flowLayout = (FlowLayout) pnState_QrtPos.getLayout();
			flowLayout.setAlignment(FlowLayout.LEADING);
			pnContent.add(pnState_QrtPos);
			{
				JLabel lblState = new JLabel("Trạng thái hiện tại:");
				pnState_QrtPos.add(lblState);
			}
			{
				txtState = new JTextField();
				pnState_QrtPos.add(txtState);
				txtState.setColumns(5);
			}
			{
				JLabel lblQrtPos = new JLabel("Đang được điều trị/ cách ly tại cơ sở:");
				pnState_QrtPos.add(lblQrtPos);
			}
			{
				txtQrtPos = new JTextField();
				pnState_QrtPos.add(txtQrtPos);
				txtQrtPos.setColumns(20);
			}
		}
		{
			JPanel pnBtn = new JPanel();
			pnBtn.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(pnBtn, BorderLayout.SOUTH);
			{
				if(mode == 0){
					btnAddToRelatedPer = new JButton("Thêm người này vào danh sách người liên quan");

					pnBtn.add(btnAddToRelatedPer);
				}
			}
			{
				btnCancel = new JButton("Huỷ");
				pnBtn.add(btnCancel);
				getRootPane().setDefaultButton(btnCancel);
			}
		}
	}
	private void addEvents(){
		if(mode == 0){
			btnAddToRelatedPer.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(!isExistedInTable(id)){
						dtm.addRow(new String[] {txtFName.getText(), txtIdCard.getText(), id});
						JOptionPane.showMessageDialog(null, "Thêm thành công");
					}
					else{
						JOptionPane.showMessageDialog(null, "Đã thêm người này trước đó");
					}
					dispose();
				}
			});
		}
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dispose();
			}
		});
	}
	private boolean isExistedInTable(String idCard){
		for(int i = 0; i < tblRelatedPer.getRowCount(); i++){
			String temp = (String) ((Vector)dtm.getDataVector().elementAt(i)).elementAt(2);
			if(temp.equals(idCard)){
				return true;
			}
		}
		return false;
	}
	private void setMode(int mode){
		if(mode == 0 || mode == 1){
			txtDOB.setEditable(false);
			txtFName.setEditable(false);
			txtIdCard.setEditable(false);
			txtProvince.setEditable(false);
			txtQrtPos.setEditable(false);
			txtState.setEditable(false);
			txtTown.setEditable(false);
			txtVlg.setEditable(false);
		}
	}
	private void getInfoFromDb(String idCard){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("call getUsrInfoByIdCard('" + idCard + "');", stmt);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			rs.next();
			txtFName.setText(rs.getString(1));
			txtIdCard.setText(rs.getString(2));
			String d = rs.getString(3).replace("-", "/");
			txtDOB.setText(Utils.changeDateFormatter(d, "dd/MM/yyyy", sdf));
//			txtDOB.setText(rs.getString(3));
			txtVlg.setText(rs.getString(4));
			txtTown.setText(rs.getString(5));
			txtProvince.setText(rs.getString(6));
			txtState.setText(rs.getString(7));
			txtQrtPos.setText(rs.getString(8));
			id = rs.getString(9);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Lỗi khi gọi hàm getUsrInfoByIdCard()");
		} finally {
			try {
				if(stmt[0] != null){
					stmt[0].close();
				}				
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("Failed!",e);
			}
		}
	}
}
