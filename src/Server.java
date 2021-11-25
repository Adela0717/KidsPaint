
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
	Boolean runFlag = true;
	int[][] data = new int[50][50];
	private ServerSocket server;
	/*
	 * 保存所有客户端输出流的集合。
	 */
	private List<ObjectOutputStream> allOut;
	private String studioName;
	
	static Server instance;
	
	//some attributes for server
	
	
	/*
	 * 用于初始化服务端
	 */

	/**
	 * 将给定的输出流存入共享集合。
	 * @param out
	 */
	public void setStudioName(String name) {
		this.studioName = name;
	}
	
	public static Server getInstance(String studioName) {
		if(instance==null)
			try {
				instance = new Server(studioName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return instance;
	}
	
	
	
	
	private void addOut(ObjectOutputStream out){
		if(allOut == null) {
			System.out.print("no client to sent");
		}else {
			synchronized(allOut) {
				allOut.add(out);
				System.out.printf("Total %d clients are connected.\n", allOut.size());
			}
		}
	}
	/**
	 * 将给定的输出流从共享集合中删除。
	 * @param out
	 */
	private void removeOut(ObjectOutputStream out){
		synchronized(allOut) {
			allOut.remove(out);
		}
			
	}
	/**
	 * 将给定的消息发送给所有客户端。
	 * @param message
	 * @throws IOException 
	 */
	public void sendMessage(Message m) throws IOException{
		synchronized(allOut) {
			for(ObjectOutputStream out:allOut){
				out.writeObject(m);
			}
		}
		
		System.out.println("sent:" + m.getContent());
	}
	
	//更新绘画像素点
	private void updatePaintPixel(int col, int row, int color) {
		if (col >= data.length || row >= data[0].length) return;
		data[col][row] = color;
	}
	
	public void updateChangeToUI(int col, int row, int color) {
		User.ui.updatePaintPixel(col, row, color);
	}
	
	public void updateMessageToUI(String s) {
		User.ui.updateText(s);
	}
	
	public Server(String studioName) throws IOException {
		setStudioName(studioName);
		listenUdpMessage();
		/*
		 * 初始化的同时申请服务端口。
		 */
		server = new ServerSocket(5000);
		allOut = new ArrayList<ObjectOutputStream>();
		/*
		 * 服务端开始工作的方法
		 */
		
		//if a client gets the IP and Port of server and decides to send request to build TCP connection
		new Thread(() -> serve()).start();
			
	}
	
	public void serve() {
		try{
			/*
			 * ServerSocket的accept方法是一个阻塞方法
			 * 作用是监听服务端口，直到一个客户端连接并创建一
			 * 个Socket，使用该Socket即可与刚连接的客户
			 * 端进行交互与数据传输。
			 */
			while(true){
				System.out.println("Waiting for connection from a client...");
				Socket socket = server.accept();
				String newip = socket.getInetAddress().getHostAddress();
				int newport = socket.getPort();
				System.out.println("A connection from"+newip+":"+newport+"is succesffuly connected by TCP");
				/*
				 * 启动一个线程，来完成与该客户端的交互。
				 */
				ClientHandler handler = new ClientHandler(socket);
				Thread t = new Thread(handler);
				t.start();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//a thread for handling new connection
	//wait for the connection request UDP from a client, send back its IP and Port Num
	public void listenUdpMessage() throws IOException {
		new Thread(new Runnable() {
            @Override
            public void run() {
            	DatagramSocket serverSocket = null;
				try {
					serverSocket = new DatagramSocket(4000);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
       		 		//2
       		 	byte[] arr = new byte[1024];
       		 	DatagramPacket packet = new DatagramPacket(arr, arr.length);
       		 	
       		 	//send the list of studios to the clients
       		 	
       		  
       		 	//3 当程序运行起来之后,receive方法会一直处于监听状态
       		 
       		 	while(runFlag) {
       		 		try {
						serverSocket.receive(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
       			//从包中将数据取出
       		 		byte[] arr1 = packet.getData();
       		 		System.out.println(new String(arr1));
       		 	//回复IP和port
       		 		
       		 		System.out.println("udp发送IP和端口");
       		 		// 1、创建UDP的Socket，使用DatagramSocket对象
       		 		DatagramSocket ds = null;
       		 		try {
       		 			ds = new DatagramSocket(4001);
       		 		} catch (SocketException e) {
       		 			// TODO Auto-generated catch block
       		 			e.printStackTrace();
       		 		}
       		 
       		 		// 2、将要发送的数据封装到数据包中,数据为studioname, 本地ip和用于建立tcp连接的端口5000
       		 		String str = studioName + "," + getLocalIpAddress()+",5000,";
       		 		
       		 		byte[] buf = str.getBytes(); //使用DatagramPacket将数据封装到该对象的包中
       		 		//发送目的地址和ip与接收到的UDP广播地址和ip一致
   
       		 		DatagramPacket dp = new DatagramPacket(buf, buf.length, packet.getAddress(),packet.getPort());
       		 
       		 		// 3、通过UDP的Socket服务将数据包发送出去，使用send方法
       		 		try {
       		 			ds.send(dp);
       		 		} catch (IOException e) {
       		 			// TODO Auto-generated catch block
       		 			e.printStackTrace();
       		 		}
       		 
       		 		// 4、关闭Socket服务
       		 		ds.close();
       		 	}
       		 	//4
       		 	serverSocket.close();
            }
        }).start();
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
	
	
	
		
		/**
		 * 该线程负责处理一个客户端交互。
		 * @author kaixu
		 *
		 */
	class ClientHandler implements Runnable{
		/*
		 * 该线程处理客户端的Socket
		 */
		private Socket socket;
		
		//客户端的地址信息
		private String host;
		//该用户的昵称
		private String username;
		
		public ClientHandler(Socket socket){
			this.socket = socket;
			/*
			 * 通过Socket可以获取远端计算机的地址信息。
			 */
			InetAddress address = socket.getInetAddress();
			//解析获取的IP地址信息
			host = address.getHostAddress();
		}
		
		public void run(){
			ObjectOutputStream osw = null;
			try {
				InputStream in = socket.getInputStream();
				ObjectInputStream isr = new ObjectInputStream(in);
				/*
				 * 通过Socket创建输出流用于将消息发送给客户端。
				 */
				OutputStream out = socket.getOutputStream();
				osw = new ObjectOutputStream(out);
				/*
				 * 将该客户端的输出流存入到共享集合中。
				 */
				addOut(osw);
//				Message dmm = new Message(MessageType.STUDIONAME);
//				dmm.setContentFromList(studioName);
				

				while(true){
					Message message = null;
					message = (Message)isr.readObject();
					//if it is a newly logged in user, send him the 

					if(message.getMt() == MessageType.LOGIN) {
						username = message.getContent();
						for(int i = 0;i<data.length;i++) {
							for(int j = 0;j<data[0].length;j++) {
								Message dm = new Message(MessageType.DIFFERENTIAL);
								int[] d = {i,j,data[i][j]};
								dm.setContentFromDifferential(d);
								osw.writeObject(dm);
							}
						}
						continue;
					}
					
					//receive diff, update and send to all clients
					if(message.getMt() == MessageType.DIFFERENTIAL) {
						int[] a = message.getDifferentialFromContent();
						updateChangeToUI(a[0], a[1],a[2]);
						updatePaintPixel(a[0], a[1], a[2]);
					}
					
					if(message.getMt() == MessageType.MESSAGE) {
						updateMessageToUI(message.getContent());
					}
					//广播消息
					sendMessage(message);
				}
			} catch (Exception e) {
				
			} finally {
				/*
				 * 处理当前客户端断开后的逻辑。
				 */
				//将该客户端的输出流从共享集合中删除。
				removeOut(osw);
				System.out.println(host+"drops connection");
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}	
	
}

