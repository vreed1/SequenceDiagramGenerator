package utilities;

import java.util.ArrayList;

public class SetableList<T> extends ArrayList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2372720566229784901L;

	
	public void SetR(T val, int index){
		if(index >= size()){
			while(index > size()){
				add(null);
			}
			add(val);
		}
		else{
			this.set(index, val);
		}
	}
	
	public void SetSize(int val){
		int prevSize = this.size();
		for(int i = val; i < prevSize; i++){
			this.remove(val);
		}
	}
}
