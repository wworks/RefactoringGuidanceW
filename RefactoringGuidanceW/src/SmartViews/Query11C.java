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
import RefactoringDangersWQL.QueryLibraryWQL;
import WQL.W;

/**
 * SmartView for query 11C
 */
public class Query11C extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Query 11D: Updates in the selected segment which affect dataflow after the selected segment, and the affected dataflow";
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
		
		Markup m = new Markup();
		
		ISelection sel = SelectionUtil.getLastSelectionEvent().getWorkbenchSelection();
		if(sel instanceof org.eclipse.jface.text.ITextSelection) {
			ITextSelection texts = (ITextSelection) sel;
			
			Q methods =com.ensoftcorp.open.commons.analysis.CommonQueries.getContainingFunctions(filteredSelection);
			if(CommonQueries.nodeSize(methods) > 1) {
				
				DisplayUtils.showMessage("Selection must be within one method");
				return new StyledResult(res, new Markup());
			}
			
			Q segment = QL.getMethodSegmentB(texts.getOffset(), texts.getLength(), methods );
			Q updates = QL.query11B(segment,methods);
			Q affected = QL.query11(segment,methods);
			
			
			
			res = updates.union(affected).induce(Query.universe().edges(XCSG.DataFlow_Edge));
			m.set(updates, MarkupProperty.NODE_BACKGROUND_COLOR, Color.RED);
			
			Q variables = Common.toQ((new QueryLibraryWQL()).Query11D(W.toW(segment.eval()) ,W.toW(methods.eval())) .eval());
			res = variables;
		}
		
		// return the styled result for display
		return new StyledResult(res, m);
	}
	
}