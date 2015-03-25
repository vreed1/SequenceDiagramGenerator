package sequenceDiagramGenerator.sdedit;
import java.util.Dictionary;

import soot.SootMethod;


public class SDMessage
{
    // <caller>[<s>]:<answer>=<callee>[m].<message>
    // Only <caller>:.<message> is required
    //private String caller;
    //private String callee;
	private SDObject caller;
	private SDObject callee;
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
    
    public SDMessage(SDObject sdCaller, SDObject sdCallee, SootMethod message, boolean aIsSuper){
    	this.caller = sdCaller;
    	this.callee = sdCallee;
    	this.message = message.getName();
    	isConstruction = false;
    	isSuper = aIsSuper;
    	if(isSuper){
    		this.message = "super";
    	}
    	else if(this.message.equals("<init>")){
    		if(this.caller.equals(this.callee)){
    			this.message = "super";
    		}
    		else{
    			this.message = "new";
    			isConstruction = true;
    		}
    	}
    }
    
    
    @Override
    public String toString() {
    	
        StringBuilder msg = new StringBuilder();
        
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
