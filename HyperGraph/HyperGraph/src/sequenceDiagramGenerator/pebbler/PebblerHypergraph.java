package sequenceDiagramGenerator.pebbler;

import java.util.ArrayList;
import java.util.List;


//
// A reduced version of the original hypergraph that provides simple pebbling and exploration
//
public class PebblerHypergraph<T, A>
{
    // The main graph data structure
    private ArrayList<PebblerHyperNode<T, A>> vertices;
    public ArrayList<PebblerHyperNode<T, A>> getVertices() { return vertices; }
    public int NumVertices() { return vertices.size(); }
    
    //
    // Returns the complete set of pebbled (marked) nodes in this hypergraph.
    //
    public List<Integer> GetPebbledNodes()
    {
        List<Integer> indices = new ArrayList<Integer>();

        for (int n = 0 ; n < vertices.size(); n++)
        {
            if (vertices.get(n).pebbled)
            {
                indices.add(n);
            }
        }

        return indices;
    }

    public PebblerHypergraph(ArrayList<PebblerHyperNode<T, A>> inputVertices)
    {
        vertices = inputVertices;
    }
}
