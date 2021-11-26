import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;




public class User {
	private String userName;
	private boolean isServer=false;
	static Server server;
	
	private groupUI gui;
	static UI ui;
	Boolean runFlag = true;
	List <String> Server_tcpNameL;
	List <String> Server_tcpIpL;
	List <Integer> Server_tcpPortL;
	Socket socket = null;
	static ObjectOutputStream objOutputStream = null;
	private String studioName;
	boolean isListen = true;
	DatagramSocket dsocket = null;
	static User instance;
	Color chosenBack = new Color(0, 0, 0);
	
	//some attributes for server
	
	public static User getInstance() {
		if(instance==null)
			instance = new User();
		return instance;
	}
	
	public User() {
		userName = JOptionPane.showInputDialog(null,"input your name:");
		Server_tcpNameL = new ArrayList<String>();
		Server_tcpIpL = new ArrayList<String>();
		Server_tcpPortL = new ArrayList<Integer>();
		try {
			dsocket = new DatagramSocket(4002);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		getServerInfo();
		showGUI();
		
		
		
			
		if(isServer) {
			//set a new blank board
			
			
			
			//run server with the new studio name
			
			
			
			User.server = Server.getInstance(studioName);	
			
			User.ui = UI.getInstance(this, userName, studioName);

			int[][]data = new int[50][50];
			for(int i = 0; i < 50; i++){
				for(int j = 0; j < 50; j++){
					data[i][j] = chosenBack.getRGB();
					server.updatePaintPixel(i, j, chosenBack.getRGB());
				}
			}
			ui.setData(data, 20);
			User.server.setData(data);
			ui.setBackgroundColor(chosenBack);
			UI.addServer(server);

			System.out.println("The server is: " + server);
			ui.setVisible(true);
			System.out.print("name:" + studioName); 
			
			
		}else {
			
			System.out.println("this is a client");
			start();
			this.ui = UI.getInstance(this, userName, studioName);
			
			ui.setVisible(true);
			//ui.updateUserL(userName);

			Message userJoin = new Message(MessageType.MESSAGE);
			userJoin.setContent("Welcome " + userName + " joins " + studioName);
			send(userJoin);
		}
	}
	
	
	
	public boolean getIsServer() {
		return isServer;
	}
	
	public void getServerInfo(){
		//1.��������
		 //�������ݱ��׽��ֲ�����󶨵�����������4002�˿ڡ�
		 
		 //2.���
		 byte[] arr = "request".getBytes();
		 //�ĸ�����: �������� ���ĳ��� �������� �˿ں�   
		 DatagramPacket packet = null;
		try {
			packet = new DatagramPacket
			  (arr, arr.length,InetAddress.getByName("255.255.255.255") , 4000);//4002
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  
		 //3.����
		 try {
			dsocket.send(packet);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 
		 //4.receive serverInfo from multiple servers��listen for 5 second)
		 long timer = System.currentTimeMillis();
		 
		 Thread thread = new Thread(() ->{
			 while(true) {
				 byte[] buf =new byte[1024];
				 DatagramPacket packetr = new DatagramPacket(buf, buf.length);
				 try {
					 dsocket.receive(packetr);
					 buf = packetr.getData(); 
					 
					 System.out.println(new String(buf));
					 String message = new String(buf,0,packetr.getLength());
					 if(!message.equals("request")) {
						 String[] info = new String(buf).split(",");
						 String Server_tcpName = info[0];
						 String Server_tcpIp = info[1];
						 Integer Server_tcpPort = Integer.parseInt(info[2]);
						 Server_tcpNameL.add(Server_tcpName);
						 System.out.println("Server_tcpIp");
						 Server_tcpIpL.add(Server_tcpIp);
						 Server_tcpPortL.add(Server_tcpPort);
					 }
					} catch (IOException e) {
						// TODO Auto-generated catch block
						continue;
					}
			 		
			 }	
			 
		 });
		 thread.start();
		 
		 while(System.currentTimeMillis() - timer < 1500) {
			 
		 }
		 
		 
		 
		 System.out.print("out");
		 //4.�ر���Դ
		 dsocket.close();
		 
	}
	
	private void showGUI() {
		
		groupUI gui = new groupUI(Server_tcpNameL);
		
		this.gui = gui;
		gui.setVisible(true);

		//display gui until receives user interface
		while(gui.getStay()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.isServer = gui.getDoCreate();
		this.studioName = gui.getChosenStudio();
		this.chosenBack = gui.getBackColor();
		System.out.println(chosenBack);
		gui.setVisible(false);
				
	}
	
	private String getLocalIpAddress() {
        try {
            Enumeration en = NetworkInterface.getNetworkInterfaces();
            for (; en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); 
                         enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && 
                           !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
 
        }
        return null;
    }
	
	public void recieveAndUpdate() {
		ServerHandler handler = new ServerHandler();
		Thread t = new Thread(handler);
		t.start();
	}
	
	public void start() {
    	System.out.println("Connecting to Server...");
    	int index = Server_tcpNameL.indexOf(studioName);
    	String Server_tcpIp = Server_tcpIpL.get(index);
    	Integer Server_tcpPort = Server_tcpPortL.get(index);
    	System.out.print(index);
    	System.out.print(Server_tcpIp);
    	
		try {
			socket = new Socket(Server_tcpIp, Server_tcpPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			objOutputStream = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Successfully connected to server");
		/*
		 * ������ȡ����˷��͹�������Ϣ���̡߳�
		 */
		
		ServerHandler handler = new ServerHandler();
		Thread t = new Thread(handler);
		t.start();
		
		
		}
	
	class ServerHandler implements Runnable{
		public void run(){
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
				while(runFlag){
					Message m = (Message)objectInputStream.readObject();
					//System.out.println("message recieved:" + m.getContent());
					switch (m.getMt()) {
					case MESSAGE:
						ui.updateText(m.getContent());
						break;
					case DIFFERENTIAL:
						int[] a = m.getDifferentialFromContent();
						ui.updatePaintPixel(a[0], a[1],a[2]);
						//System.out.println("This is a diff");
						break;
						case LOGIN:
							ui.updateUserL(m.getContent());
			
					default:
					}
					
				}
			} catch (Exception e) {
				
			}finally {
				
			}
		}
	}
	
public void send(Message m)  {
    	
    	try {
			objOutputStream.writeObject(m);
			//objOutputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
	
	
	
	
	
	

}
