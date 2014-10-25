package pebbler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hypergraph.*;
import utilities.*;

//
// A reduced version of the original hypergraph that provides simple pebbling and exploration
//
public class Pebbler
{
    // The pebbling version (integer-based) of the hypergraph to work on.
    private PebblerHypergraph<ConcreteAST.GroundedClause, hypergraph.EdgeAnnotation> pebblerGraph;

    // The actual hypergraph for reference purposes only
    private hypergraph.Hypergraph<GeometryTutorLib.ConcreteAST.GroundedClause, hypergraph.EdgeAnnotation> graph;

    // A static list of edges that can be processed using means other than a fixpoint analysis.
    public HyperEdgeMultiMap<hypergraph.EdgeAnnotation> forwardPebbledEdges;
    public HyperEdgeMultiMap<hypergraph.EdgeAnnotation> backwardPebbledEdges;

    public Pebbler(hypergraph.Hypergraph<GeometryTutorLib.ConcreteAST.GroundedClause, hypergraph.EdgeAnnotation> graph,
                   PebblerHypergraph<ConcreteAST.GroundedClause, hypergraph.EdgeAnnotation> pGraph)
    {
        this.graph = graph;
        this.pebblerGraph = pGraph;
        
        forwardPebbledEdges = new HyperEdgeMultiMap<hypergraph.EdgeAnnotation>(pGraph.vertices.Length);
        backwardPebbledEdges = new HyperEdgeMultiMap<hypergraph.EdgeAnnotation>(pGraph.vertices.Length);

        forwardPebbledEdges.SetOriginalHypergraph(graph);
        backwardPebbledEdges.SetOriginalHypergraph(graph);
    }

    // Clear all pebbles from all nodes and edges in the hypergraph
    private void ClearPebbles()
    {
        for (PebblerHyperNode<ConcreteAST.GroundedClause, hypergraph.EdgeAnnotation> node : pebblerGraph.vertices)
        {
            node.pebbled = false;

            for (PebblerHyperEdge<hypergraph.EdgeAnnotation> edge : node.edges)
            {
                edge.sourcePebbles.clear();
                edge.pebbled = false;
            }
        }
    }

    //
    // Use Dowling-Gallier pebbling technique to pebble using all given nodes
    //
    public void Pebble(List<Integer> figure, List<Integer> givens)
    {
        // Find all axiomatic, reflexive, and other obvious notions which may go both directions for solving problems.
        List<Integer> axiomaticNodes = new ArrayList<Integer>();
        List<Integer> reflexiveNodes = new ArrayList<Integer>();
        List<Integer> obviousDefinitionNodes = new ArrayList<Integer>();
        for (int v = 0; v < graph.size(); v++)
        {
            ConcreteAST.GroundedClause node = graph.GetNode(v);

            if (node.IsAxiomatic()) axiomaticNodes.add(v);
            if (node.IsReflexive()) reflexiveNodes.add(v);
            if (node.IsClearDefinition()) obviousDefinitionNodes.add(v);
        }

        // Forward pebble: it acquires the valid list of forward edges 
        PebbleForward(figure, givens, axiomaticNodes);

        // Backward pebble: acquires the valid list of bakcward edges 
        PebbleBackward(figure, axiomaticNodes, reflexiveNodes);
    }

    //
    // Use Dowling-Gallier pebbling technique to pebble using all given nodes
    //
    public void PebbleForwardForShading(List<Integer> figure, List<Integer> givens)
    {
        // Find all axiomatic, reflexive, and other obvious notions which may go both directions for solving problems.
        List<Integer> axiomaticNodes = new ArrayList<Integer>();
        List<Integer> reflexiveNodes = new ArrayList<Integer>();
        List<Integer> obviousDefinitionNodes = new ArrayList<Integer>();
        for (int v = 0; v < graph.size(); v++)
        {
            ConcreteAST.GroundedClause node = graph.GetNode(v);

            if (node.IsAxiomatic()) axiomaticNodes.add(v);
            if (node.IsReflexive()) reflexiveNodes.add(v);
            if (node.IsClearDefinition()) obviousDefinitionNodes.add(v);
        }

        // Forward pebble: it acquires the valid list of forward edges 
        PebbleForward(figure, givens, axiomaticNodes);
    }

    //
    // We are attempting to pebble exactly the same way in which the hypergraph was generated: using a
    // worklist, breadth-first manner of construction.
    //
    private void PebbleForward(List<Integer> figure, List<Integer> givens, List<Integer> axiomaticNodes)
    {
        // Combine all the given information uniquely.
        List<Integer> nodesToBePebbled = new ArrayList<Integer>(figure);
        Utilities.AddUniqueList(nodesToBePebbled, axiomaticNodes);
        Utilities.AddUniqueList(nodesToBePebbled, givens);

        // Sort in ascending order for pebbling
        Collections.sort(nodesToBePebbled);

        // Pebble all nodes and percolate
        ForwardTraversal(forwardPebbledEdges, nodesToBePebbled);
    }

    private boolean IsNodePebbled(int v)
    {
        return pebblerGraph.vertices.get(v).pebbled;
    }

    //
    // Pebble the graph in the backward direction using all pebbled nodes from the forward direction.
    // Note: we do so in a descending order (opposite the way we did from the forward direction); this attempts to 
    //
    private void PebbleBackward(List<Integer> figure, List<Integer> axiomaticNodes, List<Integer> reflexiveNodes)
    {
        //
        // Acquire all nodes which are to be pebbled (reachable during forward analysis)
        //
        List<Integer> deducedNodesToPebbleBackward = new ArrayList<Integer>();

        // Avoid re-pebbling figure again so start after the figure
        for (int v = pebblerGraph.vertices.Length - 1; v >= figure.size(); v--)
        {
            if (IsNodePebbled(v)) deducedNodesToPebbleBackward.add(v);
        }

        // Clear all pebbles (nodes and edges)
        ClearPebbles();

        //
        // Pebble all Figure nodes, but do pursue edges: node -> node.
        // That is, the goal is to pebbles all the occurrences of figure nodes in edges (without traversing further).
        // We include, not just the intrinsic nodes in the list, but other relationships as well that are obvious:
        //       reflexive, OTHERS?
        //
        List<Integer> cumulativeIntrinsic = new ArrayList<Integer>();
        cumulativeIntrinsic.addAll(figure);
        cumulativeIntrinsic.addAll(reflexiveNodes);
        Collections.sort(cumulativeIntrinsic);

        BackwardPebbleFigure(cumulativeIntrinsic);

        //
        // Pebble axiomatic nodes (and any edges); note axiomatic edges may occur in BOTH forward and backward problems
        //
        Collections.sort(axiomaticNodes);
        ForwardTraversal(backwardPebbledEdges, axiomaticNodes);

        //
        // Pebble the graph in the backward direction using all pebbled nodes from the forward direction.
        // Note: we do so in a descending order (opposite the way we did from the forward direction)
        // We create an ascending list and will pull from the back of the list
        //
        ForwardTraversal(backwardPebbledEdges, deducedNodesToPebbleBackward);
    }


    //
    // Given a node, pebble the reachable parts of the graph (in the forward direction)
    // We pebble in a breadth first manner
    //
    private void ForwardTraversal(HyperEdgeMultiMap<hypergraph.EdgeAnnotation> edgeDatabase, List<Integer> nodesToPebble)
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
            pebblerGraph.vertices.get(currentNodeIndex).pebbled = true;

            // For all hyperedges leaving this node, mark a pebble along the arc
            for (PebblerHyperEdge<hypergraph.EdgeAnnotation> currentEdge : pebblerGraph.vertices.get(currentNodeIndex).edges)
            {
                if (!Utilities.RESTRICTING_AXS_DEFINITIONS_THEOREMS || (Utilities.RESTRICTING_AXS_DEFINITIONS_THEOREMS && currentEdge.annotation.IsActive()))
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
    // Pebble only the figure DO NOT traverse the pebble through the graph 
    //
    private void BackwardPebbleFigure(List<Integer> figure) {
        for (int fIndex : figure) {
            // Pebble the current node as a backward; DO NOT PERCOLATE forward
            pebblerGraph.vertices.get(fIndex).pebbled = true;

            // For all hyperedges leaving this node, mark a pebble along the arc
            for (PebblerHyperEdge<hypergraph.EdgeAnnotation> currentEdge : pebblerGraph.vertices.get(fIndex).edges) {
                // Avoid a fully pebbled edge
                if (!currentEdge.IsFullyPebbled()) {
                    // Indicate the node has been pebbled by adding to the list of pebbled vertices
                    Utilities.AddUnique(currentEdge.sourcePebbles, fIndex);
                }
            }
        }
    }

    public void DebugDumpClauses() {
        StringBuilder edgeStr = new StringBuilder();

        int numNonPebbledNodes = 0;
        int numPebbledNodes = 0;

        System.out.println("\n Vertices:");
        edgeStr = new StringBuilder();
        for (int v = 0; v < pebblerGraph.vertices.size(); v++) {
            edgeStr.append(v + ": ");

            if (IsNodePebbled(v)) {
                edgeStr.append("PEBBLE\n");
                numPebbledNodes++;
            }
            else {
                edgeStr.append("NOT\n");
                numNonPebbledNodes++;
            }
        }

        System.out.println("\nPebbled Edges:");
        for (int v = 0; v < pebblerGraph.vertices.size(); v++) {
            if (!pebblerGraph.vertices.get(v).edges.isEmpty()) {
                edgeStr.append(v + ": {");
                for (PebblerHyperEdge<hypergraph.EdgeAnnotation> edge : pebblerGraph.vertices.get(v).edges) {
                    if (v == Collections.min(edge.sourceNodes)) {
                        edgeStr.append(" { ");

                        if (edge.IsFullyPebbled()) edgeStr.append("+ ");
                        else edgeStr.append("- ");

                        for (int s : edge.sourceNodes) {
                            edgeStr.append(s + " ");
                        }
                        edgeStr.append("} -> " + edge.targetNode + ", ");
                    }
                }
                int remove = edgeStr.length() - 2;
                edgeStr.delete(remove, remove + 2);
                edgeStr.append(" }\n");
            }
        }

        System.out.println(edgeStr);
        DebugDumpEdges();
    }

    public void DebugDumpEdges() {
        StringBuilder edgeStr = new StringBuilder();

        edgeStr.append("\nUnPebbled Edges:");
        for (int v = 0; v < pebblerGraph.vertices.size(); v++) {
            if (!pebblerGraph.vertices.get(v).edges.isEmpty()) {
                boolean containsEdge = false;
                for (PebblerHyperEdge<hypergraph.EdgeAnnotation> edge : pebblerGraph.vertices.get(v).edges) {
                    if (!edge.IsFullyPebbled() && v == Collections.min(edge.sourceNodes)) {
                        containsEdge = true;
                        break;
                    }
                }

                if (containsEdge) {
                    edgeStr.append(v + ": {");
                    for (PebblerHyperEdge<hypergraph.EdgeAnnotation> edge : pebblerGraph.vertices.get(v).edges) {
                        if (!edge.IsFullyPebbled() && v == Collections.min(edge.sourceNodes)) {
                            edgeStr.append(" { ");

                            for (int s : edge.sourceNodes) {
                                edgeStr.append(s + " ");
                            }
                            edgeStr.append("} -> " + edge.targetNode + ", ");
                        }
                    }
                }
                
                int remove = edgeStr.length() - 2;
                
                edgeStr.delete(remove, remove + 2);
                edgeStr.append(" }\n");
            }
        }

        edgeStr.append("\nPebbled Edges:");
        for (int v = 0; v < pebblerGraph.vertices.size(); v++) {
            if (pebblerGraph.vertices.get(v).edges.Any()) {
                boolean containsEdge = false;
                for (PebblerHyperEdge<hypergraph.EdgeAnnotation> edge : pebblerGraph.vertices.get(v).edges) {
                    if (edge.IsFullyPebbled() && v == Collections.min(edge.sourceNodes)) {
                        containsEdge = true;
                        break;
                    }
                }

                if (containsEdge) {
                    edgeStr.append(v + ": {");
                    for (PebblerHyperEdge<hypergraph.EdgeAnnotation> edge : pebblerGraph.vertices.get(v).edges) {
                        if (edge.IsFullyPebbled() && v == Collections.min(edge.sourceNodes)) {
                            edgeStr.append(" { + ");

                            for (int s : edge.sourceNodes) {
                                edgeStr.append(s + " ");
                            }
                            edgeStr.append("} -> " + edge.targetNode + ", ");
                        }
                    }
                }
                int remove = edgeStr.length() - 2;
                edgeStr.delete(remove, remove + 2);
                edgeStr.append(" }\n");
            }
        }

        System.out.println(edgeStr);
    }
}