package sequenceDiagramGenerator.sootAnalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sequenceDiagramGenerator.BranchableStmt;
import sequenceDiagramGenerator.GroupableStmt;
import sequenceDiagramGenerator.MethodNodeAnnot;
import sequenceDiagramGenerator.SourceCodeType;
import sequenceDiagramGenerator.TraceStatement;
import sequenceDiagramGenerator.TraceStatement.BranchStatus;
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
import utilities.Utilities;

public class Analyzer {
	
	//Many methods here are helper methods designed to 
	//work around different ways of specifying classes
	//to be analyzed.
	
	//The meat of the work is in:
	//AddClassToHypergraph
	//AddEdgesToHypergraph
	
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
	
	
	//one of two possible entry points for hypergraph generation
	//this is called if the user provides a list of .class files
	//or a directory to search.
	public static Hypergraph<MethodNodeAnnot, EdgeAnnotation> AnalyzeFromSpecificClasses(
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

	//uninteresting, except this is the entry point from UI
	//for JARs.  AddClassToHypergraph and AddEdgeToHypergraph are 
	//common between all entries.
	public static GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> AnalyzeFromJAR(
			List<String> listClassNames,
			String AppendtoClassPath){

		if(AppendtoClassPath != null ){
			if(!AppendtoClassPath.isEmpty()){
				setSootClassPath(getSootClassPath() +Utilities.GetClassPathDelim()  + AppendtoClassPath);
			}
		}

		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> toReturn = new GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation>();
		
		//The following statement makes soot/jimple 
		//preserve original variable names
		//it is crucial to leave this in, even if it seems like 
		//gibberish
		soot.PhaseOptions.v().setPhaseOptionIfUnset("jb", "use-original-names");
		
		for(int i= 0; i < listClassNames.size(); i++){
			//Brian added this try to attempt apk code.
			SootClass c= null;
			try{
				c = Scene.v().loadClassAndSupport(listClassNames.get(i));
			}
			catch(Exception ex){
				Utilities.DebugPrintln("Couldn't load class: " + listClassNames.get(i));
				Utilities.DebugPrintln(ex.getMessage());
			}
			if(c != null){
			AddClassToHypergraph(
					toReturn,
					c);}
			
		}
		
		Analyzer.AddEdgesToHypergraph(toReturn);
		return toReturn;
		
	}
	
	//also calls AddClasstoHyperGraph and AddEdgesToHypergraph.
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
		
		//The following statement makes soot/jimple 
		//preserve original variable names
		//it is crucial to leave this in, even if it seems like 
		//gibberish
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
	
	//traverses each node in the hypergraph,
	//and then calls methods to traverse each stmt
	//in the node to find invoke statements
	//after which we can add edges for those statements.
	private static void AddEdgesToHypergraph(
			GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg){
		List<HyperNode<MethodNodeAnnot, EdgeAnnotation>> lNodes = hg.GetNodes();
		for(int i = 0; i < lNodes.size(); i++){
			GroupableStmt aStmt = lNodes.get(i).data.theStmts;
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> gNode = (GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>) lNodes.get(i);
			AddRecStmts(aStmt, gNode, hg);
		}
	}
	
	//checks aStmt.theStmt to see if it contains an invoke expression.
	//if it does, then build an edge and add that edge to the hypergraph.
	//then traverse any possible child statements and do the same thing.
	private static void AddRecStmts(
			GroupableStmt aStmt, 
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> sourceNode, 
			GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg){
		//Brian added the != null checks for aStmt and aStmt.theStmt
		//as well as the try inside of the first big block.
		if(aStmt != null){
		if(aStmt.theStmt != null && aStmt.theStmt.containsInvokeExpr()){
			try{
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
			
			MethodNodeAnnot finder = new MethodNodeAnnot(sm, null, null);
			
			HyperNode<MethodNodeAnnot, EdgeAnnotation> tarNode = hg.GetCompleteNode(finder);
			
			if(tarNode == null){
				hg.AddNode(finder);
				tarNode = hg.GetCompleteNode(finder);
			}
			List<MethodNodeAnnot> ante = new ArrayList<MethodNodeAnnot>();
			ante.add(sourceNode.data);
			EdgeAnnotation ea = new EdgeAnnotation();
			hg.AddGroupableEdge(ante, tarNode.data, ea, sm);}
			catch(Exception ex){
				Utilities.DebugPrintln("Failed to parse method");
				Utilities.DebugPrintln(ex.getMessage());
			}
		}
		if(aStmt.theTrueBranch != null){
			AddRecStmts(aStmt.theTrueBranch, sourceNode, hg);
		}
		if(aStmt.theFalseBranch != null){
			AddRecStmts(aStmt.theFalseBranch, sourceNode, hg);
		}
		if(aStmt.theNext != null){
			AddRecStmts(aStmt.theNext, sourceNode, hg);
		}}
	}
	
	//Builds the method annotation which we will traverse
	//internal to each node in the graph.
	//This method annotation must be constructed from
	//the PatchingChain of statements available in
	//the body of the sootmethod.
	public static MethodNodeAnnot BuildAnnotFromMethod(
			SootMethod sm){
		PatchingChain<Unit> pcu = null;
		try{
			Body b = sm.retrieveActiveBody();
			ShimpleBody sb = Shimple.v().newBody(b);
			pcu = sb.getUnits();
		}
		catch(java.lang.RuntimeException ex){
			Utilities.DebugPrintln("Not possible to analyze method: " + sm.getName() + " Error: " + ex.getMessage());
		}
		if(pcu != null){
			if(Utilities.DEBUG){
				Utilities.DebugPrintln(" " + sm.getName());
			}
			//First we create the branching representation
			//which transforms the conceptual statement graph
			//from a connected directional graph with potential backwards 
			//edges into a tree in which even loops simply terminate
			//with a marker.  The branchablestmt representation is a tree
			//If/Else branches, for example, do not "reconnect" they simply
			//contain identical objects after a certain point
			//(if the function continues after the if else)
			BranchableStmt bs = MakeBranchableStmts(pcu);
			//I have removed the reduction step
			//it was buggy.  We may reinclude a reduction step
			//but it would probably be easier to rewrite it
			//and do it after creating the GrouapbleStmt representation.
			//bs = ReduceToInvokesAndBranches(bs);
			
			//The GroupableStmt representation takes the tree representation
			//and finds groups in it.
			//It essentially reconnects branches with identical suffixes,
			//and then places the distinct parts of that branch into groups.
			//This is difficult to describe without illustration.
			//While BranchableStmt simply has two possible child BranchableStmts
			//Groupable has three.  A True GroupableStmt and a False GroupableStmt
			//which are both groups, and a Next GroupableStmt which is the next 
			//statement on the same level. 
			GroupableStmt gs = GroupStmts(bs, new ArrayList<BranchableStmt>());
			if(Utilities.DEBUG){
				Utilities.DebugPrintln("--------GS--------");
				Utilities.DebugPrintln(gs.toString());
				Utilities.DebugPrintln("------------------");
			}
			
			List<TraceStatement> ts = GenerateAllPotentialTraces(gs, new ArrayList<TraceStatement>());
			if(Utilities.DEBUG){
				Utilities.DebugPrintln("--------TS--------");
				for(int i = 0; i < ts.size(); i++){
					Utilities.DebugPrintln("****"+i+"****");
					Utilities.DebugPrintln(ts.get(i).toString());
				}
				Utilities.DebugPrintln("------------------");
			}
			
			MethodNodeAnnot theAnnot = new MethodNodeAnnot(sm, gs, ts);
			
			return theAnnot;
		}
		//note that even if we cannot analyze the method
		//we still return a node for it.  Traversal will simply
		//return from such a node.
		return new MethodNodeAnnot(sm, null, new ArrayList<TraceStatement>());
	}

	//creates the branching representation from soot's patchingchain
	//representation.
	//this step creates a tree from a potentially arbitrary
	//directed graph.
	//if there is a loop it will simply be fully traversed in the tree
	//and the leaf will be marked with a boolean to indicate that 
	//it loops.
	private static BranchableStmt MakeBranchableStmts(
			PatchingChain<Unit> pcu){
		Iterator<Unit> i = pcu.iterator();
		List<BranchableStmt> listStmt = new ArrayList<BranchableStmt>();
		//bit lazy, but i prefer a list representation to
		//a iterator representation because i want to be able to look
		//at objects by index.
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
				if(Utilities.DEBUG)
				{
					Utilities.DebugPrintln("  " + aStmt.toString());
				}
				listStmt.add(new BranchableStmt(aStmt));
			}
			
		}
		
		//now that we have a list of branchable statements
		//we need to actually connect the nexts, and elses.
		//technically, JIfStmts do fallthrough but we handle
		//that fact in their own if block.
		//JGotoStmts do not fall through.  The fallthrough
		//check may be pointless except for the final statement
		//in the list.
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
		//return the first statement which points to the root
		//of the tree, or the entire function.
		return listStmt.get(0);
	}
	
	private static List<TraceStatement> GenerateAllPotentialTraces(
			GroupableStmt aGStmt,
			List<TraceStatement> tieUp
			){
		
		if(aGStmt == null){return tieUp;}
		
		List<TraceStatement> toReturn = GenerateAllPotentialTraces(
				aGStmt.theNext, new ArrayList<TraceStatement>());
		
		if(aGStmt.theTrueBranch != null || aGStmt.theFalseBranch != null){
			List<TraceStatement> trueTrace, falseTrace;
			if(aGStmt.theTrueBranch != null){
				trueTrace = GenerateAllPotentialTraces(
						aGStmt.theTrueBranch,
						toReturn);
			}
			else{
				trueTrace = toReturn;
			}
			if(aGStmt.theFalseBranch != null){
				falseTrace = GenerateAllPotentialTraces(
						aGStmt.theFalseBranch,
						toReturn);
			}
			else{
				falseTrace = toReturn;
			}
			if(trueTrace.size() > 0){
				for(int i = 0; i < trueTrace.size(); i++){
					TraceStatement aTStmt = new TraceStatement(aGStmt.theStmt, trueTrace.get(i));
					aTStmt.theBranchStatus = BranchStatus.TrueChosen;
					trueTrace.set(i, aTStmt);
				}
			}
			else{
				trueTrace.add(new TraceStatement(aGStmt.theStmt, null));
			}
			if(falseTrace.size() > 0){
				for(int i = 0; i < falseTrace.size(); i++){
					TraceStatement aTStmt = new TraceStatement(aGStmt.theStmt, falseTrace.get(i));
					aTStmt.theBranchStatus = BranchStatus.FalseChosen;
					falseTrace.set(i, aTStmt);
				}
			}
			else{
				falseTrace.add(new TraceStatement(aGStmt.theStmt, null));
			}
			toReturn = new ArrayList<TraceStatement>();
			toReturn.addAll(trueTrace);
			toReturn.addAll(falseTrace);
		}
		else{
			if(toReturn.size() > 0){
				for(int i = 0; i < toReturn.size(); i++){
					TraceStatement aTStmt = new TraceStatement(aGStmt.theStmt, toReturn.get(i));
					toReturn.set(i, aTStmt);
				}
			}
			else{
				toReturn.add(new TraceStatement(aGStmt.theStmt, null));
			}
		}
		return toReturn;
		
	}
	
	//GroupStmts Takes the tree representation of the function
	//and groups things into blocks.
	//this function could probably be more efficient.
	//it essentially traverses branches, and finds common suffixes 
	//for those branches, and assumes that that is the end of the branch.
	private static GroupableStmt GroupStmts(BranchableStmt aBStmt, List<BranchableStmt> loopDetect){
		if(aBStmt == null){
			//if we terminate in a normal leaf
			return null;
		}
		else if(loopDetect.contains(aBStmt)){
			//if we've already seen this branchable statement
			//we can terminate and return.
			//this is because we've correctly identified every preceding statement
			//and this statement's branch will be resolved when
			//we revisit it where it was seen earlier in the recursion
			return new GroupableStmt(true, aBStmt.theStmt);
		}
		else if(aBStmt.theStmt instanceof JIfStmt){
			//if statements are the fundamental object we're concerned with
			//because if statements are the only things with two unique
			//destinations.
			BranchableStmt tempTrue = aBStmt.theNext;
			BranchableStmt tempFalse = aBStmt.theElse;
			
			//first we recursively solve each branch independently.
			//they may have their own sub-branches.
			List<BranchableStmt> newDetect = new ArrayList<BranchableStmt>();
			newDetect.addAll(loopDetect);
			newDetect.add(aBStmt);
			
			GroupableStmt trueBranch = GroupStmts(tempTrue, newDetect);
			
			newDetect = new ArrayList<BranchableStmt>();
			newDetect.addAll(loopDetect);
			newDetect.add(aBStmt);
			
			GroupableStmt falseBranch = GroupStmts(tempFalse, newDetect);
			
			//now we have each branch in groupablestmt form.
			
			//if the branches are equivalent we can simply 
			//return the true branch as the only thing.
			//i don't think this is actually something that
			//will occur, but it essentially annihilates the if statement
			if(trueBranch.equals(falseBranch)){
				return trueBranch;
			}
			
			//now we're checking if the false branch is a complete
			//suffix of the true branch, i.e. there is no else 
			//this would mean that there is an if block or loop block only.
			while(trueBranch != null){
				
				//if we get to a point in the true branch that ends the loop.
				if(trueBranch.EndsLoop && trueBranch.theStmt.equals(aBStmt.theStmt)){
					GroupableStmt toReturn = new GroupableStmt(false, aBStmt.theStmt);
					toReturn.theTrueBranch = tempTrue.theEquiv;
					toReturn.theNext = falseBranch;
					toReturn.StartsLoop = true;
					aBStmt.theEquiv = toReturn;
					return toReturn;
				}
				
				//if we get to a point after which the false branch is equal
				//to the "rest" of the true branch, if with no else.
				//then we have a true branch and a next branch
				//but no real false branch.
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
			
			//now we're checking the opposite case, 
			//the true branch is a suffix of the false branch.
			//this means we have an else case but an empty if
			//this may not be very pretty coding but it is
			//at least possible especially w/ bytecode.
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
			
			//if we get here, it means that both the false
			//and true branches have unique and meaningful code
			//and we have to look through both to find a common suffix.
			GroupableStmt common = null;
			
			while(trueBranch != null){
				falseBranch = tempFalse.theEquiv;
				while(falseBranch != null){
					if(trueBranch.theNext != null && trueBranch.theNext.equals(falseBranch.theNext)){
						//if we find a common suffix, 
						//we set it equal to common,
						//and we kill the rest of the 
						//true and false branches.
						//now they are grouped as sub groups
						//under the branch, and we can
						//also detect the point at which
						//execution "exits" the branch.
						//starting at common
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
			//this is simply what we do if we're not looking at an if statement
			//just wrap it using the new class and proceed to the 
			//next statement.
			GroupableStmt toReturn = new GroupableStmt(false, aBStmt.theStmt);
			toReturn.theNext = GroupStmts(aBStmt.theNext, loopDetect);
			aBStmt.theEquiv = toReturn;
			return toReturn;
		}
	}



	
	//Adds a class to the hypergraph
	//by going through every method 
	//and creating a node for it.
	private static void AddClassToHypergraph(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			SootClass aClass
			){

		if(Utilities.DEBUG){
			Utilities.DebugPrintln(aClass.getName());
		}
		List<SootMethod> listMethods = aClass.getMethods();
		for(int i = 0; i < listMethods.size(); i++){
			SootMethod m = listMethods.get(i);
			MethodNodeAnnot mannot = BuildAnnotFromMethod(m);
			hg.AddNode(mannot);
		}
	}
	
	
	//The method ReduceToInvokesAndBranches has bugs
	//As it is non-crucial i have commented out references to it
	//but it remains as it may be fixable and viable at some point.
//	private static BranchableStmt ReduceToInvokesAndBranches(BranchableStmt aStmt){
//		aStmt.seen++;
//		if(aStmt.seen == 2){
//			return aStmt;
//		}
//		if(aStmt.theStmt.containsInvokeExpr()  || aStmt.theStmt.branches()){
//			if(aStmt.theNext != null)
//			{
//				aStmt.theNext= ReduceToInvokesAndBranches(aStmt.theNext);
//			}
//			if(aStmt.theElse != null){
//				aStmt.theElse = ReduceToInvokesAndBranches(aStmt.theElse);
//			}
//			if(aStmt.theElse == null && aStmt.theNext == null && !aStmt.theStmt.containsInvokeExpr()){
//				return null;
//			}
//			else
//			{
//				return aStmt;
//			}
//		}
//		if(aStmt.theNext!= null){
//			return ReduceToInvokesAndBranches(aStmt.theNext);
//		}
//		return null;
//	}
	
//	public static Hypergraph<MethodNodeAnnot, EdgeAnnotation> AnalyzeAllReachableClasses(
//			String FromClassName,
//			String AppendToClassPath){
//
//		if(AppendToClassPath != null ){
//			if(!AppendToClassPath.isEmpty()){
//				setSootClassPath(getSootClassPath() + ":" + AppendToClassPath);
//			}
//		}
//		
//		GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> toReturn = new GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation>();
//		
//		//the addition of this line is probably evidence of paranoia on my part.
//		//but it took me a long time to figure out these magic words.
//		soot.PhaseOptions.v().setPhaseOptionIfUnset("jb", "use-original-names");
//		
//		SootClass c = Scene.v().loadClassAndSupport(FromClassName);
//
//		c.setApplicationClass();
//		Chain<SootClass> chainClasses = Scene.v().getClasses();
//		
//		Iterator<SootClass> iSootClasses = chainClasses.iterator();
//		
//		while(iSootClasses.hasNext()){
//			SootClass aClass = iSootClasses.next();
//			
//			AddClassToHypergraph(
//					toReturn,
//					aClass);
//		}
//		
//		return toReturn;
//	}
	
	//This is a debug function which I am no longer using
//	public static void AnalyzeMethod(SootMethod sm){
//		try{
//			Body b = sm.retrieveActiveBody();
//			ShimpleBody sb = Shimple.v().newBody(b);
//			PatchingChain<Unit> pcu = sb.getUnits();
//			Unit u = pcu.getFirst();
//			System.out.println("---------");
//			AnalyzePC(pcu, u, new ArrayList<Unit>());
//			System.out.println("---------");
//		}
//		catch(java.lang.RuntimeException ex){
//			System.out.println(ex.getMessage());
//		}
//	}
	
//	private static void AnalyzePC(PatchingChain<Unit> pcu, Unit u, List<Unit> seen){
//	if(seen.contains(u)){
//		return;
//		}
//	System.out.println(u.toString());
//	seen.add(u);
//	
//	if(!(u instanceof soot.jimple.internal.AbstractStmt))
//	{
//		//I don't believe this is possible, which reduces the amount of 
//		//the subtree we have to deal with in practice.
//		throw new java.lang.RuntimeException("Unit is not a subclass of soot.jimple.internal.AbstractStmt");
//	}
//	else{
//		soot.jimple.internal.AbstractStmt aStmt = (soot.jimple.internal.AbstractStmt)u;
//		if(aStmt.containsInvokeExpr())
//		{
//			InvokeExpr ie = aStmt.getInvokeExpr();
//			SootMethod sm = ie.getMethod();
//			String methodName = sm.getName();
//			SootClass sc = sm.getDeclaringClass();
//			String className = sc.getName();
//					
//			if(ie instanceof soot.jimple.internal.AbstractInvokeExpr){
//				soot.jimple.internal.AbstractInvokeExpr jie = (soot.jimple.internal.AbstractInvokeExpr)ie;
//				if(jie.getArgCount() > 0){
//					ValueBox vb = jie.getArgBox(0);
//					
//				}
//			}
//					
//			try{
//				Body bsub = sm.retrieveActiveBody();
//			}catch(RuntimeException ex){
//				//This may legitimately happen
//				//if the method is a call outside of the 
//				//analyzed code then we don't have 
//				//analysis of it, e.i.:
//				//   Random r = new Random();
//				//   int i = r.nextInt();
//				//we won't be able to retrieve nextInt body.
//			}
//		}
//		if(aStmt.branches()){
//			if((u instanceof JIfStmt))
//			{
//				JIfStmt ifStmt = (JIfStmt)aStmt;
//				//toReturn.append(tabs + "Branching Stmt Below\n");
//				//String s = aStmt.toString();
//				Stmt s = ifStmt.getTarget();
//				
//			}
//			else if(u instanceof JGotoStmt){
//				JGotoStmt gotoStmt = (JGotoStmt)aStmt;
//				Unit tarU = gotoStmt.getTarget();
//			}
//		}
//	}
//	
//	
//	AnalyzePC(pcu, pcu.getSuccOf(u), seen);
//}
	
//	private static void AddClassToHypergraphOLD(
//			Hypergraph<SourceCodeType, EdgeAnnotation> hg,
//			SootClass aClass){
//		soot.Type aType = aClass.getType();
//		SourceCodeType sct = new SourceCodeType();
//		sct.theSootType = aType;
//		hg.AddNode(sct);
//		
//		List<SootMethod> listMethods = aClass.getMethods();
//		for(int i = 0; i < listMethods.size(); i++){
//			SootMethod m = listMethods.get(i);
//			
//			Type methodReturn = m.getReturnType();
//			SourceCodeType sctMethodRet = new SourceCodeType();
//			sctMethodRet.theSootType = methodReturn;
//			hg.AddNode(sctMethodRet);
//			
//			List<SourceCodeType> listArgumentTypes = new ArrayList<SourceCodeType>();
//			
//			listArgumentTypes.add(sct);
//			
//			int argCount = m.getParameterCount();
//			for(int j = 0; j < argCount; j++){
//				Type tParam = m.getParameterType(j);
//				SourceCodeType sctParam = new SourceCodeType();
//				sctParam.theSootType = tParam;
//				listArgumentTypes.add(sctParam);
//				hg.AddNode(sctParam);
//			}
//			
//			EdgeAnnotation methodAnnot = new EdgeAnnotation(m.getName(), true);
//			methodAnnot.theSootMethod = m;
//			hg.AddEdge(listArgumentTypes, sctMethodRet, methodAnnot);
//		}
//	}
}
