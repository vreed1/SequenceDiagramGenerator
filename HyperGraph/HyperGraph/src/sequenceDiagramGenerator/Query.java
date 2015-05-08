package sequenceDiagramGenerator;

import sequenceDiagramGenerator.sdedit.SequenceDiagram;

public interface Query {

	public enum QueryMode{
		Reject,
		Accept,
		Filter
	}
	
	public enum QueryResponse{
		True,
		False,
		Filter
	}
	
	public QueryResponse RunOnData(QueryDataContainer toQuery);
	public QueryResponse RunOnMethodName(String outerMethodName);
	public QueryResponse CheckFinishedDiagram(SequenceDiagram sd);
	
}
