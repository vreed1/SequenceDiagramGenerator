package sequenceDiagramGenerator.UI;

import javax.swing.JPanel;

import sequenceDiagramGenerator.MethodNodeAnnot;
import sequenceDiagramGenerator.hypergraph.HyperNode;
import sequenceDiagramGenerator.hypergraph.SimpleNodeCollection;

import java.awt.BorderLayout;

public class NodeVis extends JPanel {

	public SimpleNodeCollection<MethodNodeAnnot> theGraph;
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
	
	public void SetCenterNode(SimpleNodeCollection<MethodNodeAnnot> aGraph, HyperNode aNode){
		theGraph = aGraph;
		theCenterNode = aNode;
	}

}
