

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


	public class groupUI extends JFrame{
		
		private JLabel studio_label;
		private JLabel welcome_label;
		private JButton create_button;
		private JButton go_button;
		private JList <String> studioList;
		private List<String> studios;
		private JScrollPane sp;
		private String chosenStudio;
		private boolean doCreate;
		private JPanel panel;
		boolean stay = true;
		
		static groupUI instance;
		
		//some attributes for server
		
		public static groupUI getInstance(List<String> names) {
			if(instance==null)
				instance = new groupUI(names);
			return instance;
		}
		
		public groupUI(List<String> names) {
			
			this.studios = new ArrayList<String>();
			updateStudio(names);
			origin();
			
		}
		
		public void updateStudio(List<String> names) {
			for(String s : names) {
				studios.add(s);
			}
		}
		
		public boolean getDoCreate() {
			return doCreate;
		}
		
		public String getChosenStudio() {
			return chosenStudio;
		}
		
		public boolean getStay() {
			return stay;
		}
		
		
		
		private void origin() {
			this.setTitle("Cerate or Join a studio"); 
			this.setLocationRelativeTo(null);
			this.setSize(new Dimension(400, 400)); 
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
			
			Container container = this.getContentPane();
			container.setLayout(new BorderLayout());
			//A welcome message on the top
			welcome_label = new JLabel("Welcome to KidsPaint!");
			container.add(welcome_label, BorderLayout.NORTH);
			
			//A panel in the center
			panel = new JPanel(); 
			panel.setBackground(Color.WHITE);
			panel.setLayout(new FlowLayout());
			container.add(panel, BorderLayout.CENTER);
			
			//choose a studio
			System.out.print(studios.size());
			if(studios.size() == 0) {
				studio_label = new JLabel("Start with creating a new studio");
				studio_label.setSize(200, 40);
				studio_label.setLocation(100, 70);
				panel.add(studio_label);
				
			}else {
				studio_label = new JLabel("Choose a studio");
				studio_label.setSize(200, 40);
				studio_label.setLocation(100, 70);
				panel.add(studio_label);
				
				String[] arr = new String[studios.size()];
				for(int i = 0; i < arr.length; i++) {
					arr[i] = studios.get(i);
				}
				studioList = new JList<String>();
				studioList.setListData(arr);
			
				System.out.print(studioList.getComponentCount());
				sp = new JScrollPane(studioList); 
				sp.setPreferredSize(new Dimension(200, 100));
				panel.add(sp);
				
				
				go_button = new JButton("Join this studio");
				go_button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) { 
						chosenStudio = studioList.getSelectedValue();
						doCreate = false;
						stay = false;
					} });
				panel.add(go_button);

			}
			
            //create a studio
			create_button = new JButton("Create a new one");
			create_button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					doCreate = true;
					chosenStudio = JOptionPane.showInputDialog(null,"input studio name:");
					stay = false;
				}
				
			});
			
			
			welcome_label.setSize(150, 90);
			welcome_label.setFont(new Font("Monaco", Font.PLAIN, 20));
			
			
			
			
			
			
			create_button.setSize(70, 40);
			create_button.setLocation(100, 200);
			panel.add(create_button);

		}
		
		

	}



