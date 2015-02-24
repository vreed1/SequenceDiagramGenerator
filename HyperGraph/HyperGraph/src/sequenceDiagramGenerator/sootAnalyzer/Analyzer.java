package sequenceDiagramGenerator.sootAnalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sequenceDiagramGenerator.BranchableStmt;
import sequenceDiagramGenerator.GroupableStmt;
import sequenceDiagramGenerator.MethodNodeAnnot;
import sequenceDiagramGenerator.SourceCodeType;
import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.GroupableHyperNode;
import sequenceDiagramGenerator.hypergraph.GroupableHypergraph;
import sequenceDiagramGenerator.hypergraph.HyperNode;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import sequenceDiagramGenerator.hypergraph.GroupableHyperNodeFactory;
import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractStmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.shimple.Shimple;
import soot.shimple.ShimpleBody;
import soot.util.Chain;
import soot.Type;

public class Analyzer {
	
	//The soot class path is the class path that soot can look through
	//when you give it a class or method to analyze.  If you want to analyze
	//code outside this built project, you should set this.
	
	//Soot inherits the class path of the executable when it is started.
	//so you will already have the basic java class path defined by 
	//environment, and the class path relevant to whatever executable jar
	//or jvm that was run to execute this method.
	public static String getSootClassPath(){
		return Scene.v().getSootClassPath();
	}
	
	public static void setSootClassPath(String val){
		Scene.v().setSootClassPath(val);
	}
	
	public static Hypergraph<MethodNodeAnnot, EdgeAnnotation> AnalyzeSpecificClasses(
			String ApplicationClassName,
			String ClassDirectory,
			String AppendToClassPath){
		List<String> listOfFiles = new ArrayList<String>();
		ListAllFilesInFolder("", ClassDirectory, listOfFiles);
		return AnalyzeSpecificClasses(
				ApplicationClassName,
				listOfFiles,
				AppendToClassPath);
	}
	
	
	private static String RemoveCountFromEnd(String s, int c){
		return s.substring(0, s.length() - c );
	}
	
	private static void ListAllFilesInFolder(
			String prereq,
			String folderName,
			List<String> ListAllFiles){
		File dir = new File(folderName);
		File[] fList = dir.listFiles();
		prereq = prereq + dir.getName() + ".";
		for(File f : fList){
			if(f.isFile()){
				if(f.getName().endsWith(".class")){
					
					ListAllFiles.add(prereq + RemoveCountFromEnd(f.getName(), 6));
				}
			}
			else if(f.isDirectory()){
				ListAllFilesInFolder(prereq, f.getAbsolutePath(), ListAllFiles);
			}
		}
	}

	public static Hypergraph<MethodNodeAnnot, EdgeAnnotation> AnalyzeSpecificClasses(
			List<String> listClassNames,
			String AppendtoClassPath){

		if(AppendtoClassPath != null ){
			if(!AppendtoClassPath.isEmpty()){
				setSootClassPath(getSootClassPath() + ":" + AppendtoClassPath);
			}
		}

		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> toReturn = new GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation>();
		
		//the addition of this line is probably evidence of paranoia on my part.
		//but it took me a long time to figure out these magic words.
		soot.PhaseOptions.v().setPhaseOptionIfUnset("jb", "use-original-names");
		
		for(int i= 0; i < listClassNames.size(); i++){
			SootClass c = Scene.v().loadClassAndSupport(listClassNames.get(i));
			
//			List<SootMethod> listSM = c.getMethods();
//			for(int j = 0; j < listSM.size(); j++){
//				AnalyzeMethod(listSM.get(j));
//			}
			
			AddClassToHypergraph(
					toReturn,
					c);
		}
		
		Analyzer.AddEdgesToHypergraph(toReturn);
		return toReturn;
		
	}
	
	public static Hypergraph<MethodNodeAnnot, EdgeAnnotation> AnalyzeSpecificClasses(
			String ApplicationClassName,
			List<String> otherClassNames,
			String AppendToClassPath){
		
		if(AppendToClassPath != null ){
			if(!AppendToClassPath.isEmpty()){
				setSootClassPath(getSootClassPath() + ":" + AppendToClassPath);
			}
		}
		
		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> toReturn = new GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation>();
		
		//the addition of this line is probably evidence of paranoia on my part.
		//but it took me a long time to figure out these magic words.
		soot.PhaseOptions.v().setPhaseOptionIfUnset("jb", "use-original-names");
		
		SootClass c = Scene.v().loadClassAndSupport(ApplicationClassName);

		c.setApplicationClass();
		
		Chain<SootClass> chainClasses = Scene.v().getClasses();
		
		Iterator<SootClass> iSootClasses = chainClasses.iterator();
		
		while(iSootClasses.hasNext()){
			SootClass aClass = iSootClasses.next();
			if(otherClassNames.contains(aClass.getName())){
				
				AddClassToHypergraph(
					toReturn,
					aClass);
			}
		}

		Analyzer.AddEdgesToHypergraph(toReturn);
		return toReturn;
	}
	
	//The idea here is to take a Class to let soot load, and then grab as much as we can.
	//Loading the class and support will load other classes that are relied upon.
	//Any class that is loaded will be scanned and put in the hypergraph.
	//other approaches are possible, including a trace of called methods
	//I (Brian) figured we could just go back later and trim out if this 
	//is pulling too much.
	//For best results, use the class with the static void main call
	//that will necessarily pull in all dependencies of the program that
	//are accessible given the defined class path at call time.
	//This doesn't use any Shimple functionality, because all we care about is types
	//and Shimple isn't invoked by soot analysis until we start pulling
	//method bodies.
	public static Hypergraph<MethodNodeAnnot, EdgeAnnotation> AnalyzeAllReachableClasses(
			String FromClassName,
			String AppendToClassPath){

		if(AppendToClassPath != null ){
			if(!AppendToClassPath.isEmpty()){
				setSootClassPath(getSootClassPath() + ":" + AppendToClassPath);
			}
		}
		
		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> toReturn = new GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation>();
		
		//the addition of this line is probably evidence of paranoia on my part.
		//but it took me a long time to figure out these magic words.
		soot.PhaseOptions.v().setPhaseOptionIfUnset("jb", "use-original-names");
		
		SootClass c = Scene.v().loadClassAndSupport(FromClassName);

		c.setApplicationClass();
		Chain<SootClass> chainClasses = Scene.v().getClasses();
		
		Iterator<SootClass> iSootClasses = chainClasses.iterator();
		
		while(iSootClasses.hasNext()){
			SootClass aClass = iSootClasses.next();
			
			AddClassToHypergraph(
					toReturn,
					aClass);
		}
		
		return toReturn;
	}
	
	public static void AnalyzeMethod(SootMethod sm){
		try{
			Body b = sm.retrieveActiveBody();
			ShimpleBody sb = Shimple.v().newBody(b);
			PatchingChain<Unit> pcu = sb.getUnits();
			Unit u = pcu.getFirst();
			System.out.println("---------");
			AnalyzePC(pcu, u, new ArrayList<Unit>());
			System.out.println("---------");
		}
		catch(java.lang.RuntimeException ex){
			System.out.println(ex.getMessage());
		}
	}
	
	private static void AddEdgesToHypergraph(
			GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg){
		List<HyperNode<MethodNodeAnnot, EdgeAnnotation>> lNodes = hg.GetNodes();
		for(int i = 0; i < lNodes.size(); i++){
			GroupableStmt aStmt = lNodes.get(i).data.theStmts;
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> gNode = (GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>) lNodes.get(i);
			AddRecStmts(aStmt, gNode, hg);
		}
	}
	
	private static void AddRecStmts(
			GroupableStmt aStmt, 
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> sourceNode, 
			GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg){
		if(aStmt.theStmt.containsInvokeExpr()){
			InvokeExpr ie = null;
			if(aStmt.theStmt instanceof JAssignStmt){
				JAssignStmt assignStmt = (JAssignStmt)aStmt.theStmt;
				ie = assignStmt.getInvokeExpr();
				
			}
			else{
				InvokeStmt anInvoke = (InvokeStmt)aStmt.theStmt;

				ie = anInvoke.getInvokeExpr();
			}
			
			SootMethod sm = ie.getMethod();
			String methodName = sm.getName();
			SootClass sc = sm.getDeclaringClass();
			
			MethodNodeAnnot finder = new MethodNodeAnnot(sm, null);
			
			HyperNode<MethodNodeAnnot, EdgeAnnotation> tarNode = hg.GetCompleteNode(finder);
			
			if(tarNode == null){
				hg.AddNode(finder);
				tarNode = hg.GetCompleteNode(finder);
			}
			List<MethodNodeAnnot> ante = new ArrayList<MethodNodeAnnot>();
			ante.add(sourceNode.data);
			EdgeAnnotation ea = new EdgeAnnotation();
			hg.AddGroupableEdge(ante, tarNode.data, ea, aStmt);
		}
		if(aStmt.theTrueBranch != null){
			AddRecStmts(aStmt.theTrueBranch, sourceNode, hg);
		}
		if(aStmt.theFalseBranch != null){
			AddRecStmts(aStmt.theFalseBranch, sourceNode, hg);
		}
		if(aStmt.theNext != null){
			AddRecStmts(aStmt.theNext, sourceNode, hg);
		}
	}
	
	public static MethodNodeAnnot BuildAnnotFromMethod(
			SootMethod sm){
		PatchingChain<Unit> pcu = null;
		try{
			Body b = sm.retrieveActiveBody();
			ShimpleBody sb = Shimple.v().newBody(b);
			pcu = sb.getUnits();
		}
		catch(java.lang.RuntimeException ex){
			System.out.println("Not possible to analyze method: " + sm.getName() + " Error: " + ex.getMessage());
		}
		if(pcu != null){
			BranchableStmt bs = MakeBranchableStmts(pcu);
			//bs = ReduceToInvokesAndBranches(bs);
			GroupableStmt gs = GroupStmts(bs, new ArrayList<BranchableStmt>());
			MethodNodeAnnot theAnnot = new MethodNodeAnnot(sm, gs);
			return theAnnot;
		}
		return new MethodNodeAnnot(sm, null);
	}
	
	private static GroupableStmt GroupStmts(BranchableStmt aBStmt, List<BranchableStmt> loopDetect){
		if(aBStmt == null){
			return null;
		}
		else if(loopDetect.contains(aBStmt)){
			return new GroupableStmt(true, aBStmt.theStmt);
		}
		else if(aBStmt.theStmt instanceof JIfStmt){
			BranchableStmt tempTrue = aBStmt.theNext;
			BranchableStmt tempFalse = aBStmt.theElse;
			
			List<BranchableStmt> newDetect = new ArrayList<BranchableStmt>();
			newDetect.addAll(loopDetect);
			newDetect.add(aBStmt);
			
			GroupableStmt trueBranch = GroupStmts(tempTrue, newDetect);
			
			newDetect = new ArrayList<BranchableStmt>();
			newDetect.addAll(loopDetect);
			newDetect.add(aBStmt);
			
			GroupableStmt falseBranch = GroupStmts(tempFalse, newDetect);
			
			if(trueBranch.equals(falseBranch)){
				return trueBranch;
			}
			
			while(trueBranch != null){
				
				if(trueBranch.EndsLoop && trueBranch.theStmt.equals(aBStmt.theStmt)){
					GroupableStmt toReturn = new GroupableStmt(false, aBStmt.theStmt);
					toReturn.theTrueBranch = tempTrue.theEquiv;
					toReturn.theNext = falseBranch;
					toReturn.StartsLoop = true;
					aBStmt.theEquiv = toReturn;
					return toReturn;
				}
				
				if(trueBranch.theNext != null && trueBranch.theNext.equals(falseBranch)){
					trueBranch.theNext = null;
					GroupableStmt toReturn = new GroupableStmt(false, aBStmt.theStmt);
					toReturn.theTrueBranch = tempTrue.theEquiv;
					toReturn.theNext = falseBranch;
					aBStmt.theEquiv = toReturn;
					return toReturn;
				}
				trueBranch = trueBranch.theNext;
			}
			
			trueBranch = tempTrue.theEquiv;
			
			while(falseBranch != null){

				if(falseBranch.EndsLoop && falseBranch.theStmt.equals(aBStmt.theStmt)){

					GroupableStmt toReturn = new GroupableStmt(false, aBStmt.theStmt);
					toReturn.theFalseBranch = tempFalse.theEquiv;
					toReturn.theNext = trueBranch;
					toReturn.StartsLoop = true;
					aBStmt.theEquiv = toReturn;
					return toReturn;
				}
				
				if(falseBranch.theNext != null && falseBranch.theNext.equals(trueBranch)){
					falseBranch.theNext = null;
					GroupableStmt toReturn = new GroupableStmt(false, aBStmt.theStmt);
					toReturn.theFalseBranch = tempFalse.theEquiv;
					toReturn.theNext = trueBranch;
					aBStmt.theEquiv = toReturn;
					return toReturn;
				}
				falseBranch = falseBranch.theNext;
			}
			
			GroupableStmt common = null;
			
			while(trueBranch != null){
				falseBranch = tempFalse.theEquiv;
				while(falseBranch != null){
					if(trueBranch.theNext != null && trueBranch.theNext.equals(falseBranch.theNext)){
						common = trueBranch.theNext;
						trueBranch.theNext = null;
						falseBranch.theNext = null;
					}
					
					falseBranch = falseBranch.theNext;
				}
				trueBranch = trueBranch.theNext;
			}
			
			GroupableStmt toReturn = new GroupableStmt(false, aBStmt.theStmt);
			toReturn.theFalseBranch = tempFalse.theEquiv;
			toReturn.theTrueBranch = tempTrue.theEquiv;
			toReturn.theNext = common;
			aBStmt.theEquiv = toReturn;
			return toReturn;
			
		}
		else{
			GroupableStmt toReturn = new GroupableStmt(false, aBStmt.theStmt);
			toReturn.theNext = GroupStmts(aBStmt.theNext, loopDetect);
			aBStmt.theEquiv = toReturn;
			return toReturn;
		}
	}
	
	//The method ReduceToInvokesAndBranches has bugs
	//As it is non-crucial i have commented out references to it
	//but it remains as it may be fixable and viable at some point.
	private static BranchableStmt ReduceToInvokesAndBranches(BranchableStmt aStmt){
		aStmt.seen++;
		if(aStmt.seen == 2){
			return aStmt;
		}
		if(aStmt.theStmt.containsInvokeExpr()  || aStmt.theStmt.branches()){
			if(aStmt.theNext != null)
			{
				aStmt.theNext= ReduceToInvokesAndBranches(aStmt.theNext);
			}
			if(aStmt.theElse != null){
				aStmt.theElse = ReduceToInvokesAndBranches(aStmt.theElse);
			}
			if(aStmt.theElse == null && aStmt.theNext == null && !aStmt.theStmt.containsInvokeExpr()){
				return null;
			}
			else
			{
				return aStmt;
			}
		}
		if(aStmt.theNext!= null){
			return ReduceToInvokesAndBranches(aStmt.theNext);
		}
		return null;
	}

	private static BranchableStmt MakeBranchableStmts(
			PatchingChain<Unit> pcu){
		Iterator<Unit> i = pcu.iterator();
		List<BranchableStmt> listStmt = new ArrayList<BranchableStmt>();
		while(i.hasNext()){
			Unit u = i.next();
			if(!(u instanceof soot.jimple.internal.AbstractStmt))
			{
				//I don't believe this is possible, which reduces the amount of 
				//the subtree we have to deal with in practice.
				throw new java.lang.RuntimeException("Unit is not a subclass of soot.jimple.internal.AbstractStmt");
			}
			else{
				AbstractStmt aStmt = (soot.jimple.internal.AbstractStmt)u;
				listStmt.add(new BranchableStmt(aStmt));
			}
			
		}
		
		for(int j = 0; j < listStmt.size(); j++){
			if(listStmt.get(j).theStmt instanceof JIfStmt){
				JIfStmt ifStmt = (JIfStmt) listStmt.get(j).theStmt;
				Stmt tarStmt = ifStmt.getTarget();
				for(int k = 0; k < listStmt.size(); k++){
					if(tarStmt.equals(listStmt.get(k).theStmt)){
						listStmt.get(j).theNext = listStmt.get(k);
					}
				}
				listStmt.get(j).theElse = listStmt.get(j+1);
			}
			else if(listStmt.get(j).theStmt instanceof JGotoStmt){
				JGotoStmt gotoStmt = (JGotoStmt)listStmt.get(j).theStmt;
				Unit tar = gotoStmt.getTarget();
				for(int k = 0; k < listStmt.size(); k++){
					if(tar.equals(listStmt.get(k).theStmt)){
						listStmt.get(j).theNext = listStmt.get(k);
					}
				}
			}
			else if(listStmt.get(j).theStmt.fallsThrough()){
				if(j < listStmt.size() -1){
					listStmt.get(j).theNext = listStmt.get(j+1);
				}
			}
		}
		return listStmt.get(0);
	}
	
	private static void AnalyzePC(PatchingChain<Unit> pcu, Unit u, List<Unit> seen){
		if(seen.contains(u)){
			return;
			}
		System.out.println(u.toString());
		seen.add(u);
		
		if(!(u instanceof soot.jimple.internal.AbstractStmt))
		{
			//I don't believe this is possible, which reduces the amount of 
			//the subtree we have to deal with in practice.
			throw new java.lang.RuntimeException("Unit is not a subclass of soot.jimple.internal.AbstractStmt");
		}
		else{
			soot.jimple.internal.AbstractStmt aStmt = (soot.jimple.internal.AbstractStmt)u;
			if(aStmt.containsInvokeExpr())
			{
				InvokeExpr ie = aStmt.getInvokeExpr();
				SootMethod sm = ie.getMethod();
				String methodName = sm.getName();
				SootClass sc = sm.getDeclaringClass();
				String className = sc.getName();
						
				if(ie instanceof soot.jimple.internal.AbstractInvokeExpr){
					soot.jimple.internal.AbstractInvokeExpr jie = (soot.jimple.internal.AbstractInvokeExpr)ie;
					if(jie.getArgCount() > 0){
						ValueBox vb = jie.getArgBox(0);
						
					}
				}
						
				try{
					Body bsub = sm.retrieveActiveBody();
				}catch(RuntimeException ex){
					//This may legitimately happen
					//if the method is a call outside of the 
					//analyzed code then we don't have 
					//analysis of it, e.i.:
					//   Random r = new Random();
					//   int i = r.nextInt();
					//we won't be able to retrieve nextInt body.
				}
			}
			if(aStmt.branches()){
				if((u instanceof JIfStmt))
				{
					JIfStmt ifStmt = (JIfStmt)aStmt;
					//toReturn.append(tabs + "Branching Stmt Below\n");
					//String s = aStmt.toString();
					Stmt s = ifStmt.getTarget();
					
				}
				else if(u instanceof JGotoStmt){
					JGotoStmt gotoStmt = (JGotoStmt)aStmt;
					Unit tarU = gotoStmt.getTarget();
				}
			}
		}
		
		
		AnalyzePC(pcu, pcu.getSuccOf(u), seen);
	}
	
	private static void AddClassToHypergraph(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			SootClass aClass
			){

		List<SootMethod> listMethods = aClass.getMethods();
		for(int i = 0; i < listMethods.size(); i++){
			SootMethod m = listMethods.get(i);
			MethodNodeAnnot mannot = BuildAnnotFromMethod(m);
			hg.AddNode(mannot);
		}
	}
	
	private static void AddClassToHypergraphOLD(
			Hypergraph<SourceCodeType, EdgeAnnotation> hg,
			SootClass aClass){
		soot.Type aType = aClass.getType();
		SourceCodeType sct = new SourceCodeType();
		sct.theSootType = aType;
		hg.AddNode(sct);
		
		List<SootMethod> listMethods = aClass.getMethods();
		for(int i = 0; i < listMethods.size(); i++){
			SootMethod m = listMethods.get(i);
			
			Type methodReturn = m.getReturnType();
			SourceCodeType sctMethodRet = new SourceCodeType();
			sctMethodRet.theSootType = methodReturn;
			hg.AddNode(sctMethodRet);
			
			List<SourceCodeType> listArgumentTypes = new ArrayList<SourceCodeType>();
			
			listArgumentTypes.add(sct);
			
			int argCount = m.getParameterCount();
			for(int j = 0; j < argCount; j++){
				Type tParam = m.getParameterType(j);
				SourceCodeType sctParam = new SourceCodeType();
				sctParam.theSootType = tParam;
				listArgumentTypes.add(sctParam);
				hg.AddNode(sctParam);
			}
			
			EdgeAnnotation methodAnnot = new EdgeAnnotation(m.getName(), true);
			methodAnnot.theSootMethod = m;
			hg.AddEdge(listArgumentTypes, sctMethodRet, methodAnnot);
		}
	}
}
