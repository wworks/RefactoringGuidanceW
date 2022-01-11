package SmartViews;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

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

import com.ensoftcorp.open.commons.ui.components.*;
/**
 * SmartView for query 3
 */
public class Query3 extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Query 3: Concrete methods in (in)direct (abstract) superclasses that will be overriden when adding the given method to the selected class";
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
			Q overriden = QL.query3(filteredSelection, visibility, methodName, parameterTypes,Query.universe().types(returnType));
			res = overriden;
			
		} 
		
		// return the styled result for display
		return new StyledResult(res, m);
	}
	
}