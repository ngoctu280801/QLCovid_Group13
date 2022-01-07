package view;

import java.awt.BorderLayout;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import model.DateComparator;
import model.DbInteraction;
import model.Utils;
import model.VieStrComparator;

public class ManagementHistory extends JDialog {

	private final JPanel pnContent = new JPanel();
	private JTable tblHistory;
	private DefaultTableModel dtm;
	private DbInteraction dbi;
	private TableRowSorter<TableModel> sorter;

	/**
	 * Create the dialog.
	 */
	public ManagementHistory(DbInteraction dbi, String idCard) {
		this.dbi = dbi;
		setTitle("Lịch sử được quản lý");
		setBounds(100, 100, 800, 410);
		getContentPane().setLayout(new BorderLayout());
		pnContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(pnContent, BorderLayout.CENTER);
		addControls();
		getDataFromDb(idCard);
		setLocationRelativeTo(null);
	}
	private void addControls(){
		dtm = new DefaultTableModel();
		dtm.addColumn("Họ Tên");
		dtm.addColumn("CMND/ CCCD");
		//dtm.addColumn("Ngày sinh");
		dtm.addColumn("Vào ngày");
		dtm.addColumn("Trạng thái");
		dtm.addColumn("Địa điểm điều trị/ cách ly");
		
		tblHistory = new JTable(dtm);
		// Prevent manager edit this table
		tblHistory.setDefaultEditor(Object.class, null);
		tblHistory.setAutoCreateRowSorter(true);
		tblHistory.getRowSorter().toggleSortOrder(2);
		//dtm.addRow(new String[] {"a","a","a","a","a","a"});
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		tblHistory.getColumnModel().getColumn(0).setCellRenderer( centerRenderer );
		tblHistory.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
		tblHistory.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
		tblHistory.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
		tblHistory.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
		//tblHistory.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );
		pnContent.setLayout(new BorderLayout(0, 0));

		sorter = new TableRowSorter<TableModel>(dtm);
		tblHistory.setRowSorter(sorter);
		for (int i = 0; i < dtm.getColumnCount(); i++) {
			if(i != 2){
				sorter.setComparator(i, new VieStrComparator<String>());
			}
			else{
				sorter.setComparator(i, new DateComparator<String>());
			}
		}
		
		JScrollPane scrollPane = new JScrollPane(
				tblHistory,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		pnContent.add(scrollPane);
	}
	private void getDataFromDb(String idCard){
		Statement[] stmt = new Statement[] {null};
		ResultSet rs = dbi.query("select p.full_name, p.id_card, mh.date,"
				+ " mh.state, qrt.name from management_history mh"
				+ " join patients p on p.id = mh.id_patient"
				+ " join quarantinepos qrt on qrt.id = mh.id_qrt_pos"
				+ " where p.id_card = '" + idCard + "';", stmt);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			if(rs.isBeforeFirst()){
				while(rs.next()){
					Vector<String> rowData = new Vector<String>();
					rowData.add(rs.getString(1));
					rowData.add(rs.getString(2));
					String d = rs.getString(3).replace("-", "/");
					rowData.add(Utils.changeDateFormatter(d, "dd/MM/yyyy", sdf));
//					rowData.add(rs.getString(3));
					rowData.add(rs.getString(4));
					rowData.add(rs.getString(5));
					dtm.addRow(rowData);
				}
			}
//			else{
//				JOptionPane.showMessageDialog(null, "Người này chưa có lịch sử được quản lý");
//			}
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
