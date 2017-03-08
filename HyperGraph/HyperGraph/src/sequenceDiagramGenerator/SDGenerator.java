package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.List;

import sequenceDiagramGenerator.Query.QueryResponse;
import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.GroupableHyperEdge;
import sequenceDiagramGenerator.hypergraph.GroupableHyperNode;
import sequenceDiagramGenerator.hypergraph.GroupableHypergraph;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import sequenceDiagramGenerator.sdedit.SDListAndReturns;
import sequenceDiagramGenerator.sdedit.SDMessage;
import sequenceDiagramGenerator.sdedit.SDObject;
import sequenceDiagramGenerator.sdedit.SDObject.TaintState;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;
import sequenceDiagramGenerator.sootAnalyzer.SeqDiaSet;
import soot.SootClass;
import soot.SootField;
import soot.AbstractValueBox;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.ThisRef;
import soot.jimple.internal.IdentityRefBox;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.internal.JArrayRef;
import utilities.ByRefInt;
import utilities.Utilities;

public class SDGenerator {
	
//	public static void Generate(List<String> listClasses, String ClassPath, String SaveFile){
//		Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(listClasses, ClassPath);
//
//		//SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
//	}
	
	public static List<SequenceDiagram> GenerateAll(
			GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode,
			Query q) throws Exception {
		// TODO Auto-generated method stub
		List<SequenceDiagram> listSDs = GenerateAllDiagrams(hg, aNode, q);
//		SequenceDiagram sd = GenerateDiagramObj(hg, aNode, SaveFile);
		List<SequenceDiagram> alreadyGenerated = new ArrayList<SequenceDiagram>();
		for(int i = 0; i < listSDs.size(); i++){
			QueryResponse qr = q.CheckFinishedDiagram(listSDs.get(i));
			if(qr == QueryResponse.True){
				boolean makeDiagram = true;
				for(int j = alreadyGenerated.size() -1; j>= 0; j--){
					if(listSDs.get(i).isEquivalent(alreadyGenerated.get(j))){
						makeDiagram = false;
						break;
					}
				}
				if(makeDiagram){
					String MethodTrunc = Utilities.Truncate(aNode.data.GetMethod().getName());
					listSDs.get(i).SetName(MethodTrunc + String.valueOf(i) + ".pdf");
					//String outFile = Utilities.endWithSlash(saveDir) + MethodTrunc + String.valueOf(i) + ".pdf";
					//listSDs.get(i).CreatePDF(outFile);
					alreadyGenerated.add(listSDs.get(i));
				}
			}
		}
		return alreadyGenerated;
	}

	//Given a pre-constructed hypergraph
	//and a hypernode at which to begin traversal
	//generate a sequence diagram at SaveFile
	public static SequenceDiagram Generate(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			Query q) throws Exception{

		SequenceDiagram sd = GenerateDiagramObj(hg, aGNode, q);
		return sd;
	}

	public static void GenTest(
			GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode,
			String saveFile,
			Query q) throws Exception {
		SequenceDiagram sd = GenerateDiagramObj(hg, aNode, q);
		sd.TestOutput(saveFile);
	}
	

	private static List<SequenceDiagram> GenerateAllDiagrams(
			GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode,
			Query q) throws Exception {
		
		List<SequenceDiagram> listToReturn;
		
		SDObject.finalUnnamed = 0;
		SequenceDiagram sd = new SequenceDiagram();
		SDObject outerObject = null;
		if(aNode.data.GetMethod().isStatic()){
			outerObject = sd.GetStaticObject(aNode.data.GetMethod().getDeclaringClass());
		}
		else{
			outerObject = new SDObject(aNode.data.GetMethod().getDeclaringClass(), SDObject.GetUniqueName(), false, false, TaintState.Safe);
			sd.AddObject(outerObject);
			sd.AttachNameToObject("this", outerObject);
		}
		SDListAndReturns allSDs = new SDListAndReturns();
		allSDs.listDiagrams.add(sd);
		listToReturn = MakeAllDiagrams(
				"", 
				hg, 
				aNode, 
				allSDs,
				0,
				outerObject,
				new ArrayList<String>(),
				q,
				0).listDiagrams;

		return listToReturn;
	}
	
//	private static List<List<Integer>> GetAllChoicesFromNode(
//			GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg, 
//			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode) {
//		//TODO: Write this or figure something else out here.
//		return new ArrayList<List<Integer>>();
//	}


	private static SequenceDiagram GenerateDiagramObj(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			Query q) throws Exception{

		SDListAndReturns allSDs = new SDListAndReturns();
		SequenceDiagram sd = new SequenceDiagram();
		allSDs.listDiagrams.add(sd);
		SDObject outerObject = null;
		if(aGNode.data.GetMethod().isStatic()){
			outerObject = sd.GetStaticObject(aGNode.data.GetMethod().getDeclaringClass());
		}
		else{
			outerObject = new SDObject(aGNode.data.GetMethod().getDeclaringClass(), SDObject.GetUniqueName(), false, false, TaintState.Safe);
			sd.AddObject(outerObject);
		}
		MakeDiagram(
				"", 
				hg, 
				aGNode, 
				allSDs,
				0,
				outerObject,
				new ArrayList<String>(),
				null,
				new ByRefInt(0),
				q,
				0);
		//RecFillNodeDiagram(hg,aGNode, sd, SDObject.GetUniqueName());
		return sd;
	}

	private static SDListAndReturns MakeAllDiagrams(
			String outerMethodName,
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			SDListAndReturns allSDs,
			int sdIndex,
			SDObject outerObject,
			List<String> listCallStack,
			Query q,
			int lvl) throws Exception{
	
		SDListAndReturns toReturn = allSDs.Copy(sdIndex);
		//SDListAndReturns toReturn = new SDListAndReturns();
		//SequenceDiagram sd = allSDs.listDiagrams.get(sdIndex);
		//List<SequenceDiagram> toReturn = new ArrayList<SequenceDiagram>();
		//toReturn.listDiagrams.add(sd);
		
		if(aGNode == null){return toReturn;}

		QueryResponse result = q.RunOnMethodName(outerMethodName);
		if(result == QueryResponse.False || result == QueryResponse.Filter){
			return toReturn;
		}
		
		if(aGNode.data.getTraces() == null){return toReturn;}
		
		int tstmtssize = aGNode.data.getTraces().size();
		if(tstmtssize == 0){return toReturn;}

		SDListAndReturns cloneSource = toReturn.clone();
		toReturn.clear();
		
		for(int i = 0; i < tstmtssize; i++){

			TraceStatement tc = aGNode.data.getTraces().get(i);
			QueryDataContainer qd = new QueryDataContainer(tc);
			result = q.RunOnData(new QueryDataContainer(tc));
			if(result == QueryResponse.False){
				continue;
			}
			SDListAndReturns toSendDown = cloneSource.clone();
			if(result == QueryResponse.Filter){
				toReturn.addAll(toSendDown);
				continue;
			}
			toReturn.addAll(RecFillTraceAllStmtDiagram(
					outerMethodName,
					hg,
					aGNode,
					tc,
					toSendDown,
					sdIndex,
					outerObject.ID,
					listCallStack,
					true,
					null,
					null,
					q,
					lvl));
		}
		toReturn.compress();
		return toReturn;
	}
	
	private static SDListAndReturns MakeDiagram(
			String outerMethodName,
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			SDListAndReturns allSDs,
			int sdIndex,
			SDObject outerObject,
			List<String> listCallStack,
			List<Integer> options,
			ByRefInt optionIndex,
			Query q,
			int lvl) throws Exception{
	
		SDListAndReturns toReturn = allSDs.Copy(sdIndex);
		//toReturn.listDiagrams.add(allSDs.listDiagrams.get(sdIndex));

		QueryResponse result = q.RunOnMethodName(outerMethodName);
		if(result == QueryResponse.False || result == QueryResponse.Filter){
			return toReturn;
		}
		
		if(aGNode == null){return toReturn;}
		List<TraceStatement> tstmts = aGNode.data.getTraces();
		if(tstmts == null || tstmts.size() == 0){return toReturn;}

		int choice = 0;
		if(options != null){
			choice = options.get(optionIndex.theInt);
		}
		optionIndex.theInt = optionIndex.theInt + 1;
		
		TraceStatement tc = tstmts.get(choice);
		QueryDataContainer qd = new QueryDataContainer(tc);
		QueryResponse qr = q.RunOnData(qd);
		if(qr == QueryResponse.False || qr == QueryResponse.Filter){
			return toReturn;
		}
		
		return RecFillTraceAllStmtDiagram(
				outerMethodName,
				hg,
				aGNode,
				tc,
				allSDs,
				sdIndex,
				outerObject.ID,
				listCallStack,
				false,
				options,
				optionIndex,
				q,
				lvl);
	}
	
	private static SDListAndReturns RecFillTraceAllStmtDiagram(
			String outerMethodName,
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			TraceStatement aStmt,
			SDListAndReturns allSDs,
			int sdIndex,
			int sourceObjID,
			List<String> listCallStack,
			boolean FindAll,
			List<Integer> options,
			ByRefInt optionIndex,
			Query q,
			int lvl) throws Exception
	{
		//List<SequenceDiagram> toReturn = new ArrayList<SequenceDiagram>();
		SDListAndReturns toReturn = allSDs.Copy(sdIndex);
		SequenceDiagram sd = allSDs.listDiagrams.get(sdIndex);
		
		//if(FindAll){
		//	toReturn.listDiagrams.add(sd);
		//}
		
		if(aStmt == null){return toReturn;}
		
		SDObject sourceObj = sd.GetObjectFromID(sourceObjID);
		
		//This is the point where I am currently attempting to handle
		//assignment statements.
		if(aStmt.theStmt instanceof IdentityStmt){
			IdentityStmt identStmt = (IdentityStmt)aStmt.theStmt;
			JimpleLocal jlLeft = extractValue(identStmt.getLeftOp());
			ParameterRef paramRight = extractParam(identStmt.getRightOp());
			if(jlLeft != null && paramRight != null){
				int paramInd = paramRight.getIndex();
				String leftName = jlLeft.getName();
				String rightName = "@parameter" + String.valueOf(paramInd);

				SDObject sdRight = sd.GetObjectFromName(rightName);
				if(sdRight != null){
					sd.AttachNameToObject(leftName, sdRight);
				}
				else{
					//problem, can't get soot class the way I usually do.
					soot.Type sc = paramRight.getType();
					SDObject newObj = new SDObject(sc, "", false, false, TaintState.Safe);
					sd.AddObject(newObj);
					sd.AttachNameToObject(leftName, newObj);
				}
			}
			else if(jlLeft != null){
				String leftName = jlLeft.getName();
				SDObject sdRight = sd.GetObjectFromName("this");
				if(sdRight != null){
					sd.AttachNameToObject(leftName, sdRight);
				}
				else{
					soot.Type st = jlLeft.getType();
					SDObject newObj = new SDObject(st, "", true, false, TaintState.Safe);
					sd.AddObject(newObj);
					sd.AttachNameToObject(leftName, newObj);
				}
			}
		}
		if(aStmt.theStmt instanceof AssignStmt){
			AssignStmt assignStmt = (AssignStmt)aStmt.theStmt;
			
			toReturn = HandleAssignment(
					hg, 
					aGNode, 
					allSDs, 
					sdIndex, 
					sourceObj, 
					outerMethodName, 
					listCallStack, 
					FindAll, 
					options, 
					optionIndex, 
					assignStmt,
					q,
					lvl);
			
		}
		//If a statement contains an invoke expression
		//that expression must be extracted, we must find
		//the relevant hyperedge out of the current node
		//and traverse it.
		else if(aStmt.theStmt.containsInvokeExpr()){
			
			toReturn = HandleInvoke(
					aStmt.theStmt.getInvokeExpr(), 
					hg, 
					aGNode, 
					allSDs, 
					sdIndex, 
					sourceObj, 
					outerMethodName, 
					listCallStack, 
					FindAll, 
					options, 
					optionIndex,
					q,
					lvl);
					
		}
		if(aStmt.theStmt instanceof JReturnStmt){
			JReturnStmt rs = (JReturnStmt)aStmt.theStmt;
			SDObject retObj = extractObject(rs.getOp(), sd);
			toReturn.SetSafeReturn(0, retObj);
		}
		//after we've handled everything in this statement
		//including any calls and subsequent calls generated
		//we traverse to the next statement.
		if(FindAll){
			SDListAndReturns toReturnNew = new SDListAndReturns();
			for(int i = 0; i < toReturn.size(); i++){
				toReturnNew.addAll(RecFillTraceAllStmtDiagram(
						outerMethodName, 
						hg, 
						aGNode, 
						aStmt.theNext, 
						toReturn,
						i,
						sourceObjID,
						listCallStack,
						FindAll,
						null,
						null,
						q,
						lvl));
			}
			return toReturnNew;
		}
		else{
			RecFillTraceAllStmtDiagram(
					outerMethodName, 
					hg, 
					aGNode, 
					aStmt.theNext, 
					allSDs,
					sdIndex,
					sourceObjID,
					listCallStack,
					FindAll,
					options,
					optionIndex,
					q,
					lvl);
			return null;
		}
	}
	
	private static SDListAndReturns HandleInvoke(InvokeExpr ie,
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			SDListAndReturns allSDs,
			int sdIndex,
			SDObject sourceObj,
			String outerMethodName,
			List<String> listCallStack,
			boolean FindAll,
			List<Integer> options,
			ByRefInt optionIndex,
			Query q,
			int lvl
			) throws Exception{
		
		SequenceDiagram sd = allSDs.listDiagrams.get(sdIndex);
		SDListAndReturns toReturn = allSDs.Copy(sdIndex);
		//toReturn.listDiagrams.add(sd);
		
		SootMethod calledMethod = ie.getMethod();
		QueryResponse qr = q.RunOnMethodName(Utilities.getMethodString(calledMethod));
		if(qr == QueryResponse.False || qr == QueryResponse.Filter){
			return toReturn;
		}
		GroupableHyperEdge<EdgeAnnotation> gEdge = aGNode.GetGroupableEdge(calledMethod);
		if(gEdge != null){
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> subGNode = 
					(GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>) hg.GetCompleteNode(gEdge.targetNode);
			
			//If an object doesn't have an instance name
			//(might be static)
			//this might not work, in which case
			//the initial assignment to getuniquename
			//must suffice for the name
			List<Value> lv = ie.getUseBoxes();
		
			List<SDObject> params = new ArrayList<SDObject>();
			String tarObjName = "";
			if(lv.size() > 0){
				Object v = lv.get(0);
				JimpleLocal jl = extractValue(v);
				if(jl != null){
					tarObjName = jl.getName();
				}
				for(int i =1; i < lv.size(); i++){
					JimpleLocal jlparam = extractValue(lv.get(i));
					if(jlparam != null){
						SDObject anObj = sd.GetObjectFromName(jlparam.getName());
						params.add(anObj);
					}
					else{
						params.add(null);
					}
				}
			}
			
			//grab the three things we need to write a 
			//message into sd, source, target, message
			
			SootMethod sm = subGNode.data.GetMethod();
			SootClass scTarget = sm.getDeclaringClass();
			SootClass scSource = aGNode.data.GetMethod().getDeclaringClass();
			
			//SDObjects for source and target are created with
			//two piece of information, class and instance name
			//note source's instance name is passed in from above.
			//target's is locally available to us.
			//SDObject sdSource = sd.GetObjectFromName(sourceName);
			boolean isSuper = false;
			SDObject sdTarget = null;
			if(tarObjName.equals("this")){
				sdTarget = sourceObj;
				if(outerMethodName.equals(Utilities.getMethodString(sm))){
					isSuper = true;
				}
			}
			else if(!tarObjName.equals("")){
				sdTarget = sd.GetObjectFromName(tarObjName);
			}
			
			//If there is already a matching SDObject in sd
			//this will silently fail and we will simply link
			//to the existing object.  This may need to change
			//per the assignment statement problem.
			if(sdTarget == null){
				if(sm.isStatic()){
					sdTarget= sd.GetStaticObject(scTarget);
				}
				else{
					if(tarObjName == null || tarObjName.length() == 0){
						tarObjName = SDObject.GetUniqueName();
					}
					sdTarget = new SDObject(scTarget, tarObjName, false, false, TaintState.Safe);
					sd.AddObject(sdTarget);
				}
			}
			
			//now that the sd has a source and target, we can
			//add the message.
			SDMessage msg = new SDMessage(sourceObj, sdTarget, sm, isSuper, lvl, TaintState.Safe);
			sd.AddMessage(msg);
			
			String CallName = 
					aGNode.data.GetMethod().getDeclaringClass().getName() + 
					"." +
					aGNode.data.GetMethod().getName();
			
			if(!listCallStack.contains(CallName)){
				listCallStack.add(CallName);
				//now that we are "at" the destination point
				//of the message, we traverse into
				//the relevant hypernode for that new method.
				sd.PushNames();
				for(int i = 0; i < params.size(); i++){
					if(params.get(i) != null){
						sd.AttachNameToObject("@parameter" + String.valueOf(i), params.get(i));
					}
				}
				if(sdTarget != null && !sdTarget.isStatic){
					sd.AttachNameToObject("this", sdTarget);
				}
				if(FindAll){
					toReturn = MakeAllDiagrams(
							Utilities.getMethodString(aGNode.data.GetMethod()), 
							hg, 
							subGNode, 
							allSDs,
							sdIndex,
							sdTarget,
							listCallStack,
							q,
							lvl +1);
					for(int i = 0; i < toReturn.listDiagrams.size(); i++){
						toReturn.listDiagrams.get(i).PopNames();
					}
				}
				else{
					toReturn = MakeDiagram(
							Utilities.getMethodString(aGNode.data.GetMethod()), 
							hg, 
							subGNode, 
							allSDs,
							sdIndex,
							sdTarget,
							listCallStack,
							options,
							optionIndex,
							q,
							lvl +1);
					sd.PopNames();
				}
				listCallStack.remove(listCallStack.size() -1);
			}
		}
		else
		{
			//need to revisit this once assignment problem is cleared up
			//it may either be impossible or may represent calls like
			//int x = (new Random()).nextInt()
			//where we don't have soot data on calls to "external" libraries
		}
		return toReturn;
	}
	
	private static SDListAndReturns HandleAssignment(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			SDListAndReturns allSDs,
			int sdIndex,
			SDObject sourceObj,
			String outerMethodName,
			List<String> listCallStack,
			boolean FindAll,
			List<Integer> options,
			ByRefInt optionIndex,
			AssignStmt assignStmt,
			Query q,
			int lvl) throws Exception{

		SequenceDiagram sd = allSDs.listDiagrams.get(sdIndex);
		SDListAndReturns toReturn = allSDs.Copy(sdIndex);
		//toReturn.listDiagrams.add(sd);
		
		String leftName = extractName(assignStmt.getLeftOp(), sd);
		if(leftName == null || leftName.equals("")){
			return toReturn;
		}
		JNewExpr jne = extractNew(assignStmt.getRightOp());
		if(jne != null){
			SootClass sc = jne.getBaseType().getSootClass();
			SDObject newObj = new SDObject(sc, "", true, false, TaintState.Safe);
			sd.AddObject(newObj);
			sd.AttachNameToObject(leftName, newObj);
			return toReturn;
		}
		if(assignStmt.containsInvokeExpr()){
			SDListAndReturns allResults = HandleInvoke(
					assignStmt.getInvokeExpr(), 
					hg, 
					aGNode, 
					allSDs, 
					sdIndex, 
					sourceObj, 
					leftName, 
					listCallStack, 
					FindAll, 
					options, 
					optionIndex,
					q,
					lvl);
			
			InvokeExpr ie = assignStmt.getInvokeExpr();
			soot.Type rType = ie.getMethod().getReturnType();
			for(int i = 0; i < allResults.size(); i++){
				SDObject retObj = null;
				if(allResults.listReturns.size() <= i || allResults.listReturns.get(i) == null){
					retObj = new SDObject(rType, "", false, false, TaintState.Safe);
				}
				else{
					retObj = allResults.listReturns.get(i);
				}
				allResults.listDiagrams.get(i).AttachNameToObject(leftName, retObj);
			}
			return allResults;
		}
		
		SDObject rightObj = extractObject(assignStmt.getRightOp(), sd);
		
		if(rightObj != null){
			sd.AttachNameToObject(leftName, rightObj);
		}
		else{
			rightObj = new SDObject("UnknownType", leftName, false, false, TaintState.Safe);
			sd.AddObject(rightObj);
			sd.AttachNameToObject(leftName, rightObj);
		}
		return toReturn;
	}
	
	private static SDObject extractObject(Object v, SequenceDiagram sd){
		return (SDObject)extract(v, sd, 0);
	}
	private static String extractName(Object v, SequenceDiagram sd){
		return (String)extract(v, sd, 1);
	}
	
	private static Object extract(Object v, SequenceDiagram sd, int mode){
		Object obj = null;
		if(v instanceof AbstractValueBox){
			AbstractValueBox avb = (AbstractValueBox)v;
			obj = avb.getValue();
		}
		else if(v instanceof IdentityRefBox){
			IdentityRefBox b = (IdentityRefBox)v;
			obj = b.getValue();
		}
		else if(v instanceof StaticFieldRef){
			StaticFieldRef sfr = (StaticFieldRef)v;
			SootField sf = sfr.getField();
			String s=sf.getName();
			SootClass sc = sf.getDeclaringClass();
			SDObject sdo = sd.GetStaticObject(sc);
			if(mode == 0){
				return sdo.getField(s, sd);
			}
			else{
				return sdo.GetName() + "." + s;
			}
			
		}
		else if(v instanceof JInstanceFieldRef){
			JInstanceFieldRef ret = (JInstanceFieldRef)v;
			Value val = ret.getBase();
			SDObject baseObj = extractObject(val, sd);
			SootField sf = ret.getField();
			if(mode == 0){
				return baseObj.getField(sf.getName(), sd);
			}
			else if(mode == 1){
				return baseObj.GetName() + "." + sf.getName();
			}
		}
		else{
			obj = v;
		}
		if(obj instanceof JimpleLocal){
			JimpleLocal jl = (JimpleLocal)obj;
			if(mode == 0){
				return sd.GetObjectFromName(jl.getName());
			}
			else if(mode == 1){
				return jl.getName();
			}
		}
		if(obj instanceof ParameterRef){
			ParameterRef pr = (ParameterRef)obj;
			int pind = pr.getIndex();
			
			String prname = "@parameter" + String.valueOf(pind);
			if(mode == 0){
				return sd.GetObjectFromName(prname);
			}
			else{
				return prname;
			}
		}
		if(obj instanceof JArrayRef){
			JArrayRef jr = (JArrayRef)obj;
			Value vb = jr.getBase();
			Value vi = jr.getIndex();
			//Brian 2017 unfinished
			return vb.toString() + "[" + vi.toString() + "]";
		}
		//Brian added this, used to return null until recently.
		if(mode == 0){
			return new SDObject("TotalUnk", SDObject.GetUniqueName(),false, false , TaintState.Safe);
		}
		else{
			return SDObject.GetUniqueName();
		}
		//end of added code.
	}
	
	//helper function to extract a local from a Value
	//Values may or may not contain locals
	//the statement
	//int x = 0 
	//will contain two values, x and 0
	//x will successfully cast to a jimplelocal in 
	//one format or another, 0 will not
	//so this method CAN return null.
	//in general, at the moment, we don't care about constants.
	private static JimpleLocal extractValue(Object v){
		Object obj = null;
		if(v instanceof AbstractValueBox){
			AbstractValueBox jlb = (AbstractValueBox)v;
			obj = jlb.getValue();
		}
		else
		{
			obj = v;
		}
		if(obj instanceof JimpleLocal){
			JimpleLocal jl = (JimpleLocal)obj;
			return jl;
		}
		return null;
	}
	
	private static ParameterRef extractParam(Object v){
		Object obj = null;
		if(v instanceof IdentityRefBox){
			IdentityRefBox jlb = (IdentityRefBox)v;
			obj = jlb.getValue();
		}
		else if(v instanceof ThisRef){
			ThisRef tr = (ThisRef)v;
			return null;
		}
		else{
			obj = v;
		}
		if(obj instanceof ParameterRef){
			return (ParameterRef)obj;
		}
		return null;
	}
	
	private static JNewExpr extractNew(Object v){
		if(v instanceof JNewExpr){
			JNewExpr ret = (JNewExpr)v;
			return ret;
		}
		return null;
	}
	private static JVirtualInvokeExpr extractInvoke(Object v){
		if(v instanceof JVirtualInvokeExpr){
			return (JVirtualInvokeExpr)v;
		}
		return null;
	}

//	private static void RecFillTraceStmtDiagram(
//	String outerMethodName,
//	Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
//	GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
//	TraceStatement aStmt,
//	SequenceDiagram sd,
//	SDObject sourceObj,
//	List<String> listCallStack,
//	List<Integer> options,
//	ByRefInt optionIndex) throws Exception
//{
//if(aStmt == null){return;}
//
////This is the point where I am currently attempting to handle
////assignment statements.
////This is not completed.
//if(aStmt.theStmt instanceof AssignStmt){
//	AssignStmt assignStmt = (AssignStmt)aStmt.theStmt;
//	JimpleLocal jlLeft = extractValue(assignStmt.getLeftOp());
//	JimpleLocal jlRight = extractValue(assignStmt.getRightOp());
//	
//	if(jlLeft != null && jlRight != null){
//		String leftName = jlLeft.getName();
//		String rightName = jlRight.getName();
//		SDObject sdRight = sd.GetObjectFromName(rightName);
//		sd.AttachNameToObject(leftName, sdRight);
//	}
//	JNewExpr jne = extractNew(assignStmt.getRightOp());
//	if(jlLeft != null && jne != null){
//		String leftName = jlLeft.getName();
//		SootClass sc = jne.getBaseType().getSootClass();
//		SDObject newObj = new SDObject(sc, "", true, false);
//		sd.AddObject(newObj);
//		sd.AttachNameToObject(leftName, newObj);
//		
//		//SDMessage creationMessage = new SDMessage(sourceObj, newObj);
//		//sd.AddMessage(creationMessage);
//	}
//}
////If a statement contains an invoke expression
////that expression must be extracted, we must find
////the relevant hyperedge out of the current node
////and traverse it.
//if(aStmt.theStmt.containsInvokeExpr()){
//	SootMethod calledMethod = aStmt.theStmt.getInvokeExpr().getMethod();
//	GroupableHyperEdge<EdgeAnnotation> gEdge = aGNode.GetGroupableEdge(calledMethod);
//	if(gEdge != null){
//		GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> subGNode = 
//				(GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>) hg.GetCompleteNode(gEdge.targetNode);
//		
//		//If an object doesn't have an instance name
//		//(might be static)
//		//this might not work, in which case
//		//the initial assignment to getuniquename
//		//must suffice for the name
//		List<Value> lv = aStmt.theStmt.getInvokeExpr().getUseBoxes();
//		
//		String tarObjName = "";
//		if(lv.size() > 0){
//			Object v = lv.get(0);
//			JimpleLocal jl = extractValue(v);
//			if(jl != null){
//				tarObjName = jl.getName();
//			}
//			else{
//				int hello = 0;
//			}
//		}
//		
//		//grab the three things we need to write a 
//		//message into sd, source, target, message
//		
//		SootMethod sm = subGNode.data.theMethod;
//		SootClass scTarget = sm.getDeclaringClass();
//		SootClass scSource = aGNode.data.theMethod.getDeclaringClass();
//		
//		//SDObjects for source and target are created with
//		//two piece of information, class and instance name
//		//note source's instance name is passed in from above.
//		//target's is locally available to us.
//		//SDObject sdSource = sd.GetObjectFromName(sourceName);
//		boolean isSuper = false;
//		SDObject sdTarget = null;
//		if(tarObjName.equals("this")){
//			sdTarget = sourceObj;
//			if(outerMethodName.equals(sm.getName())){
//				isSuper = true;
//			}
//		}
//		else{
//			sdTarget = sd.GetObjectFromName(tarObjName);
//		}
//		
//		//If there is already a matching SDObject in sd
//		//this will silently fail and we will simply link
//		//to the existing object.  This may need to change
//		//per the assignment statement problem.
//		if(sdTarget == null){
//			if(sm.isStatic()){
//				sdTarget= sd.GetStaticObject(scTarget);
//			}
//			else{
//				if(tarObjName == null || tarObjName.length() == 0){
//					tarObjName = SDObject.GetUniqueName();
//				}
//				sdTarget = new SDObject(scTarget, tarObjName, false, false);
//				sd.AddObject(sdTarget);
//			}
//		}
//		
//		//now that the sd has a source and target, we can
//		//add the message.
//		SDMessage msg = new SDMessage(sourceObj, sdTarget, sm, isSuper);
//		sd.AddMessage(msg);
//		
//		String CallName = 
//				aGNode.data.theMethod.getDeclaringClass().getName() + 
//				"." +
//				aGNode.data.theMethod.getName();
//		
//		if(!listCallStack.contains(CallName)){
//			listCallStack.add(CallName);
//			//now that we are "at" the destination point
//			//of the message, we traverse into
//			//the relevant hypernode for that new method.
//			sd.PushNames();
//			MakeDiagram(
//					aGNode.data.theMethod.getName(), 
//					hg, 
//					subGNode, 
//					sd, 
//					sdTarget,
//					listCallStack,
//					options,
//					optionIndex);
//			sd.PopNames();
//			listCallStack.remove(listCallStack.size() -1);
//		}
//	}
//	else
//	{
//		//need to revisit this once assignment problem is cleared up
//		//it may either be impossible or may represent calls like
//		//int x = (new Random()).nextInt()
//		//where we don't have soot data on calls to "external" libraries
//	}
//}
////after we've handled everything in this statement
////including any calls and subsequent calls generated
////we traverse to the next statement.
//RecFillTraceStmtDiagram(
//		outerMethodName, 
//		hg, 
//		aGNode, 
//		aStmt.theNext, 
//		sd, 
//		sourceObj,
//		listCallStack,
//		options,
//		optionIndex);
//return;
//}
	
	//Simply begins traversing at the first statement
	//control will return here if an invoke statement is discovered.
	//In this way RecFillNodeDiagram and RecFillStmtDiagram 
	//are mutually recursive.
//	private static void RecFillNodeDiagram(
//			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
//			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
//			SequenceDiagram sd,
//			String sourceName) throws Exception{
//		if(aGNode == null){return;}
//		GroupableStmt aStmt = aGNode.data.theStmts;
//		RecFillStmtDiagram(hg, aGNode, aStmt, sd, sourceName);
//	}
	
	//traverses the statements in a single node 
	//in order to fill out the sequence diagram data structure
	//hg is the hypergraph we will traverse
	//aGNode is the current node in the recursive call
	//aStmt is the current statement.
	//sd is the data structure we are filling out as we traverse
	//sourceName is the string name of the instance
	//we are using as source.
	
	//Some object that is an instance in some outside location
	//has had this particular method called upon it, and 
	//we are examining that method.
	//As we are in the middle of examining that method,
	//we don't have local access to that outer instance name:
	//Car myToyota = new Car();
	//myToyota.drive();
	//if we are currently traversing the drive method of class car
	//the instance name "myToyota" is not locally available and must
	//be passed in from outside.
	
//	private static void RecFillStmtDiagram(
//			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
//			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
//			GroupableStmt aStmt,
//			SequenceDiagram sd,
//			String sourceName) throws Exception
//	{
//		if(aStmt == null){return;}
//		if(aStmt.theTrueBranch != null){
//			//There is work to do here actually adding the
//			//opening the if block in the sequence diagram
//			//or potentially loop if we are going to deal with that.
//			RecFillStmtDiagram(hg, aGNode, aStmt.theTrueBranch, sd, sourceName);
//			//and closing it here
//		}
//		if(aStmt.theFalseBranch != null){
//			//This also must be for an "else" block
//			RecFillStmtDiagram(hg, aGNode, aStmt.theFalseBranch, sd, sourceName);
//		}
//		//This is the point where I am currently attempting to handle
//		//assignment statements.
//		//This is not completed.
//		if(aStmt.theStmt instanceof AssignStmt){
//			AssignStmt assignStmt = (AssignStmt)aStmt.theStmt;
//			JimpleLocal jlLeft = extractValue(assignStmt.getLeftOp());
//			JimpleLocal jlRight = extractValue(assignStmt.getRightOp());
//			//variety of possible actions here.
//			//pretty sure something like Chris's dictionary idea
//			//will be necessary.
//		}
//		//If a statement contains an invoke expression
//		//that expression must be extracted, we must find
//		//the relevant hyperedge out of the current node
//		//and traverse it.
//		if(aStmt.theStmt.containsInvokeExpr()){
//			GroupableHyperEdge<EdgeAnnotation> gEdge = aGNode.GetGroupableEdge(aStmt);
//			if(gEdge != null){
//				GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> subGNode = 
//						(GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>) hg.GetCompleteNode(gEdge.targetNode);
//				
//				//If an object doesn't have an instance name
//				//(might be static)
//				//this might not work, in which case
//				//the initial assignment to getuniquename
//				//must suffice for the name
//				List<Value> lv = aStmt.theStmt.getInvokeExpr().getUseBoxes();
//				String tarObjName = SDObject.GetUniqueName();
//				if(lv.size() > 0){
//					JimpleLocal jl = extractValue(lv.get(0));
//					tarObjName = jl.getName();
//					
//				}
//				
//				//grab the three things we need to write a 
//				//message into sd, source, target, message
//				
//				SootMethod sm = subGNode.data.theMethod;
//				SootClass scTarget = sm.getDeclaringClass();
//				SootClass scSource = aGNode.data.theMethod.getDeclaringClass();
//				
//				//SDObjects for source and target are created with
//				//two piece of information, class and instance name
//				//note source's instance name is passed in from above.
//				//target's is locally available to us.
//				SDObject sdSource = new SDObject(scSource, sourceName);
//				SDObject sdTarget = new SDObject(scTarget, tarObjName);
//				
//				//If there is already a matching SDObject in sd
//				//this will silently fail and we will simply link
//				//to the existing object.  This may need to change
//				//per the assignment statement problem.
//				sd.AddObject(sdSource);
//				sd.AddObject(sdTarget);
//				
//				//now that the sd has a source and target, we can
//				//add the message.
//				SDMessage msg = new SDMessage(sdSource, sdTarget, sm);
//				sd.AddMessage(msg);
//				
//				//now that we are "at" the destination point
//				//of the message, we traverse into
//				//the relevant hypernode for that new method.
//				RecFillNodeDiagram(hg, subGNode, sd, tarObjName);
//			}
//			else
//			{
//				//need to revisit this once assignment problem is cleared up
//				//it may either be impossible or may represent calls like
//				//int x = (new Random()).nextInt()
//				//where we don't have soot data on calls to "external" libraries
//			}
//		}
//		//after we've handled everything in this statement
//		//including any calls and subsequent calls generated
//		//we traverse to the next statement.
//		GroupableHyperEdge<EdgeAnnotation> gEdge = null;
//		GroupableStmt aGStmt = aStmt.theNext;
//		RecFillStmtDiagram(hg, aGNode, aGStmt, sd, sourceName);
//	}
	
//	public static void GenerateNaiveSequenceDiagram(
//	Hypergraph<SourceCodeType, EdgeAnnotation> aGraph,
//	String outFile){
//List<HyperEdge<EdgeAnnotation>> lEdges = aGraph.GetAllEdges();
//SequenceDiagram sd = new SequenceDiagram();
//
//
//for(int i = 0; i < lEdges.size(); i++){
//	HyperEdge<EdgeAnnotation> aEdge = lEdges.get(i);
//	ArrayList<SourceCodeType> listSource = new ArrayList<SourceCodeType>();
//	for(int j = 0; j < aEdge.sourceNodes.size(); j++){
//		SourceCodeType sct  = aGraph.GetNode(aEdge.sourceNodes.get(j));
//		listSource.add(sct);
//	}
//	SourceCodeType tar = aGraph.GetNode(aEdge.targetNode);
//	DiagramMessageAggregator dma = new DiagramMessageAggregator(
//			listSource,
//			tar,
//			aEdge.annotation.getMethodName()
//			);
//
//	for(SDObject sdo : dma.GeSDObjects()){
//		sd.AddObject(sdo);
//	}
//	sd.AddMessage(dma.GenerateSDEditMessage());
//}
//sd.CreatePDF(outFile);
///*try {
//	FileWriter fw = new FileWriter(outFile);
//	fw.write(sd.toString());
//	fw.close();
//} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}*/
//
//}
//	public static void GenerateNaiveSequenceDiagram(
//	Hypergraph<SourceCodeType, EdgeAnnotation> aGraph,
//	String outFile){
//List<HyperEdge<EdgeAnnotation>> lEdges = aGraph.GetAllEdges();
//SequenceDiagram sd = new SequenceDiagram();
//
//
//for(int i = 0; i < lEdges.size(); i++){
//	HyperEdge<EdgeAnnotation> aEdge = lEdges.get(i);
//	ArrayList<SourceCodeType> listSource = new ArrayList<SourceCodeType>();
//	for(int j = 0; j < aEdge.sourceNodes.size(); j++){
//		SourceCodeType sct  = aGraph.GetNode(aEdge.sourceNodes.get(j));
//		listSource.add(sct);
//	}
//	SourceCodeType tar = aGraph.GetNode(aEdge.targetNode);
//	DiagramMessageAggregator dma = new DiagramMessageAggregator(
//			listSource,
//			tar,
//			aEdge.annotation.getMethodName()
//			);
//
//	for(SDObject sdo : dma.GeSDObjects()){
//		sd.AddObject(sdo);
//	}
//	sd.AddMessage(dma.GenerateSDEditMessage());
//}
//sd.CreatePDF(outFile);
///*try {
//	FileWriter fw = new FileWriter(outFile);
//	fw.write(sd.toString());
//	fw.close();
//} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}*/
//
//}

//public static void Generate(String ClassName, String ClassDir, String ClassPath, String SaveFile){
//Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(ClassName, ClassDir, ClassPath);
//
////SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
//
//}

//public static void Generate(List<String> listClasses, String ClassPath, String SaveFile){
//Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(listClasses, ClassPath);
//
////SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
//}
//public static void Generate(String ClassName, String ClassDir, String ClassPath, String SaveFile){
//Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(ClassName, ClassDir, ClassPath);
//
////SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
//
//}

//public static void Generate(List<String> listClasses, String ClassPath, String SaveFile){
//Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg = Analyzer.AnalyzeSpecificClasses(listClasses, ClassPath);
//
////SDGenerator.GenerateNaiveSequenceDiagram(hg, SaveFile);
//}
}
