
public class One {

	private Two[] theTwos;
	private int theInt;
	public One(){
		theInt = 3;
	}
	
	public void MakeTwos(){
		theTwos = new Two[theInt];
		for(int i = 0; i < theTwos.length; i++){
			theTwos[i] = new Two(i);
		}
	}
}
