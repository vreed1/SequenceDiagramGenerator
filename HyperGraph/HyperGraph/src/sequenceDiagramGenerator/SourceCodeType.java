package sequenceDiagramGenerator;

public class SourceCodeType
{
    private int uniqueId;
    public soot.Type theSootType;

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
    
    @Override
    public boolean equals(Object other){
    	if(other instanceof SourceCodeType){
    		SourceCodeType anOther = (SourceCodeType)other;
    		return anOther.theSootType.equals(this.theSootType);
    	}
    	return false;
    }
    
    @Override
    public String toString(){
    	if(theSootType == null){
    		return super.toString();
    	}
    	return theSootType.getClass().getName();
    }
}
