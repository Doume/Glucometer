package main;
import javax.swing.SwingUtilities;


	public class GlucoMeter {
		public static void main(String[] args){
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					Main_Window main_win = new Main_Window();
					main_win.setVisible(true);
					
				}
			});
		}
	}
