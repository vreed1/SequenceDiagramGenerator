package sequenceDiagramGenerator.UI;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HypergraphVis extends JPanel implements ActionListener {

	/**
	 * Create the panel.
	 */
	
	public HypergraphSelector theSelector;
	
	public HypergraphVis() {
		setLayout(new BorderLayout(0, 0));
		theSelector = new HypergraphSelector(this);

		this.add(theSelector, BorderLayout.NORTH);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
