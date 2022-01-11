package com.ensoftcorp.demo;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;

/**
 * Stock example Atlas SmartView 
 * 
 * For a selected C structure node, displays the call relation between the functions 
 * accessing instances of the selected C structure type.
 */
public class CallGraphSmartView extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Call Graph (Reference Type)";
	}
	
	@Override
	public String[] getSupportedNodeTags() {
		return new String[]{XCSG.C.Struct};
	}
	
	@Override
	public String[] getSupportedEdgeTags() {
		return FilteringAtlasSmartViewScript.NOTHING;
	}

	@Override
	protected StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
		Q type = filteredSelection;
		
		// retrieve all possible edges that can be connected to a type
		Q typeEdges = Common.codemap().edges(XCSG.TypeOf, // from Variable or DataFlow_Node to a Type
				XCSG.ArrayElementType, // arrays have an ArrayElementType
				XCSG.ReferencedType, // pointers have a ReferencedType
				XCSG.AliasedType,  // typedefs have an AliasedType
				XCSG.C.CompletedBy // OpaqueTypes are connected to the corresponding Type 
				);
		
		// find all nodes connected through the type edges to the selected type.
		Q nodes = typeEdges.reverse(type);
		
		// find all nodes that contain these nodes.
		Q containers = nodes.containers();
		Q callEdges = Common.codemap().edges(XCSG.Call);
		
		// induce the call edges between the function nodes only.
		Q result = containers.nodes(XCSG.Function).induce(callEdges);
		
		// return the styled result for display
		return new StyledResult(result);
	}

}
