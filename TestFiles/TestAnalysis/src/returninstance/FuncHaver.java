package returninstance;

import java.util.Random;

public class FuncHaver {
	public Returnable BuildRet(){
		int x = 5;
		return new Returnable(x);
	}
}
