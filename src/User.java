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
			ui.setData(new int[50][50], 20);
			UI.addServer(server);
			System.out.println("The server is: " + server);
			ui.setVisible(true);
			System.out.print("name:" + studioName); 
			
			
		}else {
			
			System.out.println("this is a client");
			start();
			this.ui = UI.getInstance(this, userName, studioName);
			
			ui.setVisible(true);
		}
	}
	
	
	
	public boolean getIsServer() {
		return isServer;
	}
	
	public void getServerInfo(){
		//1.创建对象
		 //构造数据报套接字并将其绑定到本地主机上4002端口。
		 
		 //2.打包
		 byte[] arr = "request".getBytes();
		 //四个参数: 包的数据 包的长度 主机对象 端口号   
		 DatagramPacket packet = null;
		try {
			packet = new DatagramPacket
			  (arr, arr.length,InetAddress.getByName(getLocalIpAddress()) , 4000);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  
		 //3.发送
		 try {
			dsocket.send(packet);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 
		 //4.receive serverInfo from multiple servers（listen for 5 second)
		 long timer = System.currentTimeMillis();
		 
		 Thread thread = new Thread(() ->{
			 while(true) {
				 byte[] buf =new byte[1024];
				 DatagramPacket packetr = new DatagramPacket(buf, buf.length);
				 try {
					 dsocket.receive(packetr);
					 buf = packetr.getData(); 
					 
					 System.out.println(new String(buf));
					 String[]info = new String(buf).split(",");
					 String Server_tcpName = info[0];
					 String Server_tcpIp = info[1];
					 Integer Server_tcpPort = Integer.parseInt(info[2]);
					 Server_tcpNameL.add(Server_tcpName);
					 Server_tcpIpL.add(Server_tcpIp);
					 Server_tcpPortL.add(Server_tcpPort);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						continue;
					}
			 		
			 }	
			 
		 });
		 thread.start();
		 
		 while(System.currentTimeMillis() - timer > 1500) {
			 
		 }
		 
		 
		 
		 System.out.print("out");
		 //4.关闭资源
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
		 * 启动读取服务端发送过来的消息的线程。
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
