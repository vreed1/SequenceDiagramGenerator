package Automata;

import java.util.ArrayList;
import java.util.List;

public class FANode {
	private static int idgen= 0;
	private int id;
	public List<FATrans> listTrans;
	public FANode(){
		id = idgen;
		idgen++;
		listTrans = new ArrayList<FATrans>();
	}
	public int GetID(){return id;}
	@Override
	public boolean equals(Object o){
		try{
			FANode other = (FANode)o;
			if(other.GetID() == this.id){
				return true;
			}
		}
		catch(ClassCastException ex){
			
		}
		return false;
	}
}
