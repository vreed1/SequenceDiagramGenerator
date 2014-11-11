package ToBeAnalyzed;

//Class Brian Peterson wrote to do dummy analysis of.

public class NewOption {

	private String aClassVariable;
	private boolean aConstructVariable;
	private SecondClass aSecondInstance;
	private ThirdClass aThirdInstance;
	
	public NewOption(String anArg)
	{
		aClassVariable = anArg;
		ConstructHelper();
		aSecondInstance = new SecondClass();
	}
	
	private void ConstructHelper(){
		aConstructVariable = true;
	}
	
	public String aFunc(){
		aThirdInstance = new ThirdClass();
		return aSecondInstance.toString();
	}
}
