package com.ensoftcorp.demo;

import java.io.File;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.viewer.graph.SaveUtil;

public class CallGraphAtlasScript {

	public static void create() {

		// Check if the callgraphs directory exists, if not create one
		File dir = new File(System.getProperty("user.home"),"call-graphs");
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		// get all the call edge in the code map
		Q callEdgesQ = Common.codemap().edges(XCSG.Call);
		// get all the functions in the code map
		Q methodsQ = Common.codemap().nodes(XCSG.Function);
		// store all the functions in a list
		AtlasSet<Node> methods = methodsQ.eval().nodes();
		for(final Node method : methods){
			Q callGraphQ = Common.empty();
			Q methodQ = Common.toQ(method);

			// compute a one-step forward call graph
			Q forwardStepCallGraph = callEdgesQ.forwardStep(methodQ);
			callGraphQ = callGraphQ.union(forwardStepCallGraph);

			// compute a one-step reverse call graph
			Q backwardStepCallGraph = callEdgesQ.reverseStep(methodQ);
			
			// union the one-step forward and reverse call graphs
			callGraphQ = callGraphQ.union(backwardStepCallGraph);

			// include the file context for better viewing of the call graph
			Graph callGraph = Common.extend(callGraphQ, XCSG.Contains).eval();

			// mark the current method with CORAL color
			Markup markup = new Markup();
			markup.setNode(methodQ, MarkupProperty.NODE_BACKGROUND_COLOR, MarkupProperty.Colors.CORAL);

			// export the call graph a JPG image
			File callGraphFile = new File(dir, method.getAttr(XCSG.name).toString() + ".jpg");
			SaveUtil.saveGraph(callGraphFile, callGraph, markup);
		}
	}
}
