package sequenceDiagramGenerator.sdedit;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
    
    private List<SDObject> objects;
    private List<SDMessage> messages;
    
    private String diagType = "pdf";
    private String diagFormat = "A4";
    private String diagOrientation = "portrait";
    
    
    public SequenceDiagram() {
        objects = new ArrayList<SDObject>();
        messages = new ArrayList<SDMessage>();
    }
    
    public void AddObject(SDObject obj) {
//    	for(SDObject oldObject : objects){
//    		if(oldObject.equals(obj)){
//    			return;
//    		}
//    	}
    	objects.add(obj);
    }
    
    public void AddMessage(SDMessage msg) {
        messages.add(msg);
    }
    
    public void AttachNameToObject(String name, SDObject obj){
    	for(SDObject anObj : objects){
    		anObj.DetachName(name);
    	}
    	obj.AttachName(name);
    }
    
    public SDObject GetObjectFromName(String name){
    	for(SDObject anObj : objects){
    		if(anObj.MatchesName(name)){
    			return anObj;
    		}
    	}
    	return null;
    }
    
    private void NameSafetyCheck(){
    	List<String> listUsedStrings = new ArrayList<String>();
    	for(SDObject anObj : objects){
    		anObj.fixFinalName(listUsedStrings);
    		listUsedStrings.add(anObj.GetName());
    	}
    }
    
    public void CreatePDF(String outFile) {    
    	NameSafetyCheck();
    	if(Utilities.DEBUG){
    		System.out.println("---------SD---------");
    		System.out.println(this.toString());
    		System.out.println("--------------------");
    	}
        InputStream in = new ByteArrayInputStream(this.toString().getBytes());
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
            } catch (IOException | XMLException | SemanticError | SyntaxError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            
        }
    }
    
    @Override
    public String toString() {
        String NEW_LINE = "\n";
        
        StringBuilder diagram = new StringBuilder();
        for (SDObject obj : objects) {
            diagram.append(obj.toString());
            diagram.append(NEW_LINE);
        }
        
        diagram.append(NEW_LINE);
        diagram.append(NEW_LINE);
        
        for (SDMessage msg : messages) {
            diagram.append(msg.toString());
            diagram.append(NEW_LINE);
        }
        
        return diagram.toString();
        
    }
}
