package classvar;

import java.util.Random;

public class CVar {

	private CTwo x;
	public CVar(Random r) {
		x = new CTwo(r);
	}

	public void func() {
		System.out.println(x.r.nextInt() + CTwo.five);
	}

}
