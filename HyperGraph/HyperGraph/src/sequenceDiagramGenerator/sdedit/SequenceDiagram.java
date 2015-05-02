package sequenceDiagramGenerator.sdedit;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
    private Map<SootClass, SDObject> theStaticObjects;
    
    private static String diagType = "pdf";
    private static String diagFormat = "A4";
    private static String diagOrientation = "portrait";
    
    public List<SDObject> GetObjects(){
    	List<SDObject> allObjs = new ArrayList<SDObject>();
    	allObjs.addAll(objects.values());
    	allObjs.addAll(theStaticObjects.values());
    	return allObjs;
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

    	for(SootClass aClass : theStaticObjects.keySet()){
    		SDObject anObj = theStaticObjects.get(aClass);
    		SDObject aobjClone = anObj.clone();
    		aClone.AddStaticObject(aClass, aobjClone);
    	}
    	
    	for(SDMessage aMsg : messages){
    		SDMessage aMsgClone = aMsg.clone();
    		aClone.AddMessage(aMsgClone);
    	}
    	return aClone;
    }
    
    public SequenceDiagram() {
        objects = new HashMap<Integer, SDObject>();
        messages = new ArrayList<SDMessage>();
        theStaticObjects = new HashMap<SootClass,SDObject>();
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
    
    public void AddStaticObject(SootClass key, SDObject value){
    	theStaticObjects.put(key, value);
    }
    
    public SDObject GetStaticObject(SootClass sc){
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
    		}
    		slist.SetSize(now.GetCallLevel());
    		slist.SetR(now.isSelfMessage(), now.GetCallLevel());
    		now.SetFinalLevel(flvl);
    	}
    }
    
    public void CreatePDF(String outFile) {    
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
//        InputStream in = new ByteArrayInputStream(this.toString().getBytes());
//        OutputStream out = null;
//        try {
//            out = new FileOutputStream(outFile);
//            try {
//                Pair<String, Bean<Configuration>> pair = DiagramFileHandler
//                        .load(in, ConfigurationManager.getGlobalConfiguration()
//                                .getFileEncoding());
//                TextHandler th = new TextHandler(pair.getFirst());
//                Bean<Configuration> conf = pair.getSecond();
//                
//                Exporter paintDevice = Exporter.getExporter(diagType, diagOrientation, diagFormat, out);
//                new Diagram(conf.getDataObject(), th, paintDevice).generate();
//                paintDevice.export();
//                paintDevice.close();
//            } catch (IOException | XMLException | SemanticError | SyntaxError e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } finally {
//
//                out.flush();
//                out.close();
//            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            
//        }
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
                e.printStackTrace();
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
        	if(usedIDs.contains(obj.ID)){
        		diagram.append(obj.toString());
        		diagram.append(NEW_LINE);
        	}
        }
        for (SDObject obj : objects.values()) {
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
}
