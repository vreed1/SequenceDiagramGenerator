package branchdepth;

import java.util.Random;

public class Br2Main {
	public static void main(String[] args){
		Random r = new Random();
		if(r.nextBoolean()){
			A aInst = new A(r);
			aInst.funcA();
		}
		else{
			B bInst = new B(r);
			bInst.funcB();
		}
	}

}
