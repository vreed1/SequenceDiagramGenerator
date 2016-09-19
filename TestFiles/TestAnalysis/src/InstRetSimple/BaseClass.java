package InstRetSimple;

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
		TopClass bc = new BadClass();
		Encaps endval = new Encaps();
		if(val == 3){
			endval = ((BadClass)bc).BadMethod();
		}
		System.out.println("Blah" + Integer.toString(endval.getVal()));
	}
}
