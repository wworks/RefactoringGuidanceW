package SmartViews;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

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
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;

import RefactoringDangersQ.QueryLibrary;

/**
 * For a selected node, displays the immediate type and the basis of that type.
 */
public class Query11 extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Query 11: Dataflow affected by the selected segment";
	}
	
	@Override
	public String[] getSupportedNodeTags() {
		return  FilteringAtlasSmartViewScript.EVERYTHING;
	}

	@Override
	public String[] getSupportedEdgeTags() {
		return FilteringAtlasSmartViewScript.NOTHING;
	}

	@Override
	public StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
						
		QueryLibrary QL = new QueryLibrary();
		
		Q res = Query.empty();
		
		ISelection sel = SelectionUtil.getLastSelectionEvent().getWorkbenchSelection();
		if(sel instanceof org.eclipse.jface.text.ITextSelection) {
			ITextSelection texts = (ITextSelection) sel;
			
			Q methods =com.ensoftcorp.open.commons.analysis.CommonQueries.getContainingFunctions(filteredSelection);
			if(CommonQueries.nodeSize(methods) > 1) {
				
				DisplayUtils.showMessage("Selection must be within one method");
				return new StyledResult(res, new Markup());
			}
			
			Q segment = QL.getMethodSegmentB(texts.getOffset(), texts.getLength(), methods );
			res = QL.query11(segment,methods);
			
		}
		
		// return the styled result for display
		return new StyledResult(res, new Markup());
	}
	
}