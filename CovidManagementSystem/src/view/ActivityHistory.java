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

public class ActivityHistory extends JDialog {

	private final JPanel pnContent = new JPanel();
	private JTable tblActHis;
	private DefaultTableModel dtm;
	private DbInteraction dbi;
	private TableRowSorter<TableModel> sorter;


	public ActivityHistory(DbInteraction dbi, String usrManager) {
		this.dbi = dbi;
		setTitle("Lịch sử hoạt động của tài khoản người quản lý : " + usrManager);
		setBounds(100, 100, 700, 400);
		getContentPane().setLayout(new BorderLayout());
		pnContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(pnContent, BorderLayout.CENTER);
		addControls();
		getDataFromDb(usrManager);
		setLocationRelativeTo(null);
	}
	private void addControls(){
		dtm = new DefaultTableModel();
		dtm.addColumn("Ngày");
		dtm.addColumn("Hoạt động");
		tblActHis = new JTable(dtm);
		// Prevent manager edit this table
		tblActHis.setDefaultEditor(Object.class, null);
		
		sorter = new TableRowSorter<TableModel>(dtm);
		tblActHis.setRowSorter(sorter);
		for (int i = 0; i < dtm.getColumnCount(); i++) {
			sorter.setComparator(i, new MyComparator<String>());
		}
		
		JScrollPane scrollPane = new JScrollPane(
				tblActHis,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnContent.setLayout(new BorderLayout());
		pnContent.add(scrollPane, BorderLayout.CENTER);
	}
	private void getDataFromDb(String usrManager){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select date, description from activity_history"
				+ " where usr_manager = '" + usrManager + "';", stmt);
		try {
			if(rs.isBeforeFirst()){
				while(rs.next()){
					Vector<String> rowData = new Vector<String>();
					rowData.add(rs.getString(1));
					rowData.add(rs.getString(2));
					dtm.addRow(rowData);
				}
			}
//			else{
//				JOptionPane.showMessageDialog(null, "Chưa có lịch sử hoạt động nào");
//			}
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
}
