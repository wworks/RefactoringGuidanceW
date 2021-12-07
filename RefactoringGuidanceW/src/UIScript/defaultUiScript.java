package UIScript;

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


/**
 * Shows input in window
 */
public class defaultUiScript extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Refactoring Guidance";
	}
	
	@Override
	public String[] getSupportedNodeTags() {
		return FilteringAtlasSmartViewScript.EVERYTHING;
	}

	@Override
	public String[] getSupportedEdgeTags() {
		return FilteringAtlasSmartViewScript.EVERYTHING;
	}

	@Override
	public StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
						
		
		Q res = filteredSelection;
		
		//  return the styled result for display
		return new StyledResult(res, new Markup());
	}
	
}