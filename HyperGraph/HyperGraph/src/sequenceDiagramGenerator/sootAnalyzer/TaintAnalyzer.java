package sequenceDiagramGenerator.sootAnalyzer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sequenceDiagramGenerator.MethodNodeAnnot;
import sequenceDiagramGenerator.Query;
import sequenceDiagramGenerator.TraceStatement;
import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.GroupableHyperNode;
import sequenceDiagramGenerator.hypergraph.GroupableHypergraph;
import sequenceDiagramGenerator.hypergraph.HyperEdge;
import sequenceDiagramGenerator.hypergraph.HyperNode;
import sequenceDiagramGenerator.sdedit.SDListAndReturns;
import sequenceDiagramGenerator.sdedit.SDObject;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;
import soot.SootMethod;
import utilities.Utilities;

public class TaintAnalyzer {

	private List<String> lTMethods;
	
	public TaintAnalyzer(String aFileName){
		lTMethods = new ArrayList<String>();
		try{
			FileReader fr = new FileReader(aFileName);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line = br.readLine()) != null){
				lTMethods.add(line);
			}
			br.close();
		}catch(IOException ex){}
		
	}
	
	public void TagTraces(List<TraceStatement> lt){
		for(int i = 0; i < lt.size(); i++){
			System.out.print("Stmt:");
			System.out.println(lt.get(i).theStmt.toString());
		}
	}
	
    public void CullObjects(
    		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg){
    	//List<TaintedObj> listObjs = new ArrayList<TaintedObj>();
    	List<HyperNode> listTS = new ArrayList<HyperNode>();
    	List<HyperNode<MethodNodeAnnot, EdgeAnnotation>> lv = hg.getVertices();
    	
    	for(int i = 0; i < lv.size(); i++){
    		if(IsNodeTainted(lv.get(i))){
    			listTS.add(lv.get(i));
    		}
    	}
    	
    	List<HyperEdge<EdgeAnnotation>> le = hg.GetAllEdges();
    	List<Integer> listCurrent = new ArrayList<Integer>();
    	List<Integer> listSeen = new ArrayList<Integer>();
    	for(int i = 0; i < listTS.size(); i++){
    		listCurrent.add(listTS.get(i).uniqueId);
    		listSeen.add(listTS.get(i).uniqueId);
    	}
    	List<Integer> listNext = new ArrayList<Integer>();
    	
    	List<Integer> protectedEdges = new ArrayList<Integer>();
    	
    	boolean NodesAdded = true;
    	while(NodesAdded){
    		NodesAdded = false;
			for(int j = 0; j < listCurrent.size(); j++){
				for(int i = le.size()-1; i >= 0; i--){
					boolean touchesNode = false;
					
    				if(le.get(i).targetNode == listCurrent.get(j)){
    					touchesNode = true;
    				}
    				else{
    					for(Integer aSrc : le.get(i).sourceNodes){
    						if(aSrc == listCurrent.get(j)){
    							touchesNode = true;
    							break;
    					}
    				}
    				if(touchesNode)
    					for(int k = 0; k < le.get(i).sourceNodes.size(); k++){
    						if(!listSeen.contains(le.get(i).sourceNodes.get(k))){
    							listSeen.add(le.get(i).sourceNodes.get(k));
    							listNext.add(le.get(i).sourceNodes.get(k));
    							listTS.add(hg.GetCompleteNode(le.get(i).sourceNodes.get(k)));
    							NodesAdded = true;
    						}
    					}
    					if(!listSeen.contains(le.get(i).targetNode)){
    						listSeen.add(le.get(i).targetNode);
    						listNext.add(le.get(i).targetNode);
    						listTS.add(hg.GetCompleteNode(le.get(i).targetNode));
    						NodesAdded = true;
    					}
    					protectedEdges.add(le.get(i).getUID());
    					//hg.RemoveEdge(le.get(i).getUID());
    					//le.remove(i);
    				}
    			}
    		}
			listCurrent.clear();
			listCurrent.addAll(listNext);
			listNext.clear();
    	}
    	
    	
    	for(int i = 0; i < le.size(); i++){
    		if(!protectedEdges.contains(le.get(i).getUID()))
    		{
    			hg.RemoveEdge(le.get(i).getUID());
    		}
    	}
    	List<HyperNode<MethodNodeAnnot, EdgeAnnotation>> lNodes = hg.getVertices();
    	
    	for(int i = lNodes.size()-1; i >= 0; i--){
    		boolean foundNode = false;
    		for(int j = 0; j < listTS.size(); j++){
    			if(listTS.get(j).uniqueId == lNodes.get(i).uniqueId){
    				foundNode = true;
    				break;
    			}
    		}
    		if(!foundNode){
    			hg.RemoveVertex(lNodes.get(i).uniqueId);
    		}
    	}
    	
    	//this is the point at which to generate taint traces.
    	
    	
    }
    

    
	public boolean IsNodeTainted(HyperNode<MethodNodeAnnot, EdgeAnnotation> node){
		return IsMethodTainted(node.data.GetMethod());
	}
	
	public boolean IsMethodTainted(SootMethod sm){
		String mName = Utilities.getMethodString(sm);
		for(int i = 0; i < lTMethods.size(); i++){
			if(mName.equals(lTMethods.get(i))){
				return true;
			}
		}
		return false;
	}
}
