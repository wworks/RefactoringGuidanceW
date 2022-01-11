package menuHandlers;



import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;
import com.ensoftcorp.demo.Activator;

import WQL.W;


import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;

import RefactoringDangersQ.QueryLibrary;
import RefactoringDangersWQL.QueryLibraryWQL;



/**
 * 
 * Handles a click on the menu item for the move segment analysis. Gathers user input and runs analyses.
 *
 */
public class MoveSegmentAnalysis extends AbstractHandler {
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		W.clearAllMarkers();

		QueryLibraryWQL queries = new QueryLibraryWQL();
		
		// get selected text to find offset and length of selection
		ISelection sel = SelectionUtil.getLastSelectionEvent().getWorkbenchSelection();
		if(sel instanceof org.eclipse.jface.text.ITextSelection) {
			ITextSelection texts = (ITextSelection) sel;
			
			Q methods =com.ensoftcorp.open.commons.analysis.CommonQueries.getContainingFunctions(SelectionUtil.getLastSelectionEvent().getSelection());
			if(CommonQueries.nodeSize(methods) > 1) {
				
				DisplayUtils.showMessage("Selection must be within one method");
				return null;
			}
			
			W segment = queries.getMethodSegmentB(texts.getOffset(), texts.getLength(), W.toW(methods.eval()) );
						
			W containedMethod = segment.getContainingFunctions();
			
			W outsideVariables = queries.Query10(segment);
			
			if(outsideVariables.nodeSize() > 0) {
				DisplayUtils.showMessage("Segment relies on variables outside of segment, please inspect dataflow graph in the Refactoring Guidance window");
				Activator.RGUI.input(segment.dataflow(TraversalDirection.REVERSE).eval());
				
			} 
			
			queries.Query10(segment).leaves().mark("Segment refers to this variable outside of the segment, moving the segment may accidentally rebind references to the wrong variable");
			
			queries.Query10MS(segment, containedMethod, W.universe().types("before")).mark("The segment uses this variable, but it also exists as a field in the destination type");
			
			queries.Query11D(segment, containedMethod).mark("This update has effects in the method after the segment, the result should be returned, to not lose these updates");
						
			queries.Query12(segment).mark("This return will be removed from the containing method, changing control flow and behavior" );
			
		}
		
		return null;
	}

}

