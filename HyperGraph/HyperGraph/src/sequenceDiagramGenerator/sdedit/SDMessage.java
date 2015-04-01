package sequenceDiagramGenerator.sdedit;
import java.util.Dictionary;
import java.util.Map;

import soot.SootMethod;


public class SDMessage
{
    // <caller>[<s>]:<answer>=<callee>[m].<message>
    // Only <caller>:.<message> is required
    //private String caller;
    //private String callee;
	//private SDObject caller;
	//private SDObject callee;
	
	private int callerID;
	private int calleeID;
	
    private String answer;
    private String message;
    private String specifier;
    private String mnemonic;
    private boolean isConstruction;
    private boolean isSuper;
    
    
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
    			message,specifier,mnemonic,isConstruction,isSuper);
    }
    
    private SDMessage(
    		int aCallerID,
    		int aCalleeID,
    		String aAnswer,
    		String aMessage,
    		String aSpecifier,
    		String aMnemonic,
    		boolean aIsCons,
    		boolean aIsSuper){
    	callerID = aCallerID;
    	calleeID = aCalleeID;
    	answer = aAnswer;
    	message = aMessage;
    	specifier = aSpecifier;
    	mnemonic = aMnemonic;
    	isConstruction = aIsCons;
    	isSuper = aIsSuper;
    }
    
    
    public SDMessage(SDObject sdCaller, SDObject sdCallee, SootMethod message, boolean aIsSuper){
    	//this.caller = sdCaller;
    	//this.callee = sdCallee;
    	this.calleeID = sdCallee.ID;
    	this.callerID = sdCaller.ID;
    	this.message = message.getName();
    	isConstruction = false;
    	isSuper = aIsSuper;
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
        msg.append(String.format(".%s", message));
        
        return msg.toString();
    	
    }
}
