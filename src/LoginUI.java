import java.awt.Dimension;

import javax.swing.JFrame;

public class LoginUI extends JFrame{
	private static LoginUI instance;
	public static LoginUI getInstance() {
		if (instance == null)
			instance = new LoginUI();
		
		return instance;
	}
	
	private LoginUI() {
		this.setTitle("It is my first Java GUI app"); 
		this.setSize(new Dimension(320, 240)); 
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		this.setVisible(true);
	}
	
	

}
