package com.ensoftcorp.demo;

import java.awt.Color;

import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;

/**
 * Stock example Atlas SmartView 
 * 
 * For a selected node, displays the immediate type and the basis of that type.
 */
public class TypeOfSmartView extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "TypeOf";
	}
	
	@Override
	public String[] getSupportedNodeTags() {
		return new String[]{XCSG.DataFlow_Node, 
				XCSG.Variable,
				XCSG.Type, 
				XCSG.TypeAlias};
	}

	@Override
	public String[] getSupportedEdgeTags() {
		return FilteringAtlasSmartViewScript.NOTHING;
	}

	@Override
	public StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
		
		// retrieve all possible edges that can be connected to a type
		Q typeEdges = Common.codemap().edges(XCSG.TypeOf, // from Variable or DataFlow_Node to a Type
				XCSG.ArrayElementType, // arrays have an ArrayElementType
				XCSG.ReferencedType, // pointers have a ReferencedType
				XCSG.AliasedType,  // typedefs have an AliasedType
				XCSG.C.CompletedBy // OpaqueTypes are connected to the corresponding Type 
				);
		
		// walk forwardly on these type edges from the selected nodes
		Q res = filteredSelection.forwardOn(typeEdges);
		
		Markup m = new Markup();
		// color the type edges with blue
		m.setEdge(typeEdges, MarkupProperty.EDGE_COLOR, Color.BLUE);
		
		// return the styled result for display
		return new StyledResult(res, m);
	}
	
}