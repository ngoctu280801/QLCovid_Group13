package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;

import model.DbInteraction;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Vector;

import javax.swing.SwingConstants;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.CardLayout;
import java.awt.Font;

import javax.swing.JComboBox;

public class Packages extends JDialog {
	private DbInteraction dbi;
	private String idCard;
	private final JPanel pnContent = new JPanel();
	private JTextField txtPkgName;
	private JTextField txtLimit;
	private JFormattedTextField txtDate;
	private JTextField txtPrice;
	private JPanel pnUtils;
	private JButton btnShowAddPkg, btnDelPkg;
	private JButton btnAddPkg;
	private JPanel pnFind;
	private JTextField txtPkgName2Find;
	private JTable tblPkg, tblCart;
	private DefaultTableModel dtm, dtmCart, dtmBPH;
	private JButton btnShowChangePkg;
	private SimpleDateFormat df;
	private String usrManager;
	private TableRowSorter sorter;
	private JLabel lblFind;
	private JPanel pnCart;
	private JPanel pnMain;
	private JPanel pnCartTitle;
	private JPanel pnCartFunc;
	private JLabel lblCart;
	private JButton btnRemoveFromCart;
	private JLabel lblAmount;
	private JLabel lblCost;
	private JLabel lblQuantity;
	private JComboBox cbQuantity;
	private JButton btnAddToCart;
	private JPanel pnCartUtils;
	private JPanel pnPay;
	private JPanel pnCost;
	private JButton btnPay;
	private JPanel panel;
	private Vector<Integer> quantityOfBoughtPkgL;

	/**
	 * Create the dialog.
	 */
	public Packages(DbInteraction dbi, String usrManager, String idCard, DefaultTableModel dtmBPH) {
		this.dbi = dbi;
		this.usrManager = usrManager;
		this.idCard = idCard;
		this.dtmBPH = dtmBPH;
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Các gói nhu yếu phẩm");
		setBounds(100, 100, 1020, 550);
		addControls();
		addEvents();
		if(usrManager.equals("")){
			quantityOfBoughtPkgL = new Vector<Integer>();
			pnUtils.setVisible(false);
			btnShowAddPkg.setVisible(false);
			btnShowAddPkg.setEnabled(false);
			btnShowChangePkg.setVisible(false);
			btnShowChangePkg.setEnabled(false);
			btnDelPkg.setVisible(false);
			btnDelPkg.setEnabled(false);
		}
		setLocationRelativeTo(null);
	}
	private void addEvents() {

		txtPkgName2Find.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent arg0){
				if(txtPkgName2Find.getText().equals("Nhập tên gói")){
					txtPkgName2Find.setText("");
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(!txtPkgName2Find.getText().equals("")){
					String space = "";
					String txtPkgN = txtPkgName2Find.getText();
					if(txtPkgN.charAt(txtPkgN.length() - 1) == ' '){
						space = " ";
					}
					txtPkgName2Find.setText(txtPkgN.trim().toUpperCase() + space);

				}
				search(txtPkgName2Find.getText());
			}
		});
		txtPkgName2Find.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				txtPkgName2Find.setText("");
			}
		});
		tblPkg.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				if(!usrManager.equals("")){
					if(!btnShowAddPkg.getText().equals("Huỷ thêm")){
						btnShowChangePkg.setEnabled(true);
					}
					if( tblPkg.getSelectedRowCount() > 1){
						if(!btnShowAddPkg.getText().equals("Huỷ thêm")){
							btnShowAddPkg.setEnabled(true);
							pnUtils.setVisible(false);
							txtDate.setText("");
							txtLimit.setText("");
							txtPkgName.setText("");
							txtPrice.setText("");
							btnShowChangePkg.setText("Sửa gói");
						}
						btnShowChangePkg.setEnabled(false);
					}
					else{
						if(btnShowChangePkg.isEnabled() && btnShowChangePkg.getText().equals("Huỷ sửa")){
							saveOldLine();
						}
					}
					btnDelPkg.setEnabled(true);
				}
				else{
					if(tblPkg.getSelectedRowCount() > 1 || isExistedInCart(getValInTableAt(0))){
						btnAddToCart.setEnabled(false);
					}
					else{
						btnAddToCart.setEnabled(true);
					}
				}
			}
		});
		if(!usrManager.equals("")){
			txtPrice.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					txtPrice.setText(validateNum(new StringBuilder(txtPrice.getText())));;
					onOffChangeBtn();
				}
			});
			btnShowAddPkg.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(btnShowAddPkg.isEnabled()){
						btnAddPkg.setEnabled(false);
						if(btnShowAddPkg.getText().equals("Thêm gói")){
							btnShowAddPkg.setText("Huỷ thêm");
							btnAddPkg.setText("Thêm");
							btnShowChangePkg.setEnabled(false);
							pnUtils.setVisible(true);							
						}
						else{
							if(tblPkg.getSelectedRowCount() == 1){
								btnShowChangePkg.setEnabled(true);
							}
							pnUtils.setVisible(false);
							txtDate.setText("");
							txtLimit.setText("");
							txtPkgName.setText("");
							txtPrice.setText("");
							btnShowAddPkg.setText("Thêm gói");
						}
					}
				}
			});
			txtLimit.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent arg0) {
					txtLimit.setText(validateNum(new StringBuilder(txtLimit.getText())));;
					onOffChangeBtn();
				}
			});
			btnShowChangePkg.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(btnShowChangePkg.isEnabled()){

						if(btnShowChangePkg.getText().equals("Sửa gói")){						
							btnShowChangePkg.setText("Huỷ sửa");
							btnAddPkg.setText("Thay đổi");
							saveOldLine();
							pnUtils.setVisible(true);
							btnShowAddPkg.setEnabled(false);
							btnDelPkg.setEnabled(false);
							btnAddPkg.setEnabled(false);
						}
						else{
							btnDelPkg.setEnabled(true);
							btnShowAddPkg.setEnabled(true);
							pnUtils.setVisible(false);
							txtDate.setText("");
							txtLimit.setText("");
							txtPkgName.setText("");
							txtPrice.setText("");
							btnShowChangePkg.setText("Sửa gói");
						}
					}				
				}			
			});
			btnDelPkg.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(btnDelPkg.isEnabled() && tblPkg.getSelectedRowCount() > 0){
						int dialogResult = JOptionPane.showConfirmDialog (null, 
								"Bạn có chắc chắn muốn xoá?", "Xoá", 2);
						if(dialogResult == JOptionPane.YES_OPTION){
							StringBuilder pkgNList = new StringBuilder("");
							int counter = 0;
							while(!tblPkg.getSelectionModel().isSelectionEmpty()){
								counter++;
								pkgNList.append(tblPkg.getValueAt(tblPkg.getSelectedRow(), 0) + ";");

								dtm.removeRow(tblPkg.getSelectedRow());
							}
							pkgNList = pkgNList.deleteCharAt(pkgNList.length() - 1);
							final String pkgL = pkgNList.toString();
							final int total = counter;
							Runnable runLogin = new Runnable(){
								public void run(){
									deletePkg(pkgL, total);
								}
							};
							Thread t = new Thread(runLogin);
							t.start();


						}
					}
				}
			});
			btnAddPkg.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(!btnAddPkg.isEnabled()){
						return;
					}

					if(txtPkgName.getText().equals("")){
						JOptionPane.showMessageDialog(null, "Vui lòng xem lại tên gói");
						return;
					}
					if(txtLimit.getText().equals("")){
						JOptionPane.showMessageDialog(null, "Vui lòng xem lại giới hạn gói cho mỗi người");
						return;
					}
					if(txtDate.getText().equals("")){
						JOptionPane.showMessageDialog(null, "Vui lòng xem lại ngày cuối mở bán");
						return;
					}
					if(txtPrice.getText().equals("")){
						JOptionPane.showMessageDialog(null, "Vui lòng xem lại giá của gói");
						return;
					}
					if(isPkgNameExisted(txtPkgName.getText()) && 
							!btnAddPkg.getText().equals("Thay đổi")){
						JOptionPane.showMessageDialog(null, "Tên gói này đã tồn tại, vui lòng chọn tên khác");
						return;
					}
					if(btnAddPkg.getText().equals("Thay đổi")){
						Runnable runUpdate = new Runnable(){
							public void run(){
								changePkg();
							}
						};
						Thread t = new Thread(runUpdate);
						t.start();
					}
					else{
						Runnable runAdd = new Runnable(){
							public void run(){
								payPkg();
							}
						};
						Thread t = new Thread(runAdd);
						t.start();

					}
				}
			});
			txtDate.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					if(txtDate.getText().contains(" ")){
						txtDate.setValue(null);
						if(btnAddPkg.getText().equals("Thay đổi")){
							txtDate.setText(getValInTableAt(2));
							onOffChangeBtn();
						}
						return;
					}
					//if(!txtDate.getText().equals("  /  /    ")){
					//JOptionPane.showMessageDialog(null, txtDate.getValue());
					try {
						Date d = df.parse(txtDate.getText());
					} catch (ParseException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(null, "Không tồn tại ngày này");
						txtDate.setValue(null);
						return;
					} 

					Date d = new Date(swapDayWithMon(txtDate.getText()));
					if(d.before(new Date())){
						JOptionPane.showMessageDialog(null, "Ngày cuối mở bán phải từ hôm nay trở đi");
						txtDate.setValue(null);
						return;
					}

					//}

				}
			});
			txtDate.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent arg0) {
					onOffChangeBtn();
				}
			});
			txtPkgName.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					if(!txtPkgName.getText().equals("")){
						txtPkgName.setText(txtPkgName.getText().trim().toUpperCase());
						//onOffChangeBtn();
					}
				}
			});
			txtPkgName.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent arg0) {
					onOffChangeBtn();
				}
			});
		}
		else{

			btnAddToCart.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(btnAddToCart.isEnabled()){
						if(canBuy(getValInTableAt(0))){
							dtmCart.addRow(new String[] {getValInTableAt(0), "1", getValInTableAt(3)});
							updateCost(Integer.parseInt(getValInTableAt(3).replace(",", "")));
							btnAddToCart.setEnabled(false);
							btnPay.setEnabled(true);
						}
						else{
							return;
						}
						if(!pnCart.isVisible()){
							lblQuantity.setVisible(false);
							cbQuantity.setVisible(false);
							btnRemoveFromCart.setEnabled(false);
							btnRemoveFromCart.setVisible(false);
							pnCart.setVisible(true);

						}
					}
				}
			});
			tblCart.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent event) {
					if(tblCart.getSelectedRowCount() == 1){
						lblQuantity.setVisible(true);
						updateQuantitySelection();
						cbQuantity.setEnabled(true);
						cbQuantity.setVisible(true);
					}
					else{
						cbQuantity.removeAllItems();
						cbQuantity.setEnabled(false);
					}
					btnRemoveFromCart.setEnabled(true);
					btnRemoveFromCart.setVisible(true);
				}
			});
			cbQuantity.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(cbQuantity.isEnabled() && tblCart.getSelectedRowCount() == 1){
						String priceSelectedPkg = tblCart.getValueAt(
								tblCart.getSelectedRow(), 2).toString().replace(",", "");
						int oldCostSelectedPkg = Integer.parseInt(
								tblCart.getValueAt(tblCart.getSelectedRow(), 1) + "") * 
								Integer.parseInt(priceSelectedPkg);
						int newQuan = Integer.parseInt(cbQuantity.getSelectedItem().toString());
						int newCostSelectedPkg = newQuan *
								Integer.parseInt(priceSelectedPkg);
						tblCart.setValueAt(newQuan, tblCart.getSelectedRow(), 1);
						updateCost(newCostSelectedPkg - oldCostSelectedPkg);
					}
				}
			});
			btnRemoveFromCart.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(btnRemoveFromCart.isEnabled()){
						int dialogResult = JOptionPane.showConfirmDialog (null, 
								"Bạn có chắc chắn muốn xoá?", "Xoá", 2);
						if(dialogResult == JOptionPane.YES_OPTION){
							cbQuantity.setEnabled(false);
							int credit = 0;
							//JOptionPane.showMessageDialog(null, quantityOfBoughtPkgL);
							while(!tblCart.getSelectionModel().isSelectionEmpty()){
								String priceSelected = tblCart.getValueAt(
										tblCart.getSelectedRow(), 2).toString().replace(",", "");
								String quanSelected = tblCart.getValueAt(tblCart.getSelectedRow(), 1).toString();
								credit += (Integer.parseInt(quanSelected) * Integer.parseInt(priceSelected));

								quantityOfBoughtPkgL.remove(tblCart.getSelectedRow());
								dtmCart.removeRow(tblCart.getSelectedRow());
							}
							updateCost(-credit);
							btnAddToCart.setEnabled(true);
							btnRemoveFromCart.setEnabled(false);
							cbQuantity.removeAllItems();
							cbQuantity.setEnabled(false);
							if(tblCart.getRowCount() == 0){
								btnPay.setEnabled(false);
							}
							//JOptionPane.showMessageDialog(null, quantityOfBoughtPkgL);
						}
					}
				}
			});
			btnPay.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(btnPay.isEnabled()){
						Runnable run = new Runnable(){
							public void run(){
								payPkg();
							}
						};
						Thread t = new Thread(run);
						t.start();
						
					}
				}
			});
		}

	}

	private void addControls() {

		df = new SimpleDateFormat("dd/MM/yyyy");
		df.setLenient(false);
		MaskFormatter dateMask = null;
		try {
			dateMask = new MaskFormatter("##/##/####");
		} catch (Exception e) {
			e.printStackTrace();
		}

		dtm = new DefaultTableModel();
		dtm.addColumn("Tên gói");
		dtm.addColumn("Mức giới hạn (gói/ người)");
		dtm.addColumn("Bán đến hết ngày");
		dtm.addColumn("Giá thành");
		//tblPkg.setAutoCreateRowSorter(true);
		sorter = new TableRowSorter<>(dtm);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

		pnMain = new JPanel();
		getContentPane().add(pnMain);
		pnMain.setLayout(new BorderLayout(0, 0));
		pnMain.add(pnContent);
		pnContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		pnContent.setLayout(new BorderLayout(0, 0));
		pnUtils = new JPanel();
		pnContent.add(pnUtils, BorderLayout.SOUTH);
		JLabel lblPkgName = new JLabel("Tên gói:");
		pnUtils.add(lblPkgName);
		txtPkgName = new JTextField();
		pnUtils.add(txtPkgName);
		txtPkgName.setColumns(10);
		JLabel lblLimit = new JLabel("Mức giới hạn ( gói/ người):");
		pnUtils.add(lblLimit);
		txtLimit = new JTextField();
		pnUtils.add(txtLimit);
		txtLimit.setColumns(3);
		JLabel lblDate = new JLabel("Mở bán đến hết ngày:");
		pnUtils.add(lblDate);

		txtDate = new JFormattedTextField(dateMask);
		txtDate.setHorizontalAlignment(SwingConstants.CENTER);
		pnUtils.add(txtDate);
		txtDate.setColumns(6);
		JLabel lblPrice = new JLabel("Đơn giá (VNĐ):");
		pnUtils.add(lblPrice);
		txtPrice = new JTextField();
		pnUtils.add(txtPrice);
		txtPrice.setColumns(10);
		btnAddPkg = new JButton("Thêm");
		pnUtils.add(btnAddPkg);
		JPanel pnPkg = new JPanel();
		pnContent.add(pnPkg, BorderLayout.CENTER);
		pnFind = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pnFind.getLayout();
		flowLayout.setAlignment(FlowLayout.TRAILING);
		pnContent.add(pnFind, BorderLayout.NORTH);

		lblFind = new JLabel("Tìm kiếm:");
		pnFind.add(lblFind);
		txtPkgName2Find = new JTextField();
		txtPkgName2Find.setText("Nhập tên gói");
		pnFind.add(txtPkgName2Find);
		txtPkgName2Find.setColumns(12);
		pnUtils.setVisible(false);
		tblPkg = new JTable(dtm);
		tblPkg.setRowSorter(sorter);
		tblPkg.getRowSorter().toggleSortOrder(0);
		// Prevent manager edit this table
		tblPkg.setDefaultEditor(Object.class, null);
		//		// Terminate edit when focus is lost
		//		tblPkg.putClientProperty("terminateEditOnFocusLost", true);

		JScrollPane scrollPane = new JScrollPane(
				tblPkg,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		pnPkg.setLayout(new BorderLayout());
		pnPkg.add(scrollPane, BorderLayout.CENTER);
		JPanel pnBtn = new JPanel();
		pnMain.add(pnBtn, BorderLayout.SOUTH);
		pnBtn.setLayout(new FlowLayout(FlowLayout.CENTER));


		btnShowAddPkg = new JButton("Thêm gói");
		btnShowAddPkg.setActionCommand("OK");
		pnBtn.add(btnShowAddPkg);
		getRootPane().setDefaultButton(btnShowAddPkg);

		btnShowChangePkg = new JButton("Sửa gói");
		pnBtn.add(btnShowChangePkg);
		btnShowChangePkg.setEnabled(false);
		btnDelPkg = new JButton("Xoá gói");
		btnDelPkg.setActionCommand("Cancel");
		pnBtn.add(btnDelPkg);
		btnDelPkg.setEnabled(false);


		if(usrManager.equals("")){
		dtmCart = new DefaultTableModel();
		dtmCart.addColumn("Tên gói");
		dtmCart.addColumn("Số lượng");
		dtmCart.addColumn("Giá tiền 1 gói");
		btnAddToCart = new JButton("Chọn mua");
		pnBtn.add(btnAddToCart);
		btnAddToCart.setEnabled(false);
		pnCart = new JPanel();
		getContentPane().add(pnCart);
		tblCart = new JTable(dtmCart);
		// Prevent manager edit this table
		tblCart.setDefaultEditor(Object.class, null);

		JScrollPane scrollPaneCart = new JScrollPane(
				tblCart,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		pnCart.setLayout(new BorderLayout());
		pnCart.add(scrollPaneCart, BorderLayout.CENTER);

		pnCartFunc = new JPanel();
		pnCart.add(pnCartFunc, BorderLayout.SOUTH);
		pnCartFunc.setLayout(new BoxLayout(pnCartFunc, BoxLayout.X_AXIS));

		pnCartUtils = new JPanel();
		pnCartFunc.add(pnCartUtils);

		lblQuantity = new JLabel("Số lượng");
		pnCartUtils.add(lblQuantity);

		cbQuantity = new JComboBox();
		pnCartUtils.add(cbQuantity);

		btnRemoveFromCart = new JButton("Xoá");
		pnCartUtils.add(btnRemoveFromCart);

		pnPay = new JPanel();
		pnCartFunc.add(pnPay);
		pnPay.setLayout(new BoxLayout(pnPay, BoxLayout.Y_AXIS));

		pnCost = new JPanel();
		pnPay.add(pnCost);

		lblAmount = new JLabel("Thành tiền:");
		pnCost.add(lblAmount);

		lblCost = new JLabel("0 (VNĐ)");
		pnCost.add(lblCost);
		lblCost.setFont(new Font("Tahoma", Font.BOLD, 17));

		panel = new JPanel();
		pnPay.add(panel);

		btnPay = new JButton("Đặt mua");
		panel.add(btnPay);

		pnCartTitle = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) pnCartTitle.getLayout();
		flowLayout_1.setVgap(9);
		pnCart.add(pnCartTitle, BorderLayout.NORTH);

		lblCart = new JLabel("Các gói nhu yếu phẩm muốn mua");
		lblCart.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCart.setHorizontalAlignment(SwingConstants.CENTER);
		pnCartTitle.add(lblCart);
		pnCart.setVisible(false);
		}



		getDataFromDb();
		if(tblPkg.getRowCount() == 0){
			JOptionPane.showMessageDialog(null, "Chưa có gói nhu yếu phẩm nào đang được bán");
		}
	}
	private void saveOldLine(){
		txtPkgName.setText((String) tblPkg.getValueAt(tblPkg.getSelectedRow(), 0));
		txtLimit.setText((String) tblPkg.getValueAt(tblPkg.getSelectedRow(), 1));
		txtDate.setText((String) tblPkg.getValueAt(tblPkg.getSelectedRow(), 2));
		txtPrice.setText((String) tblPkg.getValueAt(tblPkg.getSelectedRow(), 3));
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
	private void getDataFromDb(){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select pkg_name,"
				+ " limit_quantity_per_person, date_limit, price from necessary_packages"
				+ " where is_deleted = 0", stmt);
		try {
			if(rs.isBeforeFirst()){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				while(rs.next()){
					Vector<String> rowData = new Vector<String>();
					rowData.add(rs.getString(1));
					rowData.add(validateNum(new StringBuilder(rs.getString(2))));
					String d = rs.getString(3).replace("-", "/");
					rowData.add(changeDateFormatter(d, "dd/MM/yyyy", sdf));
					rowData.add(validateNum(new StringBuilder(rs.getString(4))));
					dtm.addRow(rowData);
				}
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
	private void addPkg(){
		String date = changeDateFormatter(txtDate.getValue().toString(), "yyyy/MM/dd", df);
		CallableStatement st = null;
		try {
			st = dbi.getStatement("{call addPkg(?, ?, ?, ?, ?, ?)}");
			st.registerOutParameter(6, Types.INTEGER);
			st.setString(1, txtPkgName.getText());
			st.setString(2, txtLimit.getText());
			st.setString(3, date);
			st.setString(4, txtPrice.getText());
			st.setString(5, usrManager);
			st.execute();
			int code  = st.getInt("code");
			if(code == 1){
				dtm.addRow(new String[] {txtPkgName.getText(), txtLimit.getText(),
						txtDate.getValue().toString(), txtPrice.getText()});
				JOptionPane.showMessageDialog(null, "Thêm gói nhu yếu phẩm thành công");
			}
			else{
				JOptionPane.showMessageDialog(null, "Lỗi khi thực hiện thêm gói nhu yếu phẩm");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Lỗi không xác định khi thêm gói");
		} finally{
			if(st != null){
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	private void changePkg(){
		String date = changeDateFormatter(txtDate.getValue().toString(), "yyyy/MM/dd", df);
		CallableStatement st = null;
		try {
			st = dbi.getStatement("{call updatePkg(?, ?, ?, ?, ?, ?, ?)}");
			st.registerOutParameter(7, Types.INTEGER);
			st.setString(1, getValInTableAt(0));
			st.setString(2, txtLimit.getText());
			st.setString(3, date);
			st.setString(4, txtPrice.getText());
			st.setString(5, usrManager);
			st.setString(6, txtPkgName.getText());
			st.execute();
			int code = st.getInt("code");
			if(code == 1){		
				int row = tblPkg.getSelectedRow();
				dtm.setValueAt(txtPkgName.getText(), row, 0);
				dtm.setValueAt(txtLimit.getText(), row, 1);
				dtm.setValueAt(txtDate.getValue().toString(), row, 2);
				dtm.setValueAt(txtPrice.getText(), row, 3);
				btnAddPkg.setEnabled(false);
				JOptionPane.showMessageDialog(null, "Cập nhật gói thành công");
			}
			else{
				JOptionPane.showMessageDialog(null, "Thất bại. Xin vui lòng thử lại sau");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi, không thể cập nhật gói lúc này");
		} finally{
			if(st != null){
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	private void deletePkg(String pkgNList, int counter){
		CallableStatement st = null;
		try {
			st = dbi.getStatement("{call delPkg(?, ?, ?)}");
			st.registerOutParameter(3, Types.INTEGER);
			st.setString(1, pkgNList);
			st.setString(2, usrManager);
			st.execute();
			int code  = st.getInt("code");
			if(code == counter){
				JOptionPane.showMessageDialog(null, "Xoá thành công " + code + " gói");
				return;
			}
			else{
				JOptionPane.showMessageDialog(null, "Không thể xoá " + (counter - code) + " gói trong số đó");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi, không thể xoá gói nào");
		} finally{
			if(st != null){
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		dtm.setRowCount(0);
		getDataFromDb();
	}
	private String changeDateFormatter(String date, String format, SimpleDateFormat sdf){
		SimpleDateFormat f = new SimpleDateFormat(format);
		String res = null;
		try {
			res = f.format(sdf.parse(date));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	private boolean isPkgNameExisted(String pkgN){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select count(*) from necessary_packages where pkg_name = N'"
				+ pkgN + "' and is_deleted = 0;", stmt);
		try {
			rs.next();
			if(rs.getString(1).equals("0")){
				return false;
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
		return true;
	}

	private void search(String s) {
		if (s.length() != 0) {
			sorter.setRowFilter(RowFilter.regexFilter(s));

		} else {
			sorter.setRowFilter(null);
		}
	}
	private boolean isChanged(){

		//JOptionPane.showMessageDialog(null,txtDate.getText());
		if(!txtPkgName.getText().toUpperCase().equals(getValInTableAt(0)) ||
				!txtLimit.getText().equals(getValInTableAt(1)) ||
				!txtDate.getText().equals(getValInTableAt(2)) ||
				!txtPrice.getText().equals(getValInTableAt(3))){
			return true;
		}

		return false;
	}
	private void onOffChangeBtn(){
		if(txtPkgName.getText().equals("") ||
				txtLimit.getText().equals("") ||
				txtDate.getText().equals("  /  /    ") ||
				txtPrice.getText().equals("") ){
			btnAddPkg.setEnabled(false);
			return;
		}
		if(btnAddPkg.getText().equals("Thay đổi")){
			//JOptionPane.showMessageDialog(null,isChanged());
			if(isChanged()){
				btnAddPkg.setEnabled(true);
			}
			else{
				btnAddPkg.setEnabled(false);
			}
		}
		else{
			btnAddPkg.setEnabled(true);
		}
	}
	private String getValInTableAt(int col){
		return tblPkg.getValueAt(tblPkg.getSelectedRow(), col) + "";
	}
	private void payPkg(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Vector<Vector<String>> rowData = new Vector<Vector<String>>();
		StringBuilder listPkgN = new StringBuilder();
		StringBuilder listQuan = new StringBuilder();
		for(int i = 0; i < tblCart.getRowCount(); i++){
			listPkgN.append(tblCart.getValueAt(i, 0) + "");
			listQuan.append(tblCart.getValueAt(i, 1) + "");
			if(i != tblCart.getRowCount() - 1){
				listPkgN.append(";");
				listQuan.append(";");
			}

			Vector<String> aRow = new Vector<String>();
			aRow.add(sdf.format(new Date()));
			aRow.add(tblCart.getValueAt(i, 0) + "");
			aRow.add(validateNum(new StringBuilder(tblCart.getValueAt(i, 1) + "")));
			int price = Integer.parseInt(tblCart.getValueAt(i, 2).toString().replace(",", ""));
			int quan = Integer.parseInt(tblCart.getValueAt(i, 1).toString().replace(",", ""));
			price *= quan;
			aRow.add(validateNum(new StringBuilder(price + "")));
			rowData.add(aRow);
		}
		CallableStatement st = null;
		st = dbi.getStatement("{call insertIntoBoughtPkgHis(?, ?, ?, ?)}");
		try {
			st.registerOutParameter(4, Types.INTEGER);
			st.setString(1, idCard);
			st.setString(2, listPkgN.toString());
			st.setString(3, listQuan.toString());
			st.execute();
			int code = st.getInt("code");
			if(code == rowData.size()){		
				for(int i = 0; i < tblCart.getRowCount(); i++){
					dtmBPH.addRow(rowData.get(i));
				}
				JOptionPane.showMessageDialog(null, "Đặt mua thành công");
			}
			else{
				JOptionPane.showMessageDialog(null, "Thất bại. Xin vui lòng thử lại sau");
			}		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(st != null){
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
						
		dispose();
	}
	private boolean canBuy(String pkgN){
		Date d = new Date(swapDayWithMon(getValInTableAt(2)));
		if(d.before(new Date())){
			JOptionPane.showMessageDialog(null, "Gói này đã ngừng bán, vui lòng chọn gói khác");
			return false;
		}
		int quan = getQuantityOfBoughtPkg(pkgN);
		if(quan == -2){
			quan = 0;
		}
		if(quan > -1 || quan == -2){
			String pricePkg = getValInTableAt(3).replace(",", "");
			String maxQuan = getValInTableAt(1).replace(",", "");
			if(quan == Integer.parseInt(maxQuan)){
				JOptionPane.showMessageDialog(null, "Không thể mua vì đã đạt tới mức giới hạn mua của gói này");
				return false;
			}

			quantityOfBoughtPkgL.add(Integer.parseInt(maxQuan) - quan);
			return true;
		}
		else{
			JOptionPane.showMessageDialog(null, "Không thể lấy được số lượng gói đã mua");
			return false;
		}
	}
	private String swapDayWithMon(String date){
		String day = date.substring(0, 2);
		String month = date.substring(3, 5);
		String year = date.substring(6, 10);
		return month + "/" + day + "/" + year + " 23:59:59";
	}
	private int getQuantityOfBoughtPkg(String pkgN){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select sum(bph.quantity) from bought_pkg_history bph "
				+ "join patients p on p.id = bph.id_patient "
				+ "join necessary_packages np on np.id = bph.id_pkg "
				+ "where p.id_card = '" + idCard + "' and np.pkg_name = N'" + pkgN + "'", stmt);
		try {
			rs.next();
			String temp = rs.getString(1);
			if(rs.wasNull()){
				return -2;
			}
			return Integer.parseInt(temp);
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
		return -1;
	}
	private void updateCost(int credit){
		String strCost = lblCost.getText().substring(0, lblCost.getText().length() - 6);
		int cur_cost = Integer.parseInt(strCost.replace(",", ""));
		cur_cost += credit;
		lblCost.setText(validateNum(new StringBuilder(cur_cost + "")) + " (VNĐ)");
	}
	private void updateQuantitySelection(){
		cbQuantity.setEnabled(false);
		cbQuantity.removeAllItems();
		for(int i = 1; i <= quantityOfBoughtPkgL.get(tblCart.getSelectedRow()); i++){
			cbQuantity.addItem(i + "");
		}
		cbQuantity.setSelectedItem(tblCart.getValueAt(tblCart.getSelectedRow(), 1) + "");
		cbQuantity.setEnabled(true);
	}
	private boolean isExistedInCart(String pkgN){
		for (int i = 0; i < tblCart.getRowCount(); i++) {
			if(pkgN.equals(tblCart.getValueAt(i, 0))){
				return true;
			}
		}
		return false;
	}
}
