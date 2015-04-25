package sequenceDiagramGenerator.InstanceInfo;

public class Name {
	public String VariableName;
	public String ClassName;
	public String ClassLocation;
	
	@Override
	public String toString(){
		return ClassLocation + "." + ClassName + "." + VariableName;
	}
}
