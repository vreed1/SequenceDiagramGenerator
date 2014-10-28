package pebbler;

import java.util.ArrayList;
import java.util.List;

public class PebblerHyperNode<T, A>
{
    public T data; // Original Hypergraph representation
    public int id; // index of original hypergraph node

    public List<Integer> nodes;
    public List<PebblerHyperEdge<A>> edges;

    // Coloration of the edge when pebbled
    //public PebblerColorType pebble;

    // Whether the node has been pebbled or not.
    public boolean pebbled;

    public PebblerHyperNode(T thatData, int thatId)
    {
        id = thatId;
        data = thatData;
        pebbled = false;

        edges = new ArrayList<PebblerHyperEdge<A>>();
    }

    public void AddEdge(PebblerHyperEdge<A> edge)
    {
        edges.add(edge);
    }

    public void AddEdge(A annotation, List<Integer> src, int target)
    {
        edges.add(new PebblerHyperEdge<A>(src, target, annotation));
    }

    @Override
    public String toString()
    {
        String retS = data.toString() + "\t\t\t\t= { ";

        retS += id + ", Pebbled(" + pebbled + "), ";
        retS += "SuccN={";
        for (int n : nodes) retS += n + ",";
        if (nodes.size() != 0) retS = retS.substring(0, retS.length() - 1);
        retS += "}, SuccE = { ";
        for (PebblerHyperEdge<A> edge : edges) { retS += edge.toString() + ", "; }
        if (edges.size() != 0) retS = retS.substring(0, retS.length() - 2);
        retS += " } }";

        return retS;
    }
}
