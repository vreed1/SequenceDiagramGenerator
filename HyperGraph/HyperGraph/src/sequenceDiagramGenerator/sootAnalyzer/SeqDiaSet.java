package sequenceDiagramGenerator.sootAnalyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import sequenceDiagramGenerator.sdedit.MessageCountComparator;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;
import soot.SootClass;

public class SeqDiaSet {

	private static String tempdir = "/home/brian/Desktop/gradschool/supratik/SDTEMP/";
	private List<String> listFileNames;
	private List<SootClass> listClasses;
	public SeqDiaSet(SeqDiaSet aList) {
		listFileNames = aList.getFileNames();
		listClasses = aList.getSootClasses();
	}

	public SeqDiaSet() {
		listFileNames = new ArrayList<String>();
		listClasses = new ArrayList<SootClass>();
	}
	
	public List<SootClass> getSootClasses(){
		return new ArrayList<SootClass>(listClasses);
	}
	
	public List<String> getFileNames(){
		return new ArrayList<String>(listFileNames);
	}

	public SequenceDiagram get(int i) {
		String fname = listFileNames.get(i);
		SequenceDiagram sd = SequenceDiagram.FromFile(fname);
		return sd;
	}

	public int size() {
		return listFileNames.size();
	}

	public void addSet(SeqDiaSet listSDs) {
		List<String> newfnames = listSDs.getFileNames();
		this.listFileNames.addAll(newfnames);
	}

	public void add(SequenceDiagram sd) {
		String fnamebad = tempdir + sd.IDString() + ".pdf";
		sd.MakeJSONFile(fnamebad);
		String fnamegood = tempdir + sd.IDString() + ".json";
		this.listFileNames.add(fnamegood);
	}
	
	//BRIAN WORK ON THIS! STILL A PROBLEM
	public void update(SequenceDiagram sd){
		String fnamebad = tempdir + sd.IDString() + ".pdf";
		sd.MakeJSONFile(fnamebad);
	}

	public void remove(int i) {
		listFileNames.remove(i);
	}

	public void sort(Comparator<SequenceDiagram> comp) {
		
	}

	public void clear() {
		listFileNames.clear();
	}

	public void addAll(SeqDiaSet listDiagrams) {
		this.addSet(listDiagrams);
	}

}
