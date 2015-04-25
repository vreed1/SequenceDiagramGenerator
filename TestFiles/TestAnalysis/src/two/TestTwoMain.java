package two;

public class TestTwoMain {
	public static void main(String[] args){
		CalledTwice one, two;
		one = new CalledTwice();
		two = new CalledTwice();
		System.out.println(one.Get());
		System.out.println(two.Get());
	}
}	
