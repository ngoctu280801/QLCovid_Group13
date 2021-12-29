package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

import model.ChartStat;
import model.DbInteraction;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StatisticView extends JDialog {
	private DbInteraction dbi;
	private static ChartStat chartStat;
	private static final JPanel pnChart = new JPanel();
	private static JButton btnNumOfPersonByTime, btnNumOfPkgConsumed, btnDebt,btnNumOfCuredPatients, btnNumOfChangedStatePatients;

	public StatisticView(DbInteraction dbi) {
		this.dbi = dbi;
		addControls();
		addEvents();
		chartStat = new ChartStat(dbi);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	private void addControls(){
		setTitle("Thống kê");
		setBounds(100, 100, 1100, 650);
		getContentPane().setLayout(new BorderLayout());
		pnChart.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(pnChart, BorderLayout.CENTER);
		pnChart.setLayout(new BorderLayout(0, 0));

		JPanel pnBtn = new JPanel();
		pnBtn.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(pnBtn, BorderLayout.SOUTH);

		btnNumOfPersonByTime = new JButton("Số lượng người ở từng trạng thái");
		btnNumOfPersonByTime.setActionCommand("OK");
		pnBtn.add(btnNumOfPersonByTime);
		getRootPane().setDefaultButton(btnNumOfPersonByTime);
		
		btnNumOfCuredPatients = new JButton("Số lượng người đã hết bệnh");
		btnNumOfCuredPatients.setActionCommand("OK");
		pnBtn.add(btnNumOfCuredPatients);
		getRootPane().setDefaultButton(btnNumOfCuredPatients);
		
		btnNumOfChangedStatePatients = new JButton("Số lượng người chuyển trạng thái");
		btnNumOfChangedStatePatients.setActionCommand("OK");
		pnBtn.add(btnNumOfChangedStatePatients);
		getRootPane().setDefaultButton(btnNumOfChangedStatePatients);

		btnNumOfPkgConsumed = new JButton("Số lượng gói tiêu thụ");
		btnNumOfPkgConsumed.setActionCommand("Cancel");
		pnBtn.add(btnNumOfPkgConsumed);
		
		btnDebt = new JButton("Dư nợ của người dùng");
		pnBtn.add(btnDebt);

		JPanel pnTotal = new JPanel();
		getContentPane().add(pnTotal, BorderLayout.NORTH);

		JLabel lblTotal = new JLabel("Tổng cộng:");
		lblTotal.setFont(new Font("Tahoma", Font.BOLD, 14));
		pnTotal.add(lblTotal);

		JLabel lblTotalNum = new JLabel("0");
		lblTotalNum.setFont(new Font("Tahoma", Font.BOLD, 14));
		pnTotal.add(lblTotalNum);
	}
	private void addEvents(){

		btnNumOfPersonByTime.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(btnNumOfPersonByTime.isEnabled()){
					Runnable getChart = new Runnable(){
						public void run(){
							getNumOfPerInEachState();
						}
					};
					Thread t = new Thread(getChart);
					t.start();
				}
			}
		});
		btnNumOfCuredPatients.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(btnNumOfCuredPatients.isEnabled()){
					Runnable getChart = new Runnable(){
						public void run(){
							getNumOfCuredPatients();
						}
					};
					Thread t = new Thread(getChart);
					t.start();
				}
			}
		});

		btnNumOfPkgConsumed.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(btnNumOfPkgConsumed.isEnabled()){
					Runnable getChart = new Runnable(){
						public void run(){
							getNumOfPkgConsumed();
						}
					};
					Thread t = new Thread(getChart);
					t.start();
				}
			}
		});
		btnDebt.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnDebt.isEnabled()){
					Runnable getChart = new Runnable(){
						public void run(){
							getDebtStat();
						}
					};
					Thread t = new Thread(getChart);
					t.start();
				}
			}
		});
	}
	private void getNumOfPerInEachState(){
		disableBtn();
		JFreeChart chart = chartStat.createBarChart(
				"Số lượng người ở từng trạng thái trong 14 ngày vừa qua", 
				"Ngày", "Số lượng (người)", chartStat.numOfPerInEachState(14));
		rePaint(chart);
		
		btnNumOfChangedStatePatients.setEnabled(true);
		btnNumOfCuredPatients.setEnabled(true);
		btnNumOfPkgConsumed.setEnabled(true);
		btnDebt.setEnabled(true);
	}
	private void getNumOfCuredPatients(){
		disableBtn();
		JFreeChart chart = chartStat.createBarChart(
				"Số lượng người đã hết bệnh trong 14 ngày vừa qua", 
				"Ngày", "Số lượng (người)", chartStat.numberOfCuredPatients(14));
		rePaint(chart);
		
		btnNumOfChangedStatePatients.setEnabled(true);
		btnNumOfPersonByTime.setEnabled(true);
		btnNumOfPkgConsumed.setEnabled(true);
		btnDebt.setEnabled(true);
	}

	private void getNumOfPkgConsumed(){
		disableBtn();
		
		JFreeChart chart = chartStat.createPieChart("Số lượng các gói nhu yếu phẩm đã tiêu thụ",
				chartStat.numOfPkgConsumed());
		rePaint(chart);
		
		btnNumOfChangedStatePatients.setEnabled(true);
		btnNumOfCuredPatients.setEnabled(true);
		btnNumOfPersonByTime.setEnabled(true);
		btnDebt.setEnabled(true);
	}
	private void getDebtStat(){
		disableBtn();
		
		JFreeChart chart = chartStat.createPieChart("Dư nợ của từng người dùng theo CMND/ CCCD",
				chartStat.numOfDebt());
		rePaint(chart);
		
		btnNumOfChangedStatePatients.setEnabled(true);
		btnNumOfCuredPatients.setEnabled(true);
		btnNumOfPersonByTime.setEnabled(true);
		btnNumOfPkgConsumed.setEnabled(true);
	}
	private void rePaint(JFreeChart chart){
		ChartPanel chartPanel = new ChartPanel(chart);
		pnChart.removeAll();
		pnChart.add(chartPanel);
		pnChart.revalidate();
		pnChart.repaint();
	}
	private void disableBtn(){
		btnDebt.setEnabled(false);
		btnNumOfPersonByTime.setEnabled(false);
		btnNumOfPkgConsumed.setEnabled(false);
		btnNumOfCuredPatients.setEnabled(false);
		btnNumOfChangedStatePatients.setEnabled(false);
	}
}
