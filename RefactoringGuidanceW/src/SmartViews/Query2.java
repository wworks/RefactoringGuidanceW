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
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;

import RefactoringDangersQ.QueryLibrary;

/**
 * For a selected node, displays the immediate type and the basis of that type.
 */
public class Query2 extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Query 2: Concrete subclasses of abstract class with no implementation of abstract method";
	}
	
	@Override
	public String[] getSupportedNodeTags() {
		return new String[]{XCSG.Java.AbstractClass, 
				};
	}

	@Override
	public String[] getSupportedEdgeTags() {
		return FilteringAtlasSmartViewScript.NOTHING;
	}

	@Override
	public StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
		
		String visibility = DisplayUtils.promptString("Visibility", "Please give the method visisbility(Public,Package,Private,Protected)");
		String methodName = DisplayUtils.promptString("Method name", "Please give the method name");
		String parameters = DisplayUtils.promptString("Parameters", "Please give the method parameter types, comma seperated");
		String returnType = DisplayUtils.promptString("Return type", "Please give the method return type");
		
		Q res = filteredSelection;
	
		res = CommonQueries.typeHierarchy(filteredSelection, TraversalDirection.REVERSE);
		Markup m = new Markup();
		
		//m.set(res, MarkupProperty.NODE_BORDER_STYLE, MarkupProperty.LineStyle.DASHED);

		
		if (methodName != null && parameters != null) {
			List<String> parameterTypes = Arrays.asList();
			
			
			if(!parameters.trim().isEmpty()) {
				 parameterTypes = Arrays.asList(parameters.split(",", 0));
				
				
			}
			
			QueryLibrary QL = new QueryLibrary();
			//Q cscwi = QL.concreteSubClassesWithoutImplementation2(filteredSelection, methodName, parameterTypes);
			Q cscwi = QL.concreteSubClassesWithoutImplementation3(filteredSelection, visibility , methodName, parameterTypes , Query.universe().types(returnType));
			
			
			m.set(cscwi, MarkupProperty.NODE_BACKGROUND_COLOR, Color.RED);
			m.set(res.difference(cscwi), MarkupProperty.NODE_BACKGROUND_COLOR, Color.GRAY);
			
			res = res.union(cscwi).induce(Query.universe().edges(XCSG.Java.Extends));
			
		} else {
			m.set(res, MarkupProperty.NODE_BACKGROUND_COLOR, Color.BLACK);
			
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
		
		
		// color the type edges with blue
		//m.setEdge(typeEdges, MarkupProperty.EDGE_COLOR, Color.BLUE);

		
		// return the styled result for display
		return new StyledResult(res, m);
	}
	
}