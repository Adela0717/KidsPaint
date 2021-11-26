
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
	 * �������пͻ���������ļ��ϡ�
	 */
	private List<ObjectOutputStream> allOut;

	private String studioName;
	
	static Server instance;
	
	//some attributes for server
	
	
	/*
	 * ���ڳ�ʼ�������
	 */

	/**
	 * ����������������빲���ϡ�
	 * @param
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

	public void setData(int[][] data){
		this.data = data;
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
	 * ��������������ӹ�������ɾ����
	 * @param out
	 */
	private void removeOut(ObjectOutputStream out){
		synchronized(allOut) {
			allOut.remove(out);
		}
			
	}
	/**
	 * ����������Ϣ���͸����пͻ��ˡ�
	 * @param
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
	
	//���»滭���ص�
	public void updatePaintPixel(int col, int row, int color) {
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
		 * ��ʼ����ͬʱ�������˿ڡ�
		 */
		server = new ServerSocket(5000);
		allOut = new ArrayList<ObjectOutputStream>();
		/*
		 * ����˿�ʼ�����ķ���
		 */
		
		//if a client gets the IP and Port of server and decides to send request to build TCP connection
		new Thread(() -> serve()).start();
			
	}
	
	public void serve() {
		try{
			/*
			 * ServerSocket��accept������һ����������
			 * �����Ǽ�������˿ڣ�ֱ��һ���ͻ������Ӳ�����һ
			 * ��Socket��ʹ�ø�Socket����������ӵĿͻ�
			 * �˽��н��������ݴ��䡣
			 */
			while(true){
				System.out.println("Waiting for connection from a client...");
				Socket socket = server.accept();
				String newip = socket.getInetAddress().getHostAddress();
				int newport = socket.getPort();
				System.out.println("A connection from"+newip+":"+newport+"is succesffuly connected by TCP");
				/*
				 * ����һ���̣߳��������ÿͻ��˵Ľ�����
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
       		 	
       		  
       		 	//3 ��������������֮��,receive������һֱ���ڼ���״̬
       		 
       		 	while(runFlag) {
       		 		try {
						serverSocket.receive(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
       			//�Ӱ��н�����ȡ��
       		 		byte[] arr1 = packet.getData();
       		 		System.out.println(new String(arr1));
       		 	//�ظ�IP��port
       		 		
       		 		System.out.println("udp����IP�Ͷ˿�");
       		 		// 1������UDP��Socket��ʹ��DatagramSocket����

       		 
       		 		// 2����Ҫ���͵����ݷ�װ�����ݰ���,����Ϊstudioname, ����ip�����ڽ���tcp���ӵĶ˿�5000
       		 		String str = studioName + "," + getLocalIpAddress()+",5000,";
       		 		
       		 		byte[] buf = str.getBytes(); //ʹ��DatagramPacket�����ݷ�װ���ö���İ���
       		 		//����Ŀ�ĵ�ַ��ip����յ���UDP�㲥��ַ��ipһ��
   
       		 		DatagramPacket dp = new DatagramPacket(buf, buf.length, packet.getAddress(),packet.getPort());
       		 
       		 		// 3��ͨ��UDP��Socket�������ݰ����ͳ�ȥ��ʹ��send����
       		 		try {
       		 			serverSocket.send(dp);
       		 		} catch (IOException e) {
       		 			// TODO Auto-generated catch block
       		 			e.printStackTrace();
       		 		}
       		 
       		 		// 4���ر�Socket����
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
		 * ���̸߳�����һ���ͻ��˽�����
		 * @author kaixu
		 *
		 */
	class ClientHandler implements Runnable{
		/*
		 * ���̴߳���ͻ��˵�Socket
		 */
		private Socket socket;
		
		//�ͻ��˵ĵ�ַ��Ϣ
		private String host;
		//���û����ǳ�
		private String username;
		
		public ClientHandler(Socket socket){
			this.socket = socket;
			/*
			 * ͨ��Socket���Ի�ȡԶ�˼�����ĵ�ַ��Ϣ��
			 */
			InetAddress address = socket.getInetAddress();
			//������ȡ��IP��ַ��Ϣ
			host = address.getHostAddress();
		}
		
		public void run(){
			ObjectOutputStream osw = null;
			try {
				InputStream in = socket.getInputStream();
				ObjectInputStream isr = new ObjectInputStream(in);
				/*
				 * ͨ��Socket������������ڽ���Ϣ���͸��ͻ��ˡ�
				 */
				OutputStream out = socket.getOutputStream();
				osw = new ObjectOutputStream(out);
				/*
				 * ���ÿͻ��˵���������뵽�������С�
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
						User.ui.updateUserL(username);

						for(int i = 0;i<data.length;i++) {
							for(int j = 0;j<data[0].length;j++) {
								Message dm = new Message(MessageType.DIFFERENTIAL);
								int[] d = {i,j,data[i][j]};
								dm.setContentFromDifferential(d);
								osw.writeObject(dm);

							}
						}
						//continue;
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
					//�㲥��Ϣ
					sendMessage(message);
				}
			} catch (Exception e) {
				
			} finally {
				/*
				 * ����ǰ�ͻ��˶Ͽ�����߼���
				 */
				//���ÿͻ��˵�������ӹ�������ɾ����
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

