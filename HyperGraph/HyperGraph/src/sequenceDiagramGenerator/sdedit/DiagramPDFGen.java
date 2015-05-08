package sequenceDiagramGenerator.sdedit;

import java.util.List;

import utilities.Utilities;

public class DiagramPDFGen {
	private List<SequenceDiagram> theDiagrams;
	private GenReducer theReducer;
	
	private boolean tersemode = false;
	
	public DiagramPDFGen(
			List<SequenceDiagram> allDiagrams,
			GenReducer aReducer,
			String[] args){
		theDiagrams = allDiagrams;
		theReducer = aReducer;
		String terseval = Utilities.GetArgument(args, "-terse");
		if(terseval != null && terseval.length() > 0){
			tersemode = Boolean.parseBoolean(terseval);
		}
	}
	
	public void CreatePDFs(String directory){
		List<SequenceDiagram> listDiagram = theReducer.SelectTopK(theDiagrams);
		
		for(int i = 0; i < listDiagram.size(); i++){
			listDiagram.get(i).CreatePDFInDir(directory, tersemode);
		}
	}
	
}
