package SmartViews;

import java.awt.Color;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
 * SmartView for query 4
 */
public class Query4 extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Query 4: Methods in (in)direct (abstract) subclasses that will override the the given method when added to the selected class";
	}
	
	@Override
	public String[] getSupportedNodeTags() {
		return new String[]{XCSG.Java.Class, 
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
		
		Q res = Query.empty();
	

		Markup m = new Markup();

		if (methodName != null && parameters != null && visibility !=null && returnType != null) {
			List<String> parameterTypes = Arrays.asList();
			
			if(!parameters.trim().isEmpty()) {
				 parameterTypes = Arrays.asList(parameters.split(",", 0));
				
			}
			
			QueryLibrary QL = new QueryLibrary();
			Q overriden = QL.query4(filteredSelection, visibility, methodName, parameterTypes,Query.universe().types(returnType));
			res = overriden;
			
		} 
		
		// return the styled result for display
		return new StyledResult(res, m);
	}
	
}