package sequenceDiagramGenerator.sdedit;

import utilities.Utilities;

public class GenReducerFactory {
	public static GenReducer Build(String[] args){
		String reducerMode = Utilities.GetArgument(args, "-rmode");
		if(reducerMode.equals("bymessages"))
		{
			int k = Integer.parseInt(Utilities.GetArgument(args, "-kvalue"));
			return new GenReduceByMessageCount(k);
		}
		else if(reducerMode.equals("bycoverage")){
			int k = Integer.parseInt(Utilities.GetArgument(args, "-kvalue"));
			return new GRMsgCountAndUnique(k);
		}
		else{
			return new NoGenReducer();
		}
	}
}
