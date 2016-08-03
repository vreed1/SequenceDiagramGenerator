package sequenceDiagramGenerator.sdedit;
import java.util.Map;

import org.json.simple.JSONObject;

import sequenceDiagramGenerator.sdedit.SDObject.TaintState;
import soot.SootMethod;
import utilities.Utilities;


public class SDMessage
{
    // <caller>[<s>]:<answer>=<callee>[m].<message>
    // Only <caller>:.<message> is required
    //private String caller;
    //private String callee;
	//private SDObject caller;
	//private SDObject callee;
	
	public int callerID;
	public int calleeID;
	
	private TaintState tState = TaintState.Safe;
	
    private String answer;
    private String message;
    private String specifier;
    private String mnemonic;
    private boolean isConstruction;
    private boolean isSuper;
    
    private String fullMethodName;

    private int callLevel;
    
    private int finalLevel = 0;
    
    // Simplest possible message <caller>:.<message>
//    public SDMessage (String caller, String message, Dictionary<MessageOpt, String> opts)
//    {
//        this.caller = caller;
//        this.message = message;
//        
//        this.callee = opts.get(MessageOpt.CALLEE);
//        this.answer = opts.get(MessageOpt.ANSWER);
//        this.mnemonic = opts.get(MessageOpt.MNEMONIC);
//        this.specifier = opts.get(MessageOpt.SPECIFIER);
//    }
    
    public SDMessage clone(){
    	return new SDMessage(callerID, calleeID, answer,
    			message,specifier,mnemonic,isConstruction,
    			isSuper, callLevel, fullMethodName, tState);
    }
    
    private SDMessage(
    		int aCallerID,
    		int aCalleeID,
    		String aAnswer,
    		String aMessage,
    		String aSpecifier,
    		String aMnemonic,
    		boolean aIsCons,
    		boolean aIsSuper,
    		int lvl,
    		String fMethodName,
    		TaintState inTState){
    	callerID = aCallerID;
    	calleeID = aCalleeID;
    	answer = aAnswer;
    	message = aMessage;
    	specifier = aSpecifier;
    	mnemonic = aMnemonic;
    	isConstruction = aIsCons;
    	isSuper = aIsSuper;
    	callLevel = lvl;
    	fullMethodName = fMethodName;
    	tState = inTState;
    }
    
    
    public SDMessage(SDObject sdCaller, 
    		SDObject sdCallee, 
    		SootMethod message, 
    		boolean aIsSuper,
    		int lvl,
    		TaintState inTState){
    	//this.caller = sdCaller;
    	//this.callee = sdCallee;
    	this.calleeID = sdCallee.ID;
    	this.callerID = sdCaller.ID;
    	this.message = message.getName();
    	this.tState = inTState;
    	isConstruction = false;
    	isSuper = aIsSuper;
    	callLevel = lvl;
    	fullMethodName = Utilities.getMethodString(message);
    	//this.specifier = Integer.toString(level);
    	if(isSuper){
    		this.message = "super";
    	}
    	else if(this.message.equals("<init>")){
    		if(sdCaller.equals(sdCallee)){
    			this.message = "super";
    		}
    		else{
    			this.message = "new";
    			isConstruction = true;
    		}
    	}
    }
    
    public SDMessage(JSONObject jobj){

    	finalLevel = Integer.parseInt((String)jobj.get("finalLevel"));
    	callLevel = Integer.parseInt((String)jobj.get("callLevel"));
    	callerID = Integer.parseInt((String)jobj.get("callerID"));
    	calleeID = Integer.parseInt((String)jobj.get("calleeID"));
    	
    	if(jobj.containsKey("TaintState")){
			String tString = (String)jobj.get("TaintState");
			if(tString == "Tainted"){
				this.tState = TaintState.Tainted;
			}
			else{
				this.tState = TaintState.Safe;
			}
		}
    	
		
    	answer = (String)jobj.get("answer");
    	message = (String)jobj.get("message");
    	specifier = (String)jobj.get("specifier");
    	mnemonic = (String)jobj.get("mnemonic");
    	fullMethodName = (String)jobj.get("fullMethodName");
    	
    	isConstruction = Boolean.parseBoolean((String)jobj.get("isConstruction"));
    	isSuper = Boolean.parseBoolean((String)jobj.get("isSuper"));
		
    }
    
	public JSONObject toJSONObject(){
		JSONObject topObj = new JSONObject();
		
		topObj.put("finalLevel", Integer.toString(finalLevel));
		topObj.put("callLevel", Integer.toString(callLevel));

		topObj.put("callerID", Integer.toString(callerID));
		topObj.put("calleeID", Integer.toString(calleeID));
		
		topObj.put("TaintState", tState.toString());
		
		topObj.put("answer", answer);
		topObj.put("message", message);
		topObj.put("specifier", specifier);
		topObj.put("mnemonic", mnemonic);
		topObj.put("fullMethodName", fullMethodName);

		topObj.put("isConstruction", Boolean.toString(isConstruction));
		topObj.put("isSuper", Boolean.toString(isSuper));
		
		return topObj;
	}
    
    public String GetFullMethodName(){
    	return fullMethodName;
    }
    
    public void SetTaintState(SequenceDiagram sd, TaintState inTState){
    	this.tState = inTState;
    	if(inTState == TaintState.Tainted){
    		sd.GetObjectFromID(this.calleeID).SetTaintState(sd, inTState);
    		//discuss this point in particular.
    		sd.GetObjectFromID(this.callerID).SetTaintState(sd, inTState);
    	}
    }
    
    public boolean IsTainted(){
    	return this.tState == TaintState.Tainted;
    }
    
    public void SetFinalLevel(int lvl){
    	finalLevel = lvl;
    	this.specifier = Integer.toString(lvl);
    }
    
    public int GetFinalLevel(){
    	return finalLevel;
    }
    
    public int GetCallLevel(){
    	return callLevel;
    }
    
    public boolean isSelfMessage(){
    	return this.callerID == this.calleeID;
    }
  
    public String toString(Map<Integer, SDObject> aMap) {
    	
        StringBuilder msg = new StringBuilder();
        SDObject caller = aMap.get(this.callerID);
        SDObject callee = aMap.get(this.calleeID);
        msg.append(caller.GetName());
        if (specifier != null) {
            msg.append(String.format("[%s]", specifier));
        }
        msg.append(":");
        if (answer != null) {
            msg.append(String.format("%s=", answer));
        }
        if (callee != null) {
            msg.append(callee.GetName());
        }
        if (mnemonic != null) {
            msg.append(String.format("[%s]", mnemonic));
        }
        String localMsg = message;
        if(this.tState == TaintState.Tainted){
        	if(localMsg == "new"){
        		//can't change this message to indicate anything.
        		//oh well, it is still evident in the json.
        	}
        	else{
        	localMsg = localMsg + "_Tainted";
        	}
        }
        msg.append(String.format(".%s", localMsg));
        
        
        return msg.toString();
    	
    }
}
