package sequenceDiagramGenerator;

public class SourceCodeType
{
    private int uniqueId;
    private soot.Type theSootType;

    public int getId() { return uniqueId; }

    public SourceCodeType(int id)
    {
        this.uniqueId = id;
    }
    
    //Brian added code below.  It just seemed a like less work
    private static int nextUnused = 0;
    private static int GetNextInt(){
    	return nextUnused++;
    }
    public SourceCodeType(){
    	this.uniqueId = GetNextInt();
    }
}
