package sequenceDiagramGenerator.sdedit;

import java.util.List;

public interface GenReducer {
	public List<SequenceDiagram> Prioritize(List<SequenceDiagram> aList);
	public List<SequenceDiagram> SelectTopK(List<SequenceDiagram> aList);
}
