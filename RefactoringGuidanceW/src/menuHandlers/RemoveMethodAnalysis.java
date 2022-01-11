package menuHandlers;



import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;
import WQL.W;


import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;

import RefactoringDangersQ.QueryLibrary;
import RefactoringDangersWQL.QueryLibraryWQL;



/**
 * 
 * Handles a click on the menu item for the move segment analysis. Gathers user input and runs analyses.
 *
 */
public class RemoveMethodAnalysis extends AbstractHandler {
		
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		W.clearAllMarkers();
		
		W selection = W.toW(SelectionUtil.getLastSelectionEvent().getSelection().eval());
		
		W methods = selection.nodes(XCSG.Method);
		
		W methodDestination = W.empty();
		if(methods.nodeSize() == 1) {
			methodDestination = methods;
			
		} else {
			DisplayUtils.showMessage("Incorrect selection, please select a single method");
			return null;
			
		}
		QueryLibraryWQL queries = new QueryLibraryWQL();
				
		queries.Query5(methodDestination).mark("There still is a (potential) method call to the method to be removed");
		
		queries.Query6(methodDestination).mark("This concrete method is overridden by the method to be removed, possible behaviour change when method is removed, because of dynamic binding");
		
		queries.Query7(methodDestination).mark("This method overrides the method to be removed, dynamic binding will break");
		
		queries.Query8(methodDestination).mark("The method to be removed implements this method, removing the selected method means there is no mandatory implementation anymore");
		
		queries.Query9(methodDestination).mark("This is an implementation of the abstract method to be removed, dynamic binding will break if the implemented method is removed");
		
		return null;
		
	}
	

}

