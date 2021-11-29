package view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import model.DbInteraction;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Component;

import org.apache.log4j.Logger;

public class AdminPanel extends JFrame {
	private DbInteraction dbi;
	private static final Logger logger = Logger.getLogger(AdminPanel.class); 
	private JPanel pnContent;
	private JTable tblManagerL, tblQrtPosL;
	private DefaultTableModel dtmManagerL, dtmQrtPosL;
	private JButton btnActivityHis, btnLockAcc, btnAddQrtPos, btnUpdateQrtPos, btnAdd_Update, btnAddManager;
	private JTextField txtQrtName;
	private JTextField txtCapacity;
	private JTextField txtCurCapacity;
	private JPanel pnQrtPosMngm;
	
	/**
	 * Create the frame.
	 */
	public AdminPanel(DbInteraction dbi) {
		this.dbi = dbi;
		JOptionPane.showMessageDialog(null, 
				"Successfull. This is Admin Account!");
		logger.debug("Successfull. Login Admin Account!"); 
		setTitle("Quản trị hệ thống");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1155, 520);
		pnContent = new JPanel();
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		setContentPane(pnContent);
		pnContent.setLayout(new BoxLayout(pnContent, BoxLayout.Y_AXIS));
		addControls();
		addEvents();
		getDataFromDb("select usrname, is_locked from accounts where id_permission = 0;",
				dtmManagerL, false);
		getDataFromDb("select name, capacity, current_capacity from quarantinepos;",
				dtmQrtPosL, true);
		//dtmQrtPosL.addRow(new String[] {"1", "2", "3"});
	}
	private void addControls(){
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.05);
		pnContent.add(splitPane);
		
		JPanel pnManageManager = new JPanel();
		splitPane.setLeftComponent(pnManageManager);
		pnManageManager.setLayout(new BorderLayout(0, 0));
		
		JPanel pnManagerList = new JPanel();
		pnManageManager.add(pnManagerList, BorderLayout.CENTER);
		pnManagerList.setLayout(new BorderLayout(0, 0));
		
		JPanel pnUtilsManager = new JPanel();
		pnUtilsManager.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Qu\u1EA3n l\u00FD th\u00F4ng tin ng\u01B0\u1EDDi qu\u1EA3n l\u00FD", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pnManageManager.add(pnUtilsManager, BorderLayout.SOUTH);
		pnUtilsManager.setLayout(new BoxLayout(pnUtilsManager, BoxLayout.Y_AXIS));
		
		btnLockAcc = new JButton("Khoá tài khoản này");
		btnLockAcc.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnLockAcc.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		pnUtilsManager.add(btnLockAcc);
		
		btnActivityHis = new JButton("Lịch sử hoạt động của tài khoản này");
		btnActivityHis.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnActivityHis.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		pnUtilsManager.add(btnActivityHis);
		
		JPanel pnManageQrtPos = new JPanel();
		splitPane.setRightComponent(pnManageQrtPos);
		pnManageQrtPos.setLayout(new BorderLayout(0, 0));
		
		JPanel pnQrtPosList = new JPanel();
		pnManageQrtPos.add(pnQrtPosList);
		pnQrtPosList.setLayout(new BorderLayout(0, 0));
		
		JPanel pnUtilsQrtPos = new JPanel();
		pnUtilsQrtPos.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Qu\u1EA3n l\u00FD \u0111\u1ECBa \u0111i\u1EC3m \u0111i\u1EC1u tr\u1ECB/ c\u00E1ch ly", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnManageQrtPos.add(pnUtilsQrtPos, BorderLayout.SOUTH);
		pnUtilsQrtPos.setLayout(new BoxLayout(pnUtilsQrtPos, BoxLayout.Y_AXIS));
		
		
		dtmManagerL = new DefaultTableModel();
		dtmManagerL.addColumn("Tên tài khoản");
		dtmManagerL.addColumn("Trạng thái");
		tblManagerL = new JTable(dtmManagerL);
		// Prevent manager edit this table
		tblManagerL.setDefaultEditor(Object.class, null);
		
		JScrollPane scrPManagerL = new JScrollPane(
				tblManagerL,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnManagerList.setLayout(new BorderLayout());
		pnManagerList.add(scrPManagerL, BorderLayout.CENTER);
		
		
		dtmQrtPosL = new DefaultTableModel();
		dtmQrtPosL.addColumn("Tên địa điểm");
		dtmQrtPosL.addColumn("Sức chứa tối đa");
		dtmQrtPosL.addColumn("Sức chứa hiện tại");
		tblQrtPosL = new JTable(dtmQrtPosL);
		// Prevent manager edit this table
		tblQrtPosL.setDefaultEditor(Object.class, null);
		
		JScrollPane scrPQrtPosL = new JScrollPane(
				tblQrtPosL,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnQrtPosList.setLayout(new BorderLayout());
		pnQrtPosList.add(scrPQrtPosL, BorderLayout.CENTER);
		
		btnActivityHis.setEnabled(false);
		//btnAddQrtPos.setEnabled(false);
		btnLockAcc.setEnabled(false);
		
		btnAddManager = new JButton("Thêm một tài khoản người quản lý mới");
		btnAddManager.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddManager.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		pnUtilsManager.add(btnAddManager);
		
		JPanel panel = new JPanel();
		pnUtilsManager.add(panel);
		
		pnQrtPosMngm = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pnQrtPosMngm.getLayout();
		pnUtilsQrtPos.add(pnQrtPosMngm);
		
		JLabel lblQrtName = new JLabel("Tên địa điểm điều trị/ cách ly:");
		pnQrtPosMngm.add(lblQrtName);
		
		txtQrtName = new JTextField();
		pnQrtPosMngm.add(txtQrtName);
		txtQrtName.setColumns(10);
		
		JLabel lblCapacity = new JLabel("Sức chứa tối đa:");
		pnQrtPosMngm.add(lblCapacity);
		
		txtCapacity = new JTextField();
		pnQrtPosMngm.add(txtCapacity);
		txtCapacity.setColumns(6);
		
		JLabel lblCurCapacity = new JLabel("Sức chứa hiện tại");
		pnQrtPosMngm.add(lblCurCapacity);
		
		txtCurCapacity = new JTextField();
		pnQrtPosMngm.add(txtCurCapacity);
		txtCurCapacity.setColumns(6);
		
		btnAdd_Update = new JButton("Thêm");
		pnQrtPosMngm.add(btnAdd_Update);
		pnQrtPosMngm.setVisible(false);
		JPanel pnQrtPosInteracter = new JPanel();
		pnUtilsQrtPos.add(pnQrtPosInteracter);
		
		btnAddQrtPos = new JButton("Thêm địa điểm mới");
		pnQrtPosInteracter.add(btnAddQrtPos);
		
		btnUpdateQrtPos = new JButton("Sửa địa điểm");
		pnQrtPosInteracter.add(btnUpdateQrtPos);
		btnUpdateQrtPos.setEnabled(false);
		
	}
	private void addEvents(){
		tblManagerL.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	btnActivityHis.setEnabled(true);
	        	btnLockAcc.setEnabled(true);
	            if(tblManagerL.getValueAt(tblManagerL.getSelectedRow(), 1).toString().equals("Hoạt động")){
	            	btnLockAcc.setText("Khoá tài khoản này");
	            }
	            else{
	            	btnLockAcc.setText("Mở khoá tài khoản này");
	            }
	        }
	    });
		
		tblQrtPosL.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	if(tblQrtPosL.getSelectedRowCount() > 1 && !btnAddQrtPos.getText().equals("Huỷ thêm")){
    				btnUpdateQrtPos.setText("Sửa địa điểm");
					btnAdd_Update.setEnabled(false);
					resetInput();
					pnQrtPosMngm.setVisible(false);
					btnAddQrtPos.setEnabled(true);
	        		btnUpdateQrtPos.setEnabled(false);
	        		return;
	        	}
	        	if(btnAddQrtPos.getText().equals("Thêm địa điểm mới")){
	        		btnUpdateQrtPos.setEnabled(true);
	        		if(btnUpdateQrtPos.getText().equals("Huỷ sửa")){
	        			
	        			insertInput();
	        		}
	        	}
	        }
	    });

		//btnActivityHis
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				dbi.close();
			}
		});

		//btnLockAcc

		txtQrtName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				txtQrtName.setText(txtQrtName.getText().trim());
			}
		});

		txtCapacity.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				txtCapacity.setText(validateNum(new StringBuilder(txtCapacity.getText())));
				checkChange();
			}
		});

		txtCurCapacity.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				txtCurCapacity.setText(validateNum(new StringBuilder(txtCurCapacity.getText())));
				checkChange();
			}
		});

		btnAddQrtPos.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(btnAddQrtPos.isEnabled()){
					if(btnAddQrtPos.getText().equals("Huỷ thêm")){
						btnAddQrtPos.setText("Thêm địa điểm mới");
						resetInput();
						btnAdd_Update.setEnabled(false);
						pnQrtPosMngm.setVisible(false);
						if(tblQrtPosL.getSelectedRowCount() == 1){
							btnUpdateQrtPos.setEnabled(true);
						}
					}
					else{
						btnAddQrtPos.setText("Huỷ thêm");
						btnUpdateQrtPos.setEnabled(false);
						pnQrtPosMngm.setVisible(true);
						btnAdd_Update.setEnabled(true);
						btnAdd_Update.setText("Thêm");
					}
				}
			}
		});

		btnUpdateQrtPos.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnUpdateQrtPos.isEnabled()){
					if(btnUpdateQrtPos.getText().equals("Huỷ sửa")){
						btnUpdateQrtPos.setText("Sửa địa điểm");
						btnAdd_Update.setEnabled(false);
						resetInput();
						pnQrtPosMngm.setVisible(false);
						btnAddQrtPos.setEnabled(true);
					}
					else{
						btnAddQrtPos.setEnabled(false);
						btnAdd_Update.setEnabled(false);
						pnQrtPosMngm.setVisible(true);
						insertInput();
						btnUpdateQrtPos.setText("Huỷ sửa");
						btnAdd_Update.setText("Sửa");
					}
				}
			}
		});

		btnAdd_Update.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnAdd_Update.isEnabled()){
					if(!btnUpdateQrtPos.getText().equals("Huỷ sửa") && isQrtNameExisted(txtQrtName.getText())){
						JOptionPane.showMessageDialog(null, "Tên địa điểm này đã có trong hệ thống");
						return;
					}
					if(isEmpty()){
						JOptionPane.showMessageDialog(null, "Vui lòng không bỏ trống thông tin nào");
						return;
					}
					if(Integer.parseInt(txtCapacity.getText().replace(",", "")) < 
							Integer.parseInt(txtCurCapacity.getText().replace(",", ""))){
						JOptionPane.showMessageDialog(null, "Sức chứa tối đa phải lớn hơn hoặc bằng sức chứa hiện tại");
						return;
					}
					if(btnAdd_Update.getText().equals("Thêm")){
						addQrtPos(txtQrtName.getText(), txtCapacity.getText(), txtCurCapacity.getText());
					}
					else{
						updateQrtPos(txtQrtName.getText(), txtCapacity.getText(), txtCurCapacity.getText());
					}
				}
			}
		});
		txtQrtName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(txtQrtName.getText().length() > 1){
					String space = "";
					char c = 'ư';
					if(txtQrtName.getText().charAt(txtQrtName.getText().length() - 1) == ' '){
						space = " ";
					}
					String qrtN = txtQrtName.getText().trim() + space;
					if(qrtN.length() > 50){
						qrtN = qrtN.substring(0, 50);
						JOptionPane.showMessageDialog(null, "Vui lòng không nhập quá 50 ký tự");
					}
					txtQrtName.setText(qrtN);
				}
				checkChange();
			}
		});
		btnAddManager.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				AdmRegister reg = new AdmRegister(dbi, 1, "", dtmManagerL, null);
				reg.setModal(true);
				reg.setVisible(true);
			}
		});
	}
	private void getDataFromDb(String sql, DefaultTableModel dtmTemp, boolean isQrtPosL){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query(sql, stmt);
		try {
			if(rs.isBeforeFirst()){
				while(rs.next()){
					Vector<String> rowData = new Vector<String>();
					rowData.add(rs.getString(1));
					if(isQrtPosL){
						rowData.add(validateNum(new StringBuilder(rs.getString(2))));
						rowData.add(validateNum(new StringBuilder(rs.getString(3))));
					}
					else{
						if(rs.getString(2).equals("0")){
							rowData.add("Hoạt động");
						}
						else{
							rowData.add("Bị khoá");
						}
					}
					dtmTemp.addRow(rowData);
				}
			}
			else{
				JOptionPane.showMessageDialog(null, "Chưa tồn tại người quản lý nào trong hệ thống");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  finally {
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
	private boolean LockOrUnlock(String usrManager, int isLocked){
		Statement[] stmt = new Statement[] {null};
		int rows = dbi.insert("update accounts set is_locked = " + isLocked +
				" where usrname = '" + usrManager + "'", stmt);
		try {
			if(stmt[0] != null){
				stmt[0].close();
			}				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(rows > 0){
			return true;
		}		
		return false;
	}
	private void resetInput(){
		txtQrtName.setText("");
		txtCapacity.setText("");
		txtCurCapacity.setText("");
	}
	private void insertInput(){
		txtQrtName.setText(tblQrtPosL.getValueAt(tblQrtPosL.getSelectedRow(), 0) + "");
		txtCapacity.setText(tblQrtPosL.getValueAt(tblQrtPosL.getSelectedRow(), 1) + "");
		txtCurCapacity.setText(tblQrtPosL.getValueAt(tblQrtPosL.getSelectedRow(), 2) + "");
	}
	private boolean isQrtNameExisted(String qName){
		for(int row = 0; row < tblQrtPosL.getRowCount(); row++){
			if(tblQrtPosL.getValueAt(row, 0).toString().equals(qName)){
				return true;
			}
		}
		return false;
	}
	private void addQrtPos(String qName, String cap, String curCap){
		cap = cap.replace(",", "");
		curCap = curCap.replace(",", "");
		Statement[] stmt = new Statement[] {null};
		int rows = dbi.insert("insert into quarantinepos(name, capacity, current_capacity) "
				+ "values (N'" + qName + "', '" + cap + "', '" + curCap + "');", stmt);
		try {
			if(stmt[0] != null){
				stmt[0].close();
			}				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(rows > 0){
			dtmQrtPosL.addRow(new String[] {qName, cap, curCap});
			JOptionPane.showMessageDialog(null, "Thêm thành công");
		}
		else{
			JOptionPane.showMessageDialog(null, "Thêm thất bại");
		}
	}
	private void updateQrtPos(String qName, String cap, String curCap){
		cap = cap.replace(",", "");
		curCap = curCap.replace(",", "");
		Statement[] stmt = new Statement[] {null};
		int rows = dbi.insert("update quarantinepos set name = N'" + qName + "', "
				+ "capacity = '" + cap + "', current_capacity = '" + curCap + "' "
						+ "where name = N'" + getValAt(0) + "';", stmt);
		try {
			if(stmt[0] != null){
				stmt[0].close();
			}				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(rows > 0){
			int selectedRowIndex = tblQrtPosL.getSelectedRow();
			dtmQrtPosL.setValueAt(qName, selectedRowIndex, 0);
			dtmQrtPosL.setValueAt(cap, selectedRowIndex, 1);
			dtmQrtPosL.setValueAt(curCap, selectedRowIndex, 2);
			JOptionPane.showMessageDialog(null, "Sửa thành công");
		}
		else{
			JOptionPane.showMessageDialog(null, "Sửa thất bại");
		}
	}
	private String getValAt(int col){
		return tblQrtPosL.getValueAt(tblQrtPosL.getSelectedRow(), col) + "";
	}
	private boolean isEmpty(){
		if(txtQrtName.getText().equals("") || 
				txtCapacity.getText().equals("") || 
				txtCurCapacity.getText().equals("")){
			return true;
		}
		return false;
	}
	private boolean isChanged(){
		
		if(!txtQrtName.getText().equals(getValAt(0)) ||
				!txtCapacity.getText().equals(getValAt(1)) ||
				!txtCurCapacity.getText().equals(getValAt(2))){
			return true;
		}
		if(!isEmpty()){
			return false;
		}
		return false;
	}
	private void checkChange(){
		if(btnUpdateQrtPos.getText().equals("Huỷ sửa")){
			if(isChanged()){
				btnAdd_Update.setEnabled(true);
			}
			else{
				btnAdd_Update.setEnabled(false);
			}
		}
	}
}
