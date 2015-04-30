package sequenceDiagramGenerator.UI;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import sequenceDiagramGenerator.MethodNodeAnnot;
import sequenceDiagramGenerator.Query;
import sequenceDiagramGenerator.SDGenerator;
import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.GroupableHyperNode;
import sequenceDiagramGenerator.hypergraph.GroupableHypergraph;
import sequenceDiagramGenerator.hypergraph.HyperNode;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;
import sequenceDiagramGenerator.sootAnalyzer.Analyzer;
import soot.SootClass;
import soot.SootMethod;
import utilities.Utilities;

public class TestUI implements ActionListener{

	//all the interesting code here is in
	//actionPerformed.
	private JFrame frame;
	private ControlPanel theControlPanel;
	
	public Hypergraph<MethodNodeAnnot, EdgeAnnotation> currentHypergraph;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try{
			if(args == null || args.length == 0){
				RunUI();
			}
			else
			{
				RunAnyCommandLine(args);
			}
		}
		catch(Exception ex){
			Utilities.DebugPrintln("Top level crash:");
			Utilities.DebugPrintln(ex.getMessage());
		}
		Utilities.cleanup();
	}
	private static void RunAnyCommandLine(String[] args){
		String debugFile = GetArgument(args, "-debugfile");
		if(debugFile != null && !debugFile.equals("")){
			Utilities.SetDebugFile(debugFile);
		}
		if(args[0].equals("-c")){
			String queryFile = GetArgument(args, "-queryfile");
			Query q = Query.FromFile(queryFile);
			RunCommandLine(args, q);
		}
		else if(args[0].equals("-t")){
			String queryFile = GetArgument(args, "-queryfile");
			Query q = Query.FromFile(queryFile);
			RunTest(args, q);
		}
		else if(args[0].equals("-a")){
			String queryFile = GetArgument(args, "-queryfile");
			Query q = Query.FromFile(queryFile);
			RunAllOneFunction(args, q);
		}
		else if(args[0].equals("-aa")){
			String queryFile = GetArgument(args, "-queryfile");
			Query q = Query.FromFile(queryFile);
			RunAllAllFunctions(args, q);
		}
		else if(args[0].equals("-td")){
			RunADiagram(args);
		}
		else
		{ 
			System.out.println("Bad Input Arg 0 =" + args[0]);
		}
			
	}
	
	private static void RunADiagram(String[] args){
		String fileName = GetArgument(args, "-filename");
		String fileContents = Utilities.ReadEntireFile(fileName);
		String outFile = GetArgument(args, "-outfile");
		SequenceDiagram.MakePDFFromSDEdit(fileContents, outFile);
	}
	
	private static void RunTest(String[] args, Query byQuery){
		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg = null;
		String ClassPath = GetArgument(args, "-classpath");
		String Files = GetArgument(args, "-jars");
		String[] SplitFiles = Files.split(";");
		File[] jars = new File[SplitFiles.length];
		for(int i = 0; i < jars.length; i++){
			jars[i] = new File(SplitFiles[i]);
		}
		List<String> listClasses = new ArrayList<String>();
		try {
			for(int i = 0; i < jars.length; i++){
				listClasses.addAll(Utilities.ListClassesInJar(jars[i]));
				String parentDir;
					parentDir = jars[i].getCanonicalPath();
				ClassPath = ClassPath + Utilities.GetClassPathDelim() + parentDir;
			}
			hg = Analyzer.AnalyzeFromJAR(listClasses, ClassPath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(hg == null){
			System.out.println("Could not generate hypergraph");
			return;
		}
		
		String startMethod = GetArgument(args, "-startmethod");
		
		GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode = hg.GetNodeByName(startMethod);
		if(aNode == null){
			System.out.println("Could not find node by name: " + startMethod);
			return;
		}
		String saveFile = GetArgument(args, "-outfile");
		
		if(saveFile == null || saveFile.length() == 0){
			System.out.println("outfile not specified");
			return;
		}
		
		File aFile = new File(saveFile);
		if(aFile.exists()){
			aFile.delete();
		}
		
		try {
			//this is the interesting call.
			//SDGenerator.Generate(hg, aNode, saveFile);
			SDGenerator.GenTest(hg, aNode, saveFile, byQuery);
		} catch (Exception e1) {
			// TODO Auto-generated catch block				
			e1.printStackTrace();
		}
	}
	
	private static void RunCommandLine(String[] args, Query q){
		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg = null;
		String ClassPath = GetArgument(args, "-classpath");
		String Files = GetArgument(args, "-jars");
		String[] SplitFiles = Files.split(";");
		File[] jars = new File[SplitFiles.length];
		for(int i = 0; i < jars.length; i++){
			jars[i] = new File(SplitFiles[i]);
		}
		List<String> listClasses = new ArrayList<String>();
		try {
			for(int i = 0; i < jars.length; i++){
				listClasses.addAll(Utilities.ListClassesInJar(jars[i]));
				String parentDir;
					parentDir = jars[i].getCanonicalPath();
				ClassPath = ClassPath + Utilities.GetClassPathDelim() + parentDir;
			}
			hg = Analyzer.AnalyzeFromJAR(listClasses, ClassPath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(hg == null){
			System.out.println("Could not generate hypergraph");
			return;
		}

		if(Utilities.DEBUG){
			Utilities.DebugPrintln("*********METHODS**********");
			List<HyperNode<MethodNodeAnnot,EdgeAnnotation>> lh = hg.GetNodes();
			for(int i = 0; i < lh.size(); i++){
				SootMethod sm = lh.get(i).data.theMethod;
				String mName = Utilities.getMethodString(sm);
				if(mName.startsWith("org.adblockplus")){
					Utilities.DebugPrintln(mName);
				}
			}
		}
		String startMethod = GetArgument(args, "-startmethod");
		
		GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode = hg.GetNodeByName(startMethod);
		if(aNode == null){
			System.out.println("Could not find node by name: " + startMethod);
			return;
		}
		String saveFile = GetArgument(args, "-outfile");
		
		if(saveFile == null || saveFile.length() == 0){
			System.out.println("outfile not specified");
			return;
		}
		
		File aFile = new File(saveFile);
		if(aFile.exists()){
			aFile.delete();
		}
		
		try {
			//this is the interesting call.
			SDGenerator.Generate(hg, aNode, saveFile, q);
		} catch (Exception e1) {
			// TODO Auto-generated catch block				
			e1.printStackTrace();
		}
	}
	
	private static void RunAllOneFunction(String[] args, Query q){
		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg = null;
		String ClassPath = GetArgument(args, "-classpath");
		String Files = GetArgument(args, "-jars");
		String[] SplitFiles = Files.split(";");
		File[] jars = new File[SplitFiles.length];
		for(int i = 0; i < jars.length; i++){
			jars[i] = new File(SplitFiles[i]);
		}
		List<String> listClasses = new ArrayList<String>();
		try {
			for(int i = 0; i < jars.length; i++){
				listClasses.addAll(Utilities.ListClassesInJar(jars[i]));
				String parentDir;
					parentDir = jars[i].getCanonicalPath();
				ClassPath = ClassPath + Utilities.GetClassPathDelim() + parentDir;
			}
			hg = Analyzer.AnalyzeFromJAR(listClasses, ClassPath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(hg == null){
			System.out.println("Could not generate hypergraph");
			return;
		}
		if(Utilities.DEBUG){
			Utilities.DebugPrintln("*********METHODS**********");
			List<HyperNode<MethodNodeAnnot,EdgeAnnotation>> lh = hg.GetNodes();
			for(int i = 0; i < lh.size(); i++){
				SootMethod sm = lh.get(i).data.theMethod;
				String mName = Utilities.getMethodString(sm);
				Utilities.DebugPrintln(mName);
			}
		}
		
		String startMethod = GetArgument(args, "-startmethod");
		
		GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode = hg.GetNodeByName(startMethod);
		if(aNode == null){
			System.out.println("Could not find node by name: " + startMethod);
			return;
		}
		String saveDir = GetArgument(args, "-outdir");
		
		if(saveDir == null || saveDir.length() == 0){
			System.out.println("outdir not specified");
			return;
		}
		
		File aFile = new File(saveDir);
		if(!aFile.isDirectory()){
			System.out.println("outdir not directory");
		}
		
		try {
			//this is the interesting call.
			SDGenerator.GenerateAll(hg, aNode, saveDir, q);
		} catch (Exception e1) {
			// TODO Auto-generated catch block				
			e1.printStackTrace();
		}
	}
	
	private static void RunAllAllFunctions(String[] args, Query q){
		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg = null;
		String ClassPath = GetArgument(args, "-classpath");
		String Files = GetArgument(args, "-jars");
		String[] SplitFiles = Files.split(";");
		File[] jars = new File[SplitFiles.length];
		for(int i = 0; i < jars.length; i++){
			jars[i] = new File(SplitFiles[i]);
		}
		List<String> listClasses = new ArrayList<String>();
		try {
			for(int i = 0; i < jars.length; i++){
				listClasses.addAll(Utilities.ListClassesInJar(jars[i]));
				String parentDir;
					parentDir = jars[i].getCanonicalPath();
				ClassPath = ClassPath + Utilities.GetClassPathDelim() + parentDir;
			}
			hg = Analyzer.AnalyzeFromJAR(listClasses, ClassPath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(hg == null){
			System.out.println("Could not generate hypergraph");
			return;
		}

		String startswith = GetArgument(args, "-startswith");
		List<String> listFuncsToRun = new ArrayList<String>();
		List<HyperNode<MethodNodeAnnot,EdgeAnnotation>> lh = hg.GetNodes();
		for(int i = 0; i < lh.size(); i++){
			SootMethod sm = lh.get(i).data.theMethod;
			String mName = Utilities.getMethodString(sm);
			if(mName.startsWith(startswith)){
				listFuncsToRun.add(mName);
			}
		}
		String saveDir = GetArgument(args, "-outdir");

		if(saveDir == null || saveDir.length() == 0){
			System.out.println("outdir not specified");
			return;
		}
		File aFile = new File(saveDir);
		if(!aFile.isDirectory()){
			System.out.println("outdir not directory");
		}
		for(int i = 0; i < listFuncsToRun.size(); i++){
			
			String startMethod = listFuncsToRun.get(i);
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode = hg.GetNodeByName(startMethod);
			if(aNode == null){
				System.out.println("Could not find node by name: " + startMethod);
				continue;
			}
			
			File aSubDir = new File(Utilities.endWithSlash(saveDir) + 
					Utilities.firstFiveLetters(listFuncsToRun.get(i)) + 
					String.valueOf(i));
			if(aSubDir.exists()){
				Utilities.deleteDirectory(aSubDir);
			}
			aSubDir.mkdirs();
			try {
				//this is the interesting call.
				SDGenerator.GenerateAll(hg, aNode, aSubDir.getAbsolutePath(), q);
			} catch (Exception e1) {
				// TODO Auto-generated catch block				
				e1.printStackTrace();
			}
			Utilities.cleanUpDir(aSubDir);
		}
	}
	private static void RunUI(){
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
	
	private static String GetArgument(String[] args, String tag){
		for(int i = 0; i < args.length-1; i++){
			if(args[i].equals(tag)){
				return args[i+1];
			}
		}
		return "";
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
				SootMethod sm = lh.get(i).data.theMethod;
				SootClass sc = sm.getDeclaringClass();
				String mName = sc.getName() + "." + sm.getName();
				CmbBoxItem cbi = new CmbBoxItem(lh.get(i), mName);
				theControlPanel.cmbFunctions.addItem(cbi);
			}
		}
	}
	
	//actionPerformed is called
	//when any button is clicked in the UI
	//each button is tagged with an 
	//actioncommand, and that translates to 
	//a different if block in this function.
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Analyze")){
			String ClassName = theControlPanel.tfClassName.getText();
			String ClassPath = theControlPanel.tfClassPath.getText();
			String ClassDir = theControlPanel.tfClassDir.getText();
			
			currentHypergraph = Analyzer.AnalyzeFromSpecificClasses(ClassName, ClassDir, ClassPath);
			PopulateCmb();
		}
		if(e.getActionCommand().equals("LoadJar")){
			//generate a hypergraph, store it in currentHypergraph
			
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
						ClassPath = ClassPath + Utilities.GetClassPathDelim() + parentDir;
					}
					currentHypergraph = Analyzer.AnalyzeFromJAR(listClasses, ClassPath);
					PopulateCmb();

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		if(e.getActionCommand().equals("SaveFile")){
			//just opens a window to choose a file from.
			JFileChooser fc = new JFileChooser();

			if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
				this.theControlPanel.tfSaveFile.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}
		if(e.getActionCommand().equals("MakeSequenceDiagram")){
			//using a generated hypergraph in currentHypergraph
			//and a function chosen from the combobox
			//and a filename to save to
			//generate a sequence diagram.
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
				//this is the interesting call.
				SDGenerator.Generate(currentHypergraph, aNode, saveFile, new Query(""));
			} catch (Exception e1) {
				// TODO Auto-generated catch block				
				e1.printStackTrace();
			}
		}
	}

}
