package classvar;

import java.util.Random;

public class CVar {

	private Random x;
	public CVar(Random r) {
		x = r;
	}

	public void func() {
		System.out.println(x.nextInt());
	}

}
