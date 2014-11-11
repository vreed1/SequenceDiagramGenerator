package ToBeAnalyzed;

import java.util.Random;

//Brian Peterson
//This class contains a problem I've commented out temporarily
//for some reason when I added the Random class to test branching
//function traversal stopped working in this class.
//Bizarrely, it did NOT stop working in ThirdClass, which also contains
//a Random instance.  I will ahve to look at this further.

public class SecondClass {
	private String aSecondCVar;
	public SecondClass(){
		int x = 5;
		int y = 3;
		//Random r = new Random();
		//y = r.nextInt();
		if(y > 2){
			funcone();
		}
		else{
			functwo();
		}
	}
	
	private void funcone(){
		aSecondCVar = "Hello";
	}
	
	private void functwo(){
		aSecondCVar = "World";
	}
	
	@Override
	public String toString(){
		return aSecondCVar;
	}
}
