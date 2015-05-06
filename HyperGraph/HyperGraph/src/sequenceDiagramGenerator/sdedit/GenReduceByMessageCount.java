package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GenReduceByMessageCount implements GenReducer, Comparator<SequenceDiagram> {

	private int k;
	public GenReduceByMessageCount(int aK){
		k = aK;
	}
	
	@Override
	public List<SequenceDiagram> Prioritize(List<SequenceDiagram> aList) {
		// TODO Auto-generated method stub
		ArrayList<SequenceDiagram> newList = new ArrayList<SequenceDiagram>(aList);
		Collections.sort(newList, this);
		return newList;
	}

	@Override
	public List<SequenceDiagram> SelectTopK(List<SequenceDiagram> aList) {
		// TODO Auto-generated method stub
		List<SequenceDiagram> afterSort = Prioritize(aList);
		for(int i = k; i < afterSort.size(); i++){
			afterSort.remove(k);
		}
		return afterSort;
	}

	@Override
	public int compare(SequenceDiagram x, SequenceDiagram y) {
		int xsize = x.GetMessages().size();
		int ysize = y.GetMessages().size();
		//this is deliberate inversion of the ordering
		//as defined by the comparator interface
		//in order to produce a descending sorted array
		//which is what we want.
		if(xsize < ysize){return 1;}
		else if(xsize > ysize){return -1;}
		return 0;
	}

}
