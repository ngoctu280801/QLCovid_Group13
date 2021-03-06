package model;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import javax.swing.JOptionPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public class ChartStat {
	private DbInteraction dbi;
	
	public ChartStat(DbInteraction dbi){
		this.dbi = dbi;
	}
	public JFreeChart createBarChart(String chartName, String xName,
			String yName, CategoryDataset cd){
		JFreeChart barChart = ChartFactory.createBarChart( chartName, xName, yName, 
				cd, PlotOrientation.VERTICAL, true, true, false);
		return barChart;
	}
	public JFreeChart createPieChart(String chartName, PieDataset dataset){
		JFreeChart pieChart = ChartFactory.createPieChart(
                chartName, dataset, true, true, true);
        return pieChart;
	}	
	
	public CategoryDataset numOfPerInEachState(int nDays){
		String f0Str = "";
		String f1Str = "";
		String f2Str = "";
		String f3Str = "";
		String days = "";
		
		try {
			CallableStatement st = dbi.getStatement("{call countPatientLastNDay(?, ?, ?, ?, ?, ?)}");
			st.setInt(1, nDays);
			st.registerOutParameter(2, Types.LONGVARCHAR);
			st.registerOutParameter(3, Types.LONGVARCHAR);
			st.registerOutParameter(4, Types.LONGVARCHAR);
			st.registerOutParameter(5, Types.LONGVARCHAR);
			st.registerOutParameter(6, Types.LONGVARCHAR);
			st.execute();
			
			f0Str = st.getString("f0State");
			f1Str = st.getString("f1State");
			f2Str = st.getString("f2State");
			f3Str = st.getString("f3State");
			days = st.getString("days");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return createNumOfPerInEachStateDataset(f0Str, f1Str, f2Str, f3Str, days);
		
	}
	private CategoryDataset createNumOfPerInEachStateDataset(String f0Str, String f1Str, 
			String f2Str, String f3Str, String days){
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String[] f0State = f0Str.split(";");
		String[] f1State = f1Str.split(";");
		String[] f2State = f2Str.split(";");
		String[] f3State = f3Str.split(";");
		String[] date = days.split(";");
		
		for (int i = 0; i < f0State.length; i++) {
			dataset.addValue(Integer.parseInt(f0State[i]), "F0", date[i]);
			dataset.addValue(Integer.parseInt(f1State[i]), "F1", date[i]);
			dataset.addValue(Integer.parseInt(f2State[i]), "F2", date[i]);
			dataset.addValue(Integer.parseInt(f3State[i]), "F3", date[i]);
		}
		
		return dataset;
	}
	
	public CategoryDataset numberOfCuredPatients(int nDays) {
		String curedPatientsStr = "";
		String days = "";
		try {
			CallableStatement st = dbi.getStatement("{call countCuredPatientsLastNDay(?, ?, ?)}");
			st.setInt(1, nDays);
			st.registerOutParameter(2, Types.LONGVARCHAR);
			st.registerOutParameter(3, Types.LONGVARCHAR);
			st.execute();
			
			curedPatientsStr = st.getString("curedPatients");
			days = st.getString("days");
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return createNumOfCuredPatientsDataset(curedPatientsStr, days);
	}
	private CategoryDataset createNumOfCuredPatientsDataset(String curedPatientsStr,String days){
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String[] curedPatients = curedPatientsStr.split(";");
		String[] date = days.split(";");
		
		for (int i = 0; i < curedPatients.length; i++) {
			dataset.addValue(Integer.parseInt(curedPatients[i]), "Kh???i b???nh", date[i]);
		}
		
		return dataset;
	}
	
	public CategoryDataset numberOfChangedStatePatients(int nDays) {
		String changedStatePatientsStr = "";
		String days = "";
		try {
			CallableStatement st = dbi.getStatement("{call countChangedStatePatientsLastNDay(?, ?, ?)}");
			st.setInt(1, nDays);
			st.registerOutParameter(2, Types.LONGVARCHAR);
			st.registerOutParameter(3, Types.LONGVARCHAR);
			st.execute();
			
			changedStatePatientsStr = st.getString("changedStatePatients");
			days = st.getString("days");
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return createNumOfChangedStatePatientsDataset(changedStatePatientsStr, days);
	}
	private CategoryDataset createNumOfChangedStatePatientsDataset(String changedStatePatientsStr,String days){
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String[] changedStatePatients = changedStatePatientsStr.split(";");
		String[] date = days.split(";");
		
		for (int i = 0; i < changedStatePatients.length; i++) {
			dataset.addValue(Integer.parseInt(changedStatePatients[i]), "Chuy???n tr???ng th??i", date[i]);
		}
		
		return dataset;
	}

	public PieDataset numOfPkgConsumed(){
		String pkgNList = "";
		String quanList = "";
		try {
			CallableStatement st = dbi.getStatement("{call countPkgConsumed(?, ?)}");
			st.registerOutParameter(1, Types.LONGNVARCHAR);
			st.registerOutParameter(2, Types.LONGVARCHAR);
			st.execute();
			
			pkgNList = st.getString("pkgNList");
			quanList = st.getString("quanList");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return createNumOfPkgConsumedDataset(pkgNList, quanList);
	}
	public PieDataset createNumOfPkgConsumedDataset(String pkgNList, String quanList){
		DefaultPieDataset dataset = new DefaultPieDataset();
		String[] pkgN = pkgNList.split(";");
		String[] quan = quanList.split(";");
		
		for (int i = 0; i < pkgN.length; i++) {
			int num = 0;
			if(!quan[i].equals("")) {
				num = Integer.parseInt(quan[i]);
			}
			else {
				JOptionPane.showMessageDialog(null, "Ch??a c?? l?????t ti??u th??? n??o");
			}
			if(num > 0){
				dataset.setValue(pkgN[i], num);
			}
		}
		
		return dataset;
	}

	public PieDataset numOfDebt(){
		String idCardList = "";
		String debtList = "";
		try {
			CallableStatement st = dbi.getStatement("{call countDebt(?, ?)}");
			st.registerOutParameter(1, Types.LONGVARCHAR);
			st.registerOutParameter(2, Types.LONGVARCHAR);
			st.execute();
			
			idCardList = st.getString("idCardList");
			debtList = st.getString("debtList");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return createNumOfDebtDataset(idCardList, debtList);
	}
	public PieDataset createNumOfDebtDataset(String idCardList, String debtList){
		DefaultPieDataset dataset = new DefaultPieDataset();
		String[] idCard = idCardList.split(";");
		String[] debt = debtList.split(";");
		
		for (int i = 0; i < idCard.length; i++) {
			int num = 0;
			if(!debt[i].equals("")) {
				num = Integer.parseInt(debt[i]);
			}
			else {
				JOptionPane.showMessageDialog(null, "Ch??a c?? d?? n??? n??o ???????c ghi l???i");
			}
			if(num > 0){
				dataset.setValue("CMND/ CCCD: " + idCard[i], num);
			}
		}
		
		return dataset;
	}

}
