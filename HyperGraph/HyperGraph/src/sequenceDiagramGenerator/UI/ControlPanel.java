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
import java.awt.event.ActionEvent;

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
	public JTextField tfSaveFile;

	/**
	 * Create the panel.
	 */
	public ControlPanel(ActionListener aListener) {
		setLayout(new BorderLayout());
		
		GridBagLayout gbl_subPanelOne = new GridBagLayout();
		gbl_subPanelOne.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0};
		JPanel subPanelOne = new JPanel(gbl_subPanelOne);
		
		JLabel lblClasspath = new JLabel("ClassPath");
		GridBagConstraints gbc_lblClasspath = new GridBagConstraints();
		gbc_lblClasspath.insets = new Insets(0, 0, 0, 5);
		gbc_lblClasspath.gridx = 0;
		gbc_lblClasspath.gridy = 0;
		subPanelOne.add(lblClasspath, gbc_lblClasspath);
		
		GridBagLayout gbl_subPanelTwo = new GridBagLayout();
		gbl_subPanelTwo.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0};
		JPanel subPanelTwo = new JPanel(gbl_subPanelTwo);
		
		JLabel lblAppclass = new JLabel("MainClass");
		GridBagConstraints gbc_lblAppclass = new GridBagConstraints();
		gbc_lblAppclass.insets = new Insets(0, 0, 5, 5);
		gbc_lblAppclass.gridx = 0;
		gbc_lblAppclass.gridy = 0;
		subPanelTwo.add(lblAppclass, gbc_lblAppclass);
		
				GridBagConstraints c2 = new GridBagConstraints();
				c2.insets = new Insets(0, 0, 5, 5);
				c2.gridx = 1;
				c2.gridy = 0;
				c2.weightx = 1.0;
				c2.fill = GridBagConstraints.HORIZONTAL;
				
				tfClassName = new JTextField();
				tfClassName.setText("ToBeAnalyzed.ToBeAnalyzed");
				subPanelTwo.add(tfClassName, c2);
				tfClassName.setColumns(10);
		
		JButton btnAnalyze = new JButton("Analyze");
		btnAnalyze.setActionCommand("Analyze");
		btnAnalyze.addActionListener(aListener);
		GridBagConstraints gbc_btnAnalyze = new GridBagConstraints();
		gbc_btnAnalyze.insets = new Insets(0, 0, 5, 5);
		gbc_btnAnalyze.gridx = 2;
		gbc_btnAnalyze.gridy = 0;
		subPanelTwo.add(btnAnalyze, gbc_btnAnalyze);
		
		JButton btnLoadjar = new JButton("LoadJar");
		btnLoadjar.setActionCommand("LoadJar");
		btnLoadjar.addActionListener(aListener);
		GridBagConstraints gbc_btnLoadjar = new GridBagConstraints();
		gbc_btnLoadjar.insets = new Insets(0, 0, 5, 0);
		gbc_btnLoadjar.gridx = 3;
		gbc_btnLoadjar.gridy = 0;
		subPanelTwo.add(btnLoadjar, gbc_btnLoadjar);
		
		JLabel lblOutputfile = new JLabel("OutputFile");
		GridBagConstraints gbc_lblOutputfile = new GridBagConstraints();
		gbc_lblOutputfile.anchor = GridBagConstraints.EAST;
		gbc_lblOutputfile.insets = new Insets(0, 0, 0, 5);
		gbc_lblOutputfile.gridx = 0;
		gbc_lblOutputfile.gridy = 1;
		subPanelTwo.add(lblOutputfile, gbc_lblOutputfile);
		
		tfSaveFile = new JTextField();
		tfSaveFile.setText("/home/brian/Desktop/out.pdf");
		GridBagConstraints gbc_tfSaveFile = new GridBagConstraints();
		gbc_tfSaveFile.insets = new Insets(0, 0, 0, 5);
		gbc_tfSaveFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfSaveFile.gridx = 1;
		gbc_tfSaveFile.gridy = 1;
		subPanelTwo.add(tfSaveFile, gbc_tfSaveFile);
		tfSaveFile.setColumns(10);

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
		//gbc_lblClassdir.anchor = GridBagConstraints.WEST;
		//gbc_lblClassdir.weightx = 1.0;
		gbc_lblClassdir.gridx = 2;
		gbc_lblClassdir.gridy = 0;
		subPanelOne.add(lblClassdir, gbc_lblClassdir);
		
		tfClassDir = new JTextField();
		tfClassDir.setText("/home/brian/git/SequenceDiagramGenerator/ControlFlowGraphTest/bin/ToBeAnalyzed");
		GridBagConstraints gbc_textField2 = new GridBagConstraints();
		gbc_textField2.insets = new Insets(0, 0, 0, 5);
		gbc_textField2.anchor = GridBagConstraints.WEST;
		gbc_textField2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField2.gridx = 3;
		gbc_textField2.gridy = 0;
		subPanelOne.add(tfClassDir, gbc_textField2);
		tfClassDir.setColumns(10);
		add(subPanelTwo, BorderLayout.CENTER);
		
		JButton btnSavefile = new JButton("SaveFile");
		btnSavefile.setActionCommand("SaveFile");
		btnSavefile.addActionListener(aListener);
		GridBagConstraints gbc_btnSavefile = new GridBagConstraints();
		gbc_btnSavefile.insets = new Insets(0, 0, 0, 5);
		gbc_btnSavefile.gridx = 2;
		gbc_btnSavefile.gridy = 1;
		subPanelTwo.add(btnSavefile, gbc_btnSavefile);
		
	}
}
