package SmartViews;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;

import RefactoringDangersQ.QueryLibrary;

/**
 * SmartView for query 1.
 */
public class Query1 extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Query 1: method with signature in selected type";
	}
	
	@Override
	public String[] getSupportedNodeTags() {
		return new String[]{XCSG.Type, 
				};
	}

	@Override
	public String[] getSupportedEdgeTags() {
		return FilteringAtlasSmartViewScript.NOTHING;
	}

	@Override
	public StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
		String methodName = DisplayUtils.promptString("Method name", "Please give the method name");
		String parameters = DisplayUtils.promptString("Parameters", "Please give the method parameter types, comma seperated");
		
		Q res = Query.empty();
		
		if (methodName != null && parameters != null) {
		
			List<String> parameterTypes = Arrays.asList();
			
			
			if(!parameters.trim().isEmpty()) {
				 parameterTypes = Arrays.asList(parameters.split(",", 0));
				
				
			}
			
			
			QueryLibrary QL = new QueryLibrary();
			Q methodsWithSignatureInType = QL.methodWithSignatureInType2(filteredSelection, methodName , parameterTypes);
			
			res = methodsWithSignatureInType;
		}
		

		
		// retrieve all possible edges that can be connected to a type
//		Q typeEdges = Common.codemap().edges(XCSG.TypeOf, // from Variable or DataFlow_Node to a Type
//				XCSG.ArrayElementType, // arrays have an ArrayElementType
//				XCSG.ReferencedType, // pointers have a ReferencedType
//				XCSG.AliasedType,  // typedefs have an AliasedType
//				XCSG.C.CompletedBy // OpaqueTypes are connected to the corresponding Type 
//				);
		
		// walk forwardly on these type edges from the selected nodes
		//Q res = filteredSelection.forwardOn(typeEdges);
		
		Markup m = new Markup();
		// color the type edges with blue
		//m.setEdge(typeEdges, MarkupProperty.EDGE_COLOR, Color.BLUE);
		
		// return the styled result for display
		return new StyledResult(res, m);
	}
	
}