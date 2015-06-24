package Automata;

public class Automata implements TokenOrAutomata{
	public ToAType GetType(){return ToAType.Automata;}
	
	public FANode startNode;
	public FANode acceptNode;
	
	public Automata(){
		
	}
	
}
