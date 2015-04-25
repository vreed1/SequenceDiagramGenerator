package Recursion;

public class ObjRec {
	private int times = 5;
	private int ret = 0;
	public ObjRec(int c, int r){
		times = c;
		ret = r;
	}
	public int Rec(){
		if(times > 0){
			times--;
			return Rec();
		}
		return ret;
	}
}
