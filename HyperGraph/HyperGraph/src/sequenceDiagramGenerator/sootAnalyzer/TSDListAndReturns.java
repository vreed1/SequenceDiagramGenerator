package sequenceDiagramGenerator.sootAnalyzer;

import java.util.ArrayList;
import java.util.List;

import sequenceDiagramGenerator.sdedit.SDObject;
import sequenceDiagramGenerator.sdedit.SDObject.TaintState;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;

public class TSDListAndReturns {

	public List<SequenceDiagram> listDiagrams;
	public List<SDObject> listReturns;
	public TaintState tState;
	
		public TSDListAndReturns(){
			listDiagrams = new ArrayList<SequenceDiagram>();
			listReturns = new ArrayList<SDObject>();
		}
		
		public void clear(){
			listDiagrams.clear();
			listReturns.clear();
		}
		
		public void addAll(TSDListAndReturns other){
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
		
		public TSDListAndReturns clone(){
			TSDListAndReturns toReturn = new TSDListAndReturns();
			for(int i = 0; i < listDiagrams.size(); i++){
				int id = -1;
				if(i < listReturns.size()){
					if(listReturns.get(i)!= null){
						id = listReturns.get(i).ID;
					}
				}
				toReturn.listDiagrams.add(listDiagrams.get(i).clone());
				if(id >= 0){
					toReturn.listReturns.add((SDObject)toReturn.listDiagrams.get(i).GetObjectFromID(id));
				}
			}
			return toReturn;
		}

		public TSDListAndReturns Copy(int sdIndex) {
			TSDListAndReturns toReturn = new TSDListAndReturns();
			toReturn.listDiagrams.add(listDiagrams.get(sdIndex));
			if(listReturns.size() > sdIndex){
				toReturn.listReturns.add(listReturns.get(sdIndex));
			}
			return toReturn;
		}

		public void compress() {
			for(int i = listDiagrams.size()-1; i>= 0; i--){
				SequenceDiagram sdTest = listDiagrams.get(i);
				SDObject testReturn = null;
				if(i < listReturns.size()){
					testReturn = listReturns.get(i);
				}
				for(int j = i-1; j>=0; j--){
					
					SDObject checkReturn = null;
					if(j < listReturns.size()){
						checkReturn = listReturns.get(j);
					}
					if(checkReturn == null && testReturn == null){
						
					}
					else if(checkReturn == null || testReturn == null){
						continue;
					}
					else if(!checkReturn.isEquivalent(testReturn)){
						continue;
					}
					SequenceDiagram sdCheck = listDiagrams.get(j);
					if(sdCheck.isEquivalent(sdTest)){
						listDiagrams.remove(i);
						if(i < listReturns.size()){
							listReturns.remove(i);
						}
						break;
					}
				}
			}
		}
	}
