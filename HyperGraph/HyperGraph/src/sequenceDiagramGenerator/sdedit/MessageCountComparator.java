package sequenceDiagramGenerator.sdedit;

import java.util.Comparator;

public class MessageCountComparator implements Comparator<SequenceDiagram> {

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
