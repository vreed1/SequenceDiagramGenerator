package sequenceDiagramGenerator.UI;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import sequenceDiagramGenerator.MethodNodeAnnot;
import sequenceDiagramGenerator.SDGenerator;
import sequenceDiagramGenerator.SourceCodeType;
import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.GroupableHyperNode;
import sequenceDiagramGenerator.hypergraph.HyperNode;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import sequenceDiagramGenerator.sootAnalyzer.Analyzer;
import soot.SootClass;
import soot.SootMethod;
import utilities.Utilities;

public class TestUI implements ActionListener{

	private JFrame frame;
	private ControlPanel theControlPanel;
	
	public Hypergraph<MethodNodeAnnot, EdgeAnnotation> currentHypergraph;

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
		frame.setBounds(100, 100, 450, 385);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		theControlPanel = new ControlPanel(this);
		
		frame.getContentPane().add(theControlPanel, BorderLayout.NORTH);
		
	}
	
	private void PopulateCmb(){
		theControlPanel.cmbFunctions.removeAllItems();
		if(currentHypergraph != null){
			List<HyperNode<MethodNodeAnnot,EdgeAnnotation>> lh = currentHypergraph.GetNodes();
			for(int i = 0; i < lh.size(); i++){
				//lh.get(i).data.
				//STARTHERE, still in progress.
				SootMethod sm = lh.get(i).data.theMethod;
				SootClass sc = sm.getDeclaringClass();
				String mName = sc.getName() + "." + sm.getName();
				CmbBoxItem cbi = new CmbBoxItem(lh.get(i), mName);
				theControlPanel.cmbFunctions.addItem(cbi);
			}
		}
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Analyze")){
			String ClassName = theControlPanel.tfClassName.getText();
			String ClassPath = theControlPanel.tfClassPath.getText();
			String ClassDir = theControlPanel.tfClassDir.getText();
			
			//SDGenerator.Generate(ClassName, ClassDir, ClassPath, this.theControlPanel.tfSaveFile.getText());
			currentHypergraph = Analyzer.AnalyzeSpecificClasses(ClassName, ClassDir, ClassPath);
			PopulateCmb();
		}
		if(e.getActionCommand().equals("LoadJar")){
			
			
			JFileChooser fc = new JFileChooser();

			if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
				String ClassPath = theControlPanel.tfClassPath.getText();
				File[] jars = fc.getSelectedFiles();
				if(jars.length == 0){
					File fJar = fc.getSelectedFile();
					jars = new File[]{fJar};
				}
				List<String> listClasses = new ArrayList<String>();
				try {
					for(int i = 0; i < jars.length; i++){
						listClasses.addAll(Utilities.ListClassesInJar(jars[i]));
						String parentDir;
							parentDir = jars[i].getCanonicalPath();
						ClassPath = ClassPath + ":" + parentDir;
					}
					currentHypergraph = Analyzer.AnalyzeSpecificClasses(listClasses, ClassPath);
					PopulateCmb();

					//SDGenerator.Generate(listClasses, ClassPath, this.theControlPanel.tfSaveFile.getText());
				
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		if(e.getActionCommand().equals("SaveFile")){

			JFileChooser fc = new JFileChooser();

			if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
				this.theControlPanel.tfSaveFile.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}
		if(e.getActionCommand().equals("MakeSequenceDiagram")){
			if(currentHypergraph == null){return;}
			CmbBoxItem cbi = (CmbBoxItem) this.theControlPanel.cmbFunctions.getSelectedItem();
			if(cbi == null){return;}
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode = (GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>) cbi.theObject;
			if(aNode == null){return;}
			String saveFile = this.theControlPanel.tfSaveFile.getText();
			
			if(saveFile == null){return;}
			if(saveFile.length() == 0){return;}
			
			File aFile = new File(saveFile);
			if(aFile.exists()){
				aFile.delete();
			}
			
			try {
				SDGenerator.Generate(currentHypergraph, aNode, saveFile);
			} catch (Exception e1) {
				// TODO Auto-generated catch block				
				e1.printStackTrace();
			}
		}
	}

}
