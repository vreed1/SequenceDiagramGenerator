package sequenceDiagramGenerator.sdedit;

import java.util.List;

public class DiagramPDFGen {
	private List<SequenceDiagram> theDiagrams;
	private GenReducer theReducer;
	
	public DiagramPDFGen(
			List<SequenceDiagram> allDiagrams,
			GenReducer aReducer){
		theDiagrams = allDiagrams;
		theReducer = aReducer;
	}
	
	public void CreatePDFs(String directory){
		List<SequenceDiagram> listDiagram = theReducer.SelectTopK(theDiagrams);
		
		for(int i = 0; i < listDiagram.size(); i++){
			listDiagram.get(i).CreatePDFInDir(directory);
		}
	}
	
}
