package RefactoringDangersQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;
import com.ensoftcorp.open.commons.utilities.FormattedSourceCorrespondence;


/**
 * Contains queries implemented using stock Atlas.
 * 
 * 
 */
public  class QueryLibrary {
	
	/**
	 * 
	 * Input class example.
	 * 
	 *
	 */
	public class methodName {
		private String methodName;

		public methodName(String methodName) {
			this.methodName = methodName;
			
		}
		
		public String getMethodName() {
			return this.methodName;
			
		}
	
		
	}
	
	
	/**
	 * Query 1. Detect if there is a method with a given signature in a class or interface, return that method
	 * 
	 * @param type 
	 * @param methodName
	 * @param parameterTypes
	 * @return 
	 */
	public Q methodWithSignatureInType2(Q type, String methodName, List<String> parameterTypes) {
		//get the methods with the supplied name in a type with the supplied name.
		Q methodsWithNameInType = type.contained().methods(methodName);
				
		//Compare the number of input parameters and the number of parameters of found method.
		//Only methods with the input name in the input type are considered, due to the argument methodsWithNameInType
		Q methodsWithNameAndParametersInType = methodsWithParameters(methodsWithNameInType, parameterTypes);
		
		return methodsWithNameAndParametersInType;
			
	}
	

/**
 * Detect whether there are concrete subclasses of an abstract class which have no implementation for the given abstract method, return all these subclasses that have no implementation.
 * 
 * @param abstractClass
 * @param visibility
 * @param methodName
 * @param parameterTypes
 * @param returnType
 * @return
 */
public Q query2(Q abstractClass, String visibility, String methodName, List<String> parameterTypes, Q returnType ) {
	
	//typeHierarchy(Q origin, CommonQueries.TraversalDirection direction)
	//	Produces a type hierarchy.
	//reverse means 'down'
Q typeh = CommonQueries.typeHierarchy(abstractClass, CommonQueries.TraversalDirection.REVERSE);

//exclude abstractclasses from type hierarchy
	//difference(Q... expr)
	//	Select the current graph, excluding the given graphs.
Q csubc = typeh.difference(typeh.nodes( XCSG.Java.AbstractClass));

//all methods of all concrete sub classes of abstractClass
Q methods = CommonQueries.methodsOf(csubc);
							

//collect all methods with same signature as input, from all methods of concrete sub classes.
Q matchingMethods = overrideEquivalentMethods(visibility, methodName, parameterTypes, returnType, methods );

//subtract from all concrete subclasses those classes which have a method with the signature already.
	//parent() gives the parent of a node, along the contains edge. A method is contained by a class.
Q classesWithoutMethod = csubc.difference(matchingMethods.parent());

return classesWithoutMethod; 

}
	
/**
 * 
 * Detect whether there exists an override equivalent concrete method w.r.t. the method to add in a(n) (in)direct (abstract) superclass of the class to add it to, and return all these methods in superclasses which will be overriden when adding the method to this class .
 * 
 * @param concreteClass
 * @param visibility
 * @param methodName
 * @param parameterTypes
 * @param returnType
 * @return
 */
public Q query3(Q concreteClass, String visibility, String methodName, List<String> parameterTypes , Q returnType) {
	
	//get superclasses of input class
	Q typeh = CommonQueries.typeHierarchy(concreteClass, TraversalDirection.FORWARD);
		//exclude interfaces
	Q superclasses = typeh.difference(Query.universe().nodes(XCSG.Java.Interface),concreteClass);
	//TODO remove given nodes
	
	//methods of superclasses
	Q methods = Common.methodsOf(superclasses);
	
	
	//exclude abstract methods
	Q concreteMethods = methods.difference(methods.nodes(XCSG.abstractMethod));


	//select those methods that are override equivalent w.r.t to the input method
	Q overrideEquivalentMethods = overrideEquivalentMethods(visibility, methodName, parameterTypes,returnType , concreteMethods);
			

	return overrideEquivalentMethods;
}

/**
 * Detect whether there exist an override equivalent method w.r.t. the method to add in a(n) (in)direct (abstract) subclass, and return all these methods which will override the method to add (RD: (iv)).
 * 
 * @param abstractOrConcreteClass
 * @param visibility
 * @param methodName
 * @param parameterTypes
 * @param returnType
 * @return
 */
public Q query4(Q abstractOrConcreteClass, String visibility, String methodName, List<String> parameterTypes , Q returnType) {
		//get subclasses of input class
		Q typeh = CommonQueries.typeHierarchy(abstractOrConcreteClass, TraversalDirection.REVERSE).difference(abstractOrConcreteClass);
		
		//get methods of all subclasses
		Q methods = CommonQueries.methodsOf(typeh);
						
		//select those methods that are override equivalent w.r.t to the input method
		Q overrideEquivalentMethods = overrideEquivalentMethods(visibility, methodName, parameterTypes,returnType , methods);	

		return overrideEquivalentMethods;
}

/**
 * Detect whether there is amethod call to the method to be removed, and return all potential method calls to the method (RD: (v)).
 * 
 * @param methods
 * @return
 */
public Q query5(Q methods) {
	
	Q dynamicCallSites = methods
			 .contained ()
			 .nodes ( XCSG . Identity ) // implicit ’this ’ parameter
			 .predecessorsOn ( Query . universe (). edges ( XCSG . DataFlow_Edge )) // all possible inputs for ’this ’ parameter
			 .nodes (XCSG.IdentityPass)
			 .successorsOn( Query . universe (). edges ( XCSG . IdentityPassedTo )); // from theidentity input to the callsite .
			
			Q staticCallSites = methods.predecessorsOn ( Query . universe (). edges ( XCSG . InvokedFunction ));
			
			Q callSites = dynamicCallSites . union ( staticCallSites );
			
			return callSites ;
		
}


/**
 * Detection: detect whether the method to be removed overrides a concrete method in a(n) (in)direct superclass and return all those methods that are overriden.
 * 
 * @param method
 * @return
 */
public Q query6(Q method) {
// possible input checking:	
//	Q filteredInput =  method.nodesTaggedWithAll(XCSG.Method,XCSG.abstractMethod);
//	if(CommonQueries.nodeSize(filteredInput) != 1) {
//		throw new IllegalArgumentException("query expects exactly one abstract method");
//		
//	}
//	
//	if(!CommonQueries.isEmpty(method.difference(filteredInput))) {
//		throw new IllegalArgumentException("argument method must contain only abstract methods");
//		
//		
//	}
			
	
	//get all methods the input method overrides. (FORWARD = up)
	Q overrides = CommonQueries.overrides(method, TraversalDirection.FORWARD);
	
	//only instance methods(excluding static methods and constructors) and concrete methods.
	Q result = overrides.nodes(XCSG.InstanceMethod).difference(overrides.nodes(XCSG.abstractMethod));
	
	return result.difference(method); //.difference(result.leaves());
	
	
}
	
public Q query6B(Q methods) {
	
			Q concreteMethods = Query.universe().nodes(XCSG.InstanceMethod).difference(Query.universe().nodes(XCSG.abstractMethod));

			//add override relations to graph
			Q overrideG = concreteMethods.induce(Query.universe().edges(XCSG.Overrides));

			//select all methods overridden by methods(methods itself is also in the result)
			Q overriden = overrideG.forward(methods);
				
			//select connected components only(exclude methods which do not have any override relation)
			Q connectOnly = overriden.retainEdges();
			
			//exclude roots(the lowest methods in overridechains). 
			//retainNodes removes overrides edges(as to only return methods)
			return	connectOnly.difference(connectOnly.roots()).retainNodes();
}
	
/**
 * Detect whether the method is concrete and overridden by another method (in a subclass), and return all such methods which override it (RD: (vii)).
 * 
 * @param method
 * @return

 */
public Q query7(Q method) {
	//only consider concrete methods
 method = method.difference(Query.universe().nodes(XCSG.abstractMethod));
 
 Q overriddenBy = CommonQueries.overrides(method, TraversalDirection.REVERSE);

 //exclude leaves()
 return overriddenBy.difference(overriddenBy.leaves());
		 
		 	
}

/**
 * Shorter but harder to understand  version of query 8. Detect whether the method to be removed implements an abstract method in its direct abstract superclass, and return that method.
 * 
 * @param method
 * @return
 */
public Q query8(Q method) {
	//exclude abstract methods
	 method = method.difference(Query.universe().nodes(XCSG.abstractMethod));
	 
	//navigate to parent class, navigate to supertype, select only abstract classes.  (possibly use typeHierarchyStep)
	 Q directAbstractSuperClass = method.parent().forwardStepOn(Query.universe().edges(XCSG.Supertype)).nodes(XCSG.Java.AbstractClass);
	 
	 
	 //make an override graph from the superclass methods 'down' (methods that (in) directly override a method in superclass)
	 Q superclassMethodsOverridingGraph = CommonQueries.overrides(directAbstractSuperClass.contained().nodes(XCSG.Method),TraversalDirection.REVERSE);
	 
	 //use overrides(context, origin, direction), only overrides in context are considered
	 //in this case, only the overridings of superclass methods.
	 //within this context make the override graph from the input method upwards(all the method that input method overrides).
	 //so the resulting overrides are from the input method to a method of the direct abstract superclass, ie the first implementation
	 Q directImplementation = CommonQueries.overrides(superclassMethodsOverridingGraph, method, TraversalDirection.FORWARD);
	 	  
	 return directImplementation;
	
}

/**
 * Detect whether the method to be removed implements an abstract method in its direct abstract superclass, and return that method.
 * 
 * @param method
 * @return
 */
public Q query8B(Q method) {
	//exclude abstract methods
	 method = method.difference(Query.universe().nodes(XCSG.abstractMethod));
	 
	//navigate to parent class, navigate to supertype, select only abstract classes.  (possibly use typeHierarchyStep)
	 Q directAbstractSuperClass = method.parent().forwardStepOn(Query.universe().edges(XCSG.Supertype)).nodes(XCSG.Java.AbstractClass);
	 
	 //methods of the superclass
	 Q superclassmethods = directAbstractSuperClass.contained().nodes(XCSG.Method);
	 
	 //overridegraph of input method
	 Q methodOverrides = CommonQueries.overrides( method, TraversalDirection.FORWARD);
	 
	 //select methods that are directly overridden by input method
	 //then do an intersection with the methods of the superclass, so the resulting methods are:
	 //methods of direct abstract superclass AND overridden by input method
	 Q overridden = methodOverrides.forwardStep(method).intersection(superclassmethods);
	 
	 return overridden;
	
}

/**
 * Detect whether the method to be removed is abstract and is implemented in a concrete direct subclass, and return all these methods which override the (abstract) method to be removed in any direct concrete subclass (RD: (ix))).
 * 
 * @param method
 * @return
 */
public Q query9(Q method) {
	//exclude abstract methods
	 method = method.difference(Query.universe().nodes(XCSG.abstractMethod));
	
	 //navigate to parent class, get its direct(reverseStep instead of reverse) concrete subclasses
	 Q parent = method.parent();
	 //supertypes vs extends
	 //TODO predessecceros
	 Q subclasses = parent.reverseStepOn(Query.universe().edges(XCSG.Supertype));
	 Q concreteSubclasses =  subclasses.difference(Query.universe().nodes(XCSG.Java.AbstractClass));
	 
	 //get all methods of direct concrete subclasses
	 Q subclassMethods = concreteSubclasses.contained().nodes(XCSG.Method);
	 
	//reverse overridegraph of input method
	 Q methodOverrides = CommonQueries.overrides( method, TraversalDirection.REVERSE);
	 
	 //intersection of methods overriding given abstract method with the methods of the subclasses, so the resulting methods are:
	 //methods of direct concrete subclasses AND overriding input method
	 Q implementations = methodOverrides.intersection(subclassMethods);
		 
	 return implementations;
	
}

/**
 * Query 8 using directImplementationGraph, see thesis.
 * 
 * @param methods
 * @return
 */
public Q query8C(Q methods) {
	//exclude abstract methods
	 methods = methods.difference(Query.universe().nodes(XCSG.abstractMethod));
	
	
	Q dig = directImplementationsGraph();
	
	//select all nodes  reachable from the abstract method in the direct implementation graph,
	//remove starting point. Select only reachable methods
	return dig.forward(methods).difference(methods).nodes(XCSG.Method);

}

/**
 * Query 9 using directImplementationGraph, see thesis.
 * 
 * @param methods
 * @return
 */
public Q query9B(Q methods) {
	methods = methods.nodes(XCSG.abstractMethod);
	
	
	Q dig = directImplementationsGraph();
	
	//select all nodes reverse reachable from the method in the direct implementation graph,
	//remove starting point. Select only reachable methods
	return dig.reverse(methods).difference(methods).nodes(XCSG.Method);
	
	
}


/**
 * Parametric version of query 9, see thesis.
 * 
 * @param methods
 * @return
 */
public Q query9C(Q methods) {
	methods = methods.nodes(XCSG.abstractMethod);
	

	//select all nodes reverse reachable from the method in the direct implementation graph,
	//remove starting point. Select only reachable methods
	return parametricImplementations(methods, TraversalDirection.REVERSE);
	
	
}

/**
 * Allows for to query the direct implementation graph where the direction can be given as a parameter
 * 
 * @param methods
 * @param traversalDirection
 * @return
 */
public Q parametricImplementations(Q methods, TraversalDirection traversalDirection) {
	Q dig = directImplementationsGraph();
	
	Q res = CommonQueries.traverse(dig, methods, traversalDirection);
	
	return res.difference(methods).nodes(XCSG.Method);
}

/**
 * Returns a graph containing all the direct implementations, the graph contains: abstract class, concrete class, extends, abstract method en concerete method, overrides
 * 
 * @return
 */
public Q directImplementationsGraph() {
	//only abstract methods
	 Q abstractMethods = Query.universe().nodes(XCSG.abstractMethod);
	
	 
	 //navigate to parent class, get its direct(reverseStep instead of reverse) concrete subclasses
	 Q parent = abstractMethods.parent();
	 Q subclasses = parent.reverseStepOn(Query.universe().edges(XCSG.Java.Extends));
	 Q concreteSubclasses =  subclasses.difference(Query.universe().nodes(XCSG.Java.AbstractClass));
	 
	 //get all methods of direct concrete subclasses
	 Q subclassMethods = concreteSubclasses.contained().nodes(XCSG.Method); //therefore methods are also concrete
	 
	//reverse overridegraph of input method
	 Q methodOverrides = CommonQueries.overrides( abstractMethods, TraversalDirection.REVERSE);
	 
	 //intersection of methods overriding given abstract method with the methods of the subclasses, so the resulting methods are:
	 //methods of direct concrete subclasses AND overriding input method
	 Q implementations = methodOverrides.intersection(subclassMethods);
	 
	 
	 //make a graph with abstract methods, override edges, and direct implementations
	 Q graph = abstractMethods.union( methodOverrides, implementations).induce(Query.universe().edges(XCSG.Overrides)); 
			
	//graph with edges from direct implementations to the abstract methods they implement
	 graph = graph.betweenStep(implementations,abstractMethods);
	 //add parent class and extend information.
	graph =graph.union(graph.nodes(XCSG.Method).parent()).induce(Query.universe().edges(XCSG.Contains, XCSG.Java.Extends, XCSG.Supertype)); 
	 	 
	 return graph;
	
	
	//result graph with abstract class, concrete class, extends, abstract method en concerete method, overrides
	
	
	
}


/**
 * Detect whether the segment to move refers to declarations(of local variables, parameters, fields) outside of the segment, and return all these identifiers/variable references which are not bound within the segment/ refer to local variables/parameters/fields outside the segment. Possibly additional information can be needed for the advice, e.g. the type of these variable references(they determine the type of the parameters which can be added to fix it) (RD: (x)) 
 *  
 * @param segment
 * @return
 */
public Q query10(Q segment) {
 	Q reverseDataflow = CommonQueries.data(segment, CommonQueries.TraversalDirection.REVERSE);
 	
 	Q segmentDataflow =  CommonQueries.data(segment.induce(Query.universe().edges(XCSG.Edge)).retainEdges(),segment, CommonQueries.TraversalDirection.REVERSE);
 	
 	return reverseDataflow.difference(segmentDataflow).nodes(XCSG.Parameter,XCSG.Field, XCSG.Initialization);//.forwardStepOn(Query.universe().edges(XCSG.TypeOf));
 	
}




/**
 * Part of analysis 11. Returns the  dataflow affected by the assignements in the segment to move that will be lost
 * 
 * @param segment
 * @param method
 * @return
 */
public Q query11(Q segment, Q method) {
	//only dataflow inside the method is considered, so we set up a context/graph with elements inside the method, with all edges.
	Q methodContext = method.contained().induce(Query.universe().edges(XCSG.Edge));
	
	//calculate in and outgoing dataflow
	Q dataflowToSegment = CommonQueries.data(methodContext,segment.nodes(XCSG.Assignment), CommonQueries.TraversalDirection.REVERSE);
	
	//dataflow in the method affected by assignments in the segment.
	Q dataflowFromSegment = CommonQueries.data(methodContext,segment.nodes(XCSG.Assignment), CommonQueries.TraversalDirection.FORWARD);
	
	
	Q dataflowToAndFromSegent = dataflowToSegment.union(dataflowFromSegment);
	
	Q dataflowInSegment =  CommonQueries.data(segment.induce(Query.universe().edges(XCSG.Edge)).retainEdges(),segment, CommonQueries.TraversalDirection.REVERSE);
	
	
	Q affectedDataflowRemainingSegment = dataflowFromSegment.difference(dataflowInSegment);
	
	
	return affectedDataflowRemainingSegment;
	
}

/**
 * Part of analysis 11. Returns the updates that cause trouble if removed
 *  
 * @param segment
 * @param method
 * @return
 */
public Q query11B(Q segment, Q method) {
	//only dataflow inside the method is considered, so we set up a context/graph with elements inside the method, with all edges.
	Q methodContext = method.contained().induce(Query.universe().edges(XCSG.Edge));
	
	//calculate in and outgoing dataflow
	Q dataflowToSegment = CommonQueries.data(methodContext,segment.nodes(XCSG.Assignment), CommonQueries.TraversalDirection.REVERSE);
	
	//dataflow in the method affected by assignments in the segment.
	Q dataflowFromSegment = CommonQueries.data(methodContext,segment.nodes(XCSG.Assignment), CommonQueries.TraversalDirection.FORWARD);
	
	
	Q dataflowToAndFromSegent = dataflowToSegment.union(dataflowFromSegment);
	
	Q dataflowInSegment =  CommonQueries.data(segment.induce(Query.universe().edges(XCSG.Edge)).retainEdges(),segment, CommonQueries.TraversalDirection.REVERSE);
	
	
	Q affectedDataflowRemainingSegment = dataflowFromSegment.difference(dataflowInSegment);
	

	
	return dataflowFromSegment.between(segment.nodes(XCSG.Assignment), affectedDataflowRemainingSegment).difference(affectedDataflowRemainingSegment);//.roots();//.forwardStepOn(Query.universe().edges(XCSG.TypeOf));
	
}

/**
 * Detect whether there is a return in the segment to be removed, return all these returns.
 * 
 * @param selection
 * @return
 */
public Q query12(Q selection) {

	return selection.nodes(XCSG.controlFlowExitPoint);
	
	
}

/**
 * Gets the nodes in the method within the given file offset and length. Allows partial selection.
 * 
 * @param offsetStart
 * @param length
 * @param method
 * @return
 */
public Q getMethodSegmentB(int offsetStart, int length, Q method) {
	
	Q children = method.contained();
	
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
	
	return Common.toQ(nodesInSegment);
}
	


/**
 * Returns from the given methods, methods with the given list of parameter types, by looping through the given methods one by one.
 * 
 * @param methodsToMatchFrom
 * @param parameterTypesSource
 * @return
 */
	public Q methodsWithParameters( Q methodsToMatchFrom, List<String> parameterTypesSource ) {
		//convert Q to Nodes
		AtlasSet<Node> methodNodes = methodsToMatchFrom.eval().nodes();
	
		
		//prepare result set of nodes
		AtlasSet<Node> result = new AtlasHashSet<Node>();		
	
		for(Node method : methodNodes) {
			
			
			//	traverse to parameters of methods(go to HasParameter edges, go to the nodes they point to)
			AtlasSet<Node> parameters = new AtlasHashSet<Node>();
			method.out(XCSG.HasParameter).forEach( (Edge edge) -> parameters.add(edge.to()) );
						
			boolean sameNumberOfParameters = parameters.size() == parameterTypesSource.size();
			
			if(!sameNumberOfParameters) { continue;}
			
			//check if parameter types are the same, in the same order
			boolean sameTypesOfParameters = true;
			
				//parameters is a set(unordered), not a list(ordered), so to be safe we use the parameterIndex attribute of the parameter.
			for (Node parameter : parameters) { 
				int parameterIndex = (int) parameter.getAttr(XCSG.parameterIndex);
				
				//get source type
				String pTypeSource = parameterTypesSource.get(parameterIndex);
				
				//traverse from parameter to its type
				Node type = parameter.out(XCSG.TypeOf).getFirst().to() ;
				
				//the method can have less or zero parameters, could also break out of the loop if numberofparameters is not equal
				if (type != null) {
					//get type name (possibly use fully qualified name instead, see CommonQueries)
					String pType = (String) type.getAttr(XCSG.name);
					//check if types are equal
					if (!pType.equals(pTypeSource)) {
						sameTypesOfParameters = false;
					}
					
				}
			}
			
			
			if (sameNumberOfParameters && sameTypesOfParameters) {
				result.add(method);
			}
		}
		
		//convert Nodes back to Q object.
		//Graph g = new NodeGraph(result);
		//return Query.toQ(g);
		return Common.toQ(result);
		
	}
	
	
	/**
	 * Returns from the given methods, methods with the given list of parameter types, using mostly querying techniques instead of looping through methods. This is similar to the Atlas tutorial https://ensoftatlas.com/wiki/Discovering_Valid_Java_Main_Methods
	 * 
	 * @param methodsToMatchFrom
	 * @param parameterTypesSource
	 * @return
	 */
	public Q methodsWithParametersC(Q methodsToMatchFrom, List<String> parameterTypesSource ) {
		int numOfParameters = parameterTypesSource.size();
		
		//methods with edges to their parameters
		Q paramGraph = methodsToMatchFrom.forwardOn(Query.universe().edges(XCSG.HasParameter));
		
		//a method with no paramaters has no parameter edges, so a special check is needed.
		if(numOfParameters  == 0) {
			Q methodsWithOneOrMoreParams =paramGraph.predecessors(  paramGraph.selectNode(XCSG.parameterIndex, 0));
			return methodsToMatchFrom.difference(methodsWithOneOrMoreParams);
			
		}
		
		//select methods with the right amount of paramaters, by looking if they have a nth parameter, but not a nth+1 parameter
		Q methodsWithRightNumberOfParams = paramGraph.predecessors( paramGraph.selectNode(XCSG.parameterIndex, numOfParameters -1));
		methodsWithRightNumberOfParams = methodsWithRightNumberOfParams.difference(paramGraph.predecessors( paramGraph.selectNode(XCSG.parameterIndex, numOfParameters)));
		
		//check parameter types
		Q methodsWithRightParamTypes = methodsWithRightNumberOfParams;
		for(int i = 0; i<numOfParameters; i++) {
			Q paramsWithTypes = paramGraph.forwardOn(Query.universe().edges(XCSG.TypeOf));
			Q paramType = Query.universe().types(parameterTypesSource.get(i));
			
			Q rightType = paramsWithTypes.predecessors(paramType);
			Q methodsWithRightParam = paramGraph.predecessors( rightType.selectNode(XCSG.parameterIndex, i));
			
			methodsWithRightParamTypes = methodsWithRightParamTypes.intersection(methodsWithRightParam);
			
		}
		
		return methodsWithRightParamTypes;
		
	}
	
	/**
	 * Returns from contextmethods those methods that are equally or more visible than the given method
	 * @param method
	 * @param contextMethods
	 * @return
	 */
	public Q methodsEquallyOrMoreVisible(Q method, Q contextMethods) {
		//assume 1 method
		
		// private<default(package)<protected< public
		boolean isPublic = !CommonQueries.isEmpty(method.nodes(XCSG.publicVisibility));
		boolean isPackage =!CommonQueries.isEmpty(method.nodes(XCSG.packageVisibility));
		boolean isProtected = !CommonQueries.isEmpty(method.nodes(XCSG.protectedPackageVisibility));
		boolean isPrivate = !CommonQueries.isEmpty(method.nodes(XCSG.privateVisibility));
		

		Q resultMethods = Query.empty();
		
		if(isPublic) {
			resultMethods = resultMethods.union(contextMethods.nodes(XCSG.publicVisibility));
			
		} else if(isProtected) {
			resultMethods = resultMethods.union(contextMethods.nodes(XCSG.publicVisibility,XCSG.protectedPackageVisibility));
			
		} else if(isPackage) {
			resultMethods = resultMethods.union(contextMethods.nodes(XCSG.publicVisibility,XCSG.packageVisibility,XCSG.protectedPackageVisibility));
		
		} else if(isPrivate) {
			resultMethods = resultMethods.union(contextMethods.nodes(XCSG.publicVisibility,XCSG.packageVisibility,XCSG.protectedPackageVisibility,XCSG.privateVisibility));
		}
		
		return resultMethods;
	
	}
	
	/**
	 * Returns from contextmethods those methods that are equally or more visible than the given visibility.
	 * 
	 * @param visibility
	 * @param contextMethods
	 * @return
	 */
	public Q methodsEquallyOrMoreVisible(String visibility, Q contextMethods) {
		
		
		// private<default(package)<protected< public
		boolean isPublic = visibility.equals("Public");
		boolean isPackage =visibility.equals("Package");
		boolean isProtected = visibility.equals("Protected");
		boolean isPrivate = visibility.equals("Private");
		

		Q resultMethods = Query.empty();
		
		if(isPublic) {
			resultMethods = resultMethods.union(contextMethods.nodes(XCSG.publicVisibility));
			
		} else if(isProtected) {
			resultMethods = resultMethods.union(contextMethods.nodes(XCSG.publicVisibility,XCSG.protectedPackageVisibility));
			
		} else if(isPackage) {
			resultMethods = resultMethods.union(contextMethods.nodes(XCSG.publicVisibility,XCSG.packageVisibility,XCSG.protectedPackageVisibility));
		
		} else if(isPrivate) {
			resultMethods = resultMethods.union(contextMethods.nodes(XCSG.publicVisibility,XCSG.packageVisibility,XCSG.protectedPackageVisibility,XCSG.privateVisibility));
		}
		
		return resultMethods;
	
	}
	
	/**
	 * Selects from the methods in methodsContext those methods that have a return type covariant to the given type.
	 * 
	 * @param type
	 * @param methodsContext
	 * @return
	 */
	public Q covariantReturnTypes( Q type, Q methodsContext ) {
		Q typeh = CommonQueries.typeHierarchy(type, TraversalDirection.REVERSE);
		
		Q returnGraph =  methodsContext.union(Query.universe().nodes(XCSG.Type)).induce(Query.universe().edges(XCSG.Returns));
		
		Q retTypes = returnGraph.forwardStep(methodsContext).nodes(XCSG.Type);
		
		Q subTypesOfReturnTypes = retTypes.intersection(typeh);
		
		Q methodsWithCovariantReturnTypes = returnGraph.reverseStep( subTypesOfReturnTypes).nodes(XCSG.Method);

		return methodsWithCovariantReturnTypes;
		
	}
	
	/**
	 * Selects from methodsContext those methods that are override-equivalent to the given method description.
	 * 
	 * @param visiblity
	 * @param methodName
	 * @param parameterTypes
	 * @param returnType
	 * @param methodsContext
	 * @return
	 */
	public Q overrideEquivalentMethods(String visiblity, String methodName, List<String> parameterTypes , Q returnType, Q methodsContext ) {
		System.out.println("start override");
		
		//Same name
		methodsContext = methodsContext.methods(methodName)
		
		//Rule #1:Only inherited methods can be overridden.(public or protected or default)
		.nodes(XCSG.publicVisibility,XCSG.protectedPackageVisibility,XCSG.packageVisibility)

		//Rule #2:Final and static methods cannot be overridden.
		//Rule #8:Constructors cannot be overridden.
		.nodes(XCSG.InstanceMethod)
		.difference(methodsContext.nodes(XCSG.Java.finalMethod));
		
		//Rule #4: The overriding method must have same return type (or subtype).
		methodsContext = covariantReturnTypes(returnType, methodsContext);
		
		//Rule #5: The overriding method must not have more restrictive access modifier.
		methodsContext = methodsEquallyOrMoreVisible(visiblity, methodsContext);
		
		//Rule #6: The overriding method must not throw new or broader checked exceptions.
		//TODO
		
		//Rule #3: The overriding method must have same argument list.
		methodsContext = methodsWithParameters(methodsContext, parameterTypes);
				
		return methodsContext;

	}
	

		
	

}
