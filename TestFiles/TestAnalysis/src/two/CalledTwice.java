package two;

public class CalledTwice {
	private int val;
	private static int valSource = 0;
	public CalledTwice(){
		val = valSource;
		valSource++;
	}
	public int Get(){
		return val;
	}
}
