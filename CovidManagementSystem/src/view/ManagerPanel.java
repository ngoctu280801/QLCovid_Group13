package view;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JTextField;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;

import model.DbInteraction;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.Types;
import java.sql.SQLException;
import java.util.Vector;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import model.DbInteraction;

public class ManagerPanel extends JFrame {
	private DbInteraction dbi;
	private JPanel contentPane;
	private JTextField txtIdCard2Find;
	private JTable tblPatients;
	private JPanel pnShow;
	private DefaultTableModel dtm;
	JButton btnPkgManage, btnStat, btnFind, btnAddNew,
	btnRPer, btnMHistory;
	private JButton btnChangePwd;
	private String usrManager;
	private TableRowSorter sorter;
	private JLabel currPosLab, newPosLab;
	private JTextField currPosField;
	private JComboBox cbPos;
	private JButton saveButton;
	private JButton btnShowChangeQrtPos;
	private static final Logger logger = Logger.getLogger(ManagerPanel.class);

	/**
	 * Create the frame.
	 */
	public ManagerPanel(DbInteraction dbi, String usrManager) {
		this.usrManager = usrManager;
		this.dbi = dbi;
		setTitle("Quản lý");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 940, 481);
		setLocationRelativeTo(null);
		JOptionPane.showMessageDialog(null, "Successfull. This is Manager Account!");
		logger.debug("Successfull. Login Manager Account!");
		dbi.close();
		addControls();
//		getDataFromDb();
//		addEvents();
		System.exit(0);
	}
	private void addControls(){
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		JPanel pnPkg_Stat_Find = new JPanel();
		contentPane.add(pnPkg_Stat_Find);
		pnPkg_Stat_Find.setLayout(new BoxLayout(pnPkg_Stat_Find, BoxLayout.X_AXIS));

		JPanel pnPkg_Stat = new JPanel();
		FlowLayout fl_pnPkg_Stat = (FlowLayout) pnPkg_Stat.getLayout();
		fl_pnPkg_Stat.setAlignment(FlowLayout.LEADING);
		pnPkg_Stat_Find.add(pnPkg_Stat);

		btnPkgManage = new JButton("Quản lý các gói nhu yếu phẩm");
		pnPkg_Stat.add(btnPkgManage);

		btnStat = new JButton("Xem thống kê");
		pnPkg_Stat.add(btnStat);

		btnChangePwd = new JButton("Đổi mật khẩu");
		pnPkg_Stat.add(btnChangePwd);

		JPanel pnFind = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pnFind.getLayout();
		flowLayout.setAlignment(FlowLayout.TRAILING);
		pnPkg_Stat_Find.add(pnFind);

		txtIdCard2Find = new JTextField();
		txtIdCard2Find.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {

				search(txtIdCard2Find.getText());
			}
		});
		pnFind.add(txtIdCard2Find);
		txtIdCard2Find.setText("Nhập CMND/ CCCD");
		txtIdCard2Find.setColumns(12);

		btnFind = new JButton("Tìm kiếm");
		pnFind.add(btnFind);

		JPanel pnPatientList = new JPanel();

		dtm = new DefaultTableModel();
		dtm.addColumn("Họ Tên");
		dtm.addColumn("CMND/ CCCD");
		dtm.addColumn("Ngày sinh");
		dtm.addColumn("Phường/ Xã");
		dtm.addColumn("Quận/ Huyện");
		dtm.addColumn("Thành phố/ Tỉnh");
		dtm.addColumn("Hiện là");
		dtm.addColumn("Nơi điều trị/ cách ly");
		tblPatients = new JTable(dtm);
		sorter = new TableRowSorter<>(dtm);
		tblPatients.setRowSorter(sorter);
		//tblPatients.setAutoCreateRowSorter(true);
		tblPatients.getRowSorter().toggleSortOrder(0);
		// Prevent manager edit this table
		tblPatients.setDefaultEditor(Object.class, null);
		JScrollPane scrollPane = new JScrollPane(
				tblPatients,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		pnPatientList.setLayout(new BorderLayout());
		pnPatientList.add(scrollPane, BorderLayout.CENTER);

		contentPane.add(pnPatientList);

		JPanel pnUtils = new JPanel();
		contentPane.add(pnUtils);
		pnUtils.setLayout(new BoxLayout(pnUtils, BoxLayout.Y_AXIS));

		pnShow = new JPanel();
		pnUtils.add(pnShow);
		currPosLab = new JLabel("Nơi điều trị hiện tại");
		currPosField = new JTextField();
		currPosField.setEditable(false);
		currPosField.setColumns(12);
		pnShow.add(currPosLab);
		pnShow.add(currPosField);
		newPosLab = new JLabel("Nơi điều trị mới");
		cbPos = new JComboBox();
		Statement[] stmt = new Statement[] {null};
		setComboBox(dbi.query("select name from quarantinepos"
				+ " where current_capacity < capacity order by name", stmt), cbPos);
		try {
			if(stmt[0] != null){
				stmt[0].close();
			}				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pnShow.add(newPosLab);
		pnShow.add(cbPos);
		saveButton = new JButton("Thay đổi");
		pnShow.add(saveButton);
		pnShow.setVisible(false);



		JPanel pnFuncs = new JPanel();
		pnUtils.add(pnFuncs);

		btnAddNew = new JButton("Thêm người mới");
		pnFuncs.add(btnAddNew);

		btnRPer = new JButton("Thông tin những người liên quan đến người này");
		btnRPer.setEnabled(false);
		pnFuncs.add(btnRPer);

		btnMHistory = new JButton("Xem lịch sử được quản lý");
		btnMHistory.setEnabled(false);
		pnFuncs.add(btnMHistory);


		btnShowChangeQrtPos = new JButton("Chuyển trạng nơi điều trị / cách ly");
		btnShowChangeQrtPos.setEnabled(false);
		pnFuncs.add(btnShowChangeQrtPos);

	}
	private void setComboBox(ResultSet rs, JComboBox cb){
		try {
			cb.removeAllItems();
			cb.addItem(" ");
			while(rs.next()){
				cb.addItem(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
