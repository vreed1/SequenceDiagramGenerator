package ToBeAnalyzed;

import java.util.Random;

public class ThirdClass {

	private String aThirdVar;
	public ThirdClass(){
		aThirdVar = "Elephant";
		if(aCondFunc()){
			aThirdVar = "Octopus";
		}
	}
	
	private boolean aCondFunc(){
		Random r = new Random();
		return r.nextBoolean();
	}
	
	@Override
	public String toString(){
		return aThirdVar;
	}
}
