package sequenceDiagramGenerator.UI;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;

//Brian Peterson
//Basic UI for control flow graph testing.  
//The control panel sits on top of the window
//and allows redefinition of the 
//class path,
//and definition of a class to analyze.

//This panel does not handle its own events, it takes
//an ActionListener as argument.  That listener will
//respond to button presses on this control.

//All the constructor does is UI work, like positioning
//and describing controls.

public class ControlPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5563100181795453504L;
	public JTextField tfClassPath;
	public JTextField tfClassName;

	/**
	 * Create the panel.
	 */
	public ControlPanel(ActionListener aListener) {
		setLayout(new BorderLayout());
		
		JPanel subPanelOne = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		JLabel lblClasspath = new JLabel("ClassPath");
		subPanelOne.add(lblClasspath);
		
		tfClassPath = new JTextField();
		tfClassPath.setText("/home/brian/git/SequenceDiagramGenerator/ControlFlowGraphTest/bin");
		subPanelOne.add(tfClassPath, c);
		tfClassPath.setColumns(10);
		
		JPanel subPanelTwo = new JPanel(new GridBagLayout());
		
		JLabel lblAppclass = new JLabel("Class");
		subPanelTwo.add(lblAppclass);

		GridBagConstraints c2 = new GridBagConstraints();
		c2.weightx = 1.0;
		c2.fill = GridBagConstraints.HORIZONTAL;
		
		tfClassName = new JTextField();
		tfClassName.setText("ToBeAnalyzed.ToBeAnalyzed");
		subPanelTwo.add(tfClassName, c2);
		tfClassName.setColumns(10);
		
		JButton btnAnalyze = new JButton("Analyze");
		btnAnalyze.setActionCommand("Analyze");
		btnAnalyze.addActionListener(aListener);
		subPanelTwo.add(btnAnalyze);

		add(subPanelOne, BorderLayout.NORTH);
		add(subPanelTwo, BorderLayout.CENTER);
		
	}
}
