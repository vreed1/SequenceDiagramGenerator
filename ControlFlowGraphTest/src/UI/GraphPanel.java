package UI;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;
import javax.swing.JTextArea;
import java.awt.BorderLayout;

public class GraphPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UnitGraph theGraph = null;
	
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
	
	public void SetGraph(UnitGraph aGraph){
		theGraph = aGraph;
		theTextArea.setText(GetStringOfGraph());
		this.repaint();
	}
	
	public String GetStringOfGraph(){

		StringBuilder toReturn = new StringBuilder();
		
		List<List<Unit>> topList = new ArrayList<List<Unit>>();
		
		List<Unit> seen = new ArrayList<Unit>();
		
		topList.add(theGraph.getHeads());
		seen.addAll(topList.get(0));
		
		boolean lookAhead = true;
		int i = 0;
		while(lookAhead){
			lookAhead = false;
			List<Unit> nextLevel = new ArrayList<Unit>();
			for(int j = 0; j < topList.get(i).size(); j++){
				List<Unit> toCheck = theGraph.getSuccsOf(topList.get(i).get(j));
				for(int k = 0; k < toCheck.size(); k++){
					if(!seen.contains(toCheck.get(k))){
						seen.add(toCheck.get(k));
						lookAhead = true;
						nextLevel.add(toCheck.get(k));
						toReturn.append(topList.get(i).get(j).toString());
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
