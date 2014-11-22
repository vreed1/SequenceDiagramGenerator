package sequenceDiagramGenerator.hypergraph;

//
// This class represents a descriptor applied to an edge of the hypergraph.
// Each method signature will be an edge with the name of the method being the annotation.
//
public class EdgeAnnotation
{
    // The string version of the reason that the edge was created.
    private String methodName;
    public String getMethodName() { return methodName; }

    // Is this edge allowed to be considered in the analysis? 
    public boolean active;
    
    public soot.SootMethod theSootMethod;
    
    public EdgeAnnotation()
    {
        methodName = "";
        active = false;
    }

    public EdgeAnnotation(String just, boolean active)
    {
        methodName = just;
        this.active = active;
    }

    public boolean IsActive() { return active; }

    @Override
    public String toString()
    {
        return methodName;
    }
}
