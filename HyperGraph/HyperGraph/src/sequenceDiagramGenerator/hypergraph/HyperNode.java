package sequenceDiagramGenerator.hypergraph;

import java.util.ArrayList;
import java.util.List;


public class HyperNode<T>
{
    public T data;
    public int uniqueId;

    public HyperNode(T d, int i)
    {
        uniqueId = i;
        data = d;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(data.toString());
        sb.append("\t\t\t\t= { ");
        sb.append(uniqueId);
        sb.append("SuccE = {");

        sb.append("} TargetE = { ");

        sb.append(" } } ");

        return sb.toString();
    }
}
