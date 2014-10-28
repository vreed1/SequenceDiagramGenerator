package hypergraph;

//
// This class represents a descriptor applied to an edge of the hypergraph.
// Each axiom, theorem, and definition will have ONE static instance of this class.
//
public class EdgeAnnotation
{
    // The string version of the reason that the edge was created.
    private String justification;
    public String getJustification() { return justification; }

    // Has the user indicated that the use of this 
    public boolean active;
    
    public EdgeAnnotation()
    {
        justification = "";
        active = false;
    }

    public EdgeAnnotation(String just, boolean active)
    {
        justification = just;
        this.active = active;
    }

    public boolean IsActive() { return active; }

    @Override
    public String toString()
    {
        return justification;
    }
}
