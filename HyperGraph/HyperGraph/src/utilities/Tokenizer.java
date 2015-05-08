package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Tokenizer {
	private List<String> definedTokens;
	
	public Tokenizer(List<String> in_tokens){
		definedTokens = in_tokens;
		Collections.sort(definedTokens, new ByLen());
	}
	
	public List<String> Tokenize(String input){
		String s = input;
		List<String> ret = new ArrayList<String>();
		if(s == null){return ret;}
		
		StringBuilder nontoken = new StringBuilder();
		while(s.length() > 0){
			s = s.trim();
			boolean tokenFound = false;
			for(int i = 0; i < definedTokens.size(); i++){
				if(s.startsWith(definedTokens.get(i))){
					if(nontoken.length() > 0){
						ret.add(nontoken.toString().trim());
						nontoken = new StringBuilder();
					}
					s = s.substring(definedTokens.get(i).length());
					ret.add(definedTokens.get(i));
					tokenFound = true;
					break;
				}
			}
			if(!tokenFound){
				nontoken.append(s.charAt(0));
				s = s.substring(1);
			}
		}
		if(nontoken.length() > 0){
			ret.add(nontoken.toString());
		}
		return ret;
	}
	
	
	private class ByLen implements Comparator<String>{

		@Override
		public int compare(String x, String y) {
			if(x.length() < y.length()){
				return 1;
			}
			else if(x.length() > y.length()){
				return -1;
			}
			return 0;
		}
		
	}
}
