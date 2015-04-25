package returninstance;

public class RIMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FuncHaver f = new FuncHaver();
		Returnable r = f.BuildRet();
		System.out.println(r.blah);
	}

}
