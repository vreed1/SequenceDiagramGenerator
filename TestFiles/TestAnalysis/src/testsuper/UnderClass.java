package testsuper;

public class UnderClass extends HigherClass {
	@Override
	public String func(){
		String s = super.func();
		return s + " From Lower Class";
	}
}
