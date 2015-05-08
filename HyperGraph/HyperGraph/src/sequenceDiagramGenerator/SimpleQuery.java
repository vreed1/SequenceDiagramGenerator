package sequenceDiagramGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;

import sequenceDiagramGenerator.sdedit.SDMessage;
import sequenceDiagramGenerator.sdedit.SDObject;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;
import utilities.Utilities;

public class SimpleQuery implements Query {
	
	
	private List<String> listRejectTypes;
	private List<String> listAcceptTypes;
	private List<String> listFilterTypes;
	private List<String> listRejectMethods;
	private List<String> listAcceptMethods;
	private List<String> listFilterMethods;
	private EnumSet<QueryMode> theMode;
	
	public static SimpleQuery FromFile(String fileName){
		File f = new File(fileName);
		if(!f.exists() || f.isDirectory()){return new SimpleQuery("");}
		try {
			Scanner s = new Scanner(f).useDelimiter("\\Z");
			String filecontents = s.next();
			s.close();
			return new SimpleQuery(filecontents);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return new SimpleQuery("");
		
	}
	
	public SimpleQuery(
			List<String> in_rejecttypes,
			List<String> in_accepttypes,
			List<String> in_filtertypes,
			List<String> in_rejectmethods,
			List<String> in_acceptmethods,
			List<String> in_filtermethods){
		listRejectTypes = in_rejecttypes;
		listAcceptTypes = in_accepttypes;
		listFilterTypes = in_filtertypes;
		listRejectMethods = in_rejectmethods;
		listAcceptMethods = in_acceptmethods;
		listFilterMethods = in_filtermethods;
	}
	
	public SimpleQuery(String strRep){

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
		}
		if(theMode.contains(QueryMode.Filter)){
			for(int i = 0; i < toQuery.theListRefTypes.size(); i++){
				String s = toQuery.theListRefTypes.get(i).toString();
				if(listFilterTypes.contains(s)){
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
		return QueryResponse.True;
	}
	
	public QueryResponse CheckFinishedDiagram(SequenceDiagram sd){
		if(theMode.contains(QueryMode.Accept)){
			List<SDObject> lObjs = sd.GetObjects();
			for(int i = 0; i < lObjs.size(); i++){
				String s = lObjs.get(i).GetTypeName();
				if(listAcceptTypes.contains(s)){
					return QueryResponse.True;
				}
			}
			List<SDMessage> listMsg = sd.GetMessages();
			for(int i = 0; i < listMsg.size(); i++){
				String s = listMsg.get(i).GetFullMethodName();
				if(listAcceptMethods.contains(s)){
					return QueryResponse.True;
				}
			}
			return QueryResponse.False;
		}
		if(theMode.contains(QueryMode.Reject)){
			List<SDObject> lObjs = sd.GetObjects();
			for(int i = 0; i < lObjs.size(); i++){
				String s = lObjs.get(i).GetTypeName();
				if(listRejectTypes.contains(s)){
					return QueryResponse.False;
				}
			}
			List<SDMessage> listMsg = sd.GetMessages();
			for(int i = 0; i < listMsg.size(); i++){
				String s = listMsg.get(i).GetFullMethodName();
				if(listRejectMethods.contains(s)){
					return QueryResponse.False;
				}
			}
			return QueryResponse.True;
		}
		return QueryResponse.True;
	}
}
