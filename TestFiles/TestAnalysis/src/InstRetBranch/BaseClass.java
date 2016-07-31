package InstRetBranch;

import java.util.Random;

public class BaseClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BaseClass base = new BaseClass();
		base.run();
	}

	public void run(){
		
		Random r = new Random();
		int val = r.nextInt(10);
		TopClass tc;
		Encaps endval = new Encaps();
		endval.setVal(-1);
		if(val == 0){
			tc = new TopClass();
			Encaps inVal = new Encaps();
			inVal.setVal(3);
			endval = tc.aFunc(inVal);
		}
		else if(val == 1){
			tc = new BadClass();
			endval = ((BadClass)tc).BadMethod();
		}
		else if(val == 2){
			tc = new BadEx();
			Encaps inVal = new Encaps();
			inVal.setVal(3);
			endval = ((BadEx)tc).other(inVal);
		}
		else if(val == 3){
			tc = new BadEx();
			Encaps inVal = new Encaps();
			inVal.setVal(4);
			endval = ((BadEx)tc).what(inVal);
		}
		else if(val == 4){
			tc = new OKClass();
			Encaps inVal = new Encaps();
			inVal.setVal(3);
			endval = ((OKClass)tc).someFunc(inVal);
		}
		System.out.println("Blah" + Integer.toString(endval.getVal()));
	}
}
