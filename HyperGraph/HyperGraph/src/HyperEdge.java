import java.util.List;


public class HyperEdge<A> {
	public A annotation;
	
	public List<Integer> sourceNodes;
	
	public int targetNode;
	
	public HyperEdge(List<Integer> src, int target, A annot){
		sourceNodes = src;
		targetNode = target;
		annotation = annot;
		
		if(src.contains(new Integer(target))){
			throw new IllegalArgumentException("There exists a direct cycle in a hyperedge" + this);
		}
	}
	
	@Override
	public boolean equals(Object obj){
		//instanceof isn't going to work for generic types
		//after a little bit of googling I decided that
		//a classcastexception would be easier.
		//if(!(obj instanceof HyperEdge<?>)){
		//	return false;
		//}
		//HyperEdge<?> thatEdge = (HyperEdge<?>)obj;
		try{
			HyperEdge<A> otherEdge = (HyperEdge<A>)obj;
			if(otherEdge == null){return false;}
			for(Integer src : this.sourceNodes){
				if(!otherEdge.sourceNodes.contains(src)){return false;}
			}
			return true;
		}catch(ClassCastException e){return false;}
	}
	@Override
	public int hashCode(){return super.hashCode();}
	
	public boolean DefinesEdge(List<Integer> antecedent, int consequent){
		for(Integer ante : antecedent){
			if(!sourceNodes.contains(ante)){return false;}
		}
		return targetNode == consequent;
	}
	
	@Override
	public String toString(){
		String retS = " { ";
		for(Integer node : sourceNodes){
			retS += node + ",";
		}
		if(sourceNodes.size() != 0){retS = retS.substring(0, retS.length() - 1);}
		retS += " } -> " + targetNode;
		return retS;
	}
}
