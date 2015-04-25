package manybranches;

import java.util.Random;

public class BrOne {
	public void func(){
		Random r = new Random();
		int i = r.nextInt(2);
		int j = r.nextInt(2);
		int k = r.nextInt(2);
		BrTwo toCall = new BrTwo();
		if(i == 0){
			if(j == 0){
				if(k == 0){
					toCall.afunc();
				}
				else{
					toCall.bfunc();
				}
			}
			else{
				if(k == 0){
					toCall.cfunc();
				}
				else{
					toCall.dfunc();
				}
			}
		}
		else{
			if(j == 0){
				if(k == 0){
					toCall.efunc();
				}
				else{
					toCall.ffunc();
				}
			}
			else{
				if(k == 0){
					toCall.gfunc();
				}
				else{
					toCall.hfunc();
				}
			}
		}
		
		
	}
}
