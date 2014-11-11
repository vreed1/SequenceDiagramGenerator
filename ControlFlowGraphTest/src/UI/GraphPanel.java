package UI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import soot.MethodOrMethodContext;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.UnitGraph;

import javax.swing.JTextArea;

import polyglot.visit.FlowGraph.Edge;

import java.awt.BorderLayout;

//Brian Peterson
//Very basic UI for output of analysis
//it is just a big text area in which I can drop string output.
//This is a pretty messy class, but I anticipate throwing it away completely.

public class GraphPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CallGraph theCallGraph = null;
	
	private UnitGraph theUnitGraph = null;
	
	private JTextArea theTextArea;
	
	/**
	 * Create the panel.
	 */
	public GraphPanel() {
		setLayout(new BorderLayout(0, 0));
		
		theTextArea= new JTextArea();
		add(theTextArea, BorderLayout.CENTER);

		//SetGraph(NewAnalyzerCode.ExecAnalysis.getGraph());
	}
	
	//unused
	public void SetUnitGraph(UnitGraph aGraph){
		theUnitGraph = aGraph;
		theTextArea.setText(GetStringOfUnitGraph());
		this.repaint();
	}
	
	public void SetString(String aString){
		theTextArea.setText(aString);
		this.repaint();
	}
	
	//Unused.
	public String GetStringOfCallGraph(){

		StringBuilder toReturn = new StringBuilder();

//		Iterator<MethodOrMethodContext> it = theGraph.sourceMethods();
//		
//		while(it.hasNext()){
//			MethodOrMethodContext am = it.next();
//			Iterator<Edge> itEdge = theGraph.edgesOutOf(am);
//			while(itEdge.hasNext()){
//				Edge e = itEdge.next();
//				e.getTarget()
//			}
//		}
//		
		return toReturn.toString();
	}
	
	//Unused.
	public String GetStringOfUnitGraph(){

		StringBuilder toReturn = new StringBuilder();
		
		List<List<Unit>> topList = new ArrayList<List<Unit>>();
		
		List<Unit> seen = new ArrayList<Unit>();
		
		
		topList.add(theUnitGraph.getHeads());
		seen.addAll(topList.get(0));
		
		boolean lookAhead = true;
		int i = 0;
		while(lookAhead){
			lookAhead = false;
			List<Unit> nextLevel = new ArrayList<Unit>();
			for(int j = 0; j < topList.get(i).size(); j++){
				List<Unit> toCheck = theUnitGraph.getSuccsOf(topList.get(i).get(j));
				for(int k = 0; k < toCheck.size(); k++){
					if(true || !seen.contains(toCheck.get(k))){
						seen.add(toCheck.get(k));
						lookAhead = true;
						nextLevel.add(toCheck.get(k));
						toReturn.append(toCheck.get(k).toString());
						toReturn.append(", ");
					}
				}
			}
			toReturn.append("\n");
			i++;
			topList.add(nextLevel);
			if(i == 100){break;}
		}
		
		return toReturn.toString();
	}

	/*@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		drawGraph(g);
	}

	public void drawGraph(Graphics gin){
	
		if(theGraph == null){return;}
		
		int w = this.getWidth();
		int h = this.getHeight();
		
		Graphics2D g = (Graphics2D)gin;
		
		List<List<Unit>> topList = new ArrayList<List<Unit>>();
		
		topList.add(theGraph.getHeads());
		
		boolean lookAhead = true;
		int i = 0;
		while(lookAhead){
			lookAhead = false;
			List<Unit> nextLevel = new ArrayList<Unit>();
			for(int j = 0; j < topList.get(i).size(); j++){
				lookAhead = true;
				nextLevel.addAll(theGraph.getSuccsOf(topList.get(i).get(j)));
			}
			i++;
			topList.add(nextLevel);
		}
		
		
	}*/
}
