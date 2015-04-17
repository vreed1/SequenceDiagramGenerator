package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.List;

public class SDListAndReturns {
	public List<SequenceDiagram> listDiagrams;
	public List<SDObject> listReturns;
	
	public SDListAndReturns(){
		listDiagrams = new ArrayList<SequenceDiagram>();
		listReturns = new ArrayList<SDObject>();
	}
	
	public void clear(){
		listDiagrams.clear();
		listReturns.clear();
	}
	
	public void addAll(SDListAndReturns other){
		listDiagrams.addAll(other.listDiagrams);
		listReturns.addAll(other.listReturns);
	}
	
	public int size(){
		return listDiagrams.size();
	}

	public void SetSafeReturn(int sdIndex, SDObject retObj) {
		while(listReturns.size() <= sdIndex){
			listReturns.add(null);
		}
		listReturns.set(sdIndex, retObj);
	}
	
	public SDListAndReturns clone(){
		SDListAndReturns toReturn = new SDListAndReturns();
		toReturn.listDiagrams = new ArrayList<SequenceDiagram>(listDiagrams);
		toReturn.listReturns = new ArrayList<SDObject>(listReturns);
		return toReturn;
	}

	public SDListAndReturns Copy(int sdIndex) {
		SDListAndReturns toReturn = new SDListAndReturns();
		toReturn.listDiagrams.add(listDiagrams.get(sdIndex));
		if(listReturns.size() > sdIndex){
			toReturn.listReturns.add(listReturns.get(sdIndex));
		}
		return toReturn;
	}
}
