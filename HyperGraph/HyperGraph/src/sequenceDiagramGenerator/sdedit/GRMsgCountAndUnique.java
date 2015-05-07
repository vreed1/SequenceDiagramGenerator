
package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GRMsgCountAndUnique extends GenReduceByMessageCount{

	private int k;
	public GRMsgCountAndUnique(int aK){
		super(aK);
	}
	
	@Override
	public List<SequenceDiagram> Prioritize(List<SequenceDiagram> aList) {
		// TODO Auto-generated method stub
		List<SequenceDiagram> newList = new ArrayList<SequenceDiagram>(aList);
		Collections.sort(newList, new MessageCountComparator());
		for(int i = newList.size() -1; i > 0; i--){
			SequenceDiagram test = newList.get(i);
			for(int j = i -1; j >= 0; j--){
				SequenceDiagram pred = newList.get(j);
				if(test.isSubsetOf(pred)){
					newList.remove(i);
					break;
				}
			}
		}
		newList = SortUnique(newList);
		return newList;
	}
	
	private List<SequenceDiagram> SortUnique(List<SequenceDiagram> in_sd){
		List<SequenceDiagram> ret = new ArrayList<SequenceDiagram>();
		if(in_sd.size() == 0){return ret;}

		List<SDWrapper> listTest = new ArrayList<SDWrapper>();
		for(int i = 1; i < in_sd.size(); i++){
			listTest.add(new SDWrapper(in_sd.get(i)));
		}
		SDWrapper newBest = new SDWrapper(in_sd.get(0));
		int PriCount = 0;
		int totalUniqueMsgCount = 0;
		while(newBest != null && newBest.listPossibleUniques.size() > 0){
			newBest.sd.SetPriority(PriCount);
			int newMsgCount = newBest.listPossibleUniques.size();
			newBest.sd.SetNewMsgCount(newMsgCount);
			totalUniqueMsgCount += newMsgCount;
			ret.add(newBest.sd);
			PriCount++;
			
			int bestIndex = -1;
			int bestScore = 0;
			for(int i = 0; i < listTest.size(); i++){
				listTest.get(i).Update(newBest.listPossibleUniques);
				if(listTest.get(i).listPossibleUniques.size() > bestScore){
					bestScore = listTest.get(i).listPossibleUniques.size();
					bestIndex = i;
				}
			}
			if(bestIndex > -1){
				newBest = listTest.remove(bestIndex);
			}
			else{
				newBest = null;
			}
		}
		
		for(int i = 0; i < ret.size(); i++){
			ret.get(i).SetTotalMsgsInGroup(totalUniqueMsgCount);
		}
		
		return ret;
	}
	
	private class SDWrapper{
		public SequenceDiagram sd;
		public List<String> listPossibleUniques;
		public SDWrapper(SequenceDiagram insd){
			sd = insd;
			listPossibleUniques = new ArrayList<String>();
			List<SDMessage> lmsg = sd.GetMessages();
			for(int i = 0; i < lmsg.size(); i++){
				String msgName = lmsg.get(i).GetFullMethodName();
				listPossibleUniques.add(msgName);
			}
		}
		public void Update(List<String> listSeen){
			for(int i = 0; i < listSeen.size(); i++){
				if(listPossibleUniques.contains(listSeen.get(i))){
					listPossibleUniques.remove(listSeen.get(i));
				}
			}
		}
	}
}
