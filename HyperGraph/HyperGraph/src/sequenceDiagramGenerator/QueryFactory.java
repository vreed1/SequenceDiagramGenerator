package sequenceDiagramGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import utilities.Utilities;

public class QueryFactory {
	
	public enum QueryStyle{
		SimpleQuery,
		CompoundQuery
	}
	private QueryStyle theStyle;
	public QueryFactory(String[] args){
		String queryStyle = Utilities.GetArgument(args, "-querystyle");
		if(queryStyle.equals("SimpleQuery")){
			theStyle = QueryStyle.SimpleQuery;
		}
		theStyle = QueryStyle.CompoundQuery;
	}
	
	public Query FromFileName(String fileName){
		if(fileName == null){return new SimpleQuery("");}
		if(fileName.length() == 0){return new SimpleQuery("");}
		File f = new File(fileName);
		return Build(f);
	}
	
	public Query Build(File f){
		if(!f.exists() || f.isDirectory()){return new SimpleQuery("");}
		try {
			Scanner s = new Scanner(f).useDelimiter("\\Z");
			String filecontents = s.next();
			s.close();
			return Build(filecontents);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return new SimpleQuery("");
	}
	
	public Query Build(String strrep){
		if(theStyle == QueryStyle.CompoundQuery){
			return new CompoundQuery(strrep);
		}
		else if(theStyle == QueryStyle.SimpleQuery){
			return new SimpleQuery(strrep);
		}
		throw new RuntimeException("Not Possible");
	}
}
