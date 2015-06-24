package Automata;

public class AutomataFactory {
	
	public static Automata FromRegex(String regex){
		AutomataTokens at = new AutomataTokens(regex);
		return at.Construct();
	}
	
	public static Automata SimpleTransition(String transitionValue){
		Automata a = new Automata();
		a.startNode = new FANode();
		a.acceptNode = new FANode();
		FATrans aTransaction = new FATrans(transitionValue, a.acceptNode);
		a.startNode.listTrans.add(aTransaction);
		return a;
	}

	public static Automata KleeneStar(Automata a) {

		Automata r = new Automata();
		r.startNode = new FANode();
		r.acceptNode = new FANode();
		
		FATrans t1 = new FATrans("e", a.startNode);
		FATrans t2 = new FATrans("e", r.acceptNode);
		r.startNode.listTrans.add(t1);
		r.startNode.listTrans.add(t2);
		
		FATrans t3 = new FATrans("e", a.startNode);
		FATrans t4 = new FATrans("e", r.acceptNode);
		a.acceptNode.listTrans.add(t3);
		a.acceptNode.listTrans.add(t4);
		
		return r;
	}

	public static Automata Concatenate(Automata p, Automata a) {
		Automata r = new Automata();
		r.startNode = p.startNode;
		FATrans t = new FATrans("e", a.startNode);
		p.acceptNode.listTrans.add(t);
		r.acceptNode = a.acceptNode;
		return r;
	}

	public static Automata Union(Automata p, Automata a) {
		Automata r = new Automata();
		r.startNode = new FANode();
		r.acceptNode = new FANode();
		FATrans t1 = new FATrans("e", p.startNode);
		FATrans t2 = new FATrans("e", a.startNode);
		r.startNode.listTrans.add(t1);
		r.startNode.listTrans.add(t2);
		FATrans t3 = new FATrans("e", r.acceptNode);
		FATrans t4 = new FATrans("e", r.acceptNode);
		p.acceptNode.listTrans.add(t3);
		a.acceptNode.listTrans.add(t4);
		return r;
	}
}
