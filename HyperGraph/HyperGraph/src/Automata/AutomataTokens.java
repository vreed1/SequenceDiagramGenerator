package Automata;

import java.util.ArrayList;
import java.util.List;

import utilities.Utilities;
import Automata.TokenOrAutomata.ToAType;


public class AutomataTokens{
	
	private List<TokenOrAutomata> theTokens;
	
	private AutomataTokens(List<TokenOrAutomata> aTokens){
		theTokens = aTokens;
	}
	
	public AutomataTokens(String regex){
		theTokens = new ArrayList<TokenOrAutomata>();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < regex.length(); i++){
			char c = regex.charAt(i);
			if(c == '(' || c == ')' || c == '|' || c == ',' || c == '*'){
				if(sb.length() > 0){
					theTokens.add(new Token(sb.toString()));
				}
				String s = "" + c;
				theTokens.add(new Token(s));
			}
			else{
				sb.append(c);
			}
		}
		if(sb.length() > 0){
			theTokens.add(new Token(sb.toString()));
		}
	}
	
	public Automata Construct(){
		if(theTokens.size() == 0){return null;}
		ResolveParens();
		ResolveSymbols();
		ResolveKleeneStars();
		ResolveConcates();
		ResolveUnions();
		return (Automata)theTokens.get(0);
	}
	
	private void ResolveUnions(){
		Automata priorAutomata = null;
		for(int i = 0; i < theTokens.size(); i++){
			TokenOrAutomata ta = theTokens.get(i);
			if(ta.GetType() == ToAType.Token){
				Token t = (Token)ta;
				if(t.value.equals("|")){
					Automata prior = (Automata)theTokens.get(i-1);
					Automata a = (Automata)theTokens.get(i+1);
					i--;
					theTokens.remove(i);
					theTokens.remove(i);
					theTokens.remove(i);
					a = AutomataFactory.Union(priorAutomata, a);
					theTokens.add(i, a);
				}
			}
		}
	}
	
	private void ResolveConcates(){
		Automata priorAutomata = null;
		for(int i = 0; i < theTokens.size(); i++){
			TokenOrAutomata ta = theTokens.get(i);
			if(ta.GetType() == ToAType.Automata){
				Automata a = (Automata)ta;
				if(priorAutomata != null){
					a = AutomataFactory.Concatenate(priorAutomata, a);
					i--;
					theTokens.remove(i);
					theTokens.remove(i);
					theTokens.add(i, a);
					priorAutomata = a;
				}
				else{
					priorAutomata = a;
				}
			}
			else{
				Token t = (Token)ta;
				if(t.value.equals(",")){
					Automata a = (Automata)theTokens.get(i+1);
					i--;
					theTokens.remove(i);
					theTokens.remove(i);
					theTokens.remove(i);
					a = AutomataFactory.Concatenate(priorAutomata, a);
					theTokens.add(i, a);
				}
			}
		}
	}
	
	private void ResolveKleeneStars(){
		for(int i = 0; i < theTokens.size(); i++){
			TokenOrAutomata ta = theTokens.get(i);
			if(ta.GetType() == ToAType.Automata){continue;}
			Token t = (Token)ta;
			if(t.value.equals("*")){
				Automata prior = (Automata)theTokens.get(i-1);
				Automata a = AutomataFactory.KleeneStar(prior);
				i--;
				theTokens.remove(i);
				theTokens.remove(i);
				theTokens.add(i, a);
			}
		}
	}
	
	private void ResolveSymbols(){
		for(int i = 0; i < theTokens.size(); i++){
			TokenOrAutomata ta = theTokens.get(i);
			if(ta.GetType() == ToAType.Automata){continue;}
			Token t = (Token)ta;
			if(t.value.equals("x") || t.value.equals("e") || Utilities.isInteger(t.value)){
				Automata a = AutomataFactory.SimpleTransition(t.value);
				theTokens.remove(i);
				theTokens.add(i, a);
			}
		}
	}
	
	private void ResolveParens(){
		int atIndex = 0;
		while(atIndex < theTokens.size()){
			TokenOrAutomata ta = theTokens.get(atIndex);
			if(ta.GetType() == ToAType.Automata){continue;}
			Token t = (Token)ta;
			if(t.value.equals("(")){
				int depth = 0;
				theTokens.remove(atIndex);
				List<TokenOrAutomata> aTokens = new ArrayList<TokenOrAutomata>();
				while(true){
					ta = theTokens.remove(atIndex);
					t = (Token)ta;
					if(t.value.equals("(")){
						depth++;
						aTokens.add(ta);
					}
					else if(t.value.equals(")")){
						depth--;
						if(depth == -1){
							AutomataTokens inner = new AutomataTokens(aTokens);
							Automata innerauto = inner.Construct();
							theTokens.add(atIndex, innerauto);
							break;
						}
						else{
							aTokens.add(ta);
						}
					}
					else{
						aTokens.add(ta);
					}
				}
			}
			atIndex++;
		}
	}
}