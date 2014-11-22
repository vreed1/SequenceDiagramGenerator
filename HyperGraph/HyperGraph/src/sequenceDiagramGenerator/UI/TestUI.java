package sequenceDiagramGenerator.UI;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import sequenceDiagramGenerator.SourceCodeType;
import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import sequenceDiagramGenerator.sootAnalyzer.Analyzer;

public class TestUI implements ActionListener{

	private JFrame frame;
	private ControlPanel theControlPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestUI window = new TestUI();
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
	public TestUI() {
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
		
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Analyze")){
			String ClassName = theControlPanel.tfClassName.getText();
			String ClassPath = theControlPanel.tfClassPath.getText();
			String ClassDir = theControlPanel.tfClassDir.getText();
			
			Hypergraph<SourceCodeType, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(ClassName, ClassDir, ClassPath);
			
			int hello = 0;
		}
	}

}
