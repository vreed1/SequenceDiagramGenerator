package sequenceDiagramGenerator.InstanceInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.SootField;
import soot.util.Chain;

public class InstanceObject {
	public soot.Type theType;
	public String theObjectName;
	public Map<String, InstanceObject> theMemberVars;
	
	private InstanceObject(
			soot.Type aType,
			String aObjectName){
		theType = aType;
		theObjectName = aObjectName;
		theMemberVars = new HashMap<String, InstanceObject>();
	}
	
	public static InstanceObject GenerateInstance(
			soot.Type aType, 
			String aObjectName,
			Map<soot.Type, soot.SootClass> mapToVisibleClasses){
		InstanceObject toReturn = new InstanceObject(
				aType,
				aObjectName);
		if(mapToVisibleClasses.containsKey(aType)){
			soot.SootClass aClass = mapToVisibleClasses.get(aType);
			Chain<SootField> cFields = aClass.getFields();
			Iterator<SootField> iFields = cFields.iterator();
			
			while(iFields.hasNext()){
				SootField f = iFields.next();
				toReturn.theMemberVars.put(
						f.getName(), 
						InstanceObject.GenerateInstance(
								aType,
								f.getName(),
								mapToVisibleClasses));
				
			}
		}
		
		return toReturn;
	}
}
