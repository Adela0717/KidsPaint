import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.Color;
import javax.swing.border.LineBorder;
import java.io.*;
import java.net.*;

enum PaintMode {Pixel, Area};

public class UI extends JFrame {
	private JTextField msgField;
	private JTextArea chatArea;
	private JPanel pnlColorPicker;
	private JPanel paintPanel;
	private JToggleButton tglPen;
	private JToggleButton tglBucket;
	private JToggleButton tglSave;
	private JToggleButton tglLoad;
	private Color backgroundColor;
	private JToggleButton tglClear;
	private JToggleButton seeUsers;
	public static List<String> userL;
	public String chatPrivate;
	public boolean privateMode = false;
	
	private static UI instance;
	private int selectedColor = -543230; 	//golden
	
	int[][] data = new int[50][50];			// pixel color data array
	int blockSize = 20;
	PaintMode paintMode = PaintMode.Pixel;
	static String username;
	static User user;
	static String studioName;
	static Server server;

	public void updateUserL(String username){
		userL.add(username);
	}
	public void updateUserL(){
		this.userL = userL;
	}


	
//	public Client getClient() {
//		return client;
//	}
	public User getUser() {
		return user;
	}

//	public void setClient(Client client) {
//		this.client = client;
//	}
//	public static void setUser(User user) {
//		this.user = user;
//	}
//	
//	public static void setUserName(String userName) {
//		this.username = userName;
//	}

	/**
	 * get the instance of UI. Singleton design pattern.
	 * @return
	 */
	public static UI getInstance(User user, String username, String studioName) {
		UI.user = user;
		UI.username = username;
		UI.studioName = studioName;
		userL = new ArrayList<>();
		if (instance == null)
			instance = new UI();
		
		return instance;
	}

	public void setBackgroundColor(Color c){
		this.backgroundColor=c;
	}


	
	public static void addServer(Server server) {
		UI.server = server;
		System.out.print("a server added");
	}
	
	public static UI getInstance() {
		
		if (instance == null)
			instance = new UI();
		
		return instance;
	}
	
	
	/**
	 * private constructor. To create an instance of UI, call UI.getInstance() instead.
	 */
	UI() {
		//����username
		
		//��ʼ��ͨ��ģ��
		
		/*
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					client.start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("�ͻ���������������ʧ��");
				}
			}
		}).start();
		*/
		
		setTitle("KidPaint" + "-"+ username + "-" + studioName);
		
		JPanel basePanel = new JPanel();
		getContentPane().add(basePanel, BorderLayout.CENTER);
		basePanel.setLayout(new BorderLayout(0, 0));
		
		paintPanel = new JPanel() {
			
			// refresh the paint panel
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				
				Graphics2D g2 = (Graphics2D) g; // Graphics2D provides the setRenderingHints method
				
				// enable anti-aliasing
			    RenderingHints rh = new RenderingHints(
			             RenderingHints.KEY_ANTIALIASING,
			             RenderingHints.VALUE_ANTIALIAS_ON);
			    g2.setRenderingHints(rh);
			    
			    // clear the paint panel using black
				g2.setColor(Color.black);
				g2.fillRect(0, 0, this.getWidth(), this.getHeight());
				
				// draw and fill circles with the specific colors stored in the data array
				for(int x=0; x<data.length; x++) {
					for (int y=0; y<data[0].length; y++) {
						g2.setColor(new Color(data[x][y]));
						g2.fillArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
						g2.setColor(Color.darkGray);
						g2.drawArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
					}
				}
			}
		};
		
		paintPanel.addMouseListener(new MouseListener() {
			@Override public void mouseClicked(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}

			// handle the mouse-up event of the paint panel
			@Override
			public void mouseReleased(MouseEvent e) {
				if (paintMode == PaintMode.Area && e.getX() >= 0 && e.getY() >= 0)
					paintArea(e.getX()/blockSize, e.getY()/blockSize);
			}
		});
		
		paintPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (paintMode == PaintMode.Pixel && e.getX() >= 0 && e.getY() >= 0)
					paintPixel(e.getX()/blockSize,e.getY()/blockSize);
			}

			@Override public void mouseMoved(MouseEvent e) {}
			
		});
		
		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));
		
		JScrollPane scrollPaneLeft = new JScrollPane(paintPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		basePanel.add(scrollPaneLeft, BorderLayout.CENTER);
		
		JPanel toolPanel = new JPanel();
		basePanel.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JPanel userPanel = new JPanel();
		basePanel.add(userPanel, BorderLayout.SOUTH);

		
		
		pnlColorPicker = new JPanel();
		pnlColorPicker.setPreferredSize(new Dimension(24, 24));
		pnlColorPicker.setBackground(new Color(selectedColor));
		pnlColorPicker.setBorder(new LineBorder(new Color(0, 0, 0)));

		// show the color picker
		pnlColorPicker.addMouseListener(new MouseListener() {
			@Override public void mouseClicked(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {
				ColorPicker picker = ColorPicker.getInstance(UI.instance);
				Point location = pnlColorPicker.getLocationOnScreen();
				location.y += pnlColorPicker.getHeight();
				picker.setLocation(location);
				picker.setVisible(true);
			}
			
		});
		
		toolPanel.add(pnlColorPicker);
		
		tglPen = new JToggleButton("Pen");
		tglPen.setSelected(true);
		toolPanel.add(tglPen);
		
		tglBucket = new JToggleButton("Bucket");
		toolPanel.add(tglBucket);

		tglClear = new JToggleButton("Clear");
		toolPanel.add(tglClear);
		tglClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int i = 0; i < 50; i++){
					for(int j = 0 ; j < 50; j++){
						updatePaintPixel(i, j, backgroundColor.getRGB()); //backgroundColor.getRGB()
					}
				}
			}
		});
		
		//add the save button
		tglSave = new JToggleButton("Save");
		toolPanel.add(tglSave);
		
		tglLoad = new JToggleButton("Load");
		toolPanel.add(tglLoad);
		
		//load the sketch data from a local file
		tglLoad.addActionListener(new ActionListener(){
			@Override
			
			public void actionPerformed(ActionEvent arg0){
				
				String fileName = JOptionPane.showInputDialog(null,"File name:");
				BufferedReader bufferedReader = null;
				int [][]buffer = new int[50][50];
				
				try {
					InputStreamReader input = new InputStreamReader(new FileInputStream(new File(fileName)));
					bufferedReader = new BufferedReader(input);
					String line = null;
					int i = 0;
					while((line=bufferedReader.readLine())!=null) {
						if(null != line){
							String[] string = line.split("\\t");
							for(int k = 0; k<string.length; k++) {
								buffer[i][k] = Integer.valueOf(string[k]);
								System.out.print(buffer[i][k] + "�� ");
								
							}
							i++;
						}
					}
					setData(buffer, blockSize);
					
					for(int m = 0; m < 50; m++) {
						for(int n=0; n < 50; n++ ) {
							Message dm = new Message(MessageType.DIFFERENTIAL);
							int[] d = {m, n, buffer[m][n]};
							dm.setContentFromDifferential(d);
							System.out.println(user.getIsServer());
							if(user.getIsServer()) {
								try {
									Server.instance.sendMessage(dm);
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}else {
								user.send(dm); 
							}
						}
					}
				
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});


		
		
		//save the sketch data into a local file
		tglSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				String fileName = JOptionPane.showInputDialog(null,"Save as:");
				try {
					FileWriter writer = new FileWriter(fileName);
					for(int i = 0 ; i < 50; i++) {
						for(int j = 0; j < 50; j++) {
							String content = String.valueOf(data[i][j] + "");
							writer.write(content + "\t");
						}
						writer.write("\r\n");
					}
					
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			
			}
			
		});


        //for both SERVER and USER, select a user for private chat
		seeUsers = new JToggleButton("See Users");
		seeUsers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateUserL();
				chatPrivate =  (String)JOptionPane.showInputDialog(null,"Choose a user","See Users",JOptionPane.QUESTION_MESSAGE,null,userL.toArray(), userL.toArray()[0]);
				System.out.println(chatPrivate);
				if(chatPrivate != null) {
					privateMode = true;
				}
			}
		});

		userPanel.add(seeUsers);

		//for SERVER, select a user to kick out
		
		
		// change the paint mode to PIXEL mode
		tglPen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(true);
				tglBucket.setSelected(false);
				paintMode = PaintMode.Pixel;
			}
		});
		
		// change the paint mode to AREA mode
		tglBucket.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(true);
				paintMode = PaintMode.Area;
			}
		});
		
		JPanel msgPanel = new JPanel();
		
		getContentPane().add(msgPanel, BorderLayout.EAST);
		
		msgPanel.setLayout(new BorderLayout(0, 0));
		
		msgField = new JTextField();	// text field for inputting message
		
		msgPanel.add(msgField, BorderLayout.SOUTH);
		
		// handle key-input event of the message field
		msgField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {}
			@Override public void keyPressed(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) {		// if the user press ENTER
					onTextInputted(msgField.getText());
					msgField.setText("");
				}
			}
			
		});
		
		chatArea = new JTextArea();		// the read only text area for showing messages
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		
		JScrollPane scrollPaneRight = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneRight.setPreferredSize(new Dimension(300, this.getHeight()));
		msgPanel.add(scrollPaneRight, BorderLayout.CENTER);
		
		this.setSize(new Dimension(800, 600));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		if(!user.getIsServer()) {
			sendLogin();
		}
		
	}
	
	public void sendLogin() {
		Message lm = new Message(MessageType.LOGIN);
		lm.setContent(username);
		if(user.getIsServer()) {
			try {
				server.sendMessage(lm);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}else {
			user.send(lm); 
		}
	}
	

	
	/**
	 * it will be invoked if the user selected the specific color through the color picker
	 * @param colorValue - the selected color
	 */
	public void selectColor(int colorValue) {
		SwingUtilities.invokeLater(()->{
			selectedColor = colorValue;
			pnlColorPicker.setBackground(new Color(colorValue));
		});
	}
		 
	/**
	 * it will be invoked if the user inputted text in the message field
	 * @param text - user inputted text
	 */
	private void onTextInputted(String text) {
		chatArea.setText(chatArea.getText() + username + ":" + text + "\n");
		Message mm = null;
		if(privateMode){
			mm = new Message(MessageType.PRIVATEM);
			mm.setContent(username + "," + chatPrivate +  ", "+text);
		}else {
			mm = new Message(MessageType.MESSAGE);
			mm.setContent(username + ": " + text);
		}
		if(user.getIsServer()) {
			try {
				server.sendMessage(mm);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}else {
			user.send(mm); 
		}
	}
	
	public void updateText(String text) {

		if(text.split(":")[0].equals(username))return;
		
		chatArea.setText(chatArea.getText() +text + "\n");
	}
	
	/**
	 * change the color of a specific pixel
	 * @param col, row - the position of the selected pixel
	 */
	public void paintPixel(int col, int row) {
		if (col >= data.length || row >= data[0].length) return;
		
		data[col][row] = selectedColor;
		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
		Message dm = new Message(MessageType.DIFFERENTIAL);
		int[] d = {col,row,selectedColor};
		dm.setContentFromDifferential(d);
		if(user.getIsServer()) {
			try {
				System.out.println("!!!server sends a message" + dm.getContent());
				server.sendMessage(dm);
				System.out.println("!!!server sends a message.");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}else {
			user.send(dm); 
		}
	}
	
	
	public void updatePaintPixel(int col, int row, int color) {
		if (col >= data.length || row >= data[0].length) return;
		data[col][row] = color;
		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
	}
	
	/**
	 * change the color of a specific area
	 * @param col, row - the position of the selected pixel
	 * @return a list of modified pixels
	 */
	public List paintArea(int col, int row) {
		LinkedList<Point> filledPixels = new LinkedList<Point>();

		if (col >= data.length || row >= data[0].length) return filledPixels;

		int oriColor = data[col][row];
		LinkedList<Point> buffer = new LinkedList<Point>();
		
		if (oriColor != selectedColor) {
			buffer.add(new Point(col, row));
			
			while(!buffer.isEmpty()) {
				Point p = buffer.removeFirst();
				int x = p.x;
				int y = p.y;
				
				if (data[x][y] != oriColor) continue;
				
				data[x][y] = selectedColor;
				Message dm = new Message(MessageType.DIFFERENTIAL);
				int[] d = {x,y,selectedColor};
				dm.setContentFromDifferential(d);
				
				if(user.getIsServer()) {
					try {
						server.sendMessage(dm);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}else {
					user.send(dm); 
				}
				
				filledPixels.add(p);
	
				if (x > 0 && data[x-1][y] == oriColor) buffer.add(new Point(x-1, y));
				if (x < data.length - 1 && data[x+1][y] == oriColor) buffer.add(new Point(x+1, y));
				if (y > 0 && data[x][y-1] == oriColor) buffer.add(new Point(x, y-1));
				if (y < data[0].length - 1 && data[x][y+1] == oriColor) buffer.add(new Point(x, y+1));
			}
			paintPanel.repaint();
		}
		return filledPixels;
	}
	
	/**
	 * set pixel data and block size
	 * @param data
	 * @param blockSize
	 */
	public void setData(int[][] data, int blockSize) {
		this.data = data;
		this.blockSize = blockSize;
		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));
		paintPanel.repaint();
	}
}
