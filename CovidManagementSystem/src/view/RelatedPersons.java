package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import model.DbInteraction;
import model.MyComparator;

public class RelatedPersons extends JDialog {

	private final JPanel pnTblRPer = new JPanel();
	private String idCard;
	private DefaultTableModel dtm;
	private JTable tblRPer;
	private DbInteraction dbi;
	private TableRowSorter<TableModel> sorter;

	
	public RelatedPersons(DbInteraction dbi, String idCard) {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.idCard = idCard;
		this.dbi = dbi;
		setTitle("Thông tin người liên quan tới CMND/ CCCD: " + idCard);
		setBounds(100, 100, 960, 470);
		addControls();
		addEvents();

		setLocationRelativeTo(null);
//		if(tblRPer.getRowCount() == 0){
//			haveRelatedPersons = false;
//			JOptionPane.showMessageDialog(null, "Chưa tồn tại người nào trong hệ thống");
//		}
	}

	private void addEvents() {
		// TODO Auto-generated method stub
		
	}

	private void addControls() {
		getContentPane().setLayout(new BorderLayout());
		pnTblRPer.setLayout(new FlowLayout());
		pnTblRPer.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(pnTblRPer, BorderLayout.CENTER);
//		{
//			JPanel pnBtn = new JPanel();
//			pnBtn.setLayout(new FlowLayout(FlowLayout.RIGHT));
//			getContentPane().add(pnBtn, BorderLayout.SOUTH);
//			{
//				JButton okButton = new JButton("OK");
//				okButton.setActionCommand("OK");
//				pnBtn.add(okButton);
//				getRootPane().setDefaultButton(okButton);
//			}
//			{
//				JButton cancelButton = new JButton("Cancel");
//				cancelButton.setActionCommand("Cancel");
//				pnBtn.add(cancelButton);
//			}
//		}
		dtm = new DefaultTableModel();
		dtm.addColumn("Họ Tên");
		dtm.addColumn("CMND/ CCCD");
		dtm.addColumn("Ngày sinh");
		dtm.addColumn("Phường/ Xã");
		dtm.addColumn("Quận/ Huyện");
		dtm.addColumn("Thành phố/ Tỉnh");
		dtm.addColumn("Hiện là");
		dtm.addColumn("Nơi điều trị/ cách ly");
		tblRPer = new JTable(dtm);
		tblRPer.setAutoCreateRowSorter(true);
		tblRPer.getRowSorter().toggleSortOrder(0);
		// Prevent manager edit this table
		tblRPer.setDefaultEditor(Object.class, null);
		
		sorter = new TableRowSorter<TableModel>(dtm);
		tblRPer.setRowSorter(sorter);
		for (int i = 0; i < dtm.getColumnCount(); i++) {
			sorter.setComparator(i, new MyComparator<String>());
		}
		
		JScrollPane scrollPane = new JScrollPane(
				tblRPer,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		pnTblRPer.setLayout(new BorderLayout());
		pnTblRPer.add(scrollPane, BorderLayout.CENTER);
		
		getContentPane().add(pnTblRPer);
		getDataFromDb("call getNextRelatedPersonByIdCard('"	+ idCard + "');");
		getDataFromDb("call getRelatedPersonBeforeByIdCard('"	+ idCard + "');");
	}
	private void getDataFromDb(String sql){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query(sql, stmt);
		try {
			if(rs.isBeforeFirst()){
				while(rs.next()){
					Vector<String> rowData = new Vector<String>();
					rowData.add(rs.getString(1));
					rowData.add(rs.getString(2));
					rowData.add(rs.getString(3));
					rowData.add(rs.getString(6));
					rowData.add(rs.getString(5));
					rowData.add(rs.getString(4));
					rowData.add(rs.getString(7));
					rowData.add(rs.getString(8));
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
}
