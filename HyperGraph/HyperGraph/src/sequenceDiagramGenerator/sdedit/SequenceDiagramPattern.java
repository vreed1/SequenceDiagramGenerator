package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import utilities.Tuple;
import Automata.Automata;
import Automata.AutomataFactory;
import Automata.FANode;
import Automata.FATrans;

public class SequenceDiagramPattern extends SequenceDiagram{
	private String theRegex;
	private Automata theRAutomata;
	
	public SequenceDiagramPattern(){
		super();
	}
	public SequenceDiagramPattern(JSONObject jobj){
		super(jobj);
		try{
			theRegex = (String)jobj.get("Regex");
			theRAutomata = AutomataFactory.FromRegex(theRegex);
		}
		catch(Exception ex){}
	}
	
	public void SetRegex(String regex){
		theRegex = regex;
		theRAutomata = AutomataFactory.FromRegex(theRegex);
	}
	
	public String GetRegex(){
		return theRegex;
	}
	
	//Going to look for instance of exact pattern in other
	//with no missing portions
	public int MatchExact(SequenceDiagram other){
		int ps = this.theMessages.size();
		int matches = 0;
		for(int i = 0; i < other.theMessages.size(); i++){
			if(other.theMessages.size() - i < ps){return matches;}
			boolean mh = MatchExactFromHere(other, i);
			if(mh){matches++;}
		}
		return matches;
	}
	
	private boolean MatchExactFromHere(SequenceDiagram other, int n){
		List<Tuple<SDObject, SDObject>> links = new ArrayList<Tuple<SDObject, SDObject>>();
		for(int i = 0; i < this.theMessages.size(); i++){
			SDMessage thisMsg = this.theMessages.get(i);
			SDObject thisSrc = this.GetObjectFromID(thisMsg.callerID);
			SDObject thisTgt = this.GetObjectFromID(thisMsg.calleeID);
			SDMessage otherMsg = other.theMessages.get(i+n);
			SDObject otherSrc = other.GetObjectFromID(otherMsg.calleeID);
			SDObject otherTgt = other.GetObjectFromID(otherMsg.callerID);
			boolean needsSrc = true;
			boolean needsTgt = true;
			for(int j = 0; j < links.size(); j++){
				Tuple<SDObject, SDObject> link = links.get(j);
				if(link.one.equals(thisSrc)){
					if(!link.two.equals(otherSrc)){
						return false;
					}
					else{needsSrc = false;}
				}
				else if(link.two.equals(otherSrc)){
					return false;
				}
				if(link.one.equals(thisTgt)){
					if(!link.two.equals(otherTgt)){
						return false;
					}
					else{needsTgt = false;}
				}
				else if(link.two.equals(otherTgt)){
					return false;
				}
			}
			if(needsSrc){
				Tuple<SDObject, SDObject> srcLink = new Tuple<SDObject, SDObject>(thisSrc, otherSrc);
				links.add(srcLink);
			}
			if(needsTgt){
				Tuple<SDObject, SDObject> tgtLink = new Tuple<SDObject, SDObject>(thisTgt, otherTgt);
				links.add(tgtLink);
			}
		}
		return true;
	}
	
	public boolean Match(SequenceDiagram other){
		
		List<Tuple<FANode, List<Tuple<SDObject, SDObject>>>> listStates = 
				new ArrayList<Tuple<FANode, List<Tuple<SDObject, SDObject>>>>();
		listStates.add(new Tuple<FANode, List<Tuple<SDObject, SDObject>>>(
				theRAutomata.startNode, 
				new ArrayList<Tuple<SDObject, SDObject>>()));

		List<SDMessage> listMessages = other.GetMessages();
		
		for(int i =0 ; i < listMessages.size(); i++){
			List<Tuple<FANode, List<Tuple<SDObject, SDObject>>>> listNewStates = 
					new ArrayList<Tuple<FANode, List<Tuple<SDObject, SDObject>>>>();
			for(int j = 0; j < listStates.size(); j++){
				listNewStates.addAll(Match(listStates.get(j), listMessages.get(i), other));
			}
			listStates = listNewStates;
		}
		for(int i = 0; i < listStates.size(); i++){
			if(listStates.get(i).one.equals(theRAutomata.acceptNode)){
				return true;
			}
		}
		return false;
	}
	
	
	private List<Tuple<FANode, List<Tuple<SDObject, SDObject>>>> Match(
			Tuple<FANode, List<Tuple<SDObject, SDObject>>> aState,
			SDMessage aMessage,
			SequenceDiagram msgContainer){
		List<Tuple<FANode, List<Tuple<SDObject, SDObject>>>> toReturn = 
				new ArrayList<Tuple<FANode, List<Tuple<SDObject, SDObject>>>>();
		for(int i=0;i<aState.one.listTrans.size();i++){
			FATrans t = aState.one.listTrans.get(i);
		    
			if(t.theExpr.equals("x")){
				
			}
			//toReturn.addAll(FindAllMatches(t, aState.two, other));
			
			
		}
		return toReturn;
	}
	
	private List<FANode> expandEpsilons(FANode aNode){
		List<FANode> ret = new ArrayList<FANode>();
		List<FANode> newadds = new ArrayList<FANode>();
		newadds.add(aNode);
		while(newadds.size() > 0){
			List<FANode> nextadds = new ArrayList<FANode>();
			for(int i = 0; i < newadds.size(); i++){
				for(int j = 0; j < newadds.get(i).listTrans.size(); j++){
					if(newadds.get(i).listTrans.get(j).theExpr.equals("e")){
						FANode destNode = newadds.get(i).listTrans.get(j).theDest;
						if(!ret.contains(destNode) && !newadds.contains(destNode) && !nextadds.contains(destNode)){
							nextadds.add(destNode);
						}
					}
				}
			}
			ret.addAll(newadds);
			newadds = nextadds;
		}
		return ret;
	}
}
