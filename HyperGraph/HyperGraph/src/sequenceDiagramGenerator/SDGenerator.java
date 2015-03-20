package sequenceDiagramGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.GroupableHyperEdge;
import sequenceDiagramGenerator.hypergraph.GroupableHyperNode;
import sequenceDiagramGenerator.hypergraph.HyperEdge;
import sequenceDiagramGenerator.hypergraph.HyperNode;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import sequenceDiagramGenerator.sdedit.SDMessage;
import sequenceDiagramGenerator.sdedit.SDObject;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;
import sequenceDiagramGenerator.sootAnalyzer.Analyzer;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.internal.JimpleLocalBox;

public class SDGenerator {
	
//	public static void Generate(List<String> listClasses, String ClassPath, String SaveFile){
//		Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(listClasses, ClassPath);
//
//		//SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
//	}
	
	//Given a pre-constructed hypergraph
	//and a hypernode at which to begin traversal
	//generate a sequence diagram at SaveFile
	public static void Generate(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			String SaveFile) throws Exception{

		SequenceDiagram sd = new SequenceDiagram();
		SDObject outerObject = new SDObject(aGNode.data.theMethod.getDeclaringClass(), SDObject.GetUniqueName(), false);
		sd.AddObject(outerObject);
		MakeArbitraryDiagram(hg, aGNode, sd, outerObject);
		//RecFillNodeDiagram(hg,aGNode, sd, SDObject.GetUniqueName());
		
		sd.CreatePDF(SaveFile);
	}
	
	private static void MakeArbitraryDiagram(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			SequenceDiagram sd,
			SDObject outerObject) throws Exception{
		if(aGNode == null){return;}
		List<TraceStatement> tstmts = aGNode.data.theTraces;
		if(tstmts == null || tstmts.size() == 0){return;}
		RecFillTraceStmtDiagram(hg,
				aGNode,
				tstmts.get(0),
				sd,
				outerObject);
	}
	
	private static void RecFillTraceStmtDiagram(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			TraceStatement aStmt,
			SequenceDiagram sd,
			SDObject sourceObj) throws Exception
	{
		if(aStmt == null){return;}
		
		//This is the point where I am currently attempting to handle
		//assignment statements.
		//This is not completed.
		if(aStmt.theStmt instanceof AssignStmt){
			AssignStmt assignStmt = (AssignStmt)aStmt.theStmt;
			JimpleLocal jlLeft = extractValue(assignStmt.getLeftOp());
			JimpleLocal jlRight = extractValue(assignStmt.getRightOp());
			
			if(jlLeft != null && jlRight != null){
				String leftName = jlLeft.getName();
				String rightName = jlRight.getName();
				SDObject sdRight = sd.GetObjectFromName(rightName);
				sd.AttachNameToObject(leftName, sdRight);
			}
			JNewExpr jne = extractNew(assignStmt.getRightOp());
			if(jlLeft != null && jne != null){
				String leftName = jlLeft.getName();
				SootClass sc = jne.getBaseType().getSootClass();
				SDObject newObj = new SDObject(sc, "", true);
				sd.AddObject(newObj);
				sd.AttachNameToObject(leftName, newObj);
				
				SDMessage creationMessage = new SDMessage(sourceObj, newObj);
				sd.AddMessage(creationMessage);
			}
		}
		//If a statement contains an invoke expression
		//that expression must be extracted, we must find
		//the relevant hyperedge out of the current node
		//and traverse it.
		if(aStmt.theStmt.containsInvokeExpr()){
			SootMethod calledMethod = aStmt.theStmt.getInvokeExpr().getMethod();
			GroupableHyperEdge<EdgeAnnotation> gEdge = aGNode.GetGroupableEdge(calledMethod);
			if(gEdge != null){
				GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> subGNode = 
						(GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>) hg.GetCompleteNode(gEdge.targetNode);
				
				//If an object doesn't have an instance name
				//(might be static)
				//this might not work, in which case
				//the initial assignment to getuniquename
				//must suffice for the name
				List<Value> lv = aStmt.theStmt.getInvokeExpr().getUseBoxes();
				
				String tarObjName = "";
				if(lv.size() > 0){
					Object v = lv.get(0);
					JimpleLocal jl = extractValue(v);
					tarObjName = jl.getName();
				}
				
				//grab the three things we need to write a 
				//message into sd, source, target, message
				
				SootMethod sm = subGNode.data.theMethod;
				SootClass scTarget = sm.getDeclaringClass();
				SootClass scSource = aGNode.data.theMethod.getDeclaringClass();
				
				//SDObjects for source and target are created with
				//two piece of information, class and instance name
				//note source's instance name is passed in from above.
				//target's is locally available to us.
				//SDObject sdSource = sd.GetObjectFromName(sourceName);
				SDObject sdTarget = sd.GetObjectFromName(tarObjName);
				
				//If there is already a matching SDObject in sd
				//this will silently fail and we will simply link
				//to the existing object.  This may need to change
				//per the assignment statement problem.
				if(sdTarget == null){
					if(tarObjName == null || tarObjName.length() == 0){
						tarObjName = SDObject.GetUniqueName();
					}
					sdTarget = new SDObject(scTarget, tarObjName, false);
					sd.AddObject(sdTarget);
				}
				
				//now that the sd has a source and target, we can
				//add the message.
				SDMessage msg = new SDMessage(sourceObj, sdTarget, sm);
				sd.AddMessage(msg);
				
				//now that we are "at" the destination point
				//of the message, we traverse into
				//the relevant hypernode for that new method.
				sd.PushNames();
				MakeArbitraryDiagram(hg, subGNode, sd, sdTarget);
				sd.PopNames();
			}
			else
			{
				//need to revisit this once assignment problem is cleared up
				//it may either be impossible or may represent calls like
				//int x = (new Random()).nextInt()
				//where we don't have soot data on calls to "external" libraries
			}
		}
		//after we've handled everything in this statement
		//including any calls and subsequent calls generated
		//we traverse to the next statement.
		RecFillTraceStmtDiagram(hg, aGNode, aStmt.theNext, sd, sourceObj);
	}
	
	//helper function to extract a local from a Value
	//Values may or may not contain locals
	//the statement
	//int x = 0 
	//will contain two values, x and 0
	//x will successfully cast to a jimplelocal in 
	//one format or another, 0 will not
	//so this method CAN return null.
	//in general, at the moment, we don't care about constants.
	private static JimpleLocal extractValue(Object v){
		Object obj = null;
		if(v instanceof JimpleLocalBox){
			JimpleLocalBox jlb = (JimpleLocalBox)v;
			obj = jlb.getValue();
		}
		else
		{
			obj = v;
		}
		if(obj instanceof JimpleLocal){
			JimpleLocal jl = (JimpleLocal)obj;
			return jl;
		}
		return null;
	}
	
	private static JNewExpr extractNew(Object v){
		if(v instanceof JNewExpr){
			JNewExpr ret = (JNewExpr)v;
			return ret;
		}
		return null;
	}
	
	//Simply begins traversing at the first statement
	//control will return here if an invoke statement is discovered.
	//In this way RecFillNodeDiagram and RecFillStmtDiagram 
	//are mutually recursive.
//	private static void RecFillNodeDiagram(
//			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
//			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
//			SequenceDiagram sd,
//			String sourceName) throws Exception{
//		if(aGNode == null){return;}
//		GroupableStmt aStmt = aGNode.data.theStmts;
//		RecFillStmtDiagram(hg, aGNode, aStmt, sd, sourceName);
//	}
	
	//traverses the statements in a single node 
	//in order to fill out the sequence diagram data structure
	//hg is the hypergraph we will traverse
	//aGNode is the current node in the recursive call
	//aStmt is the current statement.
	//sd is the data structure we are filling out as we traverse
	//sourceName is the string name of the instance
	//we are using as source.
	
	//Some object that is an instance in some outside location
	//has had this particular method called upon it, and 
	//we are examining that method.
	//As we are in the middle of examining that method,
	//we don't have local access to that outer instance name:
	//Car myToyota = new Car();
	//myToyota.drive();
	//if we are currently traversing the drive method of class car
	//the instance name "myToyota" is not locally available and must
	//be passed in from outside.
	
//	private static void RecFillStmtDiagram(
//			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
//			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
//			GroupableStmt aStmt,
//			SequenceDiagram sd,
//			String sourceName) throws Exception
//	{
//		if(aStmt == null){return;}
//		if(aStmt.theTrueBranch != null){
//			//There is work to do here actually adding the
//			//opening the if block in the sequence diagram
//			//or potentially loop if we are going to deal with that.
//			RecFillStmtDiagram(hg, aGNode, aStmt.theTrueBranch, sd, sourceName);
//			//and closing it here
//		}
//		if(aStmt.theFalseBranch != null){
//			//This also must be for an "else" block
//			RecFillStmtDiagram(hg, aGNode, aStmt.theFalseBranch, sd, sourceName);
//		}
//		//This is the point where I am currently attempting to handle
//		//assignment statements.
//		//This is not completed.
//		if(aStmt.theStmt instanceof AssignStmt){
//			AssignStmt assignStmt = (AssignStmt)aStmt.theStmt;
//			JimpleLocal jlLeft = extractValue(assignStmt.getLeftOp());
//			JimpleLocal jlRight = extractValue(assignStmt.getRightOp());
//			//variety of possible actions here.
//			//pretty sure something like Chris's dictionary idea
//			//will be necessary.
//		}
//		//If a statement contains an invoke expression
//		//that expression must be extracted, we must find
//		//the relevant hyperedge out of the current node
//		//and traverse it.
//		if(aStmt.theStmt.containsInvokeExpr()){
//			GroupableHyperEdge<EdgeAnnotation> gEdge = aGNode.GetGroupableEdge(aStmt);
//			if(gEdge != null){
//				GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> subGNode = 
//						(GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>) hg.GetCompleteNode(gEdge.targetNode);
//				
//				//If an object doesn't have an instance name
//				//(might be static)
//				//this might not work, in which case
//				//the initial assignment to getuniquename
//				//must suffice for the name
//				List<Value> lv = aStmt.theStmt.getInvokeExpr().getUseBoxes();
//				String tarObjName = SDObject.GetUniqueName();
//				if(lv.size() > 0){
//					JimpleLocal jl = extractValue(lv.get(0));
//					tarObjName = jl.getName();
//					
//				}
//				
//				//grab the three things we need to write a 
//				//message into sd, source, target, message
//				
//				SootMethod sm = subGNode.data.theMethod;
//				SootClass scTarget = sm.getDeclaringClass();
//				SootClass scSource = aGNode.data.theMethod.getDeclaringClass();
//				
//				//SDObjects for source and target are created with
//				//two piece of information, class and instance name
//				//note source's instance name is passed in from above.
//				//target's is locally available to us.
//				SDObject sdSource = new SDObject(scSource, sourceName);
//				SDObject sdTarget = new SDObject(scTarget, tarObjName);
//				
//				//If there is already a matching SDObject in sd
//				//this will silently fail and we will simply link
//				//to the existing object.  This may need to change
//				//per the assignment statement problem.
//				sd.AddObject(sdSource);
//				sd.AddObject(sdTarget);
//				
//				//now that the sd has a source and target, we can
//				//add the message.
//				SDMessage msg = new SDMessage(sdSource, sdTarget, sm);
//				sd.AddMessage(msg);
//				
//				//now that we are "at" the destination point
//				//of the message, we traverse into
//				//the relevant hypernode for that new method.
//				RecFillNodeDiagram(hg, subGNode, sd, tarObjName);
//			}
//			else
//			{
//				//need to revisit this once assignment problem is cleared up
//				//it may either be impossible or may represent calls like
//				//int x = (new Random()).nextInt()
//				//where we don't have soot data on calls to "external" libraries
//			}
//		}
//		//after we've handled everything in this statement
//		//including any calls and subsequent calls generated
//		//we traverse to the next statement.
//		GroupableHyperEdge<EdgeAnnotation> gEdge = null;
//		GroupableStmt aGStmt = aStmt.theNext;
//		RecFillStmtDiagram(hg, aGNode, aGStmt, sd, sourceName);
//	}
	
//	public static void GenerateNaiveSequenceDiagram(
//	Hypergraph<SourceCodeType, EdgeAnnotation> aGraph,
//	String outFile){
//List<HyperEdge<EdgeAnnotation>> lEdges = aGraph.GetAllEdges();
//SequenceDiagram sd = new SequenceDiagram();
//
//
//for(int i = 0; i < lEdges.size(); i++){
//	HyperEdge<EdgeAnnotation> aEdge = lEdges.get(i);
//	ArrayList<SourceCodeType> listSource = new ArrayList<SourceCodeType>();
//	for(int j = 0; j < aEdge.sourceNodes.size(); j++){
//		SourceCodeType sct  = aGraph.GetNode(aEdge.sourceNodes.get(j));
//		listSource.add(sct);
//	}
//	SourceCodeType tar = aGraph.GetNode(aEdge.targetNode);
//	DiagramMessageAggregator dma = new DiagramMessageAggregator(
//			listSource,
//			tar,
//			aEdge.annotation.getMethodName()
//			);
//
//	for(SDObject sdo : dma.GetSDObjects()){
//		sd.AddObject(sdo);
//	}
//	sd.AddMessage(dma.GenerateSDEditMessage());
//}
//sd.CreatePDF(outFile);
///*try {
//	FileWriter fw = new FileWriter(outFile);
//	fw.write(sd.toString());
//	fw.close();
//} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}*/
//
//}
//	public static void GenerateNaiveSequenceDiagram(
//	Hypergraph<SourceCodeType, EdgeAnnotation> aGraph,
//	String outFile){
//List<HyperEdge<EdgeAnnotation>> lEdges = aGraph.GetAllEdges();
//SequenceDiagram sd = new SequenceDiagram();
//
//
//for(int i = 0; i < lEdges.size(); i++){
//	HyperEdge<EdgeAnnotation> aEdge = lEdges.get(i);
//	ArrayList<SourceCodeType> listSource = new ArrayList<SourceCodeType>();
//	for(int j = 0; j < aEdge.sourceNodes.size(); j++){
//		SourceCodeType sct  = aGraph.GetNode(aEdge.sourceNodes.get(j));
//		listSource.add(sct);
//	}
//	SourceCodeType tar = aGraph.GetNode(aEdge.targetNode);
//	DiagramMessageAggregator dma = new DiagramMessageAggregator(
//			listSource,
//			tar,
//			aEdge.annotation.getMethodName()
//			);
//
//	for(SDObject sdo : dma.GetSDObjects()){
//		sd.AddObject(sdo);
//	}
//	sd.AddMessage(dma.GenerateSDEditMessage());
//}
//sd.CreatePDF(outFile);
///*try {
//	FileWriter fw = new FileWriter(outFile);
//	fw.write(sd.toString());
//	fw.close();
//} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}*/
//
//}

//public static void Generate(String ClassName, String ClassDir, String ClassPath, String SaveFile){
//Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(ClassName, ClassDir, ClassPath);
//
////SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
//
//}

//public static void Generate(List<String> listClasses, String ClassPath, String SaveFile){
//Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(listClasses, ClassPath);
//
////SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
//}
//public static void Generate(String ClassName, String ClassDir, String ClassPath, String SaveFile){
//Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(ClassName, ClassDir, ClassPath);
//
////SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
//
//}

//public static void Generate(List<String> listClasses, String ClassPath, String SaveFile){
//Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(listClasses, ClassPath);
//
////SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
//}
}
