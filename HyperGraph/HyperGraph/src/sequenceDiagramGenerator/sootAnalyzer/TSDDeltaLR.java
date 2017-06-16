package sequenceDiagramGenerator.sootAnalyzer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sequenceDiagramGenerator.sdedit.SDObject;
import sequenceDiagramGenerator.sdedit.SDObject.TaintState;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;

public class TSDDeltaLR extends TSDListAndReturns {
	//private List<SequenceDiagram> listDia;
	//private List<SDObject> listReturns;
	//public TaintState tState = TaintState.Safe;
	
	private static List<TSDDeltaLR> listCache = new ArrayList<TSDDeltaLR>();
	private static final int MAXCACHE = 50;

	private String backupFileName;
	private static void addtocache(TSDDeltaLR newSet){
		listCache.add(newSet);
		if(listCache.size() > MAXCACHE){
			TSDDeltaLR old = listCache.remove(0);
			try {
				old.save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException("This cannot be recovered");
			}
		}
	}
	
	
	public TSDDeltaLR(){
		super();
		backupFileName="";
		listCache.add(this);
		//listDiagrams = new ArrayList<SequenceDiagram>();
		//listReturns = new ArrayList<SDObject>();
	}
	
	public void save() throws IOException{
		backupFileName = "/tmp/" + UUID.randomUUID().toString();
		this.toFile(backupFileName);
		this.listDiagrams = null;
		this.listReturns = null;
	}
	
	protected boolean needsLoad(){
		if(backupFileName.length() > 0){
			return true;
		}
		return false;
	}
	
	protected void load(){
		if(backupFileName.length() > 0){
			this.listDiagrams = new ArrayList<SequenceDiagram>();
			this.listReturns = new ArrayList<SDObject>();
			this.load(this.backupFileName);
			backupFileName = "";
			addtocache(this);
		}
	}
	
	public void load(String ajsonfilename){
		try {
			JSONParser jp = new JSONParser();
			FileReader fr = new FileReader(ajsonfilename);
			JSONObject jobj = (JSONObject) jp.parse(fr);
			loadjson(jobj);
		}
		catch (IOException ie){
			//just ignore it and cause problem.
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadjson(JSONObject jobj){
		tState = TaintState.valueOf((String)jobj.get("tState"));
		JSONArray jarr = (JSONArray)jobj.get("listDiagrams");
		for(int i = 0; i < jarr.size(); i++){
			SequenceDiagram sd = new SequenceDiagram();
			sd.LoadJSON((JSONObject)jarr.get(i));
			this.listDiagrams.add(sd);
		}
		jarr = (JSONArray)jobj.get("listReturns");
		for(int i = 0; i < jarr.size(); i++){
			SDObject sdo = new SDObject();
			sdo.LoadJSON((JSONObject)jarr.get(i));
			this.listReturns.add(sdo);
		}
	}
	
	public JSONObject serialize(){
		JSONObject topObj = new JSONObject();
		topObj.put("tState", tState);
		
		JSONArray jarr = new JSONArray();
		for(int i= 0; i < listDiagrams.size(); i++){
			jarr.add(listDiagrams.get(i).serialize());
		}
		topObj.put("listDiagrams", listDiagrams);
		jarr = new JSONArray();
		for(int i= 0; i < listReturns.size(); i++){
			jarr.add(listReturns.get(i).serialize());
		}
		topObj.put("listReturns", jarr);
		return topObj;
	}
	
	public void toFile(String aFileName) throws IOException{
		FileWriter fw = new FileWriter(aFileName);
	
		JSONObject jobj = this.serialize();
    	String filecontents = jobj.toJSONString();

		fw.write(filecontents);
		fw.close();
	}
}
