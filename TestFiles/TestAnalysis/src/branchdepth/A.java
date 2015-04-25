package branchdepth;

import java.util.Random;

public class A {
	boolean x;
	public A(Random r){
		x = r.nextBoolean();
	}
	public void funcA(){
		if(x){
			C cInst = new C();
			cInst.funcC();
		}
		else{
			D dInst = new D();
			dInst.funcD();
		}
	}
}
