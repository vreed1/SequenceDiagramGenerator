package sequenceDiagramGenerator.UI;

import javax.swing.JPanel;

import sequenceDiagramGenerator.hypergraph.HyperNode;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import java.awt.BorderLayout;

public class NodeVis extends JPanel {

	public Hypergraph theGraph;
	public HyperNode theCenterNode;
	
	JPanel topPanel;
	JPanel centerPanel;
	/**
	 * Create the panel.
	 */
	public NodeVis() {
		setLayout(new BorderLayout(0, 0));
		topPanel = new JPanel();
		this.add(topPanel, BorderLayout.NORTH);
		
		centerPanel = new JPanel();
		this.add(centerPanel, BorderLayout.CENTER);
	}
	
	public void SetCenterNode(Hypergraph aGraph, HyperNode aNode){
		theGraph = aGraph;
		theCenterNode = aNode;
	}

}
