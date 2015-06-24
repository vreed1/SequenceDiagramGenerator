package Automata;

public class FATrans {
	public String theExpr;
	public FANode theDest;
	public FATrans(String expr, FANode dest){
		theExpr = expr;
		theDest = dest;
	}
}
