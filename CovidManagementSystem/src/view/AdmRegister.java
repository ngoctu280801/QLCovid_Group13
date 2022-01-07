package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.SwingConstants;

import model.DbInteraction;
import model.Encrypt;
import model.Utils;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;

import org.apache.log4j.Logger;

import java.awt.Component;

public class AdmRegister extends JDialog {
	private DbInteraction dbi;
	private final JPanel pnContent = new JPanel();
	private JTextField txtUsrname;
	private JPasswordField txtPwd;
	private JButton cancelButton, createButton, btnDelete, btnFind;
	private JRadioButton rdbtnManager, rdbtnPatient, rdbtnAdmin;
	private int num;
	private JPasswordField txtReEnterPwd;
	private ButtonGroup grBtn;
	private JTextField txtFName;
	private JTextField txtIdCard;
	private JComboBox cbProvinces, cbTown, cbVlg, cbQrtPos, cbState;
	private JTable tblRelatedPer;
	private DefaultTableModel dtm, dtmManagerL, dtmPInfoL;
	private JTextField txtIdCard2Find;
	private JFormattedTextField txtDOB;
	private SimpleDateFormat df;
	private String usrManager;
	private static final Logger logger = Logger.getLogger(AdmRegister.class); 
	/**
	 * Create the dialog.
	 */
	public AdmRegister(DbInteraction dbi, int num, String usrManager, 
			DefaultTableModel dtmManagerL, DefaultTableModel dtmPInfoL) {
		// num = -1: First login
		//		  1: View as admin
		//		  0: View as manager
		this.num = num;
		this.dbi = dbi;
		this.usrManager = usrManager;
		this.dtmManagerL = dtmManagerL;
		this.dtmPInfoL = dtmPInfoL;
		setTitle("Tạo một tài khoản");
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		if(num != 0){
			setBounds(100, 100, 450, 258);
		}
		else{
			setBounds(100, 100, 900, 464);
		}
		addControls();
		addEvents();
		setLocationRelativeTo(null);

	}

	private void addEvents() {
		// TODO Auto-generated method stub
		cancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				dispose();
			}
		});
		createButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if(!validateRegIn4()){
					JOptionPane.showMessageDialog(null,
							"Username và Password không được chứa khoảng cách (hoặc bỏ trống)."
									+ " Password phải gồm 8 kí tự trở lên");
					return;
				}

				if(!txtPwd.getText().equals(txtReEnterPwd.getText())){
					JOptionPane.showMessageDialog(null, "Password không khớp nhau");
					return;
				}
				if(checkAccExisted()){
					JOptionPane.showMessageDialog(null, "Username đã được sử dụng, vui lòng đặt username khác");
					return;
				}
				if(!isAsciiString(txtUsrname.getText()) || !isAsciiString(txtPwd.getText())){
					JOptionPane.showMessageDialog(null, "Username hoặc password không được chứa kí tự có dấu");
					return;
				}
				if(num == 0){
					if(txtFName.getText().equals("")){
						JOptionPane.showMessageDialog(null, "Vui lòng nhập tên hợp lệ");
						return;
					}
					if(!isOnlyContainLetter(txtFName.getText())){
						JOptionPane.showMessageDialog(null, "Tên chỉ nên chứa chữ cái");
						return;
					}
//					try {
//						Date d = df.parse(txtDOB.getText().toString());
//					} catch (ParseException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//						JOptionPane.showMessageDialog(null, "Ngày sinh không hợp lệ");
//						return;
//					} 
					if(!validateIdCard(txtIdCard.getText())){
						JOptionPane.showMessageDialog(null, "Vui lòng nhập đúng CMND/ CCCD");
						return;
					}
					if(checkIdCardExitsted()){
						JOptionPane.showMessageDialog(null, "Số CMND/ CCCD này đã tồn tại trong hệ thống");
						return;
					}
					if(isIdCardExisted(txtIdCard.getText())){
						JOptionPane.showMessageDialog(null, "Đã tồn tại người có số CMND/ CCCD này trong hệ thống");
						return;
					}
					if(!isValidAddr(cbProvinces.getSelectedItem().toString(),
							cbTown.getSelectedItem().toString(),
							cbVlg.getSelectedItem().toString(), 
							cbQrtPos.getSelectedItem().toString())){
						JOptionPane.showMessageDialog(null, "Vui lòng chọn đúng địa chỉ");
						return;
					}
					if(!cbState.getSelectedItem().toString().equals("F0") && tblRelatedPer.getRowCount() == 0){
						JOptionPane.showMessageDialog(null, "Vui lòng chọn những người liên đới ("
								+ findFBefore(cbState.getSelectedItem().toString()) + ") tới người này");
						return;
					}
				}
				Runnable runCreate = new Runnable(){
					public void run(){
						createAcc();
					}
				};
				Thread t = new Thread(runCreate);
				t.start();
			}


		});
		if(num == 0){
			txtFName.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					if(!txtFName.getText().equals("")){
						txtFName.setText(txtFName.getText().trim().toUpperCase());
					}
				}
			});
			cbProvinces.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					// TODO Auto-generated method stub
					cbTown.setEnabled(false);
					cbVlg.setEnabled(false);
					if(!cbProvinces.getSelectedItem().toString().equals(" ")){
						if(e.getStateChange() == ItemEvent.SELECTED){

							cbVlg.removeAllItems();						
							String sql = "call getTowns(N'" +
									cbProvinces.getSelectedItem().toString() +
									"');";
							Statement[] stmt = new Statement[] {null};
							setComboBox(dbi.query(sql, stmt),cbTown);
							try {
								if(stmt[0] != null){
									stmt[0].close();
								}				
							} catch (SQLException e1) {
								e1.printStackTrace();
								logger.error("Failed!",e1);
							}
							cbTown.setEnabled(true);
						}
					}
					else{
						//cbTown.setEnabled(false);
						cbTown.removeAllItems();
						//cbVlg.setEnabled(false);
						cbVlg.removeAllItems();


					}
				}
			});
			cbTown.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					// TODO Auto-generated method stub
					if(cbTown.isEnabled()){
						if(!cbTown.getSelectedItem().toString().equals(" ")){
							if(e.getStateChange() == ItemEvent.SELECTED){
								cbVlg.setEnabled(true);
								String sql = "call getVillages(N'" + cbProvinces.getSelectedItem().toString() +
										"', N'" + cbTown.getSelectedItem().toString() + "');";
								Statement[] stmt = new Statement[] {null};
								setComboBox(dbi.query(sql, stmt),cbVlg);
								try {
									if(stmt[0] != null){
										stmt[0].close();
									}				
								} catch (SQLException e1) {
									e1.printStackTrace();
									logger.error("Failed!",e1);
								}
							}
						}
						else{
							cbVlg.removeAllItems();
							cbVlg.setEnabled(false);

						}
					}
				}
			});
			cbState.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(cbState.getSelectedItem().toString().equals("F0")){
						btnFind.setEnabled(false);
					}
					else{
						btnFind.setEnabled(true);
					}
				}
			});
			btnDelete.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(btnDelete.isEnabled() && !tblRelatedPer.getSelectionModel().isSelectionEmpty()){
						int dialogResult = JOptionPane.showConfirmDialog (null, 
								"Bạn có chắc chắn muốn xoá?", "Xoá", 2);
						if(dialogResult == JOptionPane.YES_OPTION){
							while(!tblRelatedPer.getSelectionModel().isSelectionEmpty()){
								dtm.removeRow(tblRelatedPer.getSelectedRow());
							}
						}
					}
				}
			});
			
			btnFind.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(!txtIdCard2Find.getText().equals("")){
						if(!validateIdCard(txtIdCard2Find.getText())){
							JOptionPane.showMessageDialog(null, "Vui lòng kiểm tra lại số CMND/ CCCD");
							return;
						}
						String s = "select * from patients where id_card ='"
								+ txtIdCard2Find.getText() + "' and state = '"
								+ findFBefore(cbState.getSelectedItem().toString()) + "';";
						Statement[] stmt = new Statement[] {null};
						try {
							ResultSet rs = dbi.query(s, stmt);
							if(!rs.isBeforeFirst()){
								JOptionPane.showMessageDialog(null, "Không tồn tại người nào hiện đang là "
										+ findFBefore(cbState.getSelectedItem().toString())
										+ " có số CMND/ CCCD: "
										+ txtIdCard2Find.getText() 
										+ " trong hệ thống");
								return;
							}
							else{
								PatientInfo pIn4 = new PatientInfo(dbi, dtm, tblRelatedPer, txtIdCard2Find.getText(), 0);
								pIn4.setModal(true);
								pIn4.setVisible(true);
								//JOptionPane.showMessageDialog(null,
								//		((Vector)dtm.getDataVector().elementAt(row)).elementAt(col));

							}
						} catch (SQLException e) {
							e.printStackTrace();
							logger.error("Lỗi khi tìm bằng CMND/CCCD",e);
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
			});
			txtIdCard2Find.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(txtIdCard2Find.getText().equals("Nhập CMND/ CCCD")){
						txtIdCard2Find.setText("");
					}
				}
			});
			
			txtDOB.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					if(!txtDOB.getText().equals("  /  /    ")){
						//JOptionPane.showMessageDialog(null, txtDate.getValue());
						try {
							Date d = df.parse(txtDOB.getText());
						} catch (ParseException e1) {
//							e1.printStackTrace();
							JOptionPane.showMessageDialog(null, "Không tồn tại ngày này");
							txtDOB.setValue(null);
						} 
						
					}
					
				}
			});
		}
	}
	private void addControls() {
		// TODO Auto-generated method stub
		getContentPane().setLayout(new BorderLayout());
		pnContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(pnContent, BorderLayout.CENTER);
		pnContent.setLayout(new BoxLayout(pnContent, BoxLayout.Y_AXIS));
		JPanel pnUsrname = new JPanel();
		pnContent.add(pnUsrname);
		JLabel lblUsrname = new JLabel("Username:");

		lblUsrname.setHorizontalAlignment(SwingConstants.TRAILING);
		pnUsrname.add(lblUsrname);
		txtUsrname = new JTextField();
		pnUsrname.add(txtUsrname);
		txtUsrname.setColumns(15);
		JPanel pnPwd = new JPanel();
		pnContent.add(pnPwd);
		pnPwd.setLayout(new BoxLayout(pnPwd, BoxLayout.Y_AXIS));
		JPanel pnEnterPwd = new JPanel();
		pnPwd.add(pnEnterPwd);
		JLabel lblPwd = new JLabel("Mật khẩu:");
		lblPwd.setHorizontalAlignment(SwingConstants.TRAILING);
		pnEnterPwd.add(lblPwd);
		txtPwd = new JPasswordField();
		pnEnterPwd.add(txtPwd);
		txtPwd.setColumns(15);
		JPanel pnReEnterPwd = new JPanel();
		pnPwd.add(pnReEnterPwd);
		JLabel lblReEnterPwd = new JLabel("Nhập lại mật khẩu:");
		pnReEnterPwd.add(lblReEnterPwd);
		txtReEnterPwd = new JPasswordField();
		txtReEnterPwd.setColumns(15);
		pnReEnterPwd.add(txtReEnterPwd);

		JPanel pnPermission = new JPanel();
		pnContent.add(pnPermission);

		JLabel lblPermission = new JLabel("Đăng kí với tư cách là:");
		pnPermission.add(lblPermission);

		JPanel grPermission = new JPanel();
		pnPermission.add(grPermission);
		grBtn = new ButtonGroup();
		if(num == -1){
			rdbtnAdmin = new JRadioButton("Quản trị viên");
			rdbtnAdmin.setActionCommand("1");
			rdbtnAdmin.setSelected(true);
			grBtn.add(rdbtnAdmin);
			grPermission.add(rdbtnAdmin);
		}
		else if(num == 1){
			rdbtnManager = new JRadioButton("Người quản lý");
			rdbtnManager.setActionCommand("0");
			rdbtnManager.setSelected(true);
			grBtn.add(rdbtnManager);
			grPermission.add(rdbtnManager);
		}
		else if(num == 0){
			rdbtnPatient = new JRadioButton("Người được quản lý");
			rdbtnPatient.setActionCommand("2");
			rdbtnPatient.setSelected(true);
			grBtn.add(rdbtnPatient);
			grPermission.add(rdbtnPatient);
		}
		JPanel pnBtn = new JPanel();
		pnBtn.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(pnBtn, BorderLayout.SOUTH);

		createButton = new JButton("OK");

		pnBtn.add(createButton);
		getRootPane().setDefaultButton(createButton);


		cancelButton = new JButton("Cancel");


		pnBtn.add(cancelButton);

		lblUsrname.setPreferredSize(lblReEnterPwd.getPreferredSize());
		lblPwd.setPreferredSize(lblReEnterPwd.getPreferredSize());
		if(num == 0){
			JPanel pnInfo = new JPanel();
			pnContent.add(pnInfo);
			pnInfo.setLayout(new BoxLayout(pnInfo, BoxLayout.Y_AXIS));

			JPanel pnFullName_DOB = new JPanel();
			FlowLayout flowLayout_1 = (FlowLayout) pnFullName_DOB.getLayout();
			pnInfo.add(pnFullName_DOB);

			JLabel lblFName = new JLabel("Họ Tên:");
			pnFullName_DOB.add(lblFName);

			txtFName = new JTextField();
			pnFullName_DOB.add(txtFName);
			txtFName.setColumns(10);

			JLabel lblDOB = new JLabel("Ngày sinh (dd/MM/yyyy):");
			pnFullName_DOB.add(lblDOB);


			df = new SimpleDateFormat("dd/MM/yyyy");

			df.setLenient(false);
			MaskFormatter dateMask = null;
			try {
				dateMask = new MaskFormatter("##/##/####");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Failed!",e);
			}
			txtDOB = new JFormattedTextField(dateMask);
			txtDOB.setColumns(6);
			txtDOB.setHorizontalAlignment(JFormattedTextField.CENTER);
			pnFullName_DOB.add(txtDOB);

			JPanel pnIdCard_QrtPos = new JPanel();
			FlowLayout fl_pnIdCard_QrtPos = (FlowLayout) pnIdCard_QrtPos.getLayout();
			pnInfo.add(pnIdCard_QrtPos);

			JLabel lblIdCard = new JLabel("CMND/ CCCD:");
			lblIdCard.setHorizontalAlignment(SwingConstants.CENTER);
			pnIdCard_QrtPos.add(lblIdCard);

			txtIdCard = new JTextField();
			pnIdCard_QrtPos.add(txtIdCard);
			txtIdCard.setColumns(12);

			JLabel lblQrtPos = new JLabel("Nơi đang điều trị/ cách ly:");
			pnIdCard_QrtPos.add(lblQrtPos);
			cbQrtPos = new JComboBox();
			pnIdCard_QrtPos.add(cbQrtPos);
			Statement[] stmt = new Statement[] {null};
			setComboBox(dbi.query("select name from quarantinepos"
					+ " where current_capacity < capacity order by name", stmt), cbQrtPos);
			try {
				if(stmt[0] != null){
					stmt[0].close();
					stmt[0] = null;
				}				
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("Failed!",e);
			}

			JLabel lblState = new JLabel("Hiện là:");
			pnIdCard_QrtPos.add(lblState);

			cbState = new JComboBox();
			pnIdCard_QrtPos.add(cbState);
			cbState.addItem("F0");
			cbState.addItem("F1");
			cbState.addItem("F2");
			cbState.addItem("F3");


			JPanel pnAddr = new JPanel();
			FlowLayout flowLayout_2 = (FlowLayout) pnAddr.getLayout();
			pnInfo.add(pnAddr);

			JLabel lblAddr = new JLabel("Tỉnh/ Thành phố:");
			pnAddr.add(lblAddr);

			cbProvinces = new JComboBox();
			pnAddr.add(cbProvinces);

			JLabel lblTown = new JLabel("Quận/ Huyện:");
			pnAddr.add(lblTown);

			cbTown = new JComboBox();
			cbTown.setEnabled(false);
			pnAddr.add(cbTown);

			JLabel lblVlg = new JLabel("Xã/ Phường:");
			pnAddr.add(lblVlg);

			cbVlg = new JComboBox();
			cbVlg.setEnabled(false);
			pnAddr.add(cbVlg);
			
			setComboBox(dbi.query("select name from provinces order by name", stmt),
					cbProvinces);
			try {
				if(stmt[0] != null){
					stmt[0].close();
				}				
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("Failed!",e);
			}

			JPanel pnRPerson = new JPanel();
			getContentPane().add(pnRPerson, BorderLayout.EAST);
			pnRPerson.setLayout(new BorderLayout());

			JPanel pnRelatedPer = new JPanel();
			pnRPerson.add(pnRelatedPer, BorderLayout.NORTH);
			pnRPerson.setPreferredSize(new Dimension(250, 464));
			pnRelatedPer.setLayout(new BoxLayout(pnRelatedPer, BoxLayout.Y_AXIS));
			JLabel lblRelatedPer = new JLabel("Chọn những người liên quan:");
			lblRelatedPer.setAlignmentX(Component.CENTER_ALIGNMENT);
			lblRelatedPer.setHorizontalAlignment(SwingConstants.CENTER);
			pnRelatedPer.add(lblRelatedPer);

			JPanel pnFind = new JPanel();
			pnRelatedPer.add(pnFind);
			pnFind.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

			txtIdCard2Find = new JTextField();
			pnFind.add(txtIdCard2Find);
			txtIdCard2Find.setText("Nhập CMND/ CCCD");
			txtIdCard2Find.setColumns(12);

			btnFind = new JButton("Tìm kiếm");
			pnFind.add(btnFind);
			btnFind.setEnabled(false);

			dtm = new DefaultTableModel();
			dtm.addColumn("Họ Tên");
			dtm.addColumn("CMND/ CCCD");
			dtm.addColumn("ID");
			tblRelatedPer = new JTable(dtm);
			// Prevent manager edit this table
			tblRelatedPer.setDefaultEditor(Object.class, null);

			DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			centerRenderer.setHorizontalAlignment( JLabel.CENTER );
			tblRelatedPer.getColumnModel().getColumn(0).setCellRenderer( centerRenderer );

			JScrollPane scrollPane = new JScrollPane(
					tblRelatedPer,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			pnRPerson.add(scrollPane, BorderLayout.CENTER);

			btnDelete = new JButton("Xoá");
			pnRPerson.add(btnDelete, BorderLayout.SOUTH);

		}
	}
	private boolean isAsciiString(String text) {
		for(int i = 0; i < text.length(); i++){
			if(text.charAt(i) > 127){
				return false;
			}
		}
		return true;
	}
	private boolean validateRegIn4(){
		if(	txtUsrname.getText().contains(" ") 		||
				txtUsrname.getText().equals("") 		|| 
				txtReEnterPwd.getText().contains(" ") 	|| 
				txtReEnterPwd.getText().equals("") 		||
				txtReEnterPwd.getText().length() < 8){
			return false;
		}
		return true;
	}
	private boolean checkAccExisted(){
		String q = "select count(*) from accounts where usrname=\'" + txtUsrname.getText() + "\'";
		ResultSet res;
		Statement[] stmt = new Statement[] {null};
		try {
			res = dbi.query(q, stmt);
			res.next();
			if(res.getString(1).equals("0")){
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Lỗi khi kiểm tra tài khoản đã tồn tại hay chưa");
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
		return true;
	}
	private boolean checkIdCardExitsted(){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select count(*) from patients where id_card = '"
				+ txtIdCard.getText() + "';", stmt);
		try {
			rs.next();
			if(!rs.getString(1).equals("0")){
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Lỗi khi kiểm tra nếu CMND/CCCD đã tồn tại");
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
		return false;
	}
	private void createAcc(){
		String pwd_hashed = Encrypt.toPBKDF2(txtPwd.getText(), 8*25);
		String q = "insert into accounts(usrname, pwd, id_permission, is_locked) values(" +
				"\'" + txtUsrname.getText() + "\'," +
				"\'" + pwd_hashed + "\'," 	+ 
				"\'" + grBtn.getSelection().getActionCommand() + "\'," + '0' + ");";
		Statement[] stmt = new Statement[] {null};
		int linesAffected = dbi.insert(q, stmt);
		try {
			if(stmt[0] != null){
				stmt[0].close();
			}				
		} catch (SQLException e) {
			logger.error("Failed!",e);
			e.printStackTrace();
		}
		if(linesAffected > 0 && num != 0){
			JOptionPane.showMessageDialog(null, "Tạo tài khoản thành công");
			dtmManagerL.addRow(new String[] {txtUsrname.getText(), "Hoạt động"});
			dispose();
			return;
		}
		else if(linesAffected <= 0){
			JOptionPane.showMessageDialog(null, "Tạo tài khoản thất bại. Vui lòng thử lại sau");
			dispose();
		}
		if(num == 0){
			addPatientInfo();
		}
		
	}
	public void setUsrManager(String usr){
		usrManager = usr;
	}
	private int addRelatedPer() {
		char delim = ';';
		int code = 0;
		StringBuilder idList = new StringBuilder("");

		for(int i = 0; i < tblRelatedPer.getRowCount(); i++){
			String temp = tblRelatedPer.getValueAt(i, 2).toString();;
			if(i != tblRelatedPer.getRowCount() - 1){
				temp += delim;
			}

			idList.append(temp);

		}
		if(!idList.equals("")){
			CallableStatement st = null;
			try {
				st = dbi.getStatement("{call addRelatedPerson(?, ?, ?)}");
				st.registerOutParameter(3, Types.INTEGER);
				st.setString(2, idList.toString());

				st.setString(1, txtIdCard.getText());
				st.execute();
				code  = st.getInt("code");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("Lỗi khi thêm người liên đới");
				JOptionPane.showMessageDialog(null, "Có lỗi khi thêm người liên đới");
			} finally{
				try {
					if(st != null){
					st.close();}
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error("Failed!",e);
				}
			}
		}
		return code;
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
			logger.error("Failed!",e);
		}
	}
//	private String changeDateFormatter(String date, String format){
//		SimpleDateFormat f = new SimpleDateFormat(format);
//		String res = null;
//		try {
//			res = f.format(df.parse(date));
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return res;
//	}
	private boolean validateIdCard(String idCard){
		if(idCard.length() != 9 && idCard.length() != 12){
			return false;
		}
		for(int i = 0; i < idCard.length(); i++){
			if(!Character.isDigit(idCard.charAt(i))){
				return false;
			}
		}
		return true;
	}
	private boolean isIdCardExisted(String idCard){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select count(id_card) from patients where "
				+ "id_card = '" + idCard + "';", stmt);
		try {
			rs.next();
			if(!rs.getString(1).equals("0")){
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Lỗi khi kiểm tra nêu CMND/CCCD đã tồn tại");
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
		return false;
	}
	private String findFBefore(String f){
		String fBefore = null;
		if(f.equals("F1")){
			fBefore = "F0";
		}
		else if(f.equals("F2")){
			fBefore = "F1";
		}
		else if(f.equals("F3")){
			fBefore = "F2";
		}
		return fBefore;
	}
	private boolean isValidAddr(String prov, String town, String vlg, String qrtPos){
		if(prov.equals(" ") || town.equals(" ") || vlg.equals(" ") || qrtPos.equals(" ")){
			return false;
		}
		return true;
	}
	private boolean isOnlyContainLetter(String s){
		for (int i = 0; i < s.length(); i++) {
			if(Character.isDigit(s.charAt(i))){
				return false;
			}
		}
		return true;
	}
	private void addPatientInfo(){
		SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
		String dateFormated = txtDOB.getValue().toString();
		String date = Utils.changeDateFormatter(dateFormated, "yyyy/MM/dd", f);
		CallableStatement st = null;
		try {
			st = dbi.getStatement("{call addPatient(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
			st.registerOutParameter(11, Types.INTEGER);
			st.setString(1, txtUsrname.getText());
			st.setString(2, txtFName.getText());
			st.setString(3, date);
			st.setString(4, txtIdCard.getText());
			st.setString(5, cbQrtPos.getSelectedItem().toString());
			st.setString(6, cbState.getSelectedItem().toString());
			st.setString(7, cbProvinces.getSelectedItem().toString());
			st.setString(8, cbTown.getSelectedItem().toString());
			st.setString(9, cbVlg.getSelectedItem().toString());
			st.setString(10, usrManager);
			st.execute();
			int code = st.getInt("code");
			if(code == 1){		
				if(addRelatedPer() == tblRelatedPer.getRowCount()){
					dtmPInfoL.addRow(new String[] {
							txtFName.getText(),
							txtIdCard.getText(), dateFormated,
							cbVlg.getSelectedItem().toString(),
							cbTown.getSelectedItem().toString(),
							cbProvinces.getSelectedItem().toString(),
							cbState.getSelectedItem().toString(),
							cbQrtPos.getSelectedItem().toString()
					});
					JOptionPane.showMessageDialog(null, "Tạo tài khoản thành công");
				}
			}
			else{
				JOptionPane.showMessageDialog(null, "Thất bại. Xin vui lòng thử lại sau");
			}
		} catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Failed",e);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Thất bại. Xin vui lòng thử lại sau");
			logger.error("Lỗi khi gọi hàm addPatients()");
		} finally{
			try {
				if(st != null){
				st.close();}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("Failed",e);
			}
		}
		dispose();
	}
}

