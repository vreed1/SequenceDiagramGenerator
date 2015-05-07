package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GenReduceByMessageCount implements GenReducer {

	protected int k;
	public GenReduceByMessageCount(int aK){
		k = aK;
	}
	
	@Override
	public List<SequenceDiagram> Prioritize(List<SequenceDiagram> aList) {
		// TODO Auto-generated method stub
		ArrayList<SequenceDiagram> newList = new ArrayList<SequenceDiagram>(aList);
		Collections.sort(newList, new MessageCountComparator());
		for(int i = newList.size() -1; i > 0; i--){
			SequenceDiagram test = newList.get(i);
			for(int j = i -1; j >= 0; j--){
				SequenceDiagram pred = newList.get(j);
				if(test.isSubsetOf(pred)){
					newList.remove(i);
					break;
				}
			}
		}
		for(int i  = 0; i < newList.size(); i++){
			newList.get(i).SetPriority(i);
		}
		return newList;
	}

	@Override
	public List<SequenceDiagram> SelectTopK(List<SequenceDiagram> aList) {
		// TODO Auto-generated method stub
		List<SequenceDiagram> afterSort = Prioritize(aList);
		int startSize = afterSort.size();
		for(int i = k; i < startSize; i++){
			afterSort.remove(k);
		}
		return afterSort;
	}

}
