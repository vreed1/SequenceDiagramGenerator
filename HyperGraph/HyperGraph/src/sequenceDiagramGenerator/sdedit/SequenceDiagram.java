package sequenceDiagramGenerator.sdedit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import soot.SootClass;
import utilities.SetableList;
import utilities.Utilities;
import net.sf.sdedit.config.Configuration;
import net.sf.sdedit.config.ConfigurationManager;
import net.sf.sdedit.diagram.Diagram;
import net.sf.sdedit.editor.DiagramFileHandler;
import net.sf.sdedit.error.SemanticError;
import net.sf.sdedit.error.SyntaxError;
import net.sf.sdedit.server.Exporter;
import net.sf.sdedit.text.TextHandler;
import net.sf.sdedit.ui.components.configuration.Bean;
import net.sf.sdedit.util.DocUtil.XMLException;
import net.sf.sdedit.util.Pair;


public class SequenceDiagram {
    
    private Map<Integer, SDObject> objects;
    private List<SDMessage> messages;
    private Map<String, SDObject> theStaticObjects;
    private String theName;
    
    private int thePriority;
    private int theUniqueMsgCount;
    private int theMaxDepth;
    private int theTotalMsgsInGroup;
    
    private static String diagType = "pdf";
    private static String diagFormat = "A4";
    private static String diagOrientation = "portrait";
    
    public SequenceDiagram(JSONObject jobj){
    	
    	theName = (String)jobj.get("Name");
    	thePriority = Integer.parseInt((String)jobj.get("Priority"));
    	
    	theUniqueMsgCount = Integer.parseInt((String)jobj.get("UniqueMsgCount"));
    	theMaxDepth = Integer.parseInt((String)jobj.get("MaxDepth"));
    	theTotalMsgsInGroup = Integer.parseInt((String)jobj.get("TotalMsgsInGroup"));

    	theStaticObjects = new HashMap<String, SDObject>();
		JSONObject sObjs = (JSONObject)jobj.get("Statics");
		Iterator<String> iStaticNames = (Iterator<String>)sObjs.keySet().iterator();
		while(iStaticNames.hasNext()){
			String sName = iStaticNames.next();
			JSONObject sObj = (JSONObject)sObjs.get(sName);
			SDObject sdobj = new SDObject(sObj);
			theStaticObjects.put(sName, sdobj);
		}
		
		objects = new HashMap<Integer, SDObject>();
		JSONObject iObjs = (JSONObject)jobj.get("Instances");
		Iterator<String> iInstInts = (Iterator<String>)iObjs.keySet().iterator();
		while(iInstInts.hasNext()){
			String sName = iInstInts.next();
			Integer iName = Integer.parseInt(sName);
			JSONObject iObj = (JSONObject)iObjs.get(sName);
			SDObject sdiobj = new SDObject(iObj);
			objects.put(iName, sdiobj);
		}
		
		messages = new ArrayList<SDMessage>();
		JSONArray jarrMsg = (JSONArray)jobj.get("Messages");
		
		for(int i =0 ; i < jarrMsg.size(); i++){
			JSONObject msgObj = (JSONObject)jarrMsg.get(i);
			SDMessage aMsg = new SDMessage(msgObj);
			messages.add(aMsg);
		}
    }
    
    public void SetPriority(int inval){
    	thePriority = inval;
    }
    
    public int GetPriority(){
    	return thePriority;
    }
    
    public List<SDObject> GetObjects(){
    	List<SDObject> allObjs = new ArrayList<SDObject>();
    	allObjs.addAll(objects.values());
    	allObjs.addAll(theStaticObjects.values());
    	return allObjs;
    }
    
    public void SetName(String aName){
    	theName = aName;
    }
    
    public String GetName(){
    	return theName;
    }
    
    public List<SDMessage> GetMessages(){
    	return new ArrayList<SDMessage>(messages);
    }
    
    public SequenceDiagram clone(){
    	SequenceDiagram aClone = new SequenceDiagram();
    	for(SDObject anObj : objects.values()){
    		SDObject aobjClone = anObj.clone();
    		aClone.AddObject(aobjClone);
    	}

    	for(String aClass : theStaticObjects.keySet()){
    		SDObject anObj = theStaticObjects.get(aClass);
    		SDObject aobjClone = anObj.clone();
    		aClone.AddStaticObject(aClass, aobjClone);
    	}
    	
    	for(SDMessage aMsg : messages){
    		SDMessage aMsgClone = aMsg.clone();
    		aClone.AddMessage(aMsgClone);
    	}
    	aClone.SetName(this.GetName());
    	aClone.SetPriority(this.GetPriority());
    	aClone.SetNewMsgCount(this.theUniqueMsgCount);
    	aClone.SetTotalMsgsInGroup(this.theTotalMsgsInGroup);
    	aClone.SetMaxDepth(this.theMaxDepth);
    	return aClone;
    }
    
    public SequenceDiagram() {
        objects = new HashMap<Integer, SDObject>();
        messages = new ArrayList<SDMessage>();
        theStaticObjects = new HashMap<String,SDObject>();
    }
    
    private Map<Integer, SDObject> getCombinedMap(){
    	Map<Integer,SDObject> aMap = new HashMap<Integer,SDObject>(objects);
    	for(SDObject anObj : theStaticObjects.values()){
    		aMap.put(anObj.ID, anObj);
    	}
    	return aMap;
    }
    
    public void AddObject(SDObject obj) {
    	objects.put(new Integer(obj.ID), obj);
    }
    
    public void AddMessage(SDMessage msg) {
        messages.add(msg);
    }
    
    public void PushNames(){
    	for(SDObject anObj : objects.values()){
    		anObj.PushNames();
    	}
    }
    
    public void PopNames(){
    	for(SDObject anObj : objects.values()){
    		anObj.PopNames();
    	}
    }
    
    public void AddStaticObject(String key, SDObject value){
    	theStaticObjects.put(key, value);
    }
    
    public SDObject GetStaticObject(SootClass scobj){
    	String sc = scobj.getName();
    	if(!theStaticObjects.containsKey(sc)){
    		SDObject newObj = new SDObject(sc, SDObject.GetUniqueName(), false, true);
    		theStaticObjects.put(sc,  newObj);
    	}
		return theStaticObjects.get(sc);
    	
    }
    
    public void AttachNameToObject(String name, SDObject obj){
    	if(name.contains(".")){
    		String[] parts = name.split(Pattern.quote("."));
    		if(parts.length > 2){throw new RuntimeException("Should not have mulitple-reference");}
    		SDObject sdObj = this.GetObjectFromName(parts[0]);
    		sdObj.setField(parts[1], obj);
    	}
    	else{
	    	for(SDObject anObj : objects.values()){
	    		anObj.DetachName(name);
	    	}
	    	//There is a problem here.
	    	obj.AttachName(name);
    	}
    }
    
    public SDObject GetObjectFromName(String name){
    	for(SDObject anObj : objects.values()){
    		if(anObj.MatchesName(name)){
    			return anObj;
    		}
    	}
    	for(SDObject anObj : theStaticObjects.values()){
    		if(anObj.MatchesName(name)){
    			return anObj;
    		}
    	}
    	return null;
    }
    
    private void NameSafetyCheck(){
    	List<String> listUsedStrings = new ArrayList<String>();
    	for(SDObject anObj : objects.values()){
    		anObj.fixFinalName(listUsedStrings);
    		listUsedStrings.add(anObj.GetName());
    	}
    	for(SDObject anObj : theStaticObjects.values()){
    		anObj.fixFinalName(listUsedStrings);
    		listUsedStrings.add(anObj.GetName());
    	}
    }
    
    private void MessageLevelCheck(){
    	if(messages.size() == 0){return;}
    	SetableList<Boolean> slist = new SetableList<Boolean>();
    	slist.SetR(messages.get(0).isSelfMessage(), messages.get(0).GetCallLevel());
    	
    	for(int i = 1; i < messages.size(); i++){
    		SDMessage now = messages.get(i);
    		int flvl = 0;
    		
    		for(int j = now.GetCallLevel(); j < slist.size(); j++){
    			if(slist.get(j)){
    				flvl++;
    			}
    			else{
    				break;
    			}
    		}
    		slist.SetSize(now.GetCallLevel());
    		slist.SetR(now.isSelfMessage(), now.GetCallLevel());
    		now.SetFinalLevel(flvl);
    	}
    }
    
    public void CreatePDFInDir(String dirName, boolean tersemode){
    	SetMaxDepth();
    	String fileName = Utilities.endWithSlash(dirName) + 
    			Integer.toString(thePriority) + "_P-" +
    			
    					Utilities.twoDecimal(
    					
    							(100.0 * this.theUniqueMsgCount)
    							/ this.theTotalMsgsInGroup
    					
    					) +
    			"%_D-" + Integer.toString(theMaxDepth)
    			+ "_" + GetName();
    	CreatePDF(fileName, tersemode);
    }
    private boolean terse = false;
    public void CreatePDF(String outFile, boolean tersemode) {
    	terse = tersemode;
    	NameSafetyCheck();
    	MessageLevelCheck();
    	if(this.toString().trim().equals("")){
    		return;
    	}
    	if(Utilities.DEBUG){
    		Utilities.DebugPrintln("---------SD---------");
    		Utilities.DebugPrintln(this.toString());
    		Utilities.DebugPrintln("--------------------");
    	}
    	MakePDFFromSDEdit(this.toString(), outFile);

    	MakeJSONFile(outFile);
    }
    
    private void MakeJSONFile(String pdfFile){
    	String jsonFile = pdfFile.substring(0, pdfFile.length()-3) + "json";
    	JSONObject jobj = this.toJSONObject();
    	String filecontents = jobj.toJSONString();
    	
		File aFile = new File(jsonFile);
		try {
			PrintStream ps = new PrintStream(aFile);
			ps.print(filecontents);
			ps.flush();
			ps.close();
			ps = null;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Utilities.DebugPrintln("JSON File Not Found");
		}
    }
    
    public static void MakePDFFromSDEdit(String sdeditString, String outFile){
    	InputStream in = new ByteArrayInputStream(sdeditString.getBytes());
        OutputStream out = null;
        try {
            out = new FileOutputStream(outFile);
            try {
                Pair<String, Bean<Configuration>> pair = DiagramFileHandler
                        .load(in, ConfigurationManager.getGlobalConfiguration()
                                .getFileEncoding());
                TextHandler th = new TextHandler(pair.getFirst());
                Bean<Configuration> conf = pair.getSecond();
                
                Exporter paintDevice = Exporter.getExporter(diagType, diagOrientation, diagFormat, out);
                new Diagram(conf.getDataObject(), th, paintDevice).generate();
                paintDevice.export();
                paintDevice.close();
            } catch (IOException | XMLException | SemanticError | SyntaxError e) {
                // TODO Auto-generated catch block
                Utilities.DebugPrintln("Error Generating PDF");
                Utilities.DebugPrintln(e.getMessage());
                Utilities.DebugPrintln(e.getStackTrace().toString());
            } finally {

                out.flush();
                out.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            
        }
    }
    
    @Override
    public String toString() {
        String NEW_LINE = "\n";
        
        StringBuilder diagram = new StringBuilder();
        
        List<Integer> usedIDs = new ArrayList<Integer>();
        for(SDMessage aMsg : messages){
        	usedIDs.add(aMsg.calleeID);
        	usedIDs.add(aMsg.callerID);
        }
        
        for(SDObject obj : theStaticObjects.values()){
        	obj.SetTerse(terse);
        	if(usedIDs.contains(obj.ID)){
        		diagram.append(obj.toString());
        		diagram.append(NEW_LINE);
        	}
        }
        for (SDObject obj : objects.values()) {
        	obj.SetTerse(terse);
            if(usedIDs.contains(obj.ID)){
            	diagram.append(obj.toString());
            	diagram.append(NEW_LINE);
            }
        }
        
        
        diagram.append(NEW_LINE);
        diagram.append(NEW_LINE);
        
        Map<Integer, SDObject> aMap = getCombinedMap();
        
        for (SDMessage msg : messages) {
            diagram.append(msg.toString(aMap));
            diagram.append(NEW_LINE);
        }
        
        return diagram.toString();
        
    }

	public void TestOutput(String saveFile) {
		OutputStream out = null;
        try {
            out = new FileOutputStream(saveFile);
            OutputStreamWriter sw = new OutputStreamWriter(out);
            sw.append(this.toString());
            sw.flush();
            sw.close(); 
            sw = null;
            out = null;
        }
        catch(FileNotFoundException e){
			e.printStackTrace();
        } catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SDObject GetObjectFromID(int sourceObjID) {
		if(objects.containsKey(sourceObjID)){
			return objects.get(sourceObjID);
		}
		for(SDObject anObj : theStaticObjects.values()){
			if(anObj.ID == sourceObjID){
				return anObj;
			}
		}
		return null;
	}
	
	public boolean isSubsetOf(SequenceDiagram other){
		List<SDMessage> otherMsg = other.GetMessages();
		List<SDMessage> thisMsg = this.GetMessages();
		if(thisMsg.size() == 0){
			return true;
		}
		if(otherMsg.size() <= thisMsg.size()){
			return false;
		}
		for(int i = 0; i <= otherMsg.size()-thisMsg.size(); i++){
			if(MessageMatches(this, thisMsg.get(0), other, otherMsg.get(i))){
				boolean issubset = true;
				for(int j = 1; j < thisMsg.size(); j++){
					if(!MessageMatches(this, thisMsg.get(j), other, otherMsg.get(i+j))){
						issubset = false;
						break;
					}
				}
				if(issubset){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isEquivalent(SequenceDiagram other){
		List<SDMessage> otherMsg = other.GetMessages();
		List<SDMessage> thisMsg = this.GetMessages();
		
		if(otherMsg.size() != thisMsg.size()){
			return false;
		}
		for(int i = 0; i < otherMsg.size(); i++){
			if(!MessageMatches(this, thisMsg.get(i), other, otherMsg.get(i))){
				return false;
			}
		}
		
		List<SDObject> otherObjs = other.GetObjects();
		List<SDObject> thisObjs = this.GetObjects();
		if(otherObjs.size() != thisObjs.size()){
			return false;
		}
		for(int i = 0; i < otherObjs.size(); i++){
			SDObject otherObj = otherObjs.get(i);
			boolean foundMatch = false;
			for(int j = 0; j < thisObjs.size(); j++){
				SDObject thisObj = thisObjs.get(j);
				if(thisObj.isEquivalent(otherObj)){
					foundMatch = true;
					break;
				}
			}
			if(!foundMatch){
				return false;
			}
		}
		return true;
	}
	
	private static boolean MessageMatches(
			SequenceDiagram oneSD,
			SDMessage oneMsg,
			SequenceDiagram twoSD,
			SDMessage twoMsg){
		SDObject aOne = oneSD.GetObjectFromID(oneMsg.calleeID);
		SDObject bOne = oneSD.GetObjectFromID(oneMsg.callerID);
		SDObject aTwo = twoSD.GetObjectFromID(twoMsg.calleeID);
		SDObject bTwo = twoSD.GetObjectFromID(twoMsg.callerID);
		
		if(!aOne.GetTypeName().equals(aTwo.GetTypeName())){
			return false;
		}
		if(!bOne.GetTypeName().equals(bTwo.GetTypeName())){
			return false;
		}
		if(!oneMsg.GetFullMethodName().equals(twoMsg.GetFullMethodName())){
			return false;
		}
		
		return true;
	}
	
	public JSONObject toJSONObject(){
		SetMaxDepth();
		JSONObject topObj = new JSONObject();
		
		JSONObject sObjs = new JSONObject();
		Iterator<String> iStatics = this.theStaticObjects.keySet().iterator();
		while(iStatics.hasNext())
		{
			String sc = iStatics.next();
			SDObject aStaticObj = theStaticObjects.get(sc);
			sObjs.put(sc, aStaticObj.toJSONObject());
		}
		
		topObj.put("Statics", sObjs);
		
		JSONObject iObjs = new JSONObject();
		Iterator<Integer> iInst = this.objects.keySet().iterator();
		while(iInst.hasNext()){
			Integer i = iInst.next();
			SDObject aInst = objects.get(i);
			iObjs.put(Integer.toString(i), aInst.toJSONObject());
		}
		
		topObj.put("Instances", iObjs);
		
		JSONArray jmsgArr = new JSONArray();
		for(int i = 0; i < messages.size(); i++){
			jmsgArr.add(messages.get(i).toJSONObject());
		}
		
		topObj.put("Messages", jmsgArr);
		
		topObj.put("Name", this.theName);
		topObj.put("Priority", Integer.toString(this.thePriority));
		topObj.put("UniqueMsgCount", Integer.toString(this.theUniqueMsgCount));
		topObj.put("MaxDepth", Integer.toString(this.theMaxDepth));
		topObj.put("TotalMsgsInGroup", Integer.toString(this.theTotalMsgsInGroup));
		
		return topObj;
	}

	public void SetNewMsgCount(int newMsgCount) {
		this.theUniqueMsgCount = newMsgCount;
	}
	
	public void SetTotalMsgsInGroup(int msgCount){
		theTotalMsgsInGroup = msgCount;
	}
	
	public void SetMaxDepth(){
		for(int i = 0;i < this.messages.size(); i++){
			if(theMaxDepth < this.messages.get(i).GetCallLevel()){
				theMaxDepth = this.messages.get(i).GetCallLevel();
			}
		}
	}
	
	public void SetMaxDepth(int maxDepth){
		theMaxDepth = maxDepth;
	}
}
