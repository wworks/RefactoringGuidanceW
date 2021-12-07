package RefactoringDangersWQL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.NodeGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;
import com.ensoftcorp.open.commons.utilities.FormattedSourceCorrespondence;

import WQL.W;


public  class QueryLibraryWQL {
	
	public class methodName {
		private String methodName;

		public methodName(String methodName) {
			this.methodName = methodName;
			
		}
		
		public String getMethodName() {
			return this.methodName;
			
		}
	
		
	}
	
	
	public W Query1(W type, String methodName, List<String> parameterTypes) {
		
		return type.selectMethodsWithSignature(methodName, parameterTypes);
			
	}
	
	
	public W Query2(W abstractClass, String methodName, List<String> parameterTypes ) {
		W concreteSubClasses =  abstractClass
								.getSubtypes()
								.selectConcreteClasses();
		
		W classesWithMethod = concreteSubClasses						
								.getMethods()
								.selectMethodsWithSignature(methodName, parameterTypes)
								.parent();
		
		return concreteSubClasses.difference(classesWithMethod);

	
	}
	
	
	
	
	
public W Query3(W concreteClass, String visibility, String methodName, List<String> parameterTypes , W returnType) {
	
	return 
	concreteClass
	.getSuperclassesT()
	.getMethods()
	.selectConcreteMethods()
	.selectOverrideEquivalentMethods(visibility, methodName, parameterTypes, returnType);
	
	
	
}

public W Query4(W abstractOrConcreteClass, String visibility, String methodName, List<String> parameterTypes , W returnType) {
	return 
	abstractOrConcreteClass
	.getSubtypes() //since input is abstract or concrete class, should not contain interfaces(but can)
	.getMethods()
	.selectOverrideEquivalentMethods(visibility, methodName, parameterTypes, returnType);
	
	
}

public W Query5(W methods) {
	return methods.getCalledAt();
	
}


public Q getCallSites(Q methods) {
	//https://ensoftatlas.com/wiki/XCSG:IdentityPassedTo
	
		Q callSites = Query.universe().edges(XCSG.InvokedFunction, XCSG.InvokedSignature ).reverse(methods);
		return callSites;
	//Q callGraph = Query.universe().edges(XCSG.Call );
	//Q callers = callGraph.reverseStep(methods);
	//return callers;
	
}

public W Query6(W method) {
	return	method.getOverridesT().selectConcreteMethods();
		
}

public W Query7(W method) {
	//only consider concrete methods
 
 return method
		//only consider concrete methods
		 .selectConcreteMethods()
		 //get methods argument method is overrideBy
		 .getOverridenBy();
	
}


public W Query8(W method) {
	method = method.selectConcreteMethods();
	
	return method.getOverrides().intersection(
			method
			.parent()
			.getSupertypes()
			.selectAbstractClasses()
			.getMethods()
			.selectAbstractMethods()
			
			);
		
	
	
	
}

public W Query9(W method) {
	method = method.selectAbstractMethods();
			
	return	method
		.parent()
		.getSubtypes()
		.selectConcreteClasses()
		.getMethods()
		.intersection(
				method.getOverridenBy()
				
		);

}

public W Query10(W segment) {
	//only dataflow nodes, not edges
	W dataflowInSegment = segment.induce(W.universe().edges(XCSG.DataFlow_Edge));
	
return	segment
	.dataflow(TraversalDirection.REVERSE)
	.difference(dataflowInSegment);
		
	
	
}


public W Query11(W segment, W method) {
			
	//only dataflow nodes, not edges
	W dataflowInMethod = W.universe().edges(XCSG.DataFlow_Edge).intersection(method.contained());
	
	//only dataflow nodes, not edges. See 11d for edges
	W dataflowInSegment = W.universe().edges(XCSG.DataFlow_Edge).intersection(segment);
	

	
	W affectedDataFlow = 
		segment
		.nodes(XCSG.Assignment)
		.dataflow(TraversalDirection.FORWARD)
		.difference(dataflowInSegment)
		.intersection(dataflowInMethod);
	
	W dangerousUpdates = affectedDataFlow
			.dataflow(TraversalDirection.REVERSE)
			.intersection(
					segment.nodes(XCSG.Assignment)
					);
	
	W subjectVariables =  dangerousUpdates
			.dataflow(TraversalDirection.REVERSE)
			.intersection(dataflowInMethod.nodes(XCSG.Parameter,XCSG.Assignment))
			.roots();
		
	return subjectVariables;
	
	
	}


public W Query11D(W segment, W method) {

	W dataflowInSegment = segment.induce(W.universe().edgesTaggedWithAll(XCSG.DataFlow_Edge, XCSG.LocalDataFlow));

	W dataflowInMethod = method.contained().induce(  W.universe().edgesTaggedWithAll(XCSG.DataFlow_Edge));	

	return
	segment.nodes(XCSG.Assignment)
	.filterNodes(a -> a
		.dataflow(TraversalDirection.FORWARD)
		.difference(dataflowInSegment)
		.intersection(dataflowInMethod)
		.isNotEmpty()
	
	);//.nodeSize() == 1;


	
}


public W Query11AF(W segment, W method) {
			
	//only dataflow nodes, not edges
	W dataflowInMethod = W.universe().edges(XCSG.DataFlow_Edge).intersection(method.contained());
	
	//only dataflow nodes, not edges. See 11d for edges
	W dataflowInSegment = W.universe().edges(XCSG.DataFlow_Edge).intersection(segment);
	

	
	W affectedDataFlow = 
		segment
		.nodes(XCSG.Assignment)
		.dataflow(TraversalDirection.FORWARD)
		.difference(dataflowInSegment)
		.intersection(dataflowInMethod);
	
		
	return affectedDataFlow;
	
	
	}

public W Query12(W segment) {
	return segment.nodes(XCSG.controlFlowExitPoint);
	
}

/***
 * 
 * Selects all Atlas nodes for the code segment in the given method, specified by a file offset and length
 * 
 * @param offsetStart
 * @param length
 * @param method
 * @return
 */
public W getMethodSegmentB(int offsetStart, int length, W method) {
	
	W children = method.contained();
	
	AtlasSet<Node> childrenNodes = children.eval().nodes();
	
	AtlasSet<Node> nodesInSegment = new AtlasHashSet<Node>();
	
	for(Node node : childrenNodes) {
		SourceCorrespondence sc =  (SourceCorrespondence) node.getAttr(XCSG.sourceCorrespondence);
		
		if (sc != null) {
			SourceCorrespondence scMethod = new SourceCorrespondence(sc.sourceFile, offsetStart, length);
			
			//intersection is lenient, allowing partial selection
			if(sc.intersects(scMethod)) {
				nodesInSegment.add(node);
				
			}
		}
	}
	
	return W.toW( Common.toQ(nodesInSegment).eval());
}






}
