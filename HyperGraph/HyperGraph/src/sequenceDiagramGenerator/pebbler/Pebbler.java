package sequenceDiagramGenerator.pebbler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sequenceDiagramGenerator.*;
import sequenceDiagramGenerator.hypergraph.*;
import utilities.*;

import net.sf.sdedit.config.Configuration;
import net.sf.sdedit.config.ConfigurationManager;
import net.sf.sdedit.diagram.Diagram;
import net.sf.sdedit.editor.DiagramFileHandler;
import net.sf.sdedit.error.SemanticError;
import net.sf.sdedit.error.SyntaxError;
import net.sf.sdedit.server.Exporter;
import net.sf.sdedit.text.TextHandler;
import net.sf.sdedit.ui.ImagePaintDevice;
import net.sf.sdedit.ui.components.configuration.Bean;
import net.sf.sdedit.util.DocUtil.XMLException;
import net.sf.sdedit.util.Pair;

//
// A reduced version of the original hypergraph that provides simple pebbling and exploration
//
public class Pebbler
{
	// The pebbling version (integer-based) of the hypergraph to work on.
	private PebblerHypergraph<SourceCodeType, EdgeAnnotation> pebblerGraph;

	// The actual hypergraph for reference purposes only
	private Hypergraph<SourceCodeType, EdgeAnnotation> graph;

	// A static list of edges that can be processed using means other than a fixpoint analysis.
	private HyperEdgeMultiMap<EdgeAnnotation> forwardPebbledEdges;
	public HyperEdgeMultiMap<EdgeAnnotation> getForwardPebbledEdges() { return forwardPebbledEdges; }

	public Pebbler(Hypergraph<SourceCodeType, EdgeAnnotation> graph,
			PebblerHypergraph<SourceCodeType, EdgeAnnotation> pGraph)
	{
		this.graph = graph;
		this.pebblerGraph = pGraph;

		// Create the database of forward edges and set the original hypergraph as a reference.
		forwardPebbledEdges = new HyperEdgeMultiMap<EdgeAnnotation>(pGraph.NumVertices());
		forwardPebbledEdges.SetOriginalHypergraph(graph);
	}

	//
	// Clear all pebbles from all nodes and edges in the hypergraph
	//
	private void ClearPebbles()
	{
		for (PebblerHyperNode<SourceCodeType, EdgeAnnotation> node : pebblerGraph.getVertices())
		{
			node.pebbled = false;

			for (PebblerHyperEdge<EdgeAnnotation> edge : node.edges)
			{
				edge.sourcePebbles.clear();
				edge.pebbled = false;
			}
		}
	}

	//
	// Use Dowling-Gallier pebbling technique to pebble using all given nodes
	//
	public void Pebble(List<Integer> nodes) 
	{
		PebbleForward(nodes);
	}

	//
	// We are attempting to pebble exactly the same way in which the hypergraph was generated: using a
	// worklist, breadth-first manner of construction.
	//
	private void PebbleForward(List<Integer> nodes)
	{
		// Combine all the given information uniquely.
		List<Integer> nodesToBePebbled = new ArrayList<Integer>(nodes);

		// Sort in ascending order for pebbling
		Collections.sort(nodesToBePebbled);

		// Pebble all nodes and percolate
		ForwardTraversal(forwardPebbledEdges, nodesToBePebbled);
	}

	private boolean IsNodePebbled(int v)
	{
		return pebblerGraph.getVertices().get(v).pebbled;
	}

	//
	// Given a node, pebble the reachable parts of the graph (in the forward direction)
	// We pebble in a breadth first manner
	//
	private void ForwardTraversal(HyperEdgeMultiMap<EdgeAnnotation> edgeDatabase, List<Integer> nodesToPebble)
	{
		List<Integer> worklist = new ArrayList<Integer>(nodesToPebble);

		//
		// Pebble until the list is empty
		//
		while (!worklist.isEmpty())
		{
			// Acquire the next value to consider
			int currentNodeIndex = worklist.get(0);
			worklist.remove(0);

			// Pebble the current node as a forward node and percolate forward
			pebblerGraph.getVertices().get(currentNodeIndex).pebbled = true;

			// For all hyperedges leaving this node, mark a pebble along the arc
			for (PebblerHyperEdge<EdgeAnnotation> currentEdge : pebblerGraph.getVertices().get(currentNodeIndex).edges)
			{
				if (currentEdge.annotation.IsActive())
				{
					if (!currentEdge.IsFullyPebbled())
					{
						// Indicate the node has been pebbled by adding to the list of pebbled vertices; should not have to be a unique addition
						Utilities.AddUnique(currentEdge.sourcePebbles, currentNodeIndex);

						// With this new node, check if the edge is full pebbled; if so, percolate
						if (currentEdge.IsFullyPebbled())
						{
							// Has the target of this edge been pebbled previously? Pebbled -> Pebbled means we have a backward edge
							if (!IsNodePebbled(currentEdge.targetNode))
							{
								// Success, we have an edge
								// Construct a static set of pebbled hyperedges for problem construction
								edgeDatabase.Put(currentEdge);

								// Add this node to the worklist to percolate further
								if (!worklist.contains(currentEdge.targetNode))
								{
									worklist.add(currentEdge.targetNode);
									Collections.sort(worklist);
								}
							}
						}
					}
				}
			}
		}
	}

	//
	// Pebble forward acquiring the list of messages (in breadth-first order).
	//
	public List<DiagramMessageAggregator> InteractivePebble(List<Integer> nodes)
	{
		return InteractivePebbleForward(nodes);
	}

	//
	// We are attempting to pebble exactly the same way in which the hypergraph was generated: using a
	// worklist, breadth-first manner of construction.
	//
	private List<DiagramMessageAggregator> InteractivePebbleForward(List<Integer> nodes)
	{
		// Combine all the given information uniquely.
		List<Integer> nodesToBePebbled = new ArrayList<Integer>(nodes);

		// Sort in ascending order for pebbling
		Collections.sort(nodesToBePebbled);

		// Pebble all nodes and percolate individually as to acquire the ordered list of messages.
		List<DiagramMessageAggregator> messages = new ArrayList<DiagramMessageAggregator>();
		for (Integer node : nodesToBePebbled)
		{
			messages.addAll(InteractiveForwardTraversal(forwardPebbledEdges, node));
		}
		
		return messages;
	}

	//
	// Given a node, pebble the reachable parts of the graph (in the forward direction)
	// We pebble in a breadth first manner
	//
	private List<DiagramMessageAggregator> InteractiveForwardTraversal(HyperEdgeMultiMap<EdgeAnnotation> edgeDatabase, Integer node)
	{
		// Messages that were created from this pebbled node.
		List<DiagramMessageAggregator> messages = new ArrayList<DiagramMessageAggregator>();

		//
		// Pebble until the list is empty
		//
		List<Integer> worklist = new ArrayList<Integer>();
		worklist.add(node);

		while (!worklist.isEmpty())
		{
			// Acquire the next value to consider
			int currentNodeIndex = worklist.get(0);
			worklist.remove(0);

			if (!IsNodePebbled(currentNodeIndex))
			{
				// Pebble the current node as a forward node and percolate forward
				pebblerGraph.getVertices().get(currentNodeIndex).pebbled = true;

				// For all hyperedges leaving this node, mark a pebble along the arc
				for (PebblerHyperEdge<EdgeAnnotation> currentEdge : pebblerGraph.getVertices().get(currentNodeIndex).edges)
				{
					if (currentEdge.annotation.IsActive() && !currentEdge.IsFullyPebbled())
					{
						// Indicate the node has been pebbled by adding to the list of pebbled vertices; should not have to be a unique addition
						Utilities.AddUnique(currentEdge.sourcePebbles, currentNodeIndex);

						// With this new node, check if the edge is full pebbled; if so, percolate
						// Has the target of this edge been pebbled previously? Pebbled -> Pebbled means we have a backward edge
						if (currentEdge.IsFullyPebbled() && !IsNodePebbled(currentEdge.targetNode))
						{
							//
							// Success, we have a new edge
							//
							//
							// We have a fully pebbled edge; this means we have a UML sequence message
							// Create the message and add to the list.
							//
							messages.add(ConstructMessage(currentEdge));

							// Construct a static set of pebbled hyperedges for problem construction
							edgeDatabase.Put(currentEdge);

							// Add this node to the worklist to percolate further
							if (!worklist.contains(currentEdge.targetNode))
							{
								worklist.add(currentEdge.targetNode);
								Collections.sort(worklist);
							}
						}
					}
				}
			}
		}

		return messages;
	}

	//
	// Construct the SD edit message represented by this edge
	//
	private DiagramMessageAggregator ConstructMessage(PebblerHyperEdge<EdgeAnnotation> edge)
	{
		ArrayList<SourceCodeType> srcs = new ArrayList<SourceCodeType>();

		// Construct the source objects
		for (Integer src : edge.sourceNodes)
		{
			srcs.add(graph.GetNode(src));
		}

		return new DiagramMessageAggregator(srcs, graph.GetNode(edge.targetNode), edge.annotation.getMethodName());
	}
}