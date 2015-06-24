package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.List;

public class SecondGenTools {
	private static int findNextStartIndex(
			int fromhere, SequenceDiagram pattern, SequenceDiagram toTest){
		List<SDMessage> listPatternMsg = pattern.GetMessages();
    	List<SDMessage> listTestMsg = toTest.GetMessages();
    	
    	SDMessage pmsg0 = listPatternMsg.get(0);
		
		SDObject ps0 = pattern.GetObjectFromID(pmsg0.callerID);
		SDObject pt0 = pattern.GetObjectFromID(pmsg0.calleeID);
		
		for(int i = fromhere+1; i<listTestMsg.size(); i++){
			SDMessage amsg = listTestMsg.get(i);
			SDObject as = toTest.GetObjectFromID(amsg.callerID);
			SDObject at = toTest.GetObjectFromID(amsg.calleeID);
			if(as.isStatic == ps0.isStatic && at.isStatic == pt0.isStatic){
				return i;
			}
		}
		return -1;
	}
	
	private class LinkObject{
		private int idone, idtwo;
		public LinkObject(int aidone, int aidtwo){
			idone = aidone;
			idtwo = aidtwo;
		}
		
		/**
		 * This method returns 1 (one) if this and other are
		 * a perfect match
		 * -1 if they are a partial match, indicates a problem
		 * 0 if they are not a match at all.
		 * @param LinkObject other
		 * @return
		 */
		public int check(LinkObject other){
			if(other.idone == this.idone){
				if(other.idtwo == this.idtwo){
					return 1;
				}
				return -1;
			}
			if(other.idtwo == this.idtwo){
				return -1;
			}
			return 0;
		}
	}
	
	private static boolean testMethod(
			SequenceDiagram pattern, int patternind, 
			SequenceDiagram toTest, int testind,
			List<LinkObject> listLinks){
		
	}
	
	private static float SingleCompare(
			SequenceDiagram pattern, SequenceDiagram toTest, int testIndex){
		List<LinkObject> listLinks = new ArrayList<LinkObject>();
		
		
	}
	 
    public static float PatternMatches(
    		SequenceDiagram pattern, SequenceDiagram toTest){
    	List<SDMessage> listPatternMsg = pattern.GetMessages();
    	if(listPatternMsg.size() == 0){return 0.0f;}
    	float toReturn = 0.0f;
    	int nextStartIndex = -1;
    	while(true){
    		nextStartIndex = findNextStartIndex(nextStartIndex, pattern, toTest);
    		if(nextStartIndex == -1){break;}
    		float v = SingleCompare(pattern, toTest, nextStartIndex);
    		if(v > toReturn){
    			toReturn = v;
    		}
    	}
    	return toReturn;
    }
}
