package classvar;

import java.util.Random;

public class CMain {
	public static void main(String[] args){
		Random r = new Random();
		CVar c = new CVar(r);
		c.func();
	}
}
