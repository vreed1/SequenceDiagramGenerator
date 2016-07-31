package InstRetBranch;

public class BadEx extends BadClass {
	public Encaps what(Encaps val){
		Encaps x = this.BadMethod();
		x.setVal(val.getVal() + x.getVal());
		return x;
	}
	public Encaps other(Encaps val){
		val.setVal(val.getVal() + 10);
		return this.aFunc(val);
	}
}
