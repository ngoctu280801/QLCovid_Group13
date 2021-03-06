package view;

import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
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
import javax.swing.SwingUtilities;

import model.DateComparator;
import model.DbInteraction;
import model.Utils;
import model.VieStrComparator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.Types;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Color;

public class ManagerPanel extends JFrame {
	private DbInteraction dbi;
	private JPanel contentPane;
	private JTextField txtIdCard2Find;
	private JTable tblPatients;
	private JPanel pnShow, pnChangeState;
	private DefaultTableModel dtm;
	private JButton btnPkgManage, btnStat, 
	btnFind, btnAddNew,
	btnRPer, btnMHistory;
	private JButton btnChangePwd;
	private String usrManager;
	private TableRowSorter<TableModel> sorter;
	private JLabel currPosLab, newPosLab;
	private JTextField currPosField, txtCurState;
	private JComboBox cbPos, cbState;
	private JButton saveButton, btnSaveState;
	private JButton btnShowChangeQrtPos, btnShowChangeState;
	private static final Logger logger = Logger.getLogger(ManagerPanel.class);

	public ManagerPanel(DbInteraction dbi, String usrManager) {
		setBackground(Color.WHITE);
		this.usrManager = usrManager;
		this.dbi = dbi;
		setTitle("Qu???n l??");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setBounds(100, 100, 940, 481);
		setMinimumSize(new Dimension(1200, 500));
		addControls();
		getDataFromDb();
		addEvents();
		setLocationRelativeTo(null);
	}
	private void addControls(){
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
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

		btnPkgManage = new JButton("Qu???n l?? c??c g??i nhu y???u ph???m");
		pnPkg_Stat.add(btnPkgManage);

		btnStat = new JButton("Xem th???ng k??");
		pnPkg_Stat.add(btnStat);

		btnChangePwd = new JButton("?????i m???t kh???u");
		pnPkg_Stat.add(btnChangePwd);

		JPanel pnFind = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pnFind.getLayout();
		flowLayout.setAlignment(FlowLayout.TRAILING);
		pnPkg_Stat_Find.add(pnFind);

		txtIdCard2Find = new JTextField();

		pnFind.add(txtIdCard2Find);
		txtIdCard2Find.setText("Nh???p CMND/ CCCD");
		txtIdCard2Find.setColumns(12);

		btnFind = new JButton("T??m ki???m");
		pnFind.add(btnFind);

		JPanel pnPatientList = new JPanel();

		dtm = new DefaultTableModel();
		dtm.addColumn("H??? T??n");
		dtm.addColumn("CMND/ CCCD");
		dtm.addColumn("Ng??y sinh");
		dtm.addColumn("Ph?????ng/ X??");
		dtm.addColumn("Qu???n/ Huy???n");
		dtm.addColumn("Th??nh ph???/ T???nh");
		dtm.addColumn("Hi???n l??");
		dtm.addColumn("N??i ??i???u tr???/ c??ch ly");
		tblPatients = new JTable(dtm);
		sorter = new TableRowSorter<TableModel>(dtm);
		tblPatients.setRowSorter(sorter);
		for (int i = 0; i < dtm.getColumnCount(); i++) {
			if(i != 2){
				sorter.setComparator(i, new VieStrComparator<String>());
			}
			else{
				sorter.setComparator(i, new DateComparator<String>());
			}
		}
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

		pnChangeState = new JPanel();
		pnUtils.add(pnChangeState);
		JLabel lblCurState = new JLabel("Hi???n l??:");
		pnChangeState.add(lblCurState);
		txtCurState = new JTextField();
		txtCurState.setEditable(false);
		txtCurState.setColumns(6);
		pnChangeState.add(txtCurState);
		JLabel lblNewState = new JLabel("Chuy???n sang:");
		pnChangeState.add(lblNewState);
		cbState = new JComboBox();
		cbState.addItem(" ");
		cbState.addItem("F0");
		cbState.addItem("Kh???i b???nh");
		pnChangeState.add(cbState);
		btnSaveState = new JButton("Thay ?????i");
		btnSaveState.setEnabled(false);
		pnChangeState.add(btnSaveState);
		pnChangeState.setVisible(false);

		pnShow = new JPanel();
		pnUtils.add(pnShow);
		currPosLab = new JLabel("N??i ??i???u tr??? hi???n t???i:");
		currPosField = new JTextField();
		currPosField.setEditable(false);
		currPosField.setColumns(12);
		pnShow.add(currPosLab);
		pnShow.add(currPosField);
		newPosLab = new JLabel("N??i ??i???u tr??? m???i");
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
			logger.error("L???i truy v???n b???ng quarantinepos");
		}
		pnShow.add(newPosLab);
		pnShow.add(cbPos);
		saveButton = new JButton("Thay ?????i");
		saveButton.setEnabled(false);
		pnShow.add(saveButton);
		pnShow.setVisible(false);



		JPanel pnFuncs = new JPanel();
		pnUtils.add(pnFuncs);

		btnAddNew = new JButton("Th??m ng?????i m???i");
		pnFuncs.add(btnAddNew);

		btnRPer = new JButton("Th??ng tin nh???ng ng?????i li??n quan ?????n ng?????i n??y");
		btnRPer.setEnabled(false);
		pnFuncs.add(btnRPer);

		btnMHistory = new JButton("Xem l???ch s??? ???????c qu???n l??");
		btnMHistory.setEnabled(false);
		pnFuncs.add(btnMHistory);


		btnShowChangeQrtPos = new JButton("Chuy???n n??i ??i???u tr??? / c??ch ly");
		btnShowChangeQrtPos.setEnabled(false);
		pnFuncs.add(btnShowChangeQrtPos);


		btnShowChangeState = new JButton("Chuy???n tr???ng th??i");
		btnShowChangeState.setEnabled(false);
		pnFuncs.add(btnShowChangeState);

	}
	private void addEvents(){
		txtIdCard2Find.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(txtIdCard2Find.getText().equals("Nh???p CMND/ CCCD")){
					txtIdCard2Find.setText("");
				}
			}
		});
		tblPatients.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if(tblPatients.getRowCount() == 0){
					return;
				}
				if(tblPatients.getSelectedRowCount() > 1) {
					btnShowChangeQrtPos.setEnabled(false);
					btnShowChangeState.setEnabled(false);
					btnMHistory.setEnabled(false);
					btnRPer.setEnabled(false);
				}
				else {
					btnMHistory.setEnabled(true);
					btnRPer.setEnabled(true);
					btnShowChangeQrtPos.setEnabled(true);
					btnShowChangeState.setEnabled(true);
					if(tblPatients.getSelectedRowCount() == 1){
						txtIdCard2Find.setText((String) tblPatients.getValueAt(
								tblPatients.getSelectedRow(), 1));
					}
				}
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				dbi.close();
			}
		});

		//		B??? lu??n
		//		tblPatients.getTableHeader().addMouseListener(new MouseAdapter() {
		//		    @Override
		//		    public void mouseClicked(MouseEvent e) {
		//		        int col = tblPatients.columnAtPoint(e.getPoint());
		//		        String name = tblPatients.getColumnName(col);
		//		        
		//		    }
		//		});


		btnChangePwd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ChangePwd cPwd = new ChangePwd(dbi, usrManager, false);
				cPwd.setModal(true);
				cPwd.setVisible(true);
			}
		});
		btnAddNew.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				AdmRegister addPer = new AdmRegister(dbi, 0, usrManager, null, dtm);
				addPer.setModal(true);
				addPer.setVisible(true);
			}
		});
		btnRPer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RelatedPersons rp = new RelatedPersons(dbi, (String) tblPatients.getValueAt(
						tblPatients.getSelectedRow(), 1));
				rp.setVisible(true);
			}
		});
		btnFind.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!txtIdCard2Find.getText().equals("") && btnFind.isEnabled()){
					String s = "select * from patients where id_card ='"
							+ txtIdCard2Find.getText() + "';";
					Statement[] stmt = new Statement[] {null};
					ResultSet rs = dbi.query(s, stmt);
					try {
						if(!rs.isBeforeFirst()){
							JOptionPane.showMessageDialog(null, "Kh??ng t???n t???i ng?????i n??y trong h??? th???ng");
							return;
						}
						else{
							PatientInfo pIn4 = new PatientInfo(dbi, dtm, null, txtIdCard2Find.getText(), 1);
							pIn4.setModal(true);
							pIn4.setVisible(true);
							//JOptionPane.showMessageDialog(null,
							//		((Vector)dtm.getDataVector().elementAt(row)).elementAt(col));

						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						logger.error("Failed!", e1);
					} finally {
						try {
							if(stmt[0] != null){
								stmt[0].close();
							}				
						} catch (SQLException e1) {
							e1.printStackTrace();
							logger.error("L???i khi truy v???n b???ng patients khi t??m ID");
						}
					}
				}
			}
		});

		btnPkgManage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				Packages pkgMngmPn = new Packages(dbi, usrManager, "", null);
				pkgMngmPn.setModal(true);
				pkgMngmPn.setVisible(true);
			}
		});

		btnMHistory.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String idCardPatient = (String) tblPatients.getValueAt(tblPatients.getSelectedRow(), 1);
				ManagementHistory mngmHis = new ManagementHistory(dbi, idCardPatient);
				mngmHis.setModal(true);
				mngmHis.setVisible(true);
			}
		});

		cbPos.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(cbPos.getSelectedItem().toString().equals(
						tblPatients.getValueAt(tblPatients.getSelectedRow(), 7)) ||
						cbPos.getSelectedItem().toString().equals(" ")){
					saveButton.setEnabled(false);
				}
				else{
					saveButton.setEnabled(true);
				}
			}
		});


		saveButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(saveButton.isEnabled()){
					if(cbPos.getSelectedItem().toString().equals(" ")) {
						JOptionPane.showMessageDialog(null,"Vui l??ng ch???n n??i mu???n chuy???n");
						return;
					}
					else {
						final String currPos = (String) tblPatients.getValueAt(tblPatients.getSelectedRow(), 7);
						final String userIdCard = (String) tblPatients.getValueAt(tblPatients.getSelectedRow(), 1);
						Runnable runUpdate = new Runnable(){
							public void run(){
								updateQrtPos(currPos, cbPos.getSelectedItem().toString(),userIdCard,usrManager);
							}
						};
						Thread t = new Thread(runUpdate);
						t.start();
					}}
			}
		});

		btnShowChangeQrtPos.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(btnShowChangeQrtPos.isEnabled()){
					if(btnShowChangeQrtPos.getText().equals("Chuy???n n??i ??i???u tr??? / c??ch ly")) {
						pnShow.setVisible(true);
						btnPkgManage.setEnabled(false);
						btnStat.setEnabled(false);
						btnFind.setEnabled(false);
						btnAddNew.setEnabled(false);
						btnRPer.setEnabled(false);
						btnMHistory.setEnabled(false);
						btnChangePwd.setEnabled(false);
						tblPatients.setEnabled(false);
						btnShowChangeState.setEnabled(false);
						String currPos = (String) tblPatients.getValueAt(tblPatients.getSelectedRow(), 7);
						currPosField.setText(currPos);
						btnShowChangeQrtPos.setText("Hu??? chuy???n");
					}
					else {
						pnShow.setVisible(false);
						btnPkgManage.setEnabled(true);
						btnStat.setEnabled(true);
						btnFind.setEnabled(true);
						btnAddNew.setEnabled(true);
						btnRPer.setEnabled(true);
						btnMHistory.setEnabled(true);
						btnChangePwd.setEnabled(true);
						tblPatients.setEnabled(true);
						btnShowChangeState.setEnabled(true);
						cbPos.setSelectedIndex(0);
						btnShowChangeQrtPos.setText("Chuy???n n??i ??i???u tr??? / c??ch ly");
					}
				}
			}
		});

		txtIdCard2Find.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {

				search(txtIdCard2Find.getText());
			}
		});

		btnShowChangeState.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(btnShowChangeState.isEnabled()){
					if(btnShowChangeState.getText().equals("Chuy???n tr???ng th??i")) {
						pnChangeState.setVisible(true);
						btnPkgManage.setEnabled(false);
						btnStat.setEnabled(false);
						btnFind.setEnabled(false);
						btnAddNew.setEnabled(false);
						btnRPer.setEnabled(false);
						btnMHistory.setEnabled(false);
						btnChangePwd.setEnabled(false);
						tblPatients.setEnabled(false);
						btnShowChangeQrtPos.setEnabled(false);
						String curState = (String) tblPatients.getValueAt(tblPatients.getSelectedRow(), 6);
						txtCurState.setText(curState);
						btnShowChangeState.setText("Hu??? chuy???n");
					}
					else {
						pnChangeState.setVisible(false);
						btnPkgManage.setEnabled(true);
						btnStat.setEnabled(true);
						btnFind.setEnabled(true);
						btnAddNew.setEnabled(true);
						btnRPer.setEnabled(true);
						btnMHistory.setEnabled(true);
						btnChangePwd.setEnabled(true);
						tblPatients.setEnabled(true);
						btnShowChangeQrtPos.setEnabled(true);
						cbState.setSelectedIndex(0);
						btnShowChangeState.setText("Chuy???n tr???ng th??i");
					}
				}
			}
		});
		cbState.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(pnChangeState.isVisible()){
					if(cbState.getSelectedItem().toString().equals(
							tblPatients.getValueAt(tblPatients.getSelectedRow(), 6)) ||
							cbState.getSelectedItem().toString().equals(" ")){
						btnSaveState.setEnabled(false);
					}
					else{
						btnSaveState.setEnabled(true);
					}}
			}
		});
		btnSaveState.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(btnSaveState.isEnabled()) {
					Runnable runUpdate = new Runnable(){
						public void run(){
							updateState((String)tblPatients.getValueAt(tblPatients.getSelectedRow(), 1), 
									cbState.getSelectedItem().toString(),
									usrManager);


						}
					};
					Thread t = new Thread(runUpdate);
					t.start();
				}
			}
		});

		btnStat.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				StatisticView stat = new StatisticView(dbi);
				stat.setModal(true);
				stat.setVisible(true);
			}
		});
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
			logger.error("Failed!", e);
		}
	}
	private void getDataFromDb(){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("call getAllPatients();", stmt);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			if(rs.isBeforeFirst()){
				while(rs.next()){
					Vector<String> rowData = new Vector<String>();
					//rowData.add(rs.getString(1));
					rowData.add(rs.getString(2));
					rowData.add(rs.getString(3));
					String d = rs.getString(4).replace("-", "/");
					rowData.add(Utils.changeDateFormatter(d, "dd/MM/yyyy", sdf));
//					rowData.add(rs.getString(4));
					rowData.add(rs.getString(7));
					rowData.add(rs.getString(6));
					rowData.add(rs.getString(5));
					rowData.add(rs.getString(9));
					rowData.add(rs.getString(8));
					dtm.addRow(rowData);
				}
			}
			else{
				JOptionPane.showMessageDialog(null, "Ch??a t???n t???i ng?????i n??o trong h??? th???ng");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("L???i khi g???i h??m getAllPatients()");
		} finally {
			try {
				if(stmt[0] != null){
					stmt[0].close();
				}				
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("Failed!", e);
			}
		}
	}
	private void updateQrtPos(String currQrtPos, String newQrtPos, String userIdCard, String userManager) {
		Statement[] stmt1 = new Statement[] {null};
		Statement[] stmt2 = new Statement[] {null};
		Statement[] stmt3 = new Statement[] {null};
		CallableStatement st = null;
		try {
			st = dbi.getStatement("{call changeQrtPos(?, ?, ?, ?, ?, ?)}");
			ResultSet idNewPos = dbi.query("SELECT QP.id FROM quarantinepos QP WHERE QP.name = n'" + newQrtPos + "';", stmt1);
			ResultSet currCap = dbi.query("SELECT QP.current_capacity FROM quarantinepos QP WHERE QP.name = n'" + newQrtPos + "';", stmt2);
			ResultSet possibleCap = dbi.query("SELECT QP.capacity FROM quarantinepos QP WHERE QP.name = n'" + newQrtPos + "';", stmt3);

			currCap.next();
			possibleCap.next();
			if(currCap.getInt(1) == possibleCap.getInt(1)){
				JOptionPane.showMessageDialog(null, "N??i ??i???u tr??? n??y ???? ?????y, vui l??ng ch???n n??i kh??c");
				return;
			}

			idNewPos.next();
			st.registerOutParameter(6, Types.INTEGER);
			st.setString(1, userManager);
			st.setString(2, userIdCard);
			st.setString(3,currQrtPos);
			st.setLong(4, idNewPos.getInt(1));
			st.setString(5,newQrtPos);
			st.execute();
			int code = st.getInt("code");
			if(code == 1){
				int row = tblPatients.getSelectedRow();
				tblPatients.setValueAt(cbPos.getSelectedItem().toString(), row, 7);
				pnShow.setVisible(false);
				pnShow.setVisible(false);
				btnPkgManage.setEnabled(true);
				btnStat.setEnabled(true);
				btnFind.setEnabled(true);
				btnAddNew.setEnabled(true);
				btnRPer.setEnabled(true);
				btnMHistory.setEnabled(true);
				btnChangePwd.setEnabled(true);
				tblPatients.setEnabled(true);
				saveButton.setEnabled(false);
				cbPos.setSelectedItem(" ");
				btnShowChangeQrtPos.setText("Chuy???n n??i ??i???u tr??? / c??ch ly");
				JOptionPane.showMessageDialog(null, "Chuy???n n??i ??i???u tr???/c??ch ly th??nh c??ng");
				return;
			}
			else{
				JOptionPane.showMessageDialog(null, "Th???t b???i. Xin vui l??ng th??? l???i sau");
				return;
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.error("L???i khi g???i h??m changeQtrPos()");
		}  finally {
			try {
				if(stmt1[0] != null){
					stmt1[0].close();
				}
				if(stmt2[0] != null){
					stmt2[0].close();
				}
				if(stmt3[0] != null){
					stmt3[0].close();
				}
				if(st != null){
					st.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("Failed!", e);
			}
		}
	}
	private void search(String s) {
		if (s.length() != 0) {
			sorter.setRowFilter(RowFilter.regexFilter(s, 1));

		} else {
			sorter.setRowFilter(null);
		}
	}

	private void updateState(String idCard, String newState, String usrManager){
		int stateInt = stateToInt(newState);
		CallableStatement st = null;
		Statement[] stmt = new Statement[] {null};
		int code = -1;
		try {
			ResultSet rs = dbi.query("SET @@session.max_sp_recursion_depth = 5;", stmt);
			st = dbi.getStatement("{call updatePatientState(?, ?, ?, ?, ?)}");
			st.registerOutParameter(5, Types.INTEGER);
			st.setString(1, idCard);
			st.setInt(2, stateInt);
			st.setInt(3, -1);
			st.setString(4, usrManager);
			st.execute();
			code  = st.getInt("code");

			if(code != -1){

				JOptionPane.showMessageDialog(null, "Chuy???n tr???ng th??i th??nh c??ng");
			}
			else{
				JOptionPane.showMessageDialog(null, "Chuy???n tr???ng th??i th???t b???i. Vui l??ng th??? l???i sau");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("L???i khi chuy???n tr???ng th??i b???nh nh??n");
		} finally{
			try {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						pnChangeState.setVisible(false);
						btnPkgManage.setEnabled(true);
						btnStat.setEnabled(true);
						btnFind.setEnabled(true);
						btnAddNew.setEnabled(true);
						btnRPer.setEnabled(true);
						btnMHistory.setEnabled(true);
						btnChangePwd.setEnabled(true);
						tblPatients.setEnabled(true);
						btnShowChangeQrtPos.setEnabled(true);
						cbState.setSelectedIndex(0);
						btnShowChangeState.setText("Chuy???n tr???ng th??i");
						dtm.setRowCount(0);
						getDataFromDb();
					}
				});
				if(st != null){
					st.close();}
				stmt[0].close();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("Failed!", e);
			}
		}
	}
	private int stateToInt(String state){
		int res;
		if(state.equals("Kh???i b???nh")){
			res = -1;
		}
		else{
			res = Integer.parseInt(state.charAt(1) + "");
		}
		return res;
	}
}
