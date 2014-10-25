package pebbler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hypergraph.*;
import utilities.*;

//
// Implements a multi-hashtable in which an entry may appear more than once in the table.
// That is, a path contains potentially many source nodes. For each source node, we hash and add the path to the table.
// Hence, a path with n source nodes is hashed n times; this allows for fast access
// Collisions are thus handled by chaining
//
public class HyperEdgeMultiMap<A>
{
    private int TABLE_SIZE;
    private List<List<PebblerHyperEdge<A>>> table;
    public int size;

    // The actual hypergraph for reference purposes only when adding edges (check for intrinsic)
    private hypergraph.Hypergraph<GeometryTutorLib.ConcreteAST.GroundedClause, hypergraph.EdgeAnnotation> graph;
    public void SetOriginalHypergraph(hypergraph.Hypergraph<GeometryTutorLib.ConcreteAST.GroundedClause, hypergraph.EdgeAnnotation> g) { graph = g; }

    // If the user specifies the size, we will never have to rehash
    public HyperEdgeMultiMap(int sz)
    {
        size = 0;
        TABLE_SIZE = sz;
        
        table = new ArrayList<List<PebblerHyperEdge<A>>>(TABLE_SIZE);
    }

    //
    // Add the PebblerHyperEdge to all source node hash values
    //
    public void Put(PebblerHyperEdge<A> edge)
    {
        // Analyze the edge to determine if it is a mixed edge; all edges are
        // such that the target is greater than or less than all source nodes
        // Find the minimum non-intrinsic node (if it exists)
        Collections.sort(edge.sourceNodes);
        int minSrc = Collections.max(edge.sourceNodes);
        for (int src : edge.sourceNodes)
        {
            if (!graph.vertices.get(src).data.IsIntrinsic() || !graph.vertices.get(src).data.IsAxiomatic())
            {
                minSrc = src;
            }
        }
        int maxSrc = Collections.max(edge.sourceNodes);
        if (minSrc < edge.targetNode && edge.targetNode < maxSrc)
        {
            throw new IllegalArgumentException("A mixed edge was pebbled as valid: " + edge);
        }

        //Java does not natively have long-indexed arrays
        //Arrays limited to size of integer.MAX_VALUE
        //Since TABLE_SIZE was declared as an int, assuming it is okay to make hashVal an int
        int hashVal = (edge.targetNode % TABLE_SIZE);

        if (table.get(hashVal) == null)
        {
            table.set(hashVal, new ArrayList<PebblerHyperEdge<A>>());
        }

        Utilities.AddUnique(table.get(hashVal), edge);

        size++;
    }

    // Another option to acquire the pertinent problems
    public List<PebblerHyperEdge<A>> GetBasedOnGoal(int goalNodeIndex)
    {
        if (goalNodeIndex < 0 || goalNodeIndex >= TABLE_SIZE)
        {
            throw new IllegalArgumentException("HyperEdgeMultimap::Get::key(" + goalNodeIndex + ")");
        }

        return table.get(goalNodeIndex);
    }

    @Override
    public String toString()
    {
        String retS = "";

        for (int ell = 0; ell < TABLE_SIZE; ell++)
        {
            if (table.get(ell) != null)
            {
                retS += ell + ":\n";
                for (PebblerHyperEdge<A> PebblerHyperEdge : table.get(ell))
                {
                    retS += PebblerHyperEdge.toString() + "\n";
                }
            }
        }

        return retS;
    }
}
