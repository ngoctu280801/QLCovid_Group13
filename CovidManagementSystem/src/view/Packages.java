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
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;

import org.apache.log4j.Logger;

import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;

import model.DateComparator;
import model.DbInteraction;
import model.Utils;
import model.VieStrComparator;

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
import java.util.Date;
import java.util.Vector;

import javax.swing.SwingConstants;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Font;

import javax.swing.JComboBox;

public class Packages extends JDialog {
	private DbInteraction dbi;
	private String idCard;
	private final JPanel pnContent = new JPanel();
	private JTextField 	txtPkgName, 
	txtLimit, txtPrice;
	private JFormattedTextField txtDate;
	private JPanel 	pnUtils, 
					pnFind, pnCart, 
					pnMain, pnCartTitle, 
					pnCartFunc,
					pnCartUtils,
					pnPay, pnCost, panel;
	private JButton btnShowAddPkg, btnDelPkg;
	private JButton btnAddPkg;
	private JTextField txtPkgName2Find;
	private static JTable tblPkg, tblCart;
	private static DefaultTableModel dtm, dtmCart, dtmBPH;
	private JButton btnShowChangePkg;
	private SimpleDateFormat df;
	private String usrManager;
	private TableRowSorter<TableModel> sorter;
	private JLabel 	lblFind, 
					lblCart, lblAmount, 
					lblCost, lblQuantity;
	private JButton btnRemoveFromCart;
	private JComboBox cbQuantity;
	private JButton btnAddToCart, btnPay;
	private Vector<Integer> quantityOfBoughtPkgL;
	private static final Logger logger = Logger.getLogger(Packages.class); 

	public Packages(DbInteraction dbi, String usrManager, String idCard, DefaultTableModel dtmBPH) {
		this.dbi = dbi;
		this.usrManager = usrManager;
		this.idCard = idCard;
		this.dtmBPH = dtmBPH;
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("C??c g??i nhu y???u ph???m");
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
				if(txtPkgName2Find.getText().equals("Nh???p t??n g??i")){
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
					if(!btnShowAddPkg.getText().equals("Hu??? th??m")){
						btnShowChangePkg.setEnabled(true);
					}
					if( tblPkg.getSelectedRowCount() > 1){
						if(!btnShowAddPkg.getText().equals("Hu??? th??m")){
							btnShowAddPkg.setEnabled(true);
							pnUtils.setVisible(false);
							txtDate.setText("");
							txtLimit.setText("");
							txtPkgName.setText("");
							txtPrice.setText("");
							btnShowChangePkg.setText("S???a g??i");
						}
						btnShowChangePkg.setEnabled(false);
					}
					else{
						if(btnShowChangePkg.isEnabled() && btnShowChangePkg.getText().equals("Hu??? s???a")){
							saveOldLine();
						}
					}
					btnDelPkg.setEnabled(true);
					onOffChangeBtn();
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
					txtPrice.setText(Utils.validateNum(new StringBuilder(txtPrice.getText())));;
					onOffChangeBtn();
				}
			});
			btnShowAddPkg.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(btnShowAddPkg.isEnabled()){
						btnAddPkg.setEnabled(false);
						if(btnShowAddPkg.getText().equals("Th??m g??i")){
							btnShowAddPkg.setText("Hu??? th??m");
							btnAddPkg.setText("Th??m");
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
							btnShowAddPkg.setText("Th??m g??i");
						}
					}
				}
			});
			txtLimit.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent arg0) {
					txtLimit.setText(Utils.validateNum(new StringBuilder(txtLimit.getText())));;
					onOffChangeBtn();
				}
			});
			btnShowChangePkg.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(btnShowChangePkg.isEnabled()){

						if(btnShowChangePkg.getText().equals("S???a g??i")){						
							btnShowChangePkg.setText("Hu??? s???a");
							btnAddPkg.setText("Thay ?????i");
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
							btnShowChangePkg.setText("S???a g??i");
						}
					}				
				}			
			});
			btnDelPkg.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(btnDelPkg.isEnabled() && tblPkg.getSelectedRowCount() > 0){
						int dialogResult = JOptionPane.showConfirmDialog (null, 
								"B???n c?? ch???c ch???n mu???n xo???", "Xo??", 2);
						if(dialogResult == JOptionPane.YES_OPTION){
							StringBuilder pkgNList = new StringBuilder("");
							final int[] selectedRows = tblPkg.getSelectedRows();
							int counter = selectedRows.length;



							for(int i = 0; i < counter; i++){
								int row = selectedRows[i];
								pkgNList.append(tblPkg.getValueAt(row, 0) + ";");
							}

							pkgNList = pkgNList.deleteCharAt(pkgNList.length() - 1);
							final String pkgL = pkgNList.toString();
							
							final int total = counter;
							Runnable runLogin = new Runnable(){
								public void run(){
									deletePkg(pkgL, total, selectedRows);
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
						JOptionPane.showMessageDialog(null, "Vui l??ng xem l???i t??n g??i");
						return;
					}
					if(txtLimit.getText().equals("")){
						JOptionPane.showMessageDialog(null, "Vui l??ng xem l???i gi???i h???n g??i cho m???i ng?????i");
						return;
					}
					if(txtDate.getText().equals("")){
						JOptionPane.showMessageDialog(null, "Vui l??ng xem l???i ng??y cu???i m??? b??n");
						return;
					}
					if(txtPrice.getText().equals("")){
						JOptionPane.showMessageDialog(null, "Vui l??ng xem l???i gi?? c???a g??i");
						return;
					}
					if(isPkgNameExisted(txtPkgName.getText()) && 
							!btnAddPkg.getText().equals("Thay ?????i")){
						JOptionPane.showMessageDialog(null, "T??n g??i n??y ???? t???n t???i, vui l??ng ch???n t??n kh??c");
						return;
					}
					if(btnAddPkg.getText().equals("Thay ?????i")){
						Runnable runUpdate = new Runnable(){
							public void run(){
								changePkg();
							}
						};
						Thread t = new Thread(runUpdate);
						t.start();
					}
					else if(btnAddPkg.getText().equals("Th??m")){
						Runnable runAdd = new Runnable(){
							public void run(){
								addPkg();
							}
						};
						Thread t = new Thread(runAdd);
						t.start();
					}
					else{
						Runnable runPay = new Runnable(){
							public void run(){
								payPkg();
							}
						};
						Thread t = new Thread(runPay);
						t.start();

					}
				}
			});
			txtDate.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					if(txtDate.getText().contains(" ")){
						txtDate.setValue(null);
						if(btnAddPkg.getText().equals("Thay ?????i")){
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
						JOptionPane.showMessageDialog(null, "Kh??ng t???n t???i ng??y n??y");
						txtDate.setValue(null);
						return;
					} 

					Date d = new Date(swapDayWithMon(txtDate.getText()));
					if(d.before(new Date())){
						JOptionPane.showMessageDialog(null, "Ng??y cu???i m??? b??n ph???i t??? h??m nay tr??? ??i");
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
								"B???n c?? ch???c ch???n mu???n xo???", "Xo??", 2);
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
			logger.error("Failed!",e);
		}

		dtm = new DefaultTableModel();
		dtm.addColumn("T??n g??i");
		dtm.addColumn("M???c gi???i h???n (g??i/ ng?????i)");
		dtm.addColumn("B??n ?????n h???t ng??y");
		dtm.addColumn("Gi?? th??nh (VN??)");
		//tblPkg.setAutoCreateRowSorter(true);
//		sorter = new TableRowSorter<TableModel>(dtm);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

		pnMain = new JPanel();
		getContentPane().add(pnMain);
		pnMain.setLayout(new BorderLayout(0, 0));
		pnMain.add(pnContent);
		pnContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		pnContent.setLayout(new BorderLayout(0, 0));
		pnUtils = new JPanel();
		pnContent.add(pnUtils, BorderLayout.SOUTH);
		JLabel lblPkgName = new JLabel("T??n g??i:");
		pnUtils.add(lblPkgName);
		txtPkgName = new JTextField();
		pnUtils.add(txtPkgName);
		txtPkgName.setColumns(10);
		JLabel lblLimit = new JLabel("M???c gi???i h???n ( g??i/ ng?????i):");
		pnUtils.add(lblLimit);
		txtLimit = new JTextField();
		pnUtils.add(txtLimit);
		txtLimit.setColumns(3);
		JLabel lblDate = new JLabel("M??? b??n ?????n h???t ng??y:");
		pnUtils.add(lblDate);

		txtDate = new JFormattedTextField(dateMask);
		txtDate.setHorizontalAlignment(SwingConstants.CENTER);
		pnUtils.add(txtDate);
		txtDate.setColumns(6);
		JLabel lblPrice = new JLabel("????n gi?? (VN??):");
		pnUtils.add(lblPrice);
		txtPrice = new JTextField();
		pnUtils.add(txtPrice);
		txtPrice.setColumns(10);
		btnAddPkg = new JButton("Th??m");
		pnUtils.add(btnAddPkg);
		JPanel pnPkg = new JPanel();
		pnContent.add(pnPkg, BorderLayout.CENTER);
		pnFind = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pnFind.getLayout();
		flowLayout.setAlignment(FlowLayout.TRAILING);
		pnContent.add(pnFind, BorderLayout.NORTH);

		lblFind = new JLabel("T??m ki???m:");
		pnFind.add(lblFind);
		txtPkgName2Find = new JTextField();
		txtPkgName2Find.setText("Nh???p t??n g??i");
		pnFind.add(txtPkgName2Find);
		txtPkgName2Find.setColumns(12);
		pnUtils.setVisible(false);
		tblPkg = new JTable(dtm);

		sorter = new TableRowSorter<TableModel>(dtm);
		tblPkg.setRowSorter(sorter);
		for (int i = 0; i < dtm.getColumnCount(); i++) {
			if(i != 2){
				sorter.setComparator(i, new VieStrComparator<String>());
			}
			else{
				sorter.setComparator(i, new DateComparator<String>());
			}
		}
		tblPkg.getRowSorter().toggleSortOrder(0);
		// Prevent manager edit this table
		tblPkg.setDefaultEditor(Object.class, null);
		//		// Terminate edit when focus is lost
		//		tblPkg.putClientProperty("terminateEditOnFocusLost", true);

		tblPkg.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		tblPkg.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
		tblPkg.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(JLabel.CENTER);
		tblPkg.getColumnModel().getColumn(2).setCellRenderer(center);
		
		JScrollPane scrollPane = new JScrollPane(
				tblPkg,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		pnPkg.setLayout(new BorderLayout());
		pnPkg.add(scrollPane, BorderLayout.CENTER);
		JPanel pnBtn = new JPanel();
		pnMain.add(pnBtn, BorderLayout.SOUTH);
		pnBtn.setLayout(new FlowLayout(FlowLayout.CENTER));


		btnShowAddPkg = new JButton("Th??m g??i");
		btnShowAddPkg.setActionCommand("OK");
		pnBtn.add(btnShowAddPkg);
		getRootPane().setDefaultButton(btnShowAddPkg);

		btnShowChangePkg = new JButton("S???a g??i");
		pnBtn.add(btnShowChangePkg);
		btnShowChangePkg.setEnabled(false);
		btnDelPkg = new JButton("Xo?? g??i");
		btnDelPkg.setActionCommand("Cancel");
		pnBtn.add(btnDelPkg);
		btnDelPkg.setEnabled(false);


		if(usrManager.equals("")){
			dtmCart = new DefaultTableModel();
			dtmCart.addColumn("T??n g??i");
			dtmCart.addColumn("S??? l?????ng");
			dtmCart.addColumn("Gi?? ti???n 1 g??i (VN??)");
			btnAddToCart = new JButton("Ch???n mua");
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
			
			tblCart.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
			tblCart.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
			
			pnCartFunc = new JPanel();
			pnCart.add(pnCartFunc, BorderLayout.SOUTH);
			pnCartFunc.setLayout(new BoxLayout(pnCartFunc, BoxLayout.X_AXIS));

			pnCartUtils = new JPanel();
			pnCartFunc.add(pnCartUtils);

			lblQuantity = new JLabel("S??? l?????ng");
			pnCartUtils.add(lblQuantity);

			cbQuantity = new JComboBox();
			pnCartUtils.add(cbQuantity);

			btnRemoveFromCart = new JButton("Xo??");
			pnCartUtils.add(btnRemoveFromCart);

			pnPay = new JPanel();
			pnCartFunc.add(pnPay);
			pnPay.setLayout(new BoxLayout(pnPay, BoxLayout.Y_AXIS));

			pnCost = new JPanel();
			pnPay.add(pnCost);

			lblAmount = new JLabel("Th??nh ti???n:");
			pnCost.add(lblAmount);

			lblCost = new JLabel("0 (VN??)");
			pnCost.add(lblCost);
			lblCost.setFont(new Font("Tahoma", Font.BOLD, 17));

			panel = new JPanel();
			pnPay.add(panel);

			btnPay = new JButton("?????t mua");
			panel.add(btnPay);

			pnCartTitle = new JPanel();
			FlowLayout flowLayout_1 = (FlowLayout) pnCartTitle.getLayout();
			flowLayout_1.setVgap(9);
			pnCart.add(pnCartTitle, BorderLayout.NORTH);

			lblCart = new JLabel("C??c g??i nhu y???u ph???m mu???n mua");
			lblCart.setFont(new Font("Tahoma", Font.PLAIN, 14));
			lblCart.setHorizontalAlignment(SwingConstants.CENTER);
			pnCartTitle.add(lblCart);
			pnCart.setVisible(false);
		}



		getDataFromDb();
		if(tblPkg.getRowCount() == 0){
			JOptionPane.showMessageDialog(null, "Ch??a c?? g??i nhu y???u ph???m n??o ??ang ???????c b??n");
		}
	}
	private void saveOldLine(){
		txtPkgName.setText((String) tblPkg.getValueAt(tblPkg.getSelectedRow(), 0));
		txtLimit.setText((String) tblPkg.getValueAt(tblPkg.getSelectedRow(), 1));
		txtDate.setText((String) tblPkg.getValueAt(tblPkg.getSelectedRow(), 2));
		txtPrice.setText((String) tblPkg.getValueAt(tblPkg.getSelectedRow(), 3));
	}
//	private String validateNum(StringBuilder s){
//		for(int i = 0; i < s.length(); i++){
//			if(!Character.isDigit(s.charAt(i))){
//				s.deleteCharAt(i);
//				i--;
//			}
//		}
//		for(int i = s.length() - 3; i > 0; i-=3){
//			s.insert(i, ',');
//		}
//		return s.toString();
//	}
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
					rowData.add(Utils.validateNum(new StringBuilder(rs.getString(2))));
					String d = rs.getString(3).replace("-", "/");
					rowData.add(Utils.changeDateFormatter(d, "dd/MM/yyyy", sdf));
					rowData.add(Utils.validateNum(new StringBuilder(rs.getString(4))));
					dtm.addRow(rowData);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("L???i khi l???y d??? li???u t???t c??? c??c g??i t??? b???ng pkg_name");
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
	private void addPkg(){
		String date = Utils.changeDateFormatter(txtDate.getValue().toString(), "yyyy/MM/dd", df);
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
				JOptionPane.showMessageDialog(null, "Th??m g??i nhu y???u ph???m th??nh c??ng");
				btnShowAddPkg.setText("Th??m g??i");
				btnShowAddPkg.setEnabled(true);
				pnUtils.setVisible(false);
				txtDate.setText("");
				txtLimit.setText("");
				txtPkgName.setText("");
				txtPrice.setText("");
				btnShowChangePkg.setText("S???a g??i");
			}
			else{
				JOptionPane.showMessageDialog(null, "L???i khi th???c hi???n th??m g??i nhu y???u ph???m");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "L???i kh??ng x??c ?????nh khi th??m g??i");
			logger.error("L???i kh??ng x??c ?????nh khi th??m g??i");
		} finally{
			if(st != null){
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("Failed!",e);
				}
			}
		}

	}
	private void changePkg(){
		String date = Utils.changeDateFormatter(txtDate.getText(), "yyyy/MM/dd", df);
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
				row = tblPkg.convertRowIndexToModel(row);
				dtm.setValueAt(txtPkgName.getText(), row, 0);
				dtm.setValueAt(txtLimit.getText(), row, 1);
				dtm.setValueAt(txtDate.getText(), row, 2);
				dtm.setValueAt(txtPrice.getText(), row, 3);
				btnAddPkg.setEnabled(false);
				JOptionPane.showMessageDialog(null, "C???p nh???t g??i th??nh c??ng");
				btnDelPkg.setEnabled(true);
				btnShowAddPkg.setEnabled(true);
				pnUtils.setVisible(false);
				txtDate.setText("");
				txtLimit.setText("");
				txtPkgName.setText("");
				txtPrice.setText("");
				btnShowChangePkg.setText("S???a g??i");
			}
			else{
				JOptionPane.showMessageDialog(null, "Th???t b???i. Xin vui l??ng th??? l???i sau");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "???? x???y ra l???i, kh??ng th??? c???p nh???t g??i l??c n??y");
			logger.error("L???i khi c???p nh???t g??i");
		} finally{
			if(st != null){
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("Failed!",e);
				}
			}
		}
	}
	private void deletePkg(String pkgNList, int counter, int[] selectedRows){
		CallableStatement st = null;
		try {
			st = dbi.getStatement("{call delPkg(?, ?, ?)}");
			st.registerOutParameter(3, Types.INTEGER);
			st.setString(1, pkgNList);
			st.setString(2, usrManager);
			st.execute();
			int code  = st.getInt("code");
			
			dtm.setRowCount(0);
			getDataFromDb();
			
			if(code == counter){
				
				btnDelPkg.setEnabled(false);
				JOptionPane.showMessageDialog(null, "Xo?? th??nh c??ng " + code + " g??i");
				return;
			}
			else{
				JOptionPane.showMessageDialog(null, "Kh??ng th??? xo?? " + (counter - code) + " g??i trong s??? ????");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "???? x???y ra l???i, kh??ng th??? xo?? g??i n??o");
			logger.error("L???i khi xo?? g??i");
		} finally{
			if(st != null){
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("Failed!",e);
				}
			}
		}
		dtm.setRowCount(0);
		getDataFromDb();
	}
//	private String changeDateFormatter(String date, String format, SimpleDateFormat sdf){
//		SimpleDateFormat f = new SimpleDateFormat(format);
//		String res = null;
//		try {
//			res = f.format(sdf.parse(date));
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return res;
//	}
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
			logger.error("L???i khi ki???m tra n???u t??n g??i ???? t???n t???i");
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

	private void search(String s) {
		if (s.length() != 0) {
			sorter.setRowFilter(RowFilter.regexFilter(s, 0));

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
		if(btnAddPkg.getText().equals("Thay ?????i")){
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
		int row = tblPkg.getSelectedRow();
		if(row != -1){
			return tblPkg.getValueAt(row, col) + "";
		}
		return "";
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
			aRow.add(Utils.validateNum(new StringBuilder(tblCart.getValueAt(i, 1) + "")));
			int price = Integer.parseInt(tblCart.getValueAt(i, 2).toString().replace(",", ""));
			int quan = Integer.parseInt(tblCart.getValueAt(i, 1).toString().replace(",", ""));
			price *= quan;
			aRow.add(Utils.validateNum(new StringBuilder(price + "")));
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
				JOptionPane.showMessageDialog(null, "?????t mua th??nh c??ng");
			}
			else{
				JOptionPane.showMessageDialog(null, "Th???t b???i. Xin vui l??ng th??? l???i sau");
			}		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("L???i khi ?????t mua g??i");
		} finally{
			if(st != null){
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error("Failed!",e);
				}
			}
		}

		dispose();
	}
	private boolean canBuy(String pkgN){
		Date d = new Date(swapDayWithMon(getValInTableAt(2)));
		if(d.before(new Date())){
			JOptionPane.showMessageDialog(null, "G??i n??y ???? ng???ng b??n, vui l??ng ch???n g??i kh??c");
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
				JOptionPane.showMessageDialog(null, "Kh??ng th??? mua v?? ???? ?????t t???i m???c gi???i h???n mua c???a g??i n??y");
				return false;
			}

			quantityOfBoughtPkgL.add(Integer.parseInt(maxQuan) - quan);
			return true;
		}
		else{
			JOptionPane.showMessageDialog(null, "Kh??ng th??? l???y ???????c s??? l?????ng g??i ???? mua");
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
			logger.error("L???i kkhi l???y s??? l?????ng g??i ???? ???????c mua");
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
		return -1;
	}
	private void updateCost(int credit){
		String strCost = lblCost.getText().substring(0, lblCost.getText().length() - 6);
		int cur_cost = Integer.parseInt(strCost.replace(",", ""));
		cur_cost += credit;
		lblCost.setText(Utils.validateNum(new StringBuilder(cur_cost + "")) + " (VN??)");
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
