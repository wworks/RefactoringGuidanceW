package menuHandlers;



import java.util.Arrays;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

import org.eclipse.core.commands.ExecutionException;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;
import WQL.W;


import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;

import RefactoringDangersQ.QueryLibrary;

/**
 * 
 * Handles a click on the menu item for the add parameter analysis, analysis as specified by the hazardous code patterns. Gathers user input and runs analyses.
 *
 */
public class AddParameterHazard extends AbstractHandler {
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		W selection = W.toW(SelectionUtil.getLastSelectionEvent().getSelection().eval());
		
		W methods = selection.nodes(XCSG.Method);
		
		W methodDestination = W.empty();
		if(methods.nodeSize() == 1) {
			methodDestination = methods;
					
		} else {
			DisplayUtils.showMessage("Incorrect selection, please select a method");
			return null;
			
		}
				
		String parameterName = DisplayUtils.promptString("Parameter name", "Please give the parameter name");
		String parameterType = DisplayUtils.promptString("Parameter Type", "Please give the parameter type.");
		if (parameterName != null && parameterType != null ) {
			
			Hazards.Hazards.doAddParameterAnalysis(methodDestination, parameterType, parameterName);
			
		} else {
			
			DisplayUtils.showMessage("Incorrect input, please provide required details");
		}
		
		return null;
			
	}
}

