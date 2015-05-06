package sequenceDiagramGenerator.sdedit;

import utilities.Utilities;

public class GenReducerFactory {
	public static GenReducer Build(String[] args){
		int k = Integer.parseInt(Utilities.GetArgument(args, "-kvalue"));
		return new GenReduceByMessageCount(k);
	}
}
