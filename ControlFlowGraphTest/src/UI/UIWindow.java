package UI;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import NewAnalyzerCode.ExecAnalysis;

public class UIWindow implements ActionListener{

	private JFrame frame;
	private GraphPanel theGraphPanel;
	private ControlPanel theControlPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIWindow window = new UIWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public UIWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		theControlPanel = new ControlPanel(this);
		
		frame.getContentPane().add(theControlPanel, BorderLayout.NORTH);
		
		theGraphPanel = new GraphPanel();
		frame.getContentPane().add(theGraphPanel, BorderLayout.CENTER);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Set Class Path")){
			ExecAnalysis.setSootClassPath(theControlPanel.tfClassPath.getText());
		}
		if(e.getActionCommand().equals("Analyze")){
			theGraphPanel.SetGraph(ExecAnalysis.Analyze(
					theControlPanel.tfClassName.getText(), 
					theControlPanel.tfMethod.getText()));
		}
	}

}
