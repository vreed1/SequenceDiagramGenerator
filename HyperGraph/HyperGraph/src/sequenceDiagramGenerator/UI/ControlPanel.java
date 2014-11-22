package sequenceDiagramGenerator.UI;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.Insets;

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
	public JTextField tfClassDir;

	/**
	 * Create the panel.
	 */
	public ControlPanel(ActionListener aListener) {
		setLayout(new BorderLayout());
		
		GridBagLayout gbl_subPanelOne = new GridBagLayout();
		gbl_subPanelOne.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
		JPanel subPanelOne = new JPanel(gbl_subPanelOne);
		
		JLabel lblClasspath = new JLabel("ClassPath");
		GridBagConstraints gbc_lblClasspath = new GridBagConstraints();
		gbc_lblClasspath.insets = new Insets(0, 0, 0, 5);
		gbc_lblClasspath.gridx = 0;
		gbc_lblClasspath.gridy = 0;
		subPanelOne.add(lblClasspath, gbc_lblClasspath);
		
		JPanel subPanelTwo = new JPanel(new GridBagLayout());
		
		JLabel lblAppclass = new JLabel("MainClass");
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
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 0, 5);
		c.gridy = 0;
		c.gridx = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		tfClassPath = new JTextField();
		tfClassPath.setText("/home/brian/git/SequenceDiagramGenerator/ControlFlowGraphTest/bin");
		subPanelOne.add(tfClassPath, c);
		tfClassPath.setColumns(10);
		
		JLabel lblClassdir = new JLabel("ClassDir");
		GridBagConstraints gbc_lblClassdir = new GridBagConstraints();
		gbc_lblClassdir.insets = new Insets(0, 0, 0, 5);
		gbc_lblClassdir.anchor = GridBagConstraints.EAST;
		gbc_lblClassdir.gridx = 2;
		gbc_lblClassdir.gridy = 0;
		subPanelOne.add(lblClassdir, gbc_lblClassdir);
		
		tfClassDir = new JTextField();
		tfClassDir.setText("/home/brian/git/SequenceDiagramGenerator/ControlFlowGraphTest/bin/ToBeAnalyzed");
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 3;
		gbc_textField.gridy = 0;
		subPanelOne.add(tfClassDir, gbc_textField);
		tfClassDir.setColumns(10);
		add(subPanelTwo, BorderLayout.CENTER);
		
	}
}
