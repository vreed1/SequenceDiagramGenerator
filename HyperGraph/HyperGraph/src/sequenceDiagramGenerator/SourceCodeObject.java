package sequenceDiagramGenerator;

public class SourceCodeObject extends SourceCodeType
{
    protected String objectName;
    public String getObjectName() { return objectName; }
   
    public SourceCodeObject(String name, String type, int id)
    {
    	super(type, id);
    	
        this.objectName = name;
    }
}
