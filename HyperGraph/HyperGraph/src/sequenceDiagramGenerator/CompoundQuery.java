package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.List;

import sequenceDiagramGenerator.sdedit.SequenceDiagram;
import utilities.Tokenizer;

public class CompoundQuery implements Query {
	
	public enum QueryOperator{
		And,
		Or
	}
	
	private QueryFragment theFragment;
	private QueryOperator theOperator;
	private CompoundQuery theRest;

	public CompoundQuery(String strrep){
		List<String> tokOpts = new ArrayList<String>();
		tokOpts.add("||");
		tokOpts.add("&&");
		//tokOpts.add("{");
		//tokOpts.add("}");
		//tokOpts.add("*FILTER*");
		
		Tokenizer tk = new Tokenizer(tokOpts);
		
		List<String> ltok = tk.Tokenize(strrep);
		
		Construct(ltok);
		
	}
	
	public CompoundQuery(List<String> tokens){

		Construct(tokens);
	}
	
	private void Construct(List<String> tokens){
		if(tokens.size() == 0){
			theFragment = new QueryFragment("");
		}
		else if(tokens.size() == 1){
			theFragment = new QueryFragment(tokens.get(0));
		}
		else if(tokens.size() == 2){
			throw new RuntimeException("bad query");
		}
		else{
			theFragment = new QueryFragment(tokens.get(0));
			if(tokens.get(1).equals("||")){
				theOperator = QueryOperator.Or;
			}
			else if(tokens.get(1).equals("&&")){
				theOperator = QueryOperator.And;
			}
			else{
				throw new RuntimeException("Unk Operator");
			}
			tokens.remove(0);
			tokens.remove(0);
			theRest = new CompoundQuery(tokens);
		}
	}
	
	private QueryResponse respond(QueryResponse first, QueryResponse second){
		if(first == QueryResponse.Filter){
			return first;
		}		
		if(second == QueryResponse.Filter){
			return second;
		}
		if(theOperator == QueryOperator.Or){
			if(first == QueryResponse.True){
				return first;
			}
			return second;
		}
		if(theOperator == QueryOperator.And){
			if(first == QueryResponse.True){
				return second;
			}
			return first;
		}
		throw new RuntimeException("Bad query");
	}

	@Override
	public QueryResponse RunOnData(QueryDataContainer toQuery) {
		return QueryResponse.True;
	}

	@Override
	public QueryResponse RunOnMethodName(String outerMethodName) {
		return QueryResponse.True;
	}

	@Override
	public QueryResponse CheckFinishedDiagram(SequenceDiagram sd) {
		QueryResponse first = theFragment.CheckFinishedDiagram(sd);

		if(theRest == null){
			return first;
		}
		QueryResponse second = theRest.CheckFinishedDiagram(sd);
		return respond(first, second);
	}
}
