package view;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import org.apache.log4j.Logger;

import model.Server;

import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
public class PaymentServer extends JFrame {

	private JPanel contentPane;
	private JButton btnOnOff;
	private boolean isOn;
	private final String pathToSSL;
	private final String pwd;
	private final int port;
	private final Server server;
	private final Logger logger;

	public PaymentServer(String path, String pwd, int port, Logger logger) {
		
		this.logger = logger;
		
		// Kiểm tra đường dẫn chứa file SSL cert
		File ssl = new File(path);
		
		// Nếu file ssl cert tồn tại thì mới tiếp tục chạy
		if(!ssl.exists()){
			JOptionPane.showMessageDialog(null, "Không tồn tại " + path);
			System.exit(-1);
		}
		isOn = false;
		
		
		// Khỏi tạo server model
		
		server = new Server(logger);
		this.port = port;
		pathToSSL = path;
		this.pwd = pwd;
		
		// Set các thuộc tính của SSL cert vào system để có thể chạy
		setSSLProp();
		
		addControls();
		addEvents();
		setLocationRelativeTo(null);
	}
	private void addControls(){
		
		setTitle("Hệ thống thanh toán");
		
		
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		setContentPane(contentPane);
		
		
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));	
		
		btnOnOff = new JButton("Bật Server");		
		contentPane.add(btnOnOff);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 361, 100);
	}
	private void addEvents(){
		btnOnOff.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// Nếu server đang bật thì chuyển thành tắt
				if(isOn){
					closeServer();
					isOn = false;
					btnOnOff.setText("Bật Server");
				}
				// Ngược lại
				else{
					isOn = true;
					btnOnOff.setText("Tắt Server");
					
					// Bắt đầu chạy server model trên một luồng mới
					// để nhận các kết nối
					Runnable startSV = new Runnable(){
						public void run(){
							try {
								server.stop = false;
								server.start(port);

							} catch (IOException e) {
								// TODO Auto-generated catch block
								
								logger.debug("Tắt server thành công");
								
//								e.printStackTrace();
								
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
		
		// Đóng server trước khi thoát giao diện server
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
		// Set đường dẫn tới SSL cert
		System.setProperty("javax.net.ssl.keyStore", pathToSSL);
		
		// Set password của SSL cert
		System.setProperty("javax.net.ssl.keyStorePassword", pwd);
		
		// Chế độ debug khi có kết nối tới
		//System.setProperty("javax.net.debug", "all");
	}
}
