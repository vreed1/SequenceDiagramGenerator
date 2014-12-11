package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import sequenceDiagramGenerator.sdedit.MessageOpt;
import sequenceDiagramGenerator.sdedit.SDMessage;
import sequenceDiagramGenerator.sdedit.SDObject;

public class DiagramMessageAggregator
{
	private List<SourceCodeType> sources;
	private SourceCodeType target;
	private String message;
	
    public DiagramMessageAggregator(ArrayList<SourceCodeType> srcs, SourceCodeType targ, String msg)
    {
    	sources = srcs;
    	target = targ;
    	message = msg;
    }
    
    //
    // Acquire all SD objects for this particular message
    //
	public List<SDObject> GetSDObjects()
	{
	    ArrayList<SDObject> names = new ArrayList<SDObject>();
	    
    	for (SourceCodeType src : sources)
    	{
            names.add(new SDObject(src.getObjectName(), src.getClassName()));
    	}
    	
        names.add(new SDObject(target.getObjectName(), target.getClassName()));
        
        return names;
	}
    
	//
	// Acquire the message for this particular edge / Message
	//
	public SDMessage GenerateSDEditMessage()
    {
		String callee = "";
		if(this.sources.size() > 0){
			callee = this.sources.get(0).getObjectName();
		}
		                     // caller              // message    // options : TO BE Completed
        return new SDMessage(target.getObjectName(), this.message, ConstructMessage(callee, "a", "", "c"));
    }
    
    protected Dictionary<MessageOpt, String> ConstructMessage(String callee, String answer, String specifier, String mnemonic)
    {    
        Dictionary<MessageOpt, String> msg = new Hashtable<MessageOpt, String>();
        
        if (!callee.isEmpty()) msg.put(MessageOpt.CALLEE, callee);
        if (!answer.isEmpty()) msg.put(MessageOpt.ANSWER, answer);
        if (!specifier.isEmpty()) msg.put(MessageOpt.SPECIFIER, specifier);
        if (!mnemonic.isEmpty()) msg.put(MessageOpt.MNEMONIC, mnemonic);

        return msg;
    }
}
