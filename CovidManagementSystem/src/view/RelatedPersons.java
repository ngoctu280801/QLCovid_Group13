package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
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

import org.apache.log4j.Logger;

import model.DateComparator;
import model.DbInteraction;
import model.Utils;
import model.VieStrComparator;

public class RelatedPersons extends JDialog {

	private final JPanel pnTblRPer = new JPanel();
	private String idCard;
	private DefaultTableModel dtm;
	private JTable tblRPer;
	private DbInteraction dbi;
	private TableRowSorter<TableModel> sorter;
	private static final Logger logger = Logger.getLogger(RelatedPersons.class);
	
	public RelatedPersons(DbInteraction dbi, String idCard) {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.idCard = idCard;
		this.dbi = dbi;
		setTitle("Thông tin người liên quan tới CMND/ CCCD: " + idCard);
		setBounds(100, 100, 960, 470);
		addControls();
		addEvents();

		setLocationRelativeTo(null);
	}

	private void addEvents() {
		// TODO Auto-generated method stub
		
	}

	private void addControls() {
		getContentPane().setLayout(new BorderLayout());
		pnTblRPer.setLayout(new FlowLayout());
		pnTblRPer.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(pnTblRPer, BorderLayout.CENTER);
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
			if(i != 2){
				sorter.setComparator(i, new VieStrComparator<String>());
			}
			else{
				sorter.setComparator(i, new DateComparator<String>());
			}
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
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			if(rs.isBeforeFirst()){
				while(rs.next()){
					Vector<String> rowData = new Vector<String>();
					rowData.add(rs.getString(1));
					rowData.add(rs.getString(2));
					String d = rs.getString(3).replace("-", "/");
					rowData.add(Utils.changeDateFormatter(d, "dd/MM/yyyy", sdf));
//					rowData.add(rs.getString(3));
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
			logger.error("Lỗi khi lấy dữ liệu tất cả người liên quan");
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
