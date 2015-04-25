package sequenceDiagramGenerator.UI;

import javax.swing.JPanel;

import sequenceDiagramGenerator.hypergraph.Hypergraph;

import java.awt.GridBagLayout;

import javax.swing.JComboBox;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.util.List;

public class HypergraphSelector extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3139151898311406959L;

	/**
	 * Create the panel.
	 */
	
	private Hypergraph theGraph;
	
	JComboBox cmbHyperNodes;
	
	public HypergraphSelector(ActionListener aListener) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		cmbHyperNodes = new JComboBox();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		add(cmbHyperNodes, gbc_comboBox);
		cmbHyperNodes.setActionCommand("Select Node");
		cmbHyperNodes.addActionListener(aListener);
		cmbHyperNodes.setEnabled(false);
	}
	
	public void SetHypergraph(Hypergraph aGraph){
		theGraph = aGraph;
		cmbHyperNodes.removeAllItems();
		List v = theGraph.getVertices();
		for(int i = 0; i < v.size(); i++){
			cmbHyperNodes.addItem(v.get(i));
		}
		cmbHyperNodes.setEnabled(true);
	}

}
