package Automata;

public class Token implements TokenOrAutomata {
	public ToAType GetType(){return ToAType.Token;}
	public String value;
	public Token(String s){
		value = s;
	}
}
