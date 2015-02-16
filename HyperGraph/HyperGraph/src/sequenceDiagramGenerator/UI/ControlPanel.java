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
import javax.swing.JSeparator;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JTextArea;
import javax.swing.JComboBox;

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
	
	public JComboBox<CmbBoxItem> cmbFunctions;

	/**
	 * Create the panel.
	 */
	public ControlPanel(ActionListener aListener) {
		setLayout(new BorderLayout());
		
		GridBagLayout gbl_subPanelTwo = new GridBagLayout();
		gbl_subPanelTwo.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_subPanelTwo.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0};
		JPanel subPanelTwo = new JPanel(gbl_subPanelTwo);
		
		JLabel lblClasspath = new JLabel("ClassPath");
		GridBagConstraints gbc_lblClasspath = new GridBagConstraints();
		gbc_lblClasspath.anchor = GridBagConstraints.WEST;
		gbc_lblClasspath.insets = new Insets(0, 0, 5, 5);
		gbc_lblClasspath.gridx = 0;
		gbc_lblClasspath.gridy = 0;
		subPanelTwo.add(lblClasspath, gbc_lblClasspath);
		
		tfClassPath = new JTextField();
		GridBagConstraints gbc_tfClassPath = new GridBagConstraints();
		gbc_tfClassPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfClassPath.gridwidth = 5;
		gbc_tfClassPath.insets = new Insets(0, 0, 5, 0);
		gbc_tfClassPath.gridx = 1;
		gbc_tfClassPath.gridy = 0;
		subPanelTwo.add(tfClassPath, gbc_tfClassPath);
		tfClassPath.setText("/home/brian/git/SequenceDiagramGenerator/ControlFlowGraphTest/bin");
		tfClassPath.setColumns(10);
		
		JLabel lblOutputfile = new JLabel("OutputFile");
		GridBagConstraints gbc_lblOutputfile = new GridBagConstraints();
		gbc_lblOutputfile.anchor = GridBagConstraints.WEST;
		gbc_lblOutputfile.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputfile.gridx = 0;
		gbc_lblOutputfile.gridy = 1;
		subPanelTwo.add(lblOutputfile, gbc_lblOutputfile);
		
		tfSaveFile = new JTextField();
		tfSaveFile.setText("/home/brian/Desktop/out.pdf");
		GridBagConstraints gbc_tfSaveFile = new GridBagConstraints();
		gbc_tfSaveFile.insets = new Insets(0, 0, 5, 5);
		gbc_tfSaveFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfSaveFile.gridx = 1;
		gbc_tfSaveFile.gridy = 1;
		subPanelTwo.add(tfSaveFile, gbc_tfSaveFile);
		tfSaveFile.setColumns(10);
		
		JButton btnSavefile = new JButton("Select Output File");
		btnSavefile.setActionCommand("SaveFile");
		btnSavefile.addActionListener(aListener);
		GridBagConstraints gbc_btnSavefile = new GridBagConstraints();
		gbc_btnSavefile.gridwidth = 2;
		gbc_btnSavefile.insets = new Insets(0, 0, 5, 5);
		gbc_btnSavefile.gridx = 3;
		gbc_btnSavefile.gridy = 1;
		subPanelTwo.add(btnSavefile, gbc_btnSavefile);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridx = 1;
		gbc_verticalStrut.gridy = 2;
		subPanelTwo.add(verticalStrut, gbc_verticalStrut);
		
		JLabel lblClassdir = new JLabel("ClassDir");
		GridBagConstraints gbc_lblClassdir = new GridBagConstraints();
		gbc_lblClassdir.anchor = GridBagConstraints.WEST;
		gbc_lblClassdir.insets = new Insets(0, 0, 5, 5);
		gbc_lblClassdir.gridx = 0;
		gbc_lblClassdir.gridy = 4;
		subPanelTwo.add(lblClassdir, gbc_lblClassdir);
		
		tfClassDir = new JTextField();
		GridBagConstraints gbc_tfClassDir = new GridBagConstraints();
		gbc_tfClassDir.gridwidth = 4;
		gbc_tfClassDir.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfClassDir.insets = new Insets(0, 0, 5, 5);
		gbc_tfClassDir.gridx = 1;
		gbc_tfClassDir.gridy = 4;
		subPanelTwo.add(tfClassDir, gbc_tfClassDir);
		tfClassDir.setText("/home/brian/git/SequenceDiagramGenerator/ControlFlowGraphTest/bin/ToBeAnalyzed");
		tfClassDir.setColumns(10);
				
				JLabel lblAppclass = new JLabel("MainClass");
				GridBagConstraints gbc_lblAppclass = new GridBagConstraints();
				gbc_lblAppclass.anchor = GridBagConstraints.WEST;
				gbc_lblAppclass.insets = new Insets(0, 0, 5, 5);
				gbc_lblAppclass.gridx = 0;
				gbc_lblAppclass.gridy = 5;
				subPanelTwo.add(lblAppclass, gbc_lblAppclass);
		
				GridBagConstraints c2 = new GridBagConstraints();
				c2.gridwidth = 4;
				c2.insets = new Insets(0, 0, 5, 5);
				c2.gridx = 1;
				c2.gridy = 5;
				c2.weightx = 1.0;
				c2.fill = GridBagConstraints.HORIZONTAL;
				
				tfClassName = new JTextField();
				tfClassName.setText("ToBeAnalyzed.ToBeAnalyzed");
				subPanelTwo.add(tfClassName, c2);
				tfClassName.setColumns(10);
		add(subPanelTwo, BorderLayout.CENTER);
		
		JButton btnAnalyze = new JButton("Analyze From Dir and MainClass");
		btnAnalyze.setActionCommand("Analyze");
		btnAnalyze.addActionListener(aListener);
		GridBagConstraints gbc_btnAnalyze = new GridBagConstraints();
		gbc_btnAnalyze.gridwidth = 2;
		gbc_btnAnalyze.insets = new Insets(0, 0, 5, 5);
		gbc_btnAnalyze.gridx = 0;
		gbc_btnAnalyze.gridy = 6;
		subPanelTwo.add(btnAnalyze, gbc_btnAnalyze);
		
		JLabel lblFunctions = new JLabel("Functions");
		GridBagConstraints gbc_lblFunctions = new GridBagConstraints();
		gbc_lblFunctions.insets = new Insets(0, 0, 5, 5);
		gbc_lblFunctions.anchor = GridBagConstraints.EAST;
		gbc_lblFunctions.gridx = 3;
		gbc_lblFunctions.gridy = 6;
		subPanelTwo.add(lblFunctions, gbc_lblFunctions);
		
		cmbFunctions = new JComboBox<CmbBoxItem>();
		GridBagConstraints gbc_cmbFunctions = new GridBagConstraints();
		gbc_cmbFunctions.insets = new Insets(0, 0, 5, 5);
		gbc_cmbFunctions.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbFunctions.gridx = 4;
		gbc_cmbFunctions.gridy = 6;
		subPanelTwo.add(cmbFunctions, gbc_cmbFunctions);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_1.gridx = 1;
		gbc_verticalStrut_1.gridy = 7;
		subPanelTwo.add(verticalStrut_1, gbc_verticalStrut_1);
		
		JButton btnLoadjar = new JButton("Analyze From Selected JAR file");
		btnLoadjar.setActionCommand("LoadJar");
		btnLoadjar.addActionListener(aListener);
		GridBagConstraints gbc_btnLoadjar = new GridBagConstraints();
		gbc_btnLoadjar.gridwidth = 2;
		gbc_btnLoadjar.insets = new Insets(0, 0, 5, 5);
		gbc_btnLoadjar.gridx = 0;
		gbc_btnLoadjar.gridy = 8;
		subPanelTwo.add(btnLoadjar, gbc_btnLoadjar);
		
		JButton btnMakeseqdia = new JButton("MakeSeqDia");
		btnMakeseqdia.setActionCommand("MakeSequenceDiagram");
		GridBagConstraints gbc_btnMakeseqdia = new GridBagConstraints();
		gbc_btnMakeseqdia.gridwidth = 2;
		gbc_btnMakeseqdia.insets = new Insets(0, 0, 5, 5);
		gbc_btnMakeseqdia.gridx = 3;
		gbc_btnMakeseqdia.gridy = 8;
		subPanelTwo.add(btnMakeseqdia, gbc_btnMakeseqdia);
		
		JTextArea txtrTwoRunOptions = new JTextArea();
		txtrTwoRunOptions.setLineWrap(true);
		txtrTwoRunOptions.setWrapStyleWord(true);
		txtrTwoRunOptions.setRows(3);
		txtrTwoRunOptions.setText("The text options above are examples and should be changed.\nTwo run options exist.  ClassPath and OutputFile are relevant to both.  Classpath is only needed if there are external references which are not on your default jara class path.  OutputFile will be where the output will be stored.  \nThe first option is to analyze .class files in a directory.  Choose the directory next to ClassDir and the MainClass, then Analyze.\nThe second option is to analyze a jar file.  Click the Analyze button, and you will be prompted to select the jar file to analyze.");
		txtrTwoRunOptions.setEditable(false);
		GridBagConstraints gbc_txtrTwoRunOptions = new GridBagConstraints();
		gbc_txtrTwoRunOptions.insets = new Insets(0, 0, 0, 5);
		gbc_txtrTwoRunOptions.gridwidth = 5;
		gbc_txtrTwoRunOptions.fill = GridBagConstraints.BOTH;
		gbc_txtrTwoRunOptions.gridx = 0;
		gbc_txtrTwoRunOptions.gridy = 10;
		subPanelTwo.add(txtrTwoRunOptions, gbc_txtrTwoRunOptions);
		
	}
}
