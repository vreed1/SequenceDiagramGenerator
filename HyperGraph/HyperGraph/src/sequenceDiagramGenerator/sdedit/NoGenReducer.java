package sequenceDiagramGenerator.sdedit;

import java.util.List;

public class NoGenReducer implements GenReducer {

	@Override
	public List<SequenceDiagram> Prioritize(List<SequenceDiagram> aList) {
		return aList;
	}

	@Override
	public List<SequenceDiagram> SelectTopK(List<SequenceDiagram> aList) {
		return aList;
	}

}
