package WQL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.NodeGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.index.BaseNodeId;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;

import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;
import com.ensoftcorp.open.commons.utilities.FormattedSourceCorrespondence;

public   class W  {
	
	/**
	 * String constant for markers, see also plugin.xml
	 * 
	 */
	private static final String refactoringDangerMarker = "rg.myproblem";
	
	
	/**
	 * Wrapping utilities
	 * 
	 * 
	 */
	
	private Q thisQ = Query.empty();
	
	private Q Q() {
		
		return thisQ;
	}
	
	protected W(Q start) {
		thisQ = start;
		
	}
	
	protected W() {}

	
	
	
	
	/**
	 * The entire graph, shorthand for universe()
	 * 
	 * @return The entire graph
	 */
	public static W U() {
		
		return universe();
		
		
		
	};
	
	
	/**
	 * The entire graph (the usual starting point for a query). Contains the whole workspace, but also the JDK etc
	 * 
	 * @return The entire graph
	 */
	public static W universe() {
		
		return new W(Query.universe());
		
		
		
	}
	
	/**
	 * Returns those nodes which are declared by a source project.
	 * 
	 * 
	 * @return
	 */
	public static W projectDeclarations() {
		return new W(CommonQueries.projectDeclarations());
		
	}
	
	
	/**
	 * 	Returns those nodes which are declared by a source project with the given name.
	 * 
	 * 
	 * @param name
	 * @return
	 */
	public  W projectDeclarations(String name) {
		return new W(CommonQueries.projectDeclarations(name));
		
	}

	

	

	
	
	/* 
	 * Ou queries
	 * 
	 * 
	 * */
	

	/**
	 * Returns whether the given Query is empty.
	 * 
	 * @return true if the query results in an empty graph
	 */
	public boolean isEmpty() {
		return CommonQueries.isEmpty(thisQ);
		
	}
	
	/**
	 * Returns whether the given Query is not empty.
	 * 
	 * @return true if the query does not result in an empty graph
	 */
	public boolean isNotEmpty() {
		return !this.isEmpty();
		
	}
	
	/**
	 * Returns an empty graph.
	 * 
	 * May be used to seed a union of several graphs, or may be returned instead of null.
	 * 
	 * @return An empty graph.
	 */
	public static W empty() {
		
		return new W(Query.empty());
	}
	

	
	

	/* 
	 * Q queries
	 * 
	 * 
	 * */
	
	
	/**
	 * From this graph, selects the subgraph such that the given nodes in to are reachable from the nodes in from using forward traversal.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public W between(W from, W to) {
		
		return new W(thisQ.between(from.Q(), to.Q()));
	}


	/**
	 * From this graph, Selects the subgraph such that the given nodes in to are reachable from the nodes in from using forward traversal, without walking through nodes or edges in omit. Logically equivalent to this.difference(omit).between(from, to). See between(Q, Q) for additional details.
	 * @param from
	 * @param to
	 * @param omit
	 * @return
	 */
	public W between(W from, W to, W omit) {
		
		return new W(thisQ.between(from.Q(), to.Q(), omit.Q()));
	}


	/**
	 * From this graph, selects the subgraph such that the given nodes in to are reachable from the nodes in fromin a single step.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public W betweenStep(W from, W to) {
		
		return new W(thisQ.betweenStep(from.Q(), to.Q()));
	}


	/**
	 * For each node in this, select the nodes which are successors along XCSG.Contains, not including the origin.
	 * 
	 * @return
	 */
	public W children() {
		
		return new W(thisQ.children());
	}


	/**
	 * Select the nodes which are descendants along XCSG.Contains, including the origin.
	 * @return
	 */
	public W contained() {
		
		return new W(thisQ.contained());
	}


	/**
	 * Select the nodes which are ancestors along XCSG.Contains, including the origin.
	 * @return
	 */
	public W containers() {
		
		return new W(thisQ.containers());
	}


	/**
	 * 
	 * Select the current graph, excluding the given graphs.

		Note that, because an edge is only in a graph if it's nodes are in a graph, removing an edge will necessarily remove the nodes it connects as well. Removing either node would remove the edge as well.
		
		This behavior may seem counter-intuitive if one is thinking in terms of removing a single edge from a graph. Consider the graphs:
		
		    g1: a -> b -> c
		    g2: a -> b 

		g1.remove(g2) yields the graph containing only node c: because b is removed, so b -> c is also removed.

		In general, this operation is useful for removing nodes from a graph, but may not be as useful for operating on edges.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param expr
	 * @return
	 */
	public W difference(W... expr) {
		
		return new W(thisQ.difference(QLarray(expr)));
	}




	/**
	 * 
	 * Select the current graph, excluding the edges from the given graphs. 
	 * 
	 * @param expr
	 * @return
	 */
	public W differenceEdges(W... expr) {
		
		return new W(thisQ.differenceEdges(QLarray(expr)));
	}


	/**
	 * Select subgraph where Edges are tagged with at least one of tags. All Nodes are retained.
	 * 
	 * @param tags
	 * @return
	 */
	public W edges(String... tags) {
		
		return new W(thisQ.edges(tags));
	}

	
	/**
	 * Select subgraph where Edges are tagged with all of given tags. All Nodes are retained.
	 * 
	 * @param tags
	 * @return
	 */
	public W edgesTaggedWithAll(String... tags) {
		
		return new W(thisQ.edgesTaggedWithAll(tags));
	}


	/**
	 * Select subgraph where Edges are tagged with at least one of tags. All Nodes are retained.
	 * 
	 * @param tags
	 * @return
	 */
	public W edgesTaggedWithAny(String... tags) {
		
		return new W(thisQ.edgesTaggedWithAny(tags));
	}


	



	/**
	 * Evaluate the expression.
	 * 
	 * @return the Graph which is the result of evaluating the query expression
	 */
	public Graph eval() {
		
		return thisQ.eval();
	}


	/**
	 * Selects fields of a given name.
	 * 
	 * @param fieldName
	 * @return
	 */
	public W fields(String fieldName) {
		
		return new W(thisQ.fields(fieldName));
	}


	/**
	 * From this graph, selects the subgraph reachable from the given nodes using forward transitive traversal.

		A typical use is to select CALL edges, then walk from a set of methods:
		
		Q callg = universe().edgesTaggedWithAny(Attr.Edge.CALL);
		Q forwardCallg = callg.forward(universe().method("foo")); 
	 * 
	 * 
	 * @param nodes
	 * @return
	 */
	public W forward(W nodes) {
		
		return new W(thisQ.forward(nodes.Q()));
	}


	/**
	 * Within edges, selects the subgraph reachable from the nodes in this using forward transitive traversal, includes the origin nodes.

		A typical use is to select CALL edges, then walk from a set of methods:
		
		Q callEdges = universe().edgesTaggedWithAny(Attr.Edge.CALL);
		Q forwardCallg = universe().method("foo").forwardOn(callEdges); 
	 * 
	 * 
	 * @param edges
	 * @return
	 */
	public W forwardOn(W edges) {
		
		return new W(thisQ.forwardOn(edges.Q()));
	}


	/**
	 * From this graph, selects the subgraph reachable from the given nodes along a path length of 1 in the forward direction.

		The final result includes the given nodes, the traversed edges, and the reachable nodes.
	 * 
	 * 
	 * @param nodes
	 * @return
	 */
	public W forwardStep(W nodes) {
		
		return new W(thisQ.forwardStep(nodes.Q()));
	}


	/**
	 * 
	 * Within edges, selects the subgraph reachable from the nodes in this along a path length of 1 in the forward direction.

		The final result includes the given nodes, the traversed edges, and the reachable nodes.
	 * 
	 * @param edges
	 * @return
	 */
	public W forwardStepOn(W edges) {
		
		return new W(thisQ.forwardStepOn(edges.Q()));
	}


	/**
	 * Within edges, selects the subgraph reachable from the nodes in this along a path length of n in the forward direction.

		The final result includes the given nodes, the traversed edges, and the reachable nodes.
	 * 
	 * 
	 * @param edges
	 * @param pathLength
	 * @return
	 */
	public W forwardStepOn(W edges, int pathLength) {
		
		return new W(thisQ.forwardStepOn(edges.Q(),pathLength));
	}


//	/*
//	 * 
//	 * 
//	 * 
//	 */
//	public QL functions(String methodName) {
//		
//		return new QL(thisQ.functions(methodName));
//	}


	
	/**
	 * Adds edges from the given graph to this graph.
	 * 
	 * The result will contain only the nodes from this graph, the edges from this graph, and the edges from the given graph.
	 * 
	 * @param edges
	 * @return
	 */
	public W induce(W edges) {
		
		return new W(thisQ.induce(edges.Q()));
	}


	/**
	 * 
	 * Yields the intersection of this graph and the given graphs. That is, the resulting graph's nodes are the intersection of all node sets, and likewise for edges.
	 * 
	 * @param expr
	 * @return
	 */
	public W intersection(W... expr) {
	
		 return new W(
				 thisQ.intersection(QLarray(expr))
						 
						
						 
				 );
	}


	/**
	 * Selects the nodes from the given graph with no successors.
	 * 
	 * @return
	 */
	public W leaves() {
		
		return new W(thisQ.leaves());
	}

	/**
	 * Selects methods of a given name, regardless of signature.
	 * 
	 * @param methodName
	 * @return
	 */
	public W methods(String methodName) {
		
		return new W(thisQ.methods(methodName));
	}


	/**
	 * 
	 * Select subgraph where Nodes are tagged with at least one of tags. No Edges are retained.
	 * 
	 * @param tags
	 * @return
	 */
	public W nodes(String... tags) {
		
		return new W(thisQ.nodes(tags));
	}


	/**
	 * 
	 * Select subgraph where Edges are tagged with all of given tags. All Nodes are retained.
	 * 
	 * @param tags
	 * @return
	 */
	public W nodesTaggedWithAll(String... tags) {
		
		return new W(thisQ.nodesTaggedWithAll(tags));
	}


	/**
	 * Select subgraph where Edges are tagged with at least one of tags. All Nodes are retained.
	 * 
	 * @param tags
	 * @return
	 */
	public W nodesTaggedWithAny(String... tags) {
		
		return new W(thisQ.nodesTaggedWithAny(tags));
	}


	/**
	 * For each node in this, select the node which is the predecessor along XCSG.Contains, not including the origin.
	 * 
	 * @return
	 */
	public W parent() {
		
		return new W(thisQ.parent());
	}


	/**
	 * Selects a package of a given name.
	 *  
	 * @param packageName
	 * @return
	 */
	public W pkg(String packageName) {
		
		return new W(thisQ.pkg(packageName));
	}


	/**
	 * From within this graph, selects the predecessors reachable from the given nodes along a path length of 1. The result does not include edges.

		Consider the following graph:
		A -> B
		B -> C
		C -> C
		
		Given origin nodes {B}, the result is {A}.
		Given origin nodes {C}, the result is {B,C}.
	 * 
	 * @param nodes
	 * @return
	 */
	public W predecessors(W nodes) {
		
		return new W(thisQ.predecessors(nodes.Q()));
	}


	/**
	 * 
	 * From within edges, selects the predecessors reachable from the nodes in this along a path length of 1. The result does not include edges. 
	 * 
	 * @param edges
	 * @return
	 */
	public W predecessorsOn(W edges) {
		
		return new W(thisQ.predecessorsOn(edges.Q()));
	}


	/**
	 * Selects a project of a given name.
	 * 
	 * @param projectName
	 * @return
	 */
	public W project(String projectName) {
		
		return new W(thisQ.project(projectName));
	}


	/**
	 * Retain only edges, retaining only those nodes directly connected to a retained edge.
	 * 
	 * @return
	 */
	public W retainEdges() {
		
		return new W(thisQ.retainEdges());
	}


	/**
	 * Retain only nodes, ignoring all edges.
	 * 
	 * @return
	 */
	public W retainNodes() {
		
		return new W(thisQ.retainNodes());
	}


	/**
	 * From this graph, selects the subgraph reachable from the given nodes using reverse transitive traversal.
	 * 
	 * @param nodes
	 * @return
	 */
	public W reverse(W nodes) {
		
		return new W(thisQ.reverse( nodes.Q()));
	}


	/**
	 * Within edges, selects the subgraph reachable from the nodes in this using reverse transitive traversal.
	 * The final result includes the given nodes, the traversed edges, and the reachable nodes.
	 * 
	 * @param edges
	 * @return
	 */
	public W reverseOn(W edges) {
		
		return new W(thisQ.reverseOn( edges.Q()));
	}


	/**
	 * From this graph, selects the subgraph reachable from the given nodes along a path length of 1 in the reverse direction.
	 * The final result includes the given nodes, the traversed edges, and the reachable nodes.
	 * 
	 * 
	 * @param nodes
	 * @return
	 */
	public W reverseStep(W nodes) {
		
		return new W(thisQ.reverseStep( nodes.Q()));
	}


	/**
	 * Within edges, selects the subgraph reachable from the nodes in this along a path length of 1 in the reverse direction.
	 * The final result includes the given nodes, the traversed edges, and the reachable nodes.
	 * 
	 * @param edges
	 * @return
	 */
	public W reverseStepOn(W edges) {
		
		return new W(thisQ.reverseStepOn( edges.Q()));
	}


	/**
	 * 
	 * Within edges, selects the subgraph reachable from the nodes in this along a path length of n in the reverse direction.
	 * 
	 * @param edges
	 * @param pathLength
	 * @return
	 */
	public W reverseStepOn(W edges, int pathLength) {
		
		return new W(thisQ.reverseStepOn( edges.Q(),pathLength));
	}


	/**
	 * Selects the nodes from the given graph with no predecessors.
	 * 
	 * @return
	 */
	public W roots() {
		
		return new W(thisQ.roots());
	}


	/**
	 * 
	 * Select subgraph containing Edges which have a given key defined, with any value.
	 * 
	 * @param key
	 * @return
	 */
	public W selectEdge(String key) {
		
		return new W(thisQ.selectEdge(key));
	}


	/**
	 * Select subgraph where Edges have a given key with any value specified in the given values.
	 * 
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public W selectEdge(String key, Object... values) {
		
		 return new W(thisQ.selectEdge(key,values));
	}


	/**
	 * 
	 * Select subgraph containing Nodes which have a given key defined, with any value.
	 * 
	 * @param key
	 * @return
	 */
	public W selectNode(String key) {
		
		return new W(thisQ.selectNode(key));
	}


	/**
	 * Select subgraph containing Nodes which have a given key with any value specified in the given values.
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public W selectNode(String key, Object... values) {
		
		return new W(thisQ.selectNode(key,values));
	}


	/**
	 * From this graph, selects the successors reachable from the given nodes along a path length of 1.
	 * 
	 *  Consider the following graph:
		A -> A
		A -> B
		B -> C
		
		Given origin nodes {B}, the result is {C}.
		Given origin nodes {A}, the result is {A,B}.
	 * 
	 * @param nodes
	 * @return
	 */
	public W successors(W nodes) {
		
		return new W(thisQ.successors(nodes.Q()));
	}


	/**
	 * From within edges, selects the successors reachable from the nodes in this along a path length of 1. The result does not include edges. 
	 * 
	 * @param edges
	 * @return
	 */
	public W successorsOn(W edges) {
		
		return new W(thisQ.successorsOn(edges.Q()));
	}


	/**
	 * Wraps a Graph in a query expression.
	 * 
	 * @param graph
	 * @return query expression wrapping the Graph
	 */
	public static W toW(Graph graph) {
		
		return new W(Common.toQ(graph));
	}
	

	

	
	/**
	 * Selects classes, interfaces, enums and annotations of a given name.
	 * 
	 * @param typeName
	 * @return
	 */
	public W types(String typeName) {
	
		return new W(thisQ.types(typeName));
	}


	/**
	 * Yields the union of this graph and the given graphs.
	 * 
	 * @param expr
	 * @return
	 */
	public W union(W... expr) {
	
		 return new W(thisQ.union(QLarray(expr)));
		 
	}


	

	
	/* 
	 * CommonQueries queries
	 * 
	 * 
	 * */
	
	
	//this = origin
	/**
	 * Gets the call graph for the nodes in this. Traverses CALL edges in the given direction(s) from the origin. Includes the origin nodes.
	 * 
	 * @param direction
	 * @return
	 */
	public W conservativeCallGraph( CommonQueries.TraversalDirection direction) {
		
		return new W(CommonQueries.call(thisQ, direction));
	}
	
//	/**
//	 * Produces a call graph. Traverses CALL edges in the given direction(s) from the origin. Uses only the given context for the traversal. The origin nodes are included in the result
//	 * 
//	 *  
//	 * 
//	 * @param context
//	 * @param direction
//	 * @return
//	 */
//	public QL getCallGraph(QL context, CommonQueries.TraversalDirection direction) {
//		
//		return new QL(CommonQueries.call(context.Q() ,thisQ, direction));
//	}
	
//	/**
//	 * Returns the return nodes for the given methods.
//	 * 
//	 * @return
//	 */
//	public QL getReturns() {
//		return new QL(CommonQueries.methodReturn(thisQ));
//		
//	}
	
	/**
	 * Produces a type hierarchy. Traverses SUPERTYPE edges in the given direction(s) from the origin. 
	 * Origins are included.
	 * 
	 * 
	 * @param direction
	 * @return
	 */
	public W typeHierarchyGraph(CommonQueries.TraversalDirection direction) {
		
		return new W(CommonQueries.typeHierarchy(thisQ, direction));
	}
	
	/**
	 * Produces a data flow graph. Traverses DATA_FLOW edges in the given direction(s) from the origin. 
	 * 
	 * @param direction
	 * @return
	 */
	public W dataflowGraph(CommonQueries.TraversalDirection direction) {
		
		return new W(CommonQueries.data(thisQ, direction));
		
	}
	/**
	 * 
	 * Returns the overrides graph for the given methods, in the given direction
	 * 
	 * @param direction
	 * @return
	 */
	public W overrideGraph(CommonQueries.TraversalDirection direction) {
		
		return new W(CommonQueries.overrides(thisQ, direction));
	}
	
	/**
	 * Returns the number of nodes contained.
	 * 
	 * 
	 */
	public long nodeSize() {
		
		return CommonQueries.nodeSize(thisQ);
		
	}
	
	/**
	 * 
	 * Returns the number of edges contained.
	 *
	 * @return
	 */
	public long edgeSize() {
		return CommonQueries.edgeSize(thisQ);
		
		
	}
	
	/**
	 * Starting from the given origin, returns the traversal in the given direction(s) along all edges in the given context.
	 * 
	 * @param origin
	 * @param direction
	 * @return
	 */
	public W traverse( W origin, CommonQueries.TraversalDirection direction) {
		return new W(CommonQueries.traverse(thisQ, origin.Q(), direction));
		
	}
	
	
	/**
	 * Starting from the given origin, returns the traversal in the given direction(s) along all edges of the given kinds.
	 * 
	 * @param origin
	 * @param direction
	 * @param edgeTags
	 * @return
	 */
	public W traverse( W origin, CommonQueries.TraversalDirection direction, String... edgeTags) {
		return new W(CommonQueries.traverse(thisQ, origin.Q(), direction, edgeTags));
		
	}
	
	/**
	 * Starting from the given origin, returns the traversal in the given direction(s) along all edges in the given context.
	 * 
	 * @param origin
	 * @param direction
	 * @return
	 */
	public W traverseStep( W origin, CommonQueries.TraversalDirection direction) {
		return new W(CommonQueries.traverseStep(thisQ, origin.Q(), direction));
		
	}
	
	
	/**
	 * Starting from the given origin, returns the traversal in the given direction(s) along all edges of the given kinds.
	 * 
	 * @param origin
	 * @param direction
	 * @param edgeTags
	 * @return
	 */
	public W traverseStep( W origin, CommonQueries.TraversalDirection direction, String... edgeTags) {
		return new W(CommonQueries.traverseStep(thisQ, origin.Q(), direction, edgeTags));
		
	}
	
	
	/* 
	 * OpenCommonQueries queries
	 * 
	 * 
	 * */
	
//	public QL getDirectCallers() {
//		
//		return new QL(com.ensoftcorp.open.commons.analysis.CommonQueries.callers(thisQ));
//	}
	
	public W nodesAttributeValuesEndingWith(String attribute, String suffix) {
		return new W(com.ensoftcorp.open.commons.analysis.CommonQueries.nodesAttributeValuesEndingWith(thisQ, attribute, suffix));
		
	}
	
	
	public W nodesStartingWith(String substring) {
		return new W(com.ensoftcorp.open.commons.analysis.CommonQueries.nodesStartingWith(thisQ, substring));
		
	}
	

	
	
	/*
	 * 
	 * Queries from COMMON
	 * 
	 */
	
	/**
	 * Selects a method qualified by its declaring type.
	 * 
	 * @param typeName
	 * @param methodName
	 * @return
	 */
	public W selectMethods(String typeName, String methodName) {
		return new W(Common.methodSelect(typeName, methodName));
		
	}
	
	/**
	 * Selects a method qualified by its declaring type and package.
	 * 
	 * @param packageName
	 * @param typeName
	 * @param methodName
	 * @return
	 */
	public W selectMethods(String packageName, String typeName, String methodName) {
		return new W(Common.methodSelect(packageName, typeName, methodName));
		
	}
	
	/**
	 * Selects fields of a given name.
	 * 
	 * @param fieldName
	 * @return
	 */
	public W selectFields(String fieldName) {
		return new W (Common.fields(fieldName));
		
		
	}
	
	/**
	 * Selects a field qualified by its declaring type.
	 * 
	 * @param typeName
	 * @param fieldName
	 * @return
	 */
	public W selectFields(String typeName, String fieldName) {
		
		return new W(Common.fieldSelect(typeName, fieldName));
		
	}
	
	/**
	 * Selects a field qualified by its declaring type and package
	 * 
	 * @param packageName
	 * @param typeName
	 * @param fieldName
	 * @return
	 */
	public W selectFields(String packageName,String typeName, String fieldName) { 
		return new W(Common.fieldSelect(packageName, typeName, fieldName));
		
	}
	
	
	/**
	 * 
	 * Selects a type qualified by its containing package.
	 * 
	 * @param packageName
	 * @param typeName
	 * @return
	 */
	public W selectTypes(String packageName, String typeName) {
		return new W(Common.typeSelect(packageName, typeName));
		
		
	}
	
	
	/* 
	 * Custom queries
	 * 
	 * 
	 * */
	
	/**
	 * Selects a field qualified by its declaring type.
	 * 
	 * @param typeName
	 * @param methodName
	 * @return
	 */
	public  W fields(String typeName, String methodName ) {
		return this.fields(typeName, methodName);
		
		
	}
	/**
	 * Selects a field qualified by its declaring type and package
	 * 
	 * @param packageName
	 * @param typeName
	 * @param methodName
	 * @return
	 */
	public  W fields(String packageName, String typeName, String methodName ) {
		return this.selectFields(packageName, typeName, methodName);
		
		
	}
	
	/**
	 * Selects a method qualified by its declaring type.
	 * 
	 * @param typeName
	 * @param methodName
	 * @return
	 */
	public  W methods(String typeName, String methodName ) {
		return  this.selectMethods(typeName, methodName);
		
		
	}
	
	/**
	 * 
	 * 
	 * @param packageName
	 * @param typeName
	 * @param methodName
	 * @return
	 */
	public  W methods(String packageName, String typeName, String methodName ) {
		return this.selectMethods(packageName, typeName, methodName);
		
		
	}
	
	/**Selects a type qualified by its containing package.
	 * 
	 * @param packageName
	 * @param typeName
	 * @return
	 */
	public W types(String packageName, String typeName) {
		return this.selectTypes(packageName, typeName);
		
	}
	
	

	
	////////////////////////////
	
	/**
	 * Allows to the difference operation using queries relative to the current query.
	 * Instead of .difference(WQ.universe().methods()) we can write .difference(currentResult->currentResult.methods() )
	 * This also allows using the negation of a filter without either defining another "excludeX" or doing .difference(WQ.universe().selectX()): .difference(currentResult->currentResult.selectX())
	 * 
	 * graph.differenceRelative(a -> a.operation) is equivalent to: graph.difference(graph.operation)
	 * 
	 * graph.differenceRelative(a -> a.operationA, a->a.operationB) is equivalent to: graph.difference(graph.operationA, graph.operationB )
	 * 
	 * @param fn query expression that will be relative to the current result.
	 * @return Select the current graph, excluding resulting graphs of the given relative query expressions
	 */
	public W differenceR(UnaryOperator<W> ... fn ) {
		return this.difference(QLFunctionArrayToQLArray(this,fn));
	}
	
	/*
	 * Single argument version, otherwise the compiler can't determine the difference between selectFields() and selectFields(String)
	 * https://coderedirect.com/questions/350971/the-target-type-of-this-expression-must-be-a-functional-interface
	 */
	public W differenceR(UnaryOperator<W>  fn ) {
		return this.difference(fn.apply(this));
	}
	
	/**
	 * Yields the union of this graph and the given graphs. That is, the resulting graph's nodes are the union of all nodes, and likewise for edges.The given graphs are query expressions relative to the current query). 
	 * 
	 * graph.unionRelative(a -> a.operationA, a->a.operationB, ...) is equivalent to: graph.union(graph.operationA, graph.operationB, ... )
	 * 
	 * @return the union of all the given relative queries
	 */
	@SafeVarargs
	public final W unionR(UnaryOperator<W> ... fn ) {
		
		
		
		return this.union(QLFunctionArrayToQLArray(this,fn));
	}
	
	/*
	 * Single argument version, otherwise the compiler can't determine the difference between selectFields() and selectFields(String)
	 * https://coderedirect.com/questions/350971/the-target-type-of-this-expression-must-be-a-functional-interface
	 */
	public final W unionR(UnaryOperator<W> fn ) {
			
		return this.union(fn.apply(this));
	}

	
	/**
	 * Yields the intersection of this graph and the given graphs. That is, the resulting graph's nodes are the intersection of all node sets, and likewise for edges. 
	 * The graphs are given by relative query expressions
	 * 
	 * graph.intersectionRelative(a -> a.operationA, a->a.operationB, ...) is equivalent to: graph.intersection(graph.operationA, graph.operationB, ... )
	 * 
	 * @return the intersection of all the given relative queries
	 */
	public W intersectionR(UnaryOperator<W> ... fn ) {
		return this.intersection(QLFunctionArrayToQLArray(this,fn));
	}
	
	/*
	 * Single argument version, otherwise the compiler can't determine the difference between selectFields() and selectFields(String)
	 * https://coderedirect.com/questions/350971/the-target-type-of-this-expression-must-be-a-functional-interface
	 */
	public W intersectionR(UnaryOperator<W> fn ) {
		return this.intersection(fn.apply(this));
	}

	
	/**
	 * Selects the nodes for which the given function evaluates to true.
	 * For instance: 
	 * W.universe.methods("filterNodes").filterNodes(s -> s.getParameters.nodeSize == 1).show
	 * 
	 * @param fn
	 * @return
	 */
	public W filterNodes(Function<W,Boolean> fn) {
		AtlasSet<Node>  nodes = this.eval().nodes();
		
		
		AtlasSet<Node> result = new AtlasHashSet<Node>();
		for (Node node : nodes) {
			W singleW = W.toW(Common.toGraph(node));
			boolean boolResult = fn.apply(singleW);
			if(boolResult) {
				result.add(node);
			}
			
		}
		
		
		return W.toW(Common.toGraph(result));
		
	}
	
	/**
	 * Selects the node for which the given function evaluates to true, works on the underlying Graph Nodes, allowing operations on tags and attributes
	 * 
	 * @param fn
	 * @return
	 */
	public W filterGraphNodes(Function<Node,Boolean> fn) {
			AtlasSet<Node>  nodes = this.eval().nodes();
			
			
			AtlasSet<Node> result = new AtlasHashSet<Node>();
			for (Node node : nodes) {
				boolean boolResult = fn.apply(node);
				if(boolResult) {
					result.add(node);
				}
				
			}
			return W.toW(Common.toGraph(result));
	}
	
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes using forward transitive traversal. 
	 * (an origin node is only included if it itself is reachable from another origin node)
	 * 
	 * @param nodes
	 * @return the subgraph reachable from the given nodes using forward transitive traversal. 
	 */
	public W descendants(W nodes ) {
		
		return this.forward(this.successors(nodes));
		
	}
	
	/**
	 * From within edges, selects the descendants reachable from the nodes in this along a path length of 1.
	 * (an origin node is only included if it itself is reachable from another origin node)
	 * 
	 * 
	 * 
	 * @param edges
	 * @return within edges, selects the subgraph reachable from the nodes in this using forward transitive traversal.
	 */
	public W descendantsOn(W edges ) {
		
		return this.successorsOn(edges).forwardOn(edges);
		
	}
	
	
	
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes using reverse transitive traversal.
	 * nodes are only included if they are themselves reachable. 
	 * 
	 * @param nodes
	 * @return  the subgraph reachable from the given nodes using reverse transitive traversal.
	 */
	public W anchestors(W nodes) {
		return this.reverse(this.predecessors(nodes));
		
	}
	
	
	/**
	 * Within edges, selects the subgraph reachable from the nodes in this using reverse transitive traversal.
	 * this nodes are only included if they are themselves reachable. 
	 * 
	 * @param edges
	 * @return  the subgraph reachable from the given nodes using reverse transitive traversal.
	 */
	public W anchestorsOn(W edges) {
		return this.predecessorsOn(edges).reverseOn(edges);
		
	}
	
	
	/**
	 * Within edges, selects the subgraph reachable from the nodes in this using forward transitive traversal. 
	 * 
	 * Enables quick forward traversal on edges:
	 * 
	 * graph.forwardOnEdgesTaggedWithAny(XCSG.HasParameter)
	 * equivalent to
	 * graph.forwardOn(WQ.universe().edgesTaggedWithAny(XCSG.HasParameter))
	 * 
	 * 
	 * @param edges
	 * @return
	 */
	public W forwardOnEdgesTaggedWithAny(String ...edges) {
		
		return this.forwardOn(universe().edgesTaggedWithAny(edges));
	}
	
	/**
	 * 
	 * Within edges, selects the subgraph reachable from the nodes in this using forward transitive traversal. 
	 * 
	 * 
	 * @param edges
	 * @return
	 */
	public W forwardOnEdgesTaggedWithAll(String ...edges) {
		
		return this.forwardOn(universe().edgesTaggedWithAll(edges));
	}
	
	

	
//	public QL getReturnTypes() {
//		return this
//				.union(QL.universe().nodes(XCSG.Type))
//				.induce(QL.universe().edges(XCSG.Returns))
//				.forwardStep(this)
//				.nodes(XCSG.Type);
//		//TODO overbodige stappen volgens mij
//	//this.forwardStepOn(QL.universe().edges(XCSG.Returns).nodes(XCSG.Type)	
//	}
	
	
	public W methodsWithCovariantReturnTypesF( W type ) {
		return 
		this
		//add returns edges and their nodes
		.successorsOn(W.universe().edges(XCSG.Returns))
		//remove every Type that is not type or a subtype of type
		.difference(type.anchestorsOn(W.U().edges(XCSG.Supertype)))
		
		//removes nodes without edges(methods without a type)
		.predecessorsOn(W.universe().edges(XCSG.Returns))
		.intersection(this)
		//keep only the methods
		.nodes(XCSG.Method);
		
			
						
	}
	
	public W methodsEquallyOrMoreVisible(String visibility) {
		
		
		// private<default(package)<protected< public
		boolean isPublic = visibility.equals("Public");
		boolean isPackage =visibility.equals("Package");
		boolean isProtected = visibility.equals("Protected");
		boolean isPrivate = visibility.equals("Private");
		

		W resultMethods = W.empty();
		
		if(isPublic) {
			resultMethods = resultMethods.union(this.nodes(XCSG.publicVisibility));
			
		} else if(isProtected) {
			resultMethods = resultMethods.union(this.nodes(XCSG.publicVisibility,XCSG.protectedPackageVisibility));
			
		} else if(isPackage) {
			resultMethods = resultMethods.union(this.nodes(XCSG.publicVisibility,XCSG.packageVisibility,XCSG.protectedPackageVisibility));
		
		} else if(isPrivate) {
			resultMethods = resultMethods.union(this.nodes(XCSG.publicVisibility,XCSG.packageVisibility,XCSG.protectedPackageVisibility,XCSG.privateVisibility));
		}
		
		return resultMethods.nodes(XCSG.Method);
	
	}
	
	
	

/*
 * Selections
 * 
 * 
 * 
 */
	
	
	

	/**
	 * Selects nodes with the given name
	 * 
	 * @param name
	 * @return
	 */
	public W selectNodesNamed(String name) {
		return this.selectNode(XCSG.name, name);
		
	}
	
	/**
	 * Selects all nodes tagged with XCSG.Java.Class
	 * 
	 * @return
	 */
	public W selectClasses() {
		return this.nodes(XCSG.Java.Class);
	}
	
	/**
	 * Selects all nodes tagged with XCSG.Method
	 * 
	 * @return
	 */
	public W selectMethods() {
		return this.nodes(XCSG.Method);
		
	}
	
	/**
	 * Selects all nodes tagged with XCSG.Field
	 * 
	 * @return
	 */
	public W selectFields() {
		return this.nodes(XCSG.Field);
		
	}
	
	/**
	 * Selects concrete classes
	 *  
	 * 
	 * @return
	 */
	public W selectConcreteClasses() {
		return this.nodes(XCSG.Java.Class).difference(this.nodes(XCSG.Java.AbstractClass));
		
	}
	
	
	/**
	 * Selects abstract classes
	 * 
	 * @return
	 */
	public W selectAbstractClasses() {
		return this.nodes(XCSG.Java.AbstractClass);
		
	}
	
	/**
	 * Selects interfaces
	 * 
	 * @return
	 */
	public W selectInterfaces() {
		return this.nodes(XCSG.Java.Interface);
		
	}
	
	/**
	 * Selects abstract methods
	 * 
	 * @return
	 */
	public W selectAbstractMethods() {
		return this.nodes(XCSG.abstractMethod);
		
	}
	
	/**
	 * Selects concretemethods
	 * 
	 * @return
	 */
	public W selectConcreteMethods() {
		return this.nodes(XCSG.Method).difference(W.universe().nodes(XCSG.abstractMethod));
		
		
	}
	
	/**
	 * Select methods with parameters equivalent(order and type of parameters, not names) to the given parameters
	 * 
	 * @param parameters
	 * @return
	 */
	public W selectMethodsWithParameters(List<String> parameters) {
		
		return new W (methodsWithParametersB(thisQ,parameters));
		
		
	}
	
	/**
	 * Selects methods with the given signature(name and parameter),
	 * see selectMethodsWithParameters for more details
	 * 
	 * @param methodName
	 * @param parameterTypes
	 * @return
	 */
	public W selectMethodsWithSignature(String methodName, List<String> parameterTypes) {
		
		return this
				
				.getMethods().methods(methodName)
								
				.selectMethodsWithParameters(parameterTypes); 

	}
	
	
	/**
	 * Selects methods that are override equivalent to the given method informations
	 * 
	 * Rules
	 * Same name
	 * The overriding method must have same argument list.
	 * Only inherited methods can be overridden.(public or protected or default)
	 * Final and static methods cannot be overridden.
	 * Constructors cannot be overridden.
	 * The overriding method must have same return type (or subtype).(covariant return type)
	 * The overriding method must not have more restrictive access modifier.
	 * NOT IMPLEMENTED: The overriding method must not throw new or broader checked exceptions.
	 * 
	 * 
	 * 
	 * @param visiblity
	 * @param methodName
	 * @param parameterTypes
	 * @param returnType
	 * @return
	 */
	public W selectOverrideEquivalentMethods(String visiblity, String methodName, List<String> parameterTypes , W returnType ) {
		
		return
		//Same name
		this
		.methods(methodName)
		
		//Rule #1:Only inherited methods can be overridden.(public or protected or default)
		.nodes(XCSG.publicVisibility,XCSG.protectedPackageVisibility,XCSG.packageVisibility)
		
		//Rule #2:Final and static methods cannot be overridden.
		//Rule #8:Constructors cannot be overridden.
		.nodes(XCSG.InstanceMethod)
		.difference(this.nodes(XCSG.Java.finalMethod))
		
		//Rule #4: The overriding method must have same return type (or subtype).
		 .methodsWithCovariantReturnTypesF(returnType)
		
		
		//Rule #5: The overriding method must not have more restrictive access modifier.
		.methodsEquallyOrMoreVisible(visiblity)
		
		//Rule #6: The overriding method must not throw new or broader checked exceptions.
		//TODO
		
		//Rule #3: The overriding method must have same argument list.
		.selectMethodsWithParameters(parameterTypes);	
	}
	
	
	/**
	 * Select callsites which start with 'super.'
	 * 
	 * @return
	 */
	public W selectCallSitesWithSuper() {
		//The callsite node only knows a this, so we string match its containing controlflowname, if it starts with "super."
		//a semantic version might be possible by checking whether the this dataflows to a super method.
		return this.nodes(XCSG.CallSite).parent().selectNodesStartingWith("super.").children().nodes(XCSG.CallSite);
		

		
		
	}


	
	/**
	 * Selects methods by their signature attribute(return type, name and parameter types), relies on an exact string match, usage discouraged
	 * see ##signature in Atlas documentation
	 * 
	 * @param signature
	 * @return
	 */
	public W selectMethodsWithSignature(String signature) {
		return this.nodes(XCSG.Method).selectNode("##signature", signature);
		
	}

			
	
	/*
	 * 
	 * Navigations
	 * 
	 */
	
	/**
	 * Returns the methods declared by the given types
	 * 
	 * @return
	 */
	public W getMethods() {
		return new W (Common.methodsOf(thisQ));
	
	}
	
	/**
	 * Given a query, selects all fields reachable by forward traversal over Attr.Edge.DECLARES edges from said query.
	 * 
	 * @return
	 */
	public W getFields() {
		return new W(Common.fieldsOf(thisQ));
		
	}
	
	/**
	 * Given a query, selects all types reachable by forward traversal over Attr.Edge.DECLARES edges from said query. The original entities are not included in the result.
	 * @return
	 */
	public W getTypes() {
		return new W(Common.typesOf(thisQ));
		
	}
	
	
	/**
	 * Gets the interfaces implemented by the given classes
	 * 
	 * @return
	 */
	public W getImplements() {
		return this.successorsOn(W.universe().edges(XCSG.Java.Implements));
		
	}
	
//	/**
//	 * Gets the interfaces implemented by the given classes, and all interfaces these interfaces extend.
//	 * 
//	 * @return
//	 */
//	public QL getImplementsTransitive() {
//		return this.getImplements().descendantsOn(QL.universe().edges(XCSG.Java.Extends));
//		
//	}
	
	/**
	 * Gets the direct supertypes of the given types.
	 * 
	 * @return
	 */
	public W getSupertypes() {
		return this.successorsOn(W.universe().edges(XCSG.Supertype));
				
	}
	
	/**
	 * Gets the direct and indirect supertypes of the given types.
	 * 
	 * @return
	 */
	public W getSupertypesT() {
		return this.descendantsOn(W.universe().edges(XCSG.Supertype)).nodes(XCSG.Type);
				
	}
	
	
	/**
	 * Gets the direct subtypes of the given types
	 * 
	 * @return
	 */
	public W getSubtypes() {
		return this.predecessorsOn(W.universe().edges(XCSG.Supertype));
		
	}
	
	/**
	 * Gets the direct and indirect subtypes of the given types
	 * 
	 * @return
	 */
	public W getSubtypesT() {
		return this.anchestorsOn(W.universe().edges(XCSG.Supertype)).nodes(XCSG.Type);
		
	}
	
	

	/**
	 * Gets the superclasses the given classes extend.
	 * 
	 * @return
	 */
	public W getSuperclasses() {
		return this.successorsOn(W.universe().edges(XCSG.Java.Extends)).nodes(XCSG.Java.Class);
				
	}
	
	/**
	 * Gets all direct and indirect superclasses extended by the given classes
	 * 
	 * @return
	 */
	public W getSuperclassesT() {
		return this.descendantsOn(W.universe().edges(XCSG.Java.Extends)).nodes(XCSG.Java.Class);		
	}
	
	/**
	 * Gets the direct subclasses oh the given nodes
	 * 
	 * @return
	 */
	public W getSubclasses() {
		return this.predecessorsOn(W.universe().edges(XCSG.Java.Extends)).nodes(XCSG.Java.Class);
		
	}
	
	/**
	 * Gets the direct and indirect subclasses oh the given nodes
	 * 
	 * @return
	 */
	public W getSubclassesT() {
		return this.anchestorsOn(W.universe().edges(XCSG.Java.Extends)).nodes(XCSG.Java.Class);
		
	}
	
	
	/**
	 * Gets the classes that implement this interface
	 * 
	 * @return
	 */
	public W getImplementedBy() {
		return this.predecessorsOn(W.universe().edges(XCSG.Java.Implements));
		
	}
	
	
	/**
	 * Gets the methods that override the given methods
	 * 
	 * @return
	 */
	public W getOverridenBy() {
		return this.predecessorsOn(W.universe().edges(XCSG.Overrides));
	}
	
	/**
	 * Gets the methods that directly or indirectly override the given methods
	 * 
	 * @return
	 */
	public W getOverridenByT() {
		return this.anchestorsOn(W.universe().edges(XCSG.Overrides)).nodes(XCSG.Method);
	}
	
	

	/**
	 * Gets the methods overriden by the given methods
	 * 
	 * @return
	 */
	public W getOverrides() {
		return this.successorsOn(W.universe().edges(XCSG.Overrides));
		
	}
	
	/**
	 * Gets the methods directly or indirectly overriden by the given methods
	 * 
	 * @return
	 */
	public W getOverridesT() {
		return this.descendantsOn(W.universe().edges(XCSG.Overrides)).nodes(XCSG.Method);
		
	}
	
	/**
	 * Gets all the parameters of all the given methods 
	 * 
	 * @return
	 */
	public W getParameters() {
		return this.successorsOn(W.universe().edges(XCSG.HasParameter));
		
	}

	/**
	 * 
	 * Gets all the parameters(at the given index) of all the given methods 
	 * 
	 * 
	 * @param index
	 * @return
	 */
	public W getParameters(int index) {
		return new W(CommonQueries.methodParameter(thisQ, index));
		
	}
	
	/**
	 * Gets the annotations for the nodes in this
	 * 
	 * @return
	 */
	public W getAnnotations() {
		return this.successorsOn(W.U().edges(XCSG.Java.AnnotatedWith));

		
	}
	
	
	/**
	 * Gets all the callSites the given methods are (potentially, through dynamic binding, or definitely, when static) called at
	 * 
	 * 
	 * @return
	 */
	public W getCalledAt() {
		W dynamicCallSites = this
				.contained()
				.nodes(XCSG.Identity) //implicit 'this' parameter
				.predecessorsOn(W.universe().edges(XCSG.DataFlow_Edge)) //all possible inputs for 'this' parameter
				.nodes(XCSG.IdentityPass) 
				.successorsOn(W.universe().edges(XCSG.IdentityPassedTo)); //from the identity input to the callsite.
				
				W invokedSignature = this.predecessorsOn(W.U().edges(XCSG.InvokedSignature));
		
				W staticCallSites = this
				.predecessorsOn(W.universe().edges(XCSG.InvokedFunction));

				W callSites = dynamicCallSites.union(staticCallSites,invokedSignature);

				return callSites;
				
		//return this.predecessorsOn(W.U().edges(XCSG.InvokedSignature,XCSG.InvokedFunction));
				
		
	}
	
	
	/**
	 * Gets the methods that have a callsite to the given methods
	 * 
	 * @return
	 */
	public W getCallers() {
		//return this.predecessorsOn(W.U().edges(XCSG.Call));
		return this.getCalledAt().getContainingFunctions().nodes(XCSG.Method);
				
		
	}
	
	/**
	 * Returns all methods potentially called by the given methods
	 * 
	 * @return
	 */
	public W getCallees() {
		W cs = this.contained().nodes(XCSG.StaticDispatchCallSite,XCSG.DynamicDispatchCallSite,XCSG.CallSite,XCSG.ObjectOrientedCallSite,XCSG.ObjectOrientedStaticCallSite,XCSG.SimpleCallSite);
		
		//something fishy with chained method calls not having a call edges or sth
		W staticCS = cs.successorsOn(W.U().edges(XCSG.InvokedFunction));
		W dynCs = cs.predecessorsOn(W.U().edges(XCSG.IdentityPassedTo)).successorsOn(W.U().edges(XCSG.DataFlow_Edge)).parent();
		W dynSigCs = cs.successorsOn(W.U().edges(XCSG.InvokedSignature));
		
		return staticCS.union(dynCs, dynSigCs);
	}
	

	
	/**
	 * Returns the methods the given elements are contained in
	 * 
	 * @return
	 */
	public W getContainingFunctions() {
		
		return new W(com.ensoftcorp.open.commons.analysis.CommonQueries.getContainingFunctions(thisQ));
	}
	

	/**
	 * Returns the types of the given nodes
	 * 
	 * @return
	 */
	public W getTypesOf() {
		return this.successorsOn(W.U().edges(XCSG.TypeOf));
		
	}
	
	public Stream<Object> getAttributeValuesAsMap(String attribute) {
		
		return StreamSupport.stream(this.eval().nodes().spliterator(), true).map(e ->e.getAttr(attribute));
		
	}
	
	
	/**
	 * Gets the direct children declared by this which are classes.
	 * 
	 * @param className
	 * @return
	 */
	public W getClasses() {
		return this.successorsOn(W.U().edges(Attr.Edge.DECLARES)).nodes(XCSG.Java.Class);
		
		
		
	}

	/**
	 * Gets the direct children declared by this which are classes with the given name
	 * 
	 * @param className
	 * @return
	 */
	public W getClasses(String className) {
		return this.successorsOn(W.U().edges(Attr.Edge.DECLARES)).types(className).nodes(XCSG.Java.Class);
		
		
		
	}
	
	/**
	 * returns all containing packages of the given elements
	 * 
	 * @return
	 */
	public W getContainingPackages() {
		// TODO Auto-generated method stub
		return this.containers().nodes(XCSG.Package);
	}

	/**
	 * Gets all transitively contained interfaces
	 * 
	 * @return
	 */
	public W getInterfaces() {
		// TODO Auto-generated method stub
		return this.contained().nodes(XCSG.Java.Interface);
	}
	
	
	
	
	
	/*
	 * IDE utilities 
	 * 
	 */
	
	/**
	 * shows the current query in a graph editor in the IDE
	 * 
	 * @return
	 */
	public W show() {
		DisplayUtil.displayGraph(thisQ.eval());
		return this;
		
	}
	
	
	/**
	 * 
	 * Marks the nodes in this in the IDE with the given message
	 * 
	 * @param message
	 * @return
	 */
	public W mark(String message) {
		StreamSupport.stream(this.eval().nodes().spliterator(), true).forEach(node -> {
			String name = (String) node.getAttr(XCSG.name);
			//BaseNodeId id = (BaseNodeId) node.getAttr(XCSG.id);
			
			SourceCorrespondence sc = (SourceCorrespondence) node.getAttr(XCSG.sourceCorrespondence);
			if(sc == null) {return;}
			if(sc.sourceFile == null) {return;}
			//https://www.linuxtopia.org/online_books/eclipse_documentation/eclipse_platform_plug-in_developer_guide/topic/org.eclipse.platform.doc.isv/guide/eclipse_platform_plugin_resAdv_markers.htm
			
			//https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Fconcepts%2Fconcepts-11.htm&cp%3D0_2_6
			try {
				IMarker marker = sc.sourceFile.createMarker(refactoringDangerMarker);
				//marker.setAttribute(IMarker.LINE_NUMBER,1);
				marker.setAttribute(IMarker.CHAR_START, sc.offset);
				marker.setAttribute(IMarker.CHAR_END, sc.offset + sc.length);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				marker.setAttribute(IMarker.MESSAGE, message);
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				marker.setAttribute(IMarker.LOCATION, name);
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			});
		
		return this;
		
		
	}
			
	/**
	 * Deletes any marker for the given nodes
	 * 
	 * 
	 * @return
	 */
	public W deleteMarkers() {
		StreamSupport.stream(this.eval().nodes().spliterator(), true).forEach(node -> {
			
			SourceCorrespondence sc = (SourceCorrespondence) node.getAttr(XCSG.sourceCorrespondence);
			
			if(sc == null) {return;}
			if(sc.sourceFile == null) {return;}
			
			
			try {
				IMarker[] ms= sc.sourceFile.findMarkers(refactoringDangerMarker,false,IResource.DEPTH_ONE);
				for(IMarker marker : ms) {
					int start = (int )marker.getAttribute(IMarker.CHAR_START);
					int end = (int) marker.getAttribute(IMarker.CHAR_END);
					if(start == sc.offset && end == sc.offset + sc.length) {
						marker.delete();
						
					}
					
				}
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		});
		
		return this;
		
		
	}
	
	public static void clearAllMarkers() {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			root.deleteMarkers(refactoringDangerMarker, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	




	/**
	 * Returns the dataflow graph in the given direction, includes the origin nodes
	 * 
	 * @param direction
	 * @return
	 */
	public W dataflow(TraversalDirection direction) {
		
		return new W(CommonQueries.data(thisQ, direction));
	}
	

	public W getDataflow(TraversalDirection direction) {
		// TODO Auto-generated method stub
		if(direction == TraversalDirection.FORWARD) {
			return this.descendantsOn(W.U().edges(XCSG.DataFlow_Edge));
			
			
		} else if(direction == TraversalDirection.REVERSE) {
			return this.anchestorsOn(W.U().edges(XCSG.DataFlow_Edge));
			
		} else if(direction == TraversalDirection.BIDIRECTIONAL) {
			return this.anchestorsOn(W.U().edges(XCSG.DataFlow_Edge)).union(this.descendantsOn(W.U().edges(XCSG.DataFlow_Edge)));
			
		} else return empty();
		
	}
	
		
	
	
	private Q methodsWithParametersB( Q methodsToMatchFrom, List<String> parameterTypesSource ) {
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
	 * Helper function to aid with wrapping Q
	 * 
	 * @param expr
	 * @return
	 */
	private Q[] QLarray(W... expr) {
		 Q[] e = new Q[expr.length];
			for(int i = 0; i< expr.length;i++) {
				e[i] = expr[i].Q();
				
			}
			return e;
	}
	
	/**
	 *
	 * applies the "relative" query to the given functions, and returns an array of the resulting queries
	 * 
	 * @param expr
	 * @return
	 */
	private W[] QLFunctionArrayToQLArray(W relative , UnaryOperator<W> ... fn ) {
		 W[] e = new W[fn.length];
			for(int i = 0; i< fn.length;i++) {
				e[i] = fn[i].apply(relative);
				
			}
			return e;
	}
	
	
	

	



	
	

	
	

	
	public W selectNodesStartingWith(String... name) {
		Q res = Query.empty();
		
		for(int i = 0 ; i<name.length; i++) {
			
			res =res.union(CommonQueries.nodesStartingWith(thisQ, name[i]));
		
		}
		
		
		return new W(res);
		
		
	}
	
	/**
	 * Returns a Set of the names of the elements in this, no duplicates
	 * 
	 * @return
	 */
	public Set<String> getNamesUnique() {
		Set<String> names = new HashSet<String>();
		
		this.getAttributeValuesAsMap(XCSG.name).forEach(n -> names.add((String)n ));
		
		return names;
		
	}
	
	/**
	 * Returns a list of the names of the elements in this.
	 * No assumptions on order should be made.
	 * 
	 * @return
	 */
	public List<String> getNames() {
		List<String> names = new ArrayList<String>();
		this.getAttributeValuesAsMap(XCSG.name).forEach(n -> names.add((String)n ));
		return names;
		
		
	}
	
	public List<String> getAssignmentNames() {
		List<String> names = new ArrayList<String>();
		this.getAttributeValuesAsMap(XCSG.name).forEach(n -> names.add(((String)n).replace("=", "") ));
		
		return names;
		
	}
	
	/**
	 * Gets the selected element in the IDE
	 * Only useful when a single element(method, class, variable) is selected, otherwise unexpected results are returned
	 * 
	 * 
	 * @return
	 */
	public static W singleSelection() {
		return new W(SelectionUtil.getLastSelectionEvent().getSelection());
		
	}
	
	
	
	
	

}



