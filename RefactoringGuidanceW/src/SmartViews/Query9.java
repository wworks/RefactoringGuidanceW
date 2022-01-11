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
 * SmartView for query 9
 */
public class Query9 extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Query 9: Methods which are the first implementations of the selected abstract method";
	}
	
	@Override
	public String[] getSupportedNodeTags() {
		return new String[]{XCSG.Method, 
				};
	}

	@Override
	public String[] getSupportedEdgeTags() {
		return FilteringAtlasSmartViewScript.NOTHING;
	}

	@Override
	public StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
						
		QueryLibrary QL = new QueryLibrary();
		Q res = QL.query9B(filteredSelection);
		
		// return the styled result for display
		return new StyledResult(res, new Markup());
	}
	
}