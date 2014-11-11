package ToBeAnalyzed;

//Class Brian Peterson wrote to analyze.

public class ToBeAnalyzed {
	public static void NotMain(String[] Args){
		int x = 0;
		int y = x;
		for(int j = 0; j < 10; j++){
			x = x+1;
			y = x-2;
		}
	}
}
