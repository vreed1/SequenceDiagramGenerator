package sequenceDiagramGenerator.UI;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sequenceDiagramGenerator.MethodNodeAnnot;
import sequenceDiagramGenerator.Query;
import sequenceDiagramGenerator.QueryFactory;
import sequenceDiagramGenerator.SimpleQuery;
import sequenceDiagramGenerator.Query.QueryResponse;
import sequenceDiagramGenerator.SDGenerator;
import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.GroupableHyperNode;
import sequenceDiagramGenerator.hypergraph.GroupableHypergraph;
import sequenceDiagramGenerator.hypergraph.HyperNode;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import sequenceDiagramGenerator.sdedit.DiagramPDFGen;
import sequenceDiagramGenerator.sdedit.GenReducer;
import sequenceDiagramGenerator.sdedit.GenReducerFactory;
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
			ex.printStackTrace();
		}
		Utilities.cleanup();
	}
	private static void RunAnyCommandLine(String[] args){
		String debugFile = Utilities.GetArgument(args, "-debugfile");
		QueryFactory qf = new QueryFactory(args);
		String queryFile = Utilities.GetArgument(args, "-queryfile");
		Query q = qf.FromFileName(queryFile);
		if(debugFile != null && !debugFile.equals("")){
			Utilities.SetDebugFile(debugFile);
		}
		String perfFile = Utilities.GetArgument(args, "-perffile");
		if(perfFile != null && !perfFile.equals("")){
			Utilities.SetPerfFile(perfFile);
		}
		if(args[0].equals("-c")){
			RunCommandLine(args, q);
		}
		else if(args[0].equals("-t")){
			RunTest(args, q);
		}
		else if(args[0].equals("-a")){
			RunAllOneFunction(args, q);
		}
		else if(args[0].equals("-aa")){
			RunAllAllFunctions(args, q);
		}
		else if(args[0].equals("-td")){
			RunADiagram(args);
		}
		else if(args[0].equals("-gj")){
			RunADiagramFromJSON(args);
		}
		else if(args[0].equals("-jd")){
			RunJSONDirectory(args, q);
		}
		else
		{ 
			System.out.println("Bad Input Arg 0 =" + args[0]);
		}
			
	}
	
	private static void RunJSONDirectory(String[] args, Query q){
		String jsonInputDir = Utilities.GetArgument(args, "-jsoninputdir");
		File dir = new File(jsonInputDir);
		List<SequenceDiagram> lsd = new ArrayList<SequenceDiagram>();
		File[] files = dir.listFiles();
		
		for(int i = 0; i < files.length; i++ ){
			if(files[i].getName().endsWith(".json")){
				FileReader fr = null;
				try {
					JSONParser jp = new JSONParser();
					fr = new FileReader(files[i]);
					JSONObject jobj = (JSONObject) jp.parse(fr);
					SequenceDiagram sd = new SequenceDiagram(jobj);
					QueryResponse qr = q.CheckFinishedDiagram(sd);
					if(qr == QueryResponse.True){
						lsd.add(sd);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally{
					if(fr != null){
						try {
							fr.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						fr = null;
					}
				}
			}
		}
		String outdir = Utilities.GetArgument(args, "-outdir");
		GenReducer gr = GenReducerFactory.Build(args);
		DiagramPDFGen dpg = new DiagramPDFGen(lsd, gr,args);
		dpg.CreatePDFs(outdir);
		
	}
	
	private static void RunADiagramFromJSON(String[] args){
		try {
			String jsonFile = Utilities.GetArgument(args, "-jsonfile");
			String pdfFile = Utilities.GetArgument(args, "-pdffile");
			JSONParser jp = new JSONParser();
			FileReader fr = new FileReader(jsonFile);
			JSONObject jobj = (JSONObject) jp.parse(fr);
			SequenceDiagram sd = new SequenceDiagram(jobj);
			String tersemode = Utilities.GetArgument(args, "-terse");
			boolean terse = false;
			if(tersemode == null && tersemode.length() > 0){
				terse = Boolean.parseBoolean(tersemode);
			}
			sd.CreatePDF(pdfFile, terse);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void RunADiagram(String[] args){
		String fileName = Utilities.GetArgument(args, "-filename");
		String fileContents = Utilities.ReadEntireFile(fileName);
		String outFile = Utilities.GetArgument(args, "-outfile");
		SequenceDiagram.MakePDFFromSDEdit(fileContents, outFile);
	}
	
	private static void RunTest(String[] args, Query q){
		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg = null;
		String ClassPath = Utilities.GetArgument(args, "-classpath");
		String Files = Utilities.GetArgument(args, "-jars");
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
		
		String startMethod = Utilities.GetArgument(args, "-startmethod");
		
		GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode = hg.GetNodeByName(startMethod);
		if(aNode == null){
			System.out.println("Could not find node by name: " + startMethod);
			return;
		}
		String saveFile = Utilities.GetArgument(args, "-outfile");
		
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
			SDGenerator.GenTest(hg, aNode, saveFile, q);
		} catch (Exception e1) {
			// TODO Auto-generated catch block				
			e1.printStackTrace();
		}
	}
	
	private static void RunCommandLine(String[] args, Query q){
		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg = null;
		String ClassPath = Utilities.GetArgument(args, "-classpath");
		String Files = Utilities.GetArgument(args, "-jars");
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
				SootMethod sm = lh.get(i).data.GetMethod();
				String mName = Utilities.getMethodString(sm);
				if(mName.startsWith("org.adblockplus")){
					Utilities.DebugPrintln(mName);
				}
			}
		}
		String startMethod = Utilities.GetArgument(args, "-startmethod");
		
		GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode = hg.GetNodeByName(startMethod);
		if(aNode == null){
			System.out.println("Could not find node by name: " + startMethod);
			return;
		}
		String saveFile = Utilities.GetArgument(args, "-outfile");
		
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
			SequenceDiagram sd = SDGenerator.Generate(hg, aNode, q);
			String tersemode = Utilities.GetArgument(args, "-terse");
			boolean terse = false;
			if(tersemode == null && tersemode.length() > 0){
				terse = Boolean.parseBoolean(tersemode);
			}
			sd.CreatePDF(saveFile, terse);
		} catch (Exception e1) {
			// TODO Auto-generated catch block				
			e1.printStackTrace();
		}
	}
	
	private static void RunAllOneFunction(String[] args, Query q){
		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg = null;
		String ClassPath = Utilities.GetArgument(args, "-classpath");
		String Files = Utilities.GetArgument(args, "-jars");
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
				SootMethod sm = lh.get(i).data.GetMethod();
				String mName = Utilities.getMethodString(sm);
				Utilities.DebugPrintln(mName);
			}
		}
		
		String startMethod = Utilities.GetArgument(args, "-startmethod");
		
		GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode = hg.GetNodeByName(startMethod);
		if(aNode == null){
			System.out.println("Could not find node by name: " + startMethod);
			return;
		}
		String saveDir = Utilities.GetArgument(args, "-outdir");
		
		if(saveDir == null || saveDir.length() == 0){
			System.out.println("outdir not specified");
			return;
		}
		
		File aFile = new File(saveDir);
		if(!aFile.isDirectory()){
			System.out.println("outdir not directory");
		}
		
		List<SequenceDiagram> listD = null;
		try {
			//this is the interesting call.
			listD = SDGenerator.GenerateAll(hg, aNode, q);
		} catch (Exception e1) {
			// TODO Auto-generated catch block				
			e1.printStackTrace();
		}
		
		GenReducer gr = GenReducerFactory.Build(args);
		DiagramPDFGen dpg = new DiagramPDFGen(listD, gr,args);
		dpg.CreatePDFs(saveDir);
	}
	
	private static void RunAllAllFunctions(String[] args, Query q){
		
		Utilities.PerfLogPrintln("Start_RunAllAllFunctions," + Long.toString(System.nanoTime()));
		
		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg = null;
		String ClassPath = Utilities.GetArgument(args, "-classpath");
		String Files = Utilities.GetArgument(args, "-jars");
		String[] SplitFiles = Files.split(";");
		File[] jars = new File[SplitFiles.length];
		for(int i = 0; i < jars.length; i++){
			jars[i] = new File(SplitFiles[i]);
		}
		List<String> listClasses = new ArrayList<String>();
		Utilities.PerfLogPrintln("BeforeHyperGraph_RunAllAllFunctions," + Long.toString(System.nanoTime()));

		String startswith = Utilities.GetArgument(args, "-startswith");
		
		try {
			for(int i = 0; i < jars.length; i++){
				List<String> unfilteredClasses = Utilities.ListClassesInJar(jars[i]);
				for(int j = 0; j < unfilteredClasses.size(); j++){
					if(unfilteredClasses.get(j).startsWith(startswith)){
						listClasses.add(unfilteredClasses.get(j));
					}
				}
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
		Utilities.PerfLogPrintln("AfterHyperGraph_RunAllAllFunctions," + Long.toString(System.nanoTime()));
		
		Utilities.DebugPrintln("HG-NODECOUNT:"+Integer.toString(hg.size()));
		Utilities.DebugPrintln("HG-EDGECOUNT:"+Integer.toString(hg.EdgeCount()));
		
		List<String> listFuncsToRun = new ArrayList<String>();
		List<HyperNode<MethodNodeAnnot,EdgeAnnotation>> lh = hg.GetNodes();
		for(int i = 0; i < lh.size(); i++){
			SootMethod sm = lh.get(i).data.GetMethod();
			String mName = Utilities.getMethodString(sm);
			if(mName.startsWith(startswith)){
				listFuncsToRun.add(mName);
			}
		}
		String saveDir = Utilities.GetArgument(args, "-outdir");

		saveDir = saveDir + "temp";
		if(saveDir == null || saveDir.length() == 0){
			System.out.println("outdir not specified");
			return;
		}
		File aFile = new File(saveDir);
		if(!aFile.exists()){
			aFile.mkdir();
		}
		if(!aFile.isDirectory()){
			System.out.println("outdir not directory");
		}
		
		Utilities.PerfLogPrintln("BeforeTraversalJarAnalysis_RunAllAllFunctions," + Long.toString(System.nanoTime()));
		
		for(int i = 0; i < listFuncsToRun.size(); i++){

			Utilities.DebugPrintln("Starting Method #" + Integer.toString(i));
			String startMethod = listFuncsToRun.get(i);
			Utilities.DebugPrintln("    Name: "+ startMethod);
					
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode = hg.GetNodeByName(startMethod);
			if(aNode == null){
				System.out.println("Could not find node by name: " + startMethod);
				continue;
			}
			
//			File aSubDir = new File(Utilities.endWithSlash(saveDir) + 
//					Utilities.Truncate(aNode.data.GetMethod().getName()) + 
//					String.valueOf(i));
//			if(aSubDir.exists()){
//				Utilities.deleteDirectory(aSubDir);
//			}
//			aSubDir.mkdirs();
			try {
				//this is the interesting call.
				List<SequenceDiagram> listD = SDGenerator.GenerateAll(hg, aNode, q);
				GenReducer gr = GenReducerFactory.Build(args);
				DiagramPDFGen dpg = new DiagramPDFGen(listD, gr,args);
				dpg.CreatePDFs(saveDir);
			} catch (Exception e1) {
				// TODO Auto-generated catch block				
				//e1.printStackTrace();
				Utilities.DebugPrintln("Crash Generating #" + Integer.toString(i));
				Utilities.DebugPrintln(e1.getMessage());
				Utilities.DebugPrintln(e1.getStackTrace().toString());
			}
			//Utilities.cleanUpDir(aSubDir);
		}

		
		Utilities.PerfLogPrintln("AfterTraversalJarAnalysis_RunAllAllFunctions," + Long.toString(System.nanoTime()));
		
		
		File dir = new File(saveDir);
		List<SequenceDiagram> lsd = new ArrayList<SequenceDiagram>();
		File[] files = dir.listFiles();
		
		for(int i = 0; i < files.length; i++ ){
			if(files[i].getName().endsWith(".json")){
				FileReader fr = null;
				try {
					JSONParser jp = new JSONParser();
					fr = new FileReader(files[i]);
					JSONObject jobj = (JSONObject) jp.parse(fr);
					SequenceDiagram sd = new SequenceDiagram(jobj);
					QueryResponse qr = q.CheckFinishedDiagram(sd);
					if(qr == QueryResponse.True){
						lsd.add(sd);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally{
					if(fr != null){
						try {
							fr.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						fr = null;
					}
				}
			}
		}
		String outdir = Utilities.GetArgument(args, "-outdir");
		GenReducer gr = GenReducerFactory.Build(args);
		DiagramPDFGen dpg = new DiagramPDFGen(lsd, gr,args);
		dpg.CreatePDFs(outdir);
		
		Utilities.PerfLogPrintln("AfterQueryReduction_RunAllAllFunctions," + Long.toString(System.nanoTime()));
		
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
				SootMethod sm = lh.get(i).data.GetMethod();
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
				SequenceDiagram sd = SDGenerator.Generate(currentHypergraph, aNode, new SimpleQuery(""));

				sd.CreatePDF(saveFile, false);
			} catch (Exception e1) {
				// TODO Auto-generated catch block				
				e1.printStackTrace();
			}
		}
	}

}
