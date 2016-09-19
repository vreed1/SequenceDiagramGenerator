package InstRetSimple;

public class BadClass extends TopClass{
	public Encaps BadMethod(){
		Encaps ret = new Encaps();
		ret.setVal(4);
		return ret;
	}
}
