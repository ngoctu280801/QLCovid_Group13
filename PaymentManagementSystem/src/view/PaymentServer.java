package view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import model.Server;

import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class PaymentServer extends JFrame {

	private JPanel contentPane;
	private JButton btnOnOff;
	private boolean isOn;
	private final String pathToSSL;
	private final String pwd;
	private final int port;
	private final Server server;

	public PaymentServer(String path, String pwd, int port) {
		isOn = false;
		File ssl = new File(path);
		if(!ssl.exists()){
			JOptionPane.showMessageDialog(null, "Không tồn tại " + path);
			System.exit(-1);
		}
		server = new Server();
		this.port = port;
		pathToSSL = path;
		this.pwd = pwd;
		setSSLProp();
		addControls();
		addEvents();
		setLocationRelativeTo(null);
	}
	private void addControls(){
		setTitle("Hệ thống thanh toán");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 361, 100);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));		
		btnOnOff = new JButton("Bật Server");		
		contentPane.add(btnOnOff);
	}
	private void addEvents(){
		btnOnOff.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(isOn){
					closeServer();
					isOn = false;
					btnOnOff.setText("Bật Server");
				}
				else{
					isOn = true;
					btnOnOff.setText("Tắt Server");
					
					Runnable startSV = new Runnable(){
						public void run(){
							try {
								server.stop = false;
								server.start(port);

							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								isOn = false;
								btnOnOff.setText("Bật Server");
								System.out.println("Đã tắt server thành công");
							}
						}
					};
					Thread t = new Thread(startSV);
					t.start();
						
					
				}
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				closeServer();
				
			}
		});
	}
	private void closeServer(){
		if(server.serverSocket != null){
			try {
				server.stop = true;
				server.serverSocket.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				server.serverSocket = null;
				System.out.println("Đã tắt server thành công");
			}
		}
	}
	private void setSSLProp(){
		System.setProperty("javax.net.ssl.keyStore", pathToSSL);
		System.setProperty("javax.net.ssl.keyStorePassword", pwd);
		//System.setProperty("javax.net.debug", "all");
	}
}
