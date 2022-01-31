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
import com.ensoftcorp.atlas.core.db.graph.AddressLimitException;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.licensing.AtlasLicenseException;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;
import com.ensoftcorp.open.commons.utilities.FormattedSourceCorrespondence;
import com.ensoftcorp.open.commons.utilities.MappingUtils;


import WQL.W;


public  class QueryLibraryWQL {
	
	/**
	 * Detect if there is a method with a given signature in a class or interface(RD: (i)), return that method
	 * 
	 * @param type
	 * @param methodName
	 * @param parameterTypes
	 * @return
	 */
	public W Query1(W type, String methodName, List<String> parameterTypes) {
		
		return type.getMethods().selectMethodsWithSignature(methodName, parameterTypes);
			
	}
	
	
	/**
	 *  Detect whether there are concrete subclasses of an abstract class which have no implementation for the given abstract method , return all these subclasses that have no implementation .

	 * @param abstractClass
	 * @param visibility
	 * @param methodName
	 * @param parameterTypes
	 * @param returnType
	 * @return
	 */
	public W Query2(W abstractClass, String visibility, String methodName, List<String> parameterTypes, W returnType ) {
		W concreteSubClasses =  abstractClass
								.getFirstConcreteSubclasses();
		
		W classesWithMethod = concreteSubClasses						
								.getMethods()
								.selectOverrideEquivalentMethods(visibility, methodName, parameterTypes, returnType)
								.parent();
		
		return concreteSubClasses.difference(classesWithMethod);

	
	}
	
	
	
	
/**
 * Detect whether there exists an override equivalent concrete method w.r.t. the method to add exists in a(n) (in) direct ( abstract ) superclass of the class to add it to , and return all these methods in superclasses which will be overriden when adding the method to this class.
 * 	
 * @param concreteClass
 * @param visibility
 * @param methodName
 * @param parameterTypes
 * @param returnType
 * @return
 */
public W Query3(W concreteClass, String visibility, String methodName, List<String> parameterTypes , W returnType) {
	
	return 
	concreteClass
	.getSuperclassesT()
	.getMethods()
	.selectConcreteMethods()
	.selectOverrideEquivalentMethods(visibility, methodName, parameterTypes, returnType);
	
	
	
}


/**
 * Detect whether there exist an override equivalent method w.r.t. the method to add in a(n) (in) direct ( abstract ) subclass , and return all these methods which will override the method to add  
 * @param abstractOrConcreteClass
 * @param visibility
 * @param methodName
 * @param parameterTypes
 * @param returnType
 * @return
 */
public W Query4(W abstractOrConcreteClass, String visibility, String methodName, List<String> parameterTypes , W returnType) {
	return 
	abstractOrConcreteClass
	.getSubtypesT() //since input is abstract or concrete class, should not contain interfaces(but can)
	.getMethods()
	.selectOverrideEquivalentMethods(visibility, methodName, parameterTypes, returnType);
	
	
}

/**
 * Detect whether there is a method call to the method to be removed , and return all method calls to the method
 * 
 * @param methods
 * @return
 */
public W Query5(W methods) {
	return methods.getCalledAt();
	
}


/**
 * Detect whether the method to be removed overrides a concrete method in a(n) (in) direct superclass and return all those methods that are overriden
 * 
 * @param method
 * @return
 */
public W Query6(W method) {
	return	method.getOverridesT().selectConcreteMethods();
		
}

/**
 * Detect whether the method is concrete and overridden by another method (in a subclass ), and return all such methods which override it
 * 
 * @param method
 * @return
 */
public W Query7(W method) {
	//only consider concrete methods
 
 return method
		//only consider concrete methods
		 .selectConcreteMethods()
		 //get methods argument method is overrideBy
		 .getOverridenBy();
	
}

/**
 * Detect whether the method to be removed is concrete and implements an abstract method in its direct abstract superclass , and return that method
 * @param method
 * @return
 */
public W Query8(W method) {
	method = method.selectConcreteMethods();
	
	return method.getOverrides().intersection(
			method
			.parent()
			.getSupertypesT()
			.selectAbstractClasses()
			.getMethods()
			.selectAbstractMethods()
			
			);
		
	
	
	
}

/**
 * Detect whether the method to be removed is abstract and is implemented in a concrete direct subclass , and return all these methods which override the ( abstract ) method to be removed in any direct concrete subclass
 * 
 * @param method
 * @return
 */
public W Query9(W method) {
	method = method.selectAbstractMethods();
			
	
	
	return	method
		.parent()
		.getSubtypesT()
		.selectConcreteClasses()
		.getMethods()
		.intersection(
				method.getOverridenBy()
				
		);

	
	
}

/**
 * Detect whether the segment to add refers to declarations (of local variables , parameters , fields ) outside of the segment , and return all these identifiers / variable references which are not bound within the segment / refer to local variables / parameters / fields outside the segment . 
 * 
 * @param segment
 * @return
 */
public W Query10(W segment) {
	//only dataflow nodes, not edges
	W dataflowInSegment = segment.induce(W.universe().edges(XCSG.DataFlow_Edge));
	
return	segment
		.predecessorsOn(W.universe().edges(XCSG.DataFlow_Edge))
	//.dataflow(TraversalDirection.REVERSE)
	.difference(dataflowInSegment);
		
	
	
}

/**
 * Opzet voor analyse of variablen gebonden raken na verplaatsen
 */
public W Query10MS(W segment, W containingMethod, W classDestination) {
	//only dataflow nodes, not edges
	W dataflowInSegment = segment.induce(W.universe().edges(XCSG.DataFlow_Edge));
		
	
	return  segment
	.predecessorsOn(W.universe().edges(XCSG.DataFlow_Edge))
	.difference(dataflowInSegment)
	.nodes(XCSG.Assignment, XCSG.Parameter,XCSG.Field)
	.difference(classDestination.getFields()) //When moving to the same type, references to the same fields are not dangerous
	.filterNodes((W s) -> classDestination.getFields().getNames().contains(s.getAssignmentNames().iterator().next())
	//TODO: parameter names can hide fields
	);
	
}

/**
 * Detect whether the segment updates variables that are later used in the method that the segment is removed from , and return all local variables and parameters in the source method used later in the remaining segment which are modified by the extracted segmented .
 * 
 * @param segment
 * @param method
 * @return
 */
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

/**
 * Detect whether the segment updates variables that are later used in the method that
the segment is removed from , and return all local variables and parameters in the
source method used later in the remaining segment which are modified by the
extracted segmented .
 * 
 * @param segment
 * @param method
 * @return
 */
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


private W Query11E(W segment, W method) {
	return
	segment.nodes(XCSG.Assignment)
	.filterNodes(a -> a
		.dataflow(TraversalDirection.FORWARD)
		.difference(segment)
		.intersection(method.contained())
		.isNotEmpty()
	
	);//.nodeSize() == 1;


	
}

private W Query11AF(W segment, W method) {
			
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

/**
 * Detect whether there is a return in the segment to be removed , return all these returns
 * @param segment
 * @return
 */
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
