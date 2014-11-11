package UI;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import NewAnalyzerCode.ExecAnalysis;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

//Brian Peterson
//Basic UI for control flow graph testing.  
//The control panel sits on top of the window
//and allows redefinition of the 
//class path,
//and definition of a class and method to analyze.

//This panel does not handle its own events, it takes
//an ActionListener as argument.  That listener will
//respond to button presses on this control.

//All the constructor does is UI work, like positioning
//and describing controls.

public class ControlPanel extends JPanel {
	public JTextField tfClassPath;
	public JTextField tfClassName;
	public JTextField tfMethod;

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
		subPanelOne.add(tfClassPath, c);
		tfClassPath.setColumns(10);
		
		JButton btnSetClassPath = new JButton("Set");
		btnSetClassPath.setActionCommand("Set Class Path");
		btnSetClassPath.addActionListener(aListener);
		subPanelOne.add(btnSetClassPath);
		
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
		
		JLabel lblMethod = new JLabel("Method");
		subPanelTwo.add(lblMethod);

		GridBagConstraints c3 = new GridBagConstraints();
		c3.weightx = 1.0;
		c3.fill = GridBagConstraints.HORIZONTAL;
		
		tfMethod = new JTextField();
		tfMethod.setText("NotMain");
		subPanelTwo.add(tfMethod, c3);
		tfMethod.setColumns(10);
		subPanelTwo.add(btnAnalyze);

		add(subPanelOne, BorderLayout.NORTH);
		add(subPanelTwo, BorderLayout.CENTER);
		
		tfClassPath.setText(ExecAnalysis.getSootClassPath());
	}
}
