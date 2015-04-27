package sequenceDiagramGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;

import sequenceDiagramGenerator.Query.QueryResponse;
import utilities.Utilities;

public class Query {
	
	public enum QueryMode{
		Reject,
		Accept,
		Filter
	}
	
	public enum QueryResponse{
		True,
		False,
		Filter
	}
	
	private List<String> listRejectTypes;
	private List<String> listAcceptTypes;
	private List<String> listFilterTypes;
	private List<String> listRejectMethods;
	private List<String> listAcceptMethods;
	private List<String> listFilterMethods;
	private EnumSet<QueryMode> theMode;
	
	public static Query FromFile(String fileName){
		File f = new File(fileName);
		if(!f.exists() || f.isDirectory()){return new Query("");}
		try {
			Scanner s = new Scanner(f).useDelimiter("\\Z");
			String filecontents = s.next();
			s.close();
			return new Query(filecontents);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return new Query("");
		
	}
	
	public Query(String strRep){
		listRejectTypes = new ArrayList<String>();
		listAcceptTypes = new ArrayList<String>();
		listFilterTypes = new ArrayList<String>();
		listRejectMethods = new ArrayList<String>();
		listAcceptMethods = new ArrayList<String>();
		listFilterMethods = new ArrayList<String>();
		String[] str = strRep.split(" ");
		if(str.length == 0){
			return;
		}
		theMode = EnumSet.noneOf(QueryMode.class);
		if(str[0].contains("r")){
			theMode = EnumSet.of(QueryMode.Reject);
			for(int i = 1; i < str.length; i++){
				String[] subSplit = str[i].split(":");
				if(subSplit[0].equals("rejectmethod")){
					listRejectMethods.add(subSplit[1]);
				}
				else if(subSplit[0].equals("rejecttype")){
					listRejectTypes.add(subSplit[1]);
				}
			}
		}
		if(str[0].contains("a")){
			theMode = EnumSet.of(QueryMode.Accept);
			for(int i = 1; i < str.length; i++){
				String[] subSplit = str[i].split(":");
				if(subSplit[0].equals("acceptmethod")){
					listAcceptMethods.add(subSplit[1]);
				}
				else if(subSplit[0].equals("accepttype")){
					listAcceptTypes.add(subSplit[1]);
				}
			}
		}
		if(str[0].contains("f")){
			theMode.add(QueryMode.Filter);
			for(int i = 1; i < str.length; i++){
				String[] subSplit = str[i].split(":");
				if(subSplit[0].equals("filtermethod")){
					listFilterMethods.add(subSplit[1]);
				}
				else if(subSplit[0].equals("filtertype")){
					listFilterTypes.add(subSplit[1]);
				}
			}
		}
	}
	
	public QueryResponse RunOnData(QueryDataContainer toQuery){
		if(theMode.contains(QueryMode.Accept)){
			for(int i = 0; i < toQuery.theListRefTypes.size(); i++){
				String s = toQuery.theListRefTypes.get(i).toString();
				if(listAcceptTypes.contains(s)){
					return QueryResponse.True;
				}
			}
			for(int i = 0; i < toQuery.theListCalledMethods.size(); i++){
				String s = Utilities.getMethodString(toQuery.theListCalledMethods.get(i));
				if(listAcceptMethods.contains(s)){
					return QueryResponse.True;
				}
			}
			return QueryResponse.False;
		}
		if(theMode.contains(QueryMode.Reject)){
			for(int i = 0; i < toQuery.theListRefTypes.size(); i++){
				String s = toQuery.theListRefTypes.get(i).toString();
				if(listRejectTypes.contains(s)){
					return QueryResponse.False;
				}
			}
			for(int i = 0; i < toQuery.theListCalledMethods.size(); i++){
				String s = Utilities.getMethodString(toQuery.theListCalledMethods.get(i));
				if(listRejectMethods.contains(s)){
					return QueryResponse.False;
				}
			}
			return QueryResponse.True;
		}
		if(theMode.contains(QueryMode.Filter)){
			for(int i = 0; i < toQuery.theListRefTypes.size(); i++){
				String s = toQuery.theListRefTypes.get(i).toString();
				if(listRejectTypes.contains(s)){
					return QueryResponse.Filter;
				}
			}
		}
		return QueryResponse.True;
	}

	public QueryResponse RunOnMethodName(String outerMethodName) {
		if(theMode.contains(QueryMode.Filter) || theMode.contains(QueryMode.Reject)){
			if(listRejectMethods.contains(outerMethodName)){
				return QueryResponse.False;
			}
			if(listFilterMethods.contains(outerMethodName)){
				return QueryResponse.Filter;
			}
			return QueryResponse.True;
		}
		if(theMode.contains(QueryMode.Accept)){
			if(listAcceptMethods.contains(outerMethodName)){
				return QueryResponse.True;
			}
			return QueryResponse.False;
		}
		return QueryResponse.True;
	}
}
