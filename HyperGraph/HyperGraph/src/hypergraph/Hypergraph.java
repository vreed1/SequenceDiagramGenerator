package hypergraph;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pebbler.*;


public class Hypergraph<T, A>
{
    // The main graph data structure
    private List<HyperNode<T, A>> vertices;
    public List<HyperNode<T,A>> getVertices() { return vertices; }

    public Hypergraph(){
        vertices = new ArrayList<HyperNode<T, A>>();
        edgeCount = 0;
    }

    public Hypergraph(int capacity){
        vertices = new ArrayList<HyperNode<T, A>>(capacity);
        edgeCount = 0;
    }


    public int Size() { return vertices.size(); }
    private int edgeCount;
    public int EdgeCount() { return edgeCount; }

    //
    // Integer-based representation of the main hypergraph
    //
    public PebblerHypergraph<T, A> GetPebblerHypergraph() {
        //
        // Strictly create the nodes
        //
        ArrayList<PebblerHyperNode<T, A>> pebblerNodes = new ArrayList<PebblerHyperNode<T, A>>(vertices.size());
        for (int v = 0; v < vertices.size(); v++) {
            pebblerNodes.set(v, vertices.get(v).CreatePebblerNode());
        }

        //
        // Non-redundantly create all hyperedges
        //
        for (int v = 0; v < vertices.size(); v++) {
            for (HyperEdge<A> edge : vertices.get(v).edges) {
                // Only add once to all nodes when this is the 'minimum' source node
                if (v == Collections.min(edge.sourceNodes)) {
                    PebblerHyperEdge<A> newEdge = new PebblerHyperEdge<A>(edge.sourceNodes, edge.targetNode, edge.annotation);
                    for (int src : edge.sourceNodes) {
                        pebblerNodes.get(src).AddEdge(newEdge);
                    }
                }
            }
        }

        return new PebblerHypergraph<T, A>(pebblerNodes);
    }

    /*  
 	public List<Strengthened> GetStrengthenedNodes(List<Integer> indices) {
        List<Strengthened> strengList = new List<Strengthened>();

        for (int index : indices) {
        	T data = vertices.get(index).data;
            if (data is Strengthened) strengList.Add(data as Strengthened);
        }

        return strengList;
    }*/

    //
    // Check if the graph contains this specific grounded clause
    //
    private int ConvertToLocalIntegerIndex(T inputData) {
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).data.equals(inputData)) {
                return i;
            }
        }

        return -1;
    }

    //
    // Return the index of the given node
    //
    public int GetNodeIndex(T inputData) {
        return ConvertToLocalIntegerIndex(inputData);
    }

    //
    // Return the stored node in the graph
    //
    public T GetNode(int id) {
        if (id < 0 || id > vertices.size()) {
            throw new IllegalArgumentException("Unexpected id in hypergraph node access: " + id);
        }

        return vertices.get(id).data;
    }

    //
    // Check if the graph contains this specific grounded clause
    //
    public boolean HasNode(T inputData) {
        for (HyperNode<T, A> vertex : vertices) {
            if (vertex.data.equals(inputData)) return true;
        }

        return false;
    }

    //
    // Check if the graph contains this specific grounded clause
    //
    public T GetNode(T inputData) {
        for (HyperNode<T, A> vertex : vertices) {
            if (vertex.data.equals(inputData)) return vertex.data;
        }

        return null;
    }

    //
    // Check if the graph contains this specific grounded clause
    //
    public boolean AddNode(T inputData) {
        if (!HasNode(inputData)) {
            vertices.add(new HyperNode<T, A>(inputData, vertices.size())); // <data, id>
            return true;
        }

        return false;
    }

    //
    // Is this edge in the graph (using local, integer-based information)
    //
    private boolean HasLocalEdge(List<Integer> antecedent, int consequent)
    {
        for (int ante : antecedent) {
            if (ante < 0 || ante > vertices.size()) 
                throw new IllegalArgumentException("Index of bounds on local edge: " + ante);
        }

        if (consequent < 0 || consequent > vertices.size()) 
            throw new IllegalArgumentException("Index of bounds on local edge: " + consequent);


        for (HyperNode<T, A> vertex : vertices) {
            for (HyperEdge<A> edge : vertex.edges) {
                if (edge.DefinesEdge(antecedent, consequent)) return true;
            }
        }

        return false;
    }

    //
    // Check if the graph contains an edge defined by a many to one clause mapping
    //
    public boolean HasEdge(List<T> antecedent, T consequent)
    {
        SimpleEntry<List<Integer>, Integer> local = ConvertToLocal(antecedent, consequent);

        return HasLocalEdge(local.getKey(), local.getValue());
    }

    //
    // Convert information to local, integer-based representation
    //
    private SimpleEntry<List<Integer>, Integer> ConvertToLocal(List<T> antecedent, T consequent) {
        List<Integer> localAnte = new ArrayList<Integer>();

        for (T ante : antecedent) {
            int index = ConvertToLocalIntegerIndex(ante);

            if (index == -1) {
                throw new IllegalArgumentException("Source node not found as a hypergraph node: " + ante);
            }

            localAnte.add(index);
        }

        int localConsequent = ConvertToLocalIntegerIndex(consequent);

        if (localConsequent == -1) {
            throw new IllegalArgumentException("Target value referenced not found as a hypergraph node: " + consequent);
        }

        return new SimpleEntry<List<Integer>, Integer>(localAnte, localConsequent);
    }

    //
    // Adding an edge to the graph
    //
    public boolean AddEdge(List<T> antecedent, T consequent, A annotation)
    {
        //
        // Add a local representaiton of this edge to each node in which it is applicable
        //
        if (HasEdge(antecedent, consequent)) return false;

        SimpleEntry<List<Integer>, Integer> local = ConvertToLocal(antecedent, consequent);

        HyperEdge<A> edge = new HyperEdge<A>(local.getKey(), local.getValue(), annotation);

        //System.Diagnostics.Debug.WriteLine("Adding edge: " + edge.ToString());

        for (int src : local.getKey()) {
            vertices.get(src).AddEdge(edge);
        }

        // Add this as a target edge to the target node.
        vertices.get(local.getValue()).AddTargetEdge(edge);
        edgeCount++;

        return true;
    }

    //
    // Adding an edge to the graph based on known indices.
    //
    public boolean AddIndexEdge(List<Integer> antecedent, int consequent, A annotation){
        if (HasLocalEdge(antecedent, consequent)) return false;

        HyperEdge<A> edge = new HyperEdge<A>(antecedent, consequent, annotation);

        for (int src : antecedent) {
            vertices.get(src).AddEdge(edge);
        }

        // Add this as a target edge to the target node.
        vertices.get(consequent).AddTargetEdge(edge);
        edgeCount++;

        return true;
    }

    //
    // Debug output
    //
    public void DebugDumpClauses() {
        System.out.println("All Clauses:\n");

        StringBuilder edgeStr = new StringBuilder();
        for (int v = 0; v < vertices.size(); v++) {
            System.out.println(edgeStr + " " + v + " " + vertices.get(v).data.toString());
        }


        System.out.println("\nEdges: ");
        DumpEdges();
    }

    public void DumpClauseForwardEdges() {
        System.out.println("\n Forward Edges: ");
        DumpEdges();
    }

    private void DumpEdges() {
        StringBuilder edgeStr = new StringBuilder();
        for (int v = 0; v < vertices.size(); v++) {
            if (!vertices.get(v).edges.isEmpty()) {
                edgeStr.append(v + ": {");
                for (HyperEdge<A> edge : vertices.get(v).edges) {
                    edgeStr.append(" { ");
                    for (int s : edge.sourceNodes) {
                        edgeStr.append(s + " ");
                    }
                    edgeStr.append("} -> " + edge.targetNode + ", ");
                }
                int remove = edgeStr.length() - 2;
                edgeStr.delete(remove, remove + 2);
                edgeStr.append(" }\n");
            }
        }
        System.out.println(edgeStr);    	
    }
}
