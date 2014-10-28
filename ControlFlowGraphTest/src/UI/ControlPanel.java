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
		
		tfClassName = new JTextField();
		subPanelTwo.add(tfClassName, c);
		tfClassName.setColumns(10);
		
		JButton btnAnalyze = new JButton("Analyze");
		btnAnalyze.setActionCommand("Analyze");
		btnAnalyze.addActionListener(aListener);
		
		JLabel lblMethod = new JLabel("Method");
		subPanelTwo.add(lblMethod);
		
		tfMethod = new JTextField();
		subPanelTwo.add(tfMethod, c);
		tfMethod.setColumns(10);
		subPanelTwo.add(btnAnalyze);

		add(subPanelOne, BorderLayout.NORTH);
		add(subPanelTwo, BorderLayout.CENTER);
		
		tfClassPath.setText(ExecAnalysis.getSootClassPath());
	}
}
