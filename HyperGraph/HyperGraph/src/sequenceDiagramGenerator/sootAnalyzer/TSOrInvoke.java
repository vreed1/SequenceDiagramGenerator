package sequenceDiagramGenerator.sootAnalyzer;


import java.util.ArrayList;
import java.util.List;

import sequenceDiagramGenerator.MethodNodeAnnot;
import sequenceDiagramGenerator.Query;
import sequenceDiagramGenerator.QueryDataContainer;
import sequenceDiagramGenerator.TraceStatement;
import sequenceDiagramGenerator.Query.QueryResponse;
import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.GroupableHyperEdge;
import sequenceDiagramGenerator.hypergraph.GroupableHyperNode;
import sequenceDiagramGenerator.hypergraph.GroupableHypergraph;
import sequenceDiagramGenerator.hypergraph.HyperNode;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import sequenceDiagramGenerator.sdedit.SDListAndReturns;
import sequenceDiagramGenerator.sdedit.SDMessage;
import sequenceDiagramGenerator.sdedit.SDObject;
import sequenceDiagramGenerator.sdedit.SDObject.TaintState;
import sequenceDiagramGenerator.sdedit.SequenceDiagram;
import soot.AbstractValueBox;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.ThisRef;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.AbstractStmt;
import soot.jimple.internal.IdentityRefBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.shimple.internal.SPhiExpr;
import soot.shimple.internal.SValueUnitPair;
import utilities.ByRefInt;
import utilities.Utilities;

public class TSOrInvoke{
	
	private TaintAnalyzer tTest;
	
	public TSOrInvoke(TaintAnalyzer inTest){
		
		tTest = inTest;
	}
	
	public List<SequenceDiagram> GenerateTaintDiagrams(
			GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			Query q){
		List<HyperNode<MethodNodeAnnot,EdgeAnnotation>> lnodes = hg.GetNodes();
		List<SequenceDiagram> lDias = new ArrayList<SequenceDiagram>();
		for(int i = 0; i < lnodes.size(); i++){
			try {
				//BRIAN - DO NOT CHECK THIS IN IT IS TEMPORARY
				//String mName = lnodes.get(i).data.GetMethod().getName();
				//if(!mName.equals("run")){continue;}
				//THIS IS JUST TO RESTRICT FOR SOME SPECIFIC TESTING STOP HERE.
				
				List<SequenceDiagram> listSDs =GenerateAllDiagrams(hg,
						(GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>)lnodes.get(i),
						q);

				String MethodTrunc = Utilities.Truncate(lnodes.get(i).data.GetMethod().getName());
				for(int j = 0; j < listSDs.size(); j++){
					listSDs.get(j).SetName(MethodTrunc + String.valueOf(j) + ".pdf");
				}
				lDias.addAll(listSDs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return lDias;
	}

	private List<SequenceDiagram> GenerateAllDiagrams(
			GroupableHypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aNode,
			Query q) throws Exception {
		
		List<SequenceDiagram> listToReturn;
		
		SDObject.finalUnnamed = 0;
		SequenceDiagram sd = new SequenceDiagram();
		SDObject outerObject = null;
		if(aNode.data.GetMethod().isStatic()){
			outerObject = (SDObject)sd.GetStaticObject(aNode.data.GetMethod().getDeclaringClass());
		}
		else{
			outerObject = new SDObject(aNode.data.GetMethod().getDeclaringClass(), SDObject.GetUniqueName(), false, false, TaintState.Safe);
			sd.AddObject(outerObject);
			sd.AttachNameToObject("this", outerObject);
		}
		TSDListAndReturns allSDs = new TSDListAndReturns();
		allSDs.listDiagrams.add(sd);
		listToReturn = MakeAllTaintTraces(
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

	
	public TSDListAndReturns MakeAllTaintTraces(
			String outerMethodName,
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			TSDListAndReturns allSDs,
			int sdIndex,
			SDObject outerObject,
			List<String> listCallStack,
			Query q,
			int lvl) throws Exception{
	
		TSDListAndReturns toReturn = allSDs.Copy(sdIndex);
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

		TSDListAndReturns cloneSource = toReturn.clone();
		toReturn.clear();
		
		for(int i = 0; i < tstmtssize; i++){

			TraceStatement tc = aGNode.data.getTraces().get(i);
			QueryDataContainer qd = new QueryDataContainer(tc);
			result = q.RunOnData(new QueryDataContainer(tc));
			if(result == QueryResponse.False){
				continue;
			}
			TSDListAndReturns toSendDown = cloneSource.clone();
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
	
	private TSDListAndReturns MakeTaintTrace(
			String outerMethodName,
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			TSDListAndReturns allSDs,
			int sdIndex,
			SDObject outerObject,
			List<String> listCallStack,
			List<Integer> options,
			ByRefInt optionIndex,
			Query q,
			int lvl) throws Exception{
	
		TSDListAndReturns toReturn = allSDs.Copy(sdIndex);
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
	
	private TSDListAndReturns RecFillTraceAllStmtDiagram(
			String outerMethodName,
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			TraceStatement aStmt,
			TSDListAndReturns allSDs,
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
		TSDListAndReturns toReturn = allSDs.Copy(sdIndex);
		SequenceDiagram sd = allSDs.listDiagrams.get(sdIndex);
		
		//if(FindAll){
		//	toReturn.listDiagrams.add(sd);
		//}
		
		if(aStmt == null){return toReturn;}
		
		SDObject sourceObj = (SDObject)sd.GetObjectFromID(sourceObjID);
		
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

				SDObject sdRight = (SDObject)sd.GetObjectFromName(rightName);
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
				SDObject sdRight = (SDObject)sd.GetObjectFromName("this");
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
		//if(aStmt.theStmt instanceof JGotoStmt){
		//	throw new Exception("Should never happen, ruled out by tracestatement constructor");
		//			}
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
			TSDListAndReturns toReturnNew = new TSDListAndReturns();
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
	
	private TSDListAndReturns HandleInvoke(InvokeExpr ie,
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			TSDListAndReturns allSDs,
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
		TSDListAndReturns toReturn = allSDs.Copy(sdIndex);
		//toReturn.listDiagrams.add(sd);
		
		SootMethod calledMethod = ie.getMethod();

		boolean taintIntro = this.tTest.IsMethodTainted(calledMethod);
		
		//moved this here to help taint analysis.  
		//i don't think this will have any impact.
		
		if(sourceObj.IsTainted()){
			taintIntro = true;
		}
		
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
					SDObject anObj = (SDObject)sd.GetObjectFromName(jlparam.getName());
					params.add(anObj);
					if(anObj != null){
						if(taintIntro){
							anObj.SetTaintState(sd, TaintState.Tainted);
					
						}
					}
					else{
						String dbgsit = "hello";
						System.out.println(dbgsit);
					}
				}
				else{
					params.add(null);
				}
			}
		}
		
		
		if(taintIntro){
			toReturn.tState = TaintState.Tainted;
		}
		
		String methodStringName = Utilities.getMethodString(calledMethod);
		QueryResponse qr = q.RunOnMethodName(methodStringName);
		if(qr == QueryResponse.False || qr == QueryResponse.Filter){
			return toReturn;
		}
		GroupableHyperEdge<EdgeAnnotation> gEdge = aGNode.GetGroupableEdge(calledMethod);
		if(gEdge != null){
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> subGNode = 
					(GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation>) hg.GetCompleteNode(gEdge.targetNode);
						
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
				sdTarget = (SDObject)sd.GetObjectFromName(tarObjName);
			}
			
			//If there is already a matching SDObject in sd
			//this will silently fail and we will simply link
			//to the existing object.  This may need to change
			//per the assignment statement problem.
			if(sdTarget == null){
				if(sm.isStatic()){
					sdTarget= (SDObject)sd.GetStaticObject(scTarget);
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
			msg.SetTaintState(sd, toReturn.tState);
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
					toReturn = MakeAllTaintTraces(
							Utilities.getMethodString(aGNode.data.GetMethod()), 
							hg, 
							subGNode, 
							allSDs,
							sdIndex,
							sdTarget,
							listCallStack,
							q,
							lvl +1);
					//BLP - forcing taint regardless of substitution 
					//if it was introduced
					if(taintIntro){
						toReturn.tState = TaintState.Tainted;
					}
					for(int i = 0; i < toReturn.listDiagrams.size(); i++){
						toReturn.listDiagrams.get(i).PopNames();
					}
				}
				else{
					toReturn = MakeTaintTrace(
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
					//BLP - forcing taint regardless of substitution 
					//if it was introduced
					if(taintIntro){
						toReturn.tState = TaintState.Tainted;
					}
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

	
	private TSDListAndReturns HandleAssignment(
			Hypergraph<MethodNodeAnnot, EdgeAnnotation> hg,
			GroupableHyperNode<MethodNodeAnnot, EdgeAnnotation> aGNode,
			TSDListAndReturns allSDs,
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
		TSDListAndReturns toReturn = allSDs.Copy(sdIndex);
		//toReturn.listDiagrams.add(sd);
		
		toReturn.tState = TaintState.Safe;
		
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
			TSDListAndReturns allResults = HandleInvoke(
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
			
			if(allResults.tState == TaintState.Tainted){

				toReturn.tState = TaintState.Tainted;
			}
			
			InvokeExpr ie = assignStmt.getInvokeExpr();
			soot.Type rType = ie.getMethod().getReturnType();
			for(int i = 0; i < allResults.size(); i++){
				SDObject retObj = null;
				if(allResults.listReturns.size() <= i || allResults.listReturns.get(i) == null){
					retObj = new SDObject(rType, "", false, false,TaintState.Safe);
				}
				else{
					retObj = allResults.listReturns.get(i);
				}
				allResults.listDiagrams.get(i).AttachNameToObject(leftName, retObj);
				retObj.SetTaintState(allResults.listDiagrams.get(i), allResults.tState);
			}
			return allResults;
		}
		
		SDObject rightObj = extractObject(assignStmt.getRightOp(), sd);
		
		if(rightObj != null){
			sd.AttachNameToObject(leftName, rightObj);
			
			rightObj.SetTaintState(sd, toReturn.tState);
		}
		else{
			rightObj = new SDObject("UnknownType", leftName, false, false,TaintState.Safe);
			sd.AddObject(rightObj);
			sd.AttachNameToObject(leftName, rightObj);
			rightObj.SetTaintState(sd,toReturn.tState);
		}
		return toReturn;
	}
	
	private SDObject extractObject(Object v, SequenceDiagram sd){
		return (SDObject)extract(v, sd, 0);
	}
	
	private String extractName(Object v, SequenceDiagram sd){
		return (String)extract(v, sd, 1);
	}
	
	private Object extract(Object v, SequenceDiagram sd, int mode){
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
			SDObject sdo = (SDObject)sd.GetStaticObject(sc);
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
		if(obj instanceof SPhiExpr){
			SPhiExpr sp = (SPhiExpr)obj;
			for(int i = 0; i < sp.getArgs().size(); i++){
				SValueUnitPair su = (SValueUnitPair)sp.getArgs().get(i);
				Value vb = su.getValue();
				SDObject aObj = extractObject(vb, sd);
				if(aObj != null){
					return aObj;
				}
			}
			return null;
		}
		if(obj instanceof JCastExpr){
			JCastExpr jce = (JCastExpr)obj;
			Value vb = jce.getOp();
			SDObject aObj = extractObject(vb, sd);
			if(aObj != null){
				return aObj;
			}
		}
		//Brian added this, used to return null until recently.
		if(mode == 0){
			return new SDObject("TotalUnk", SDObject.GetUniqueName(),false, false, TaintState.Safe );
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
	private JimpleLocal extractValue(Object v){
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
	
	private ParameterRef extractParam(Object v){
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
	
	private JNewExpr extractNew(Object v){
		if(v instanceof JNewExpr){
			JNewExpr ret = (JNewExpr)v;
			return ret;
		}
		return null;
	}
	
	private JVirtualInvokeExpr extractInvoke(Object v){
		if(v instanceof JVirtualInvokeExpr){
			return (JVirtualInvokeExpr)v;
		}
		return null;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
}