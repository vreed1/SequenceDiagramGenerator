package branchdepth;

import java.util.Random;

public class B {
	boolean x;
	public B(Random r){
		x = r.nextBoolean();
	}
	public void funcB(){
		if(x){
			E eInst = new E();
			eInst.funcE();
		}
		else{
			F fInst = new F();
			fInst.funcF();
		}
	}
}
