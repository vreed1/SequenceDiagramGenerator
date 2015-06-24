package Automata;

public interface TokenOrAutomata {
	public enum ToAType{
		Token,
		Automata
	};
	public ToAType GetType();
}
