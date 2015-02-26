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
import soot.jimple.internal.JimpleLocal;
import soot.jimple.internal.JimpleLocalBox;

public class SDGenerator {
//	public static void GenerateNaiveSequenceDiagram(
//			Hypergraph<SourceCodeType, EdgeAnnotation> aGraph,
//			String outFile){
//		List<HyperEdge<EdgeAnnotation>> lEdges = aGraph.GetAllEdges();
//		SequenceDiagram sd = new SequenceDiagram();
//		
//		
//		for(int i = 0; i < lEdges.size(); i++){
//			HyperEdge<EdgeAnnotation> aEdge = lEdges.get(i);
//			ArrayList<SourceCodeType> listSource = new ArrayList<SourceCodeType>();
//			for(int j = 0; j < aEdge.sourceNodes.size(); j++){
//				SourceCodeType sct  = aGraph.GetNode(aEdge.sourceNodes.get(j));
//				listSource.add(sct);
//			}
//			SourceCodeType tar = aGraph.GetNode(aEdge.targetNode);
//			DiagramMessageAggregator dma = new DiagramMessageAggregator(
//					listSource,
//					tar,
//					aEdge.annotation.getMethodName()
//					);
//		
//			for(SDObject sdo : dma.GetSDObjects()){
//				sd.AddObject(sdo);
//			}
//			sd.AddMessage(dma.GenerateSDEditMessage());
//		}
//		sd.CreatePDF(outFile);
//		/*try {
//			FileWriter fw = new FileWriter(outFile);
//			fw.write(sd.toString());
//			fw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}*/
//		
//	}
	
	public static void Generate(String ClassName, String ClassDir, String ClassPath, String SaveFile){
		Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(ClassName, ClassDir, ClassPath);

		//SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
		
	}
	
	public static void Generate(List<String> listClasses, String ClassPath, String SaveFile){
		Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(listClasses, ClassPath);

		//SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
	}
	
	public static void Generate(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			String SaveFile) throws Exception{

		SequenceDiagram sd = new SequenceDiagram();
		RecFillNodeDiagram(hg,aGNode, sd, SDObject.GetUniqueName());
		
		sd.CreatePDF(SaveFile);
	}
	
	private static void RecFillStmtDiagram(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			GroupableStmt aStmt,
			SequenceDiagram sd,
			String sourceName) throws Exception
	{
		if(aStmt == null){return;}
		if(aStmt.theTrueBranch != null){
			//worry about this later
			//SD stuff will have to be updated to 
			//deal with if/else blocks
			RecFillStmtDiagram(hg, aGNode, aStmt.theTrueBranch, sd, sourceName);
		}
		if(aStmt.theFalseBranch != null){
			//This also must be fixed.
			RecFillStmtDiagram(hg, aGNode, aStmt.theFalseBranch, sd, sourceName);
		}
		if(aStmt.theStmt.containsInvokeExpr()){
			GroupableHyperEdge<EdgeAnnotation> gEdge = aGNode.GetGroupableEdge(aStmt);
			if(gEdge != null){
				GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> subGNode = 
						(GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>) hg.GetCompleteNode(gEdge.targetNode);
				
				List<Value> lv = aStmt.theStmt.getInvokeExpr().getUseBoxes();
				String tarObjName = SDObject.GetUniqueName();
				if(lv.size() > 0){
					Object obj = null;
					if(lv.get(0) instanceof JimpleLocalBox){
						JimpleLocalBox jlb = (JimpleLocalBox)lv.get(0);
						obj = jlb.getValue();
					}
					else
					{
						obj = lv.get(0);
					}
					if(obj instanceof JimpleLocal){
						JimpleLocal jl = (JimpleLocal)obj;
						tarObjName = jl.getName();
					}
					
				}
				
				SootMethod sm = subGNode.data.theMethod;
				SootClass scTarget = sm.getDeclaringClass();
				SootClass scSource = aGNode.data.theMethod.getDeclaringClass();
				
				//This also needs to be improved to deal with 
				//instance names.
				SDObject sdSource = new SDObject(scSource, sourceName);
				SDObject sdTarget = new SDObject(scTarget, tarObjName);
				sd.AddObject(sdSource);
				sd.AddObject(sdTarget);
				
				SDMessage msg = new SDMessage(sdSource, sdTarget, sm);
				sd.AddMessage(msg);
				RecFillNodeDiagram(hg, subGNode, sd, tarObjName);
			}
			else
			{
				//throw new Exception("Incomplete Graph - This should not happen");
				//this should not happen as hypergraph edge 
				//filling should eliminate possibility
			}
		}
		GroupableHyperEdge<EdgeAnnotation> gEdge = null;
		GroupableStmt aGStmt = aStmt.theNext;
		RecFillStmtDiagram(hg, aGNode, aGStmt, sd, sourceName);
	}
	
	private static void RecFillNodeDiagram(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			SequenceDiagram sd,
			String sourceName) throws Exception{
		if(aGNode == null){return;}
		GroupableStmt aStmt = aGNode.data.theStmts;
		RecFillStmtDiagram(hg, aGNode, aStmt, sd, sourceName);
	}
}
