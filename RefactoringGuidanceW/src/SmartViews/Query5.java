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
 * SmartView for query 5
 */
public class Query5 extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Query 5: Potential callsites for the selected method";
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
		Q res = QL.query5(filteredSelection);
		
		// return the styled result for display
		return new StyledResult(res, new Markup());
	}
	
}