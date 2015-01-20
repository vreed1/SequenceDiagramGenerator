package sequenceDiagramGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.HyperEdge;
import sequenceDiagramGenerator.hypergraph.HyperNode;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import sequenceDiagramGenerator.sdedit.SDObject;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;
import sequenceDiagramGenerator.sootAnalyzer.Analyzer;

public class SDGenerator {
	public static void GenerateNaiveSequenceDiagram(
			Hypergraph<SourceCodeType, EdgeAnnotation> aGraph,
			String outFile){
		List<HyperEdge<EdgeAnnotation>> lEdges = aGraph.GetAllEdges();
		SequenceDiagram sd = new SequenceDiagram();
		
		
		for(int i = 0; i < lEdges.size(); i++){
			HyperEdge<EdgeAnnotation> aEdge = lEdges.get(i);
			ArrayList<SourceCodeType> listSource = new ArrayList<SourceCodeType>();
			for(int j = 0; j < aEdge.sourceNodes.size(); j++){
				SourceCodeType sct  = aGraph.GetNode(aEdge.sourceNodes.get(j));
				listSource.add(sct);
			}
			SourceCodeType tar = aGraph.GetNode(aEdge.targetNode);
			DiagramMessageAggregator dma = new DiagramMessageAggregator(
					listSource,
					tar,
					aEdge.annotation.getMethodName()
					);
		
			for(SDObject sdo : dma.GetSDObjects()){
				sd.AddObject(sdo);
			}
			sd.AddMessage(dma.GenerateSDEditMessage());
		}
		sd.CreatePDF(outFile);
		/*try {
			FileWriter fw = new FileWriter(outFile);
			fw.write(sd.toString());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	
	public static void Generate(String ClassName, String ClassDir, String ClassPath, String SaveFile){
		Hypergraph<SourceCodeType, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(ClassName, ClassDir, ClassPath);

		SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
		
	}
	
	public static void Generate(List<String> listClasses, String ClassPath, String SaveFile){
		Hypergraph<SourceCodeType, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(listClasses, ClassPath);

		SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
	}
}
