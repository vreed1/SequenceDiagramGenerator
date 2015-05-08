package sequenceDiagramGenerator;

import java.util.List;

import sequenceDiagramGenerator.sdedit.SDMessage;
import sequenceDiagramGenerator.sdedit.SDObject;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;
import utilities.Utilities;

public class QueryFragment implements Query {
	
	private String theMethod;
	private String theType;
	private QueryMode theMode;
	
	public QueryFragment(String strRep){
		String[] str = strRep.split(":");
		theMethod = "";
		theType = "";
		if(str.length != 3){
			return;
		}
		if(str[0].equals("reject")){
			theMode = QueryMode.Reject;
		}
		else if(str[0].equals("accept")){
			theMode = QueryMode.Accept;
		}
		else if(str[0].equals("filter")){
			theMode = QueryMode.Filter;
		}
		if(str[1].equals("method")){
			theMethod = str[1];
		}
		else if(str[1].equals("type")){
			theType = str[1];
		}
	}
	
	public QueryFragment(String amethod, String atype, QueryMode aMode){
		theMode = aMode;
		theMethod = amethod;
		theType = atype;
	}

	@Override
	public QueryResponse RunOnData(QueryDataContainer toQuery) {
		if(theMode == QueryMode.Reject){
			for(int i = 0; i < toQuery.theListRefTypes.size(); i++){
				String s = toQuery.theListRefTypes.get(i).toString();
				if(theType.equals(s)){
					return QueryResponse.False;
				}
			}
			for(int i = 0; i < toQuery.theListCalledMethods.size(); i++){
				String s = Utilities.getMethodString(toQuery.theListCalledMethods.get(i));
				if(theMethod.equals(s)){
					return QueryResponse.False;
				}
			}
		}
		if(theMode ==QueryMode.Filter){
			for(int i = 0; i < toQuery.theListRefTypes.size(); i++){
				String s = toQuery.theListRefTypes.get(i).toString();
				if(theType.equals(s)){
					return QueryResponse.Filter;
				}
			}
		}
		return QueryResponse.True;
	}

	@Override
	public QueryResponse RunOnMethodName(String outerMethodName) {
		if(theMode == QueryMode.Filter){
			if(outerMethodName.equals(theMethod)){
				return QueryResponse.Filter;
			}
		}
		else if(theMode == QueryMode.Reject){
			if(outerMethodName.equals(theMethod)){
				return QueryResponse.False;
			}
		}
		return QueryResponse.True;
	}

	@Override
	public QueryResponse CheckFinishedDiagram(SequenceDiagram sd) {
		if(theMode == QueryMode.Accept){
			List<SDObject> lObjs = sd.GetObjects();
			for(int i = 0; i < lObjs.size(); i++){
				String s = lObjs.get(i).GetTypeName();
				if(theType.equals(s)){
					return QueryResponse.True;
				}
			}
			List<SDMessage> listMsg = sd.GetMessages();
			for(int i = 0; i < listMsg.size(); i++){
				String s = listMsg.get(i).GetFullMethodName();
				if(theMethod.equals(s)){
					return QueryResponse.True;
				}
			}
			return QueryResponse.False;
		}
		if(theMode == QueryMode.Reject){
			List<SDObject> lObjs = sd.GetObjects();
			for(int i = 0; i < lObjs.size(); i++){
				String s = lObjs.get(i).GetTypeName();
				if(theType.equals(s)){
					return QueryResponse.False;
				}
			}
			List<SDMessage> listMsg = sd.GetMessages();
			for(int i = 0; i < listMsg.size(); i++){
				String s = listMsg.get(i).GetFullMethodName();
				if(theMethod.equals(s)){
					return QueryResponse.False;
				}
			}
			return QueryResponse.True;
		}
		return QueryResponse.True;
	}
	
}
