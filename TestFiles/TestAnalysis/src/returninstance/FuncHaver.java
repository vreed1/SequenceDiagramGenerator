package returninstance;

import java.util.Random;

public class FuncHaver {
	public Returnable BuildRet(){
		int x = (new Random()).nextInt();
		return new Returnable(x);
	}
}
