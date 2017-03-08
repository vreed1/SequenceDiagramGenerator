package sequenceDiagramGenerator.sdedit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.util.UUID;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
import sequenceDiagramGenerator.sdedit.SDObject.TaintState;


public class SequenceDiagram {
    
    protected Map<Integer, SDObject> theInstanceObjects;
    protected List<SDMessage> theMessages;
    protected Map<String, SDObject> theStaticObjects;
    protected String theName;
    
    protected int thePriority;
    protected int theUniqueMsgCount;
    protected int theMaxDepth;
    protected int theTotalMsgsInGroup;
    
    protected static String diagType = "pdf";
    protected static String diagFormat = "A4";
    protected static String diagOrientation = "portrait";
    
    protected UUID theID;

    public SequenceDiagram() {
        theInstanceObjects = new HashMap<Integer, SDObject>();
        theMessages = new ArrayList<SDMessage>();
        theStaticObjects = new HashMap<String,SDObject>();
        theID = UUID.randomUUID();
    }
    
    public static SequenceDiagram FromFile(String filename){
    	try{
    		JSONParser jp = new JSONParser();
    		FileReader fr = new FileReader(filename);
    		JSONObject jobj = (JSONObject) jp.parse(fr);
    		fr.close();
    		fr = null;
    		return new SequenceDiagram(jobj);
    	}
    	catch(Exception ex){
    		return null;
    	}
    }
    
    public SequenceDiagram(JSONObject jobj){
    	
    	if(jobj.containsKey("ID")){
    		theID = UUID.fromString((String)jobj.get("ID"));
    	}
    	else{
    		theID = UUID.randomUUID();
    	}
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
		
		theInstanceObjects = new HashMap<Integer, SDObject>();
		JSONObject iObjs = (JSONObject)jobj.get("Instances");
		Iterator<String> iInstInts = (Iterator<String>)iObjs.keySet().iterator();
		while(iInstInts.hasNext()){
			String sName = iInstInts.next();
			Integer iName = Integer.parseInt(sName);
			JSONObject iObj = (JSONObject)iObjs.get(sName);
			SDObject sdiobj = new SDObject(iObj);
			theInstanceObjects.put(iName, sdiobj);
		}
		
		theMessages = new ArrayList<SDMessage>();
		JSONArray jarrMsg = (JSONArray)jobj.get("Messages");
		
		for(int i =0 ; i < jarrMsg.size(); i++){
			JSONObject msgObj = (JSONObject)jarrMsg.get(i);
			SDMessage aMsg = new SDMessage(msgObj);
			theMessages.add(aMsg);
		}
    }
    
    public void SetPriority(int inval){
    	thePriority = inval;
    }
    
    public boolean hasTaint(){
    	for(Map.Entry<Integer, SDObject> entry : this.theInstanceObjects.entrySet()){
    		if(entry.getValue().IsTainted()){
    			return true;
    		}
    	}
    	for(Map.Entry<String, SDObject> entry : this.theStaticObjects.entrySet()){
    		if(entry.getValue().IsTainted()){
    			return true;
    		}
    	}
    	for(int i =0; i < this.theMessages.size(); i++){
    		if(this.theMessages.get(i).IsTainted()){
    			return true;
    		}
    	}
    	return false;
    }
    
    public int GetPriority(){
    	return thePriority;
    }
    
    public List<SDObject> GetObjects(){
    	List<SDObject> allObjs = new ArrayList<SDObject>();
    	allObjs.addAll(theInstanceObjects.values());
    	allObjs.addAll(theStaticObjects.values());
    	return allObjs;
    }
    
    public void SetName(String aName){
    	theName = Utilities.MakeFileSafe(aName);
    }
    
    //private static int nx = 0;
    public String GetName(){
    	//return "Name"+nx++;
    	return theName;
    }
    
    public List<SDMessage> GetMessages(){
    	return new ArrayList<SDMessage>(theMessages);
    }
    
    public SequenceDiagram clone(){
    	SequenceDiagram aClone = new SequenceDiagram();
    	for(SDObject anObj : theInstanceObjects.values()){
    		SDObject aobjClone = anObj.clone();
    		aClone.AddObject(aobjClone);
    	}

    	for(String aClass : theStaticObjects.keySet()){
    		SDObject anObj = theStaticObjects.get(aClass);
    		SDObject aobjClone = anObj.clone();
    		aClone.AddStaticObject(aClass, aobjClone);
    	}
    	
    	for(SDMessage aMsg : theMessages){
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
      
    private Map<Integer, SDObject> getCombinedMap(){
    	Map<Integer,SDObject> aMap = new HashMap<Integer,SDObject>(theInstanceObjects);
    	for(SDObject anObj : theStaticObjects.values()){
    		aMap.put(anObj.ID, anObj);
    	}
    	return aMap;
    }
    
    public void AddObject(SDObject obj) {
    	theInstanceObjects.put(new Integer(obj.ID), obj);
    }
    
    public void AddMessage(SDMessage msg) {
        theMessages.add(msg);
    }
    
    public void PushNames(){
    	for(SDObject anObj : theInstanceObjects.values()){
    		anObj.PushNames();
    	}
    }
    
    public void PopNames(){
    	for(SDObject anObj : theInstanceObjects.values()){
    		anObj.PopNames();
    	}
    }
    
    public void AddStaticObject(String key, SDObject value){
    	theStaticObjects.put(key, value);
    }
    
    public SDObject GetStaticObject(SootClass scobj){
    	String sc = scobj.getName();
    	if(!theStaticObjects.containsKey(sc)){
    		SDObject newObj = new SDObject(sc, SDObject.GetUniqueName(), false, true, TaintState.Safe);
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
	    	for(SDObject anObj : theInstanceObjects.values()){
	    		anObj.DetachName(name);
	    	}
	    	//There is a problem here.
	    	obj.AttachName(name);
    	}
    }
    
    public SDObject GetObjectFromName(String name){
    	for(SDObject anObj : theInstanceObjects.values()){
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
    	for(SDObject anObj : theInstanceObjects.values()){
    		anObj.fixFinalName(listUsedStrings);
    		listUsedStrings.add(anObj.GetName());
    	}
    	for(SDObject anObj : theStaticObjects.values()){
    		anObj.fixFinalName(listUsedStrings);
    		listUsedStrings.add(anObj.GetName());
    	}
    }
    
    private void MessageLevelCheck(){
    	if(theMessages.size() == 0){return;}
    	SetableList<Boolean> slist = new SetableList<Boolean>();
    	slist.SetR(theMessages.get(0).isSelfMessage(), theMessages.get(0).GetCallLevel());
    	
    	for(int i = 1; i < theMessages.size(); i++){
    		SDMessage now = theMessages.get(i);
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
    		String ofDbg = outFile + "DEBUG.txt";
    		File aFile = new File(ofDbg);
    		try{
    			PrintStream ps = new PrintStream(aFile);
    			ps.print(this.toString());
    			ps.flush();
    			ps.close();
    			ps = null;
    		} catch(FileNotFoundException e){
    			e.printStackTrace();
    			Utilities.DebugPrintln("DBG File Not Found");
    			
    		}
    		Utilities.DebugPrintln("--------------------");
    	}
    	MakePDFFromSDEdit(this.toString(), outFile);

    	MakeJSONFile(outFile);
    }
    
    public void MakeJSONFile(String pdfFile){
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
        for(SDMessage aMsg : theMessages){
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
        for (SDObject obj : theInstanceObjects.values()) {
        	obj.SetTerse(terse);
            if(usedIDs.contains(obj.ID)){
            	diagram.append(obj.toString());
            	diagram.append(NEW_LINE);
            }
        }
        
        
        diagram.append(NEW_LINE);
        diagram.append(NEW_LINE);
        
        Map<Integer, SDObject> aMap = getCombinedMap();
        
        for (SDMessage msg : theMessages) {
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
		if(theInstanceObjects.containsKey(sourceObjID)){
			return theInstanceObjects.get(sourceObjID);
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
		
		topObj.put("ID", theID.toString());
		
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
		Iterator<Integer> iInst = this.theInstanceObjects.keySet().iterator();
		while(iInst.hasNext()){
			Integer i = iInst.next();
			SDObject aInst = theInstanceObjects.get(i);
			iObjs.put(Integer.toString(i), aInst.toJSONObject());
		}
		
		topObj.put("Instances", iObjs);
		
		JSONArray jmsgArr = new JSONArray();
		for(int i = 0; i < theMessages.size(); i++){
			jmsgArr.add(theMessages.get(i).toJSONObject());
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
		for(int i = 0;i < this.theMessages.size(); i++){
			if(theMaxDepth < this.theMessages.get(i).GetCallLevel()){
				theMaxDepth = this.theMessages.get(i).GetCallLevel();
			}
		}
	}
	
	public void SetMaxDepth(int maxDepth){
		theMaxDepth = maxDepth;
	}

	public void Filter(SDObject sdObject) {
		if(sdObject.isStatic){

			String stype = sdObject.GetTypeName();
			if(theStaticObjects.containsKey(stype)){
				theStaticObjects.remove(stype);
			}
		}
		else if(theInstanceObjects.containsKey(sdObject.ID)){
			theInstanceObjects.remove(sdObject.ID);
		}
		for(int i = theMessages.size()-1; i >= 0; i--){
			if(theMessages.get(i).calleeID == sdObject.ID || theMessages.get(i).callerID == sdObject.ID){
				Filter(theMessages.get(i));
			}
		}
	}

	public void Filter(SDMessage sdMessage) {
		int ind = theMessages.indexOf(sdMessage);
		if(ind == -1){return;}
		int maxtoremove = ind+1;
		for(maxtoremove = ind+1; maxtoremove < theMessages.size(); maxtoremove++){
			if(theMessages.get(maxtoremove).GetCallLevel() <= theMessages.get(maxtoremove).GetCallLevel()){
				break;
			}
		}
		for(int i = maxtoremove -1; i >= ind; i--){
			theMessages.remove(i);
		}
	}

	public int GetMaxDepth() {
		return this.theMaxDepth;
	}

	public String IDString() {
		return theID.toString();
	}
}
