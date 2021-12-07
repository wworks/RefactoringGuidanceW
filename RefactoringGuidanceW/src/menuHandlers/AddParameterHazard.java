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




public class AddParameterHazard extends AbstractHandler {
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		//DisplayUtil.displayGraph((new QueryLibrary()).methodsWithParametersC(Query.universe().project("Query2").contained().nodes(XCSG.Method), Arrays.asList("String")).eval());
		
//		Q method = Query.universe().methods("myUsefulMethod");
//
//		//all parents of the method
//		Q visualization = Query.universe().edgesTaggedWithAny(XCSG.Contains).reverse(method);
//		//the parameters of the method
//		Q parameters = Query.universe().edgesTaggedWithAny(XCSG.HasParameter).forward(method);
//		//types of the parameters
//		Q parameterTypes = Query.universe().forwardStep(CommonQueries.methodParameter(method)).nodes(XCSG.Type);
//		//combine all information in one graph, and add edges between the parameters and their types
//		visualization = visualization.union(parameters, parameterTypes).induce(Query.universe().edges(XCSG.TypeOf));
//
//		//display the result to the user, using the SDK
//		DisplayUtil.displayGraph(visualization.eval());
		
		
		W selection = W.toW(SelectionUtil.getLastSelectionEvent().getSelection().eval());
		
		W methods = selection.nodes(XCSG.Method);
		
		W methodDestination = W.empty();
		if(methods.nodeSize() == 1) {
			methodDestination = methods;
			
			
		} else {
			DisplayUtils.showMessage("Incorrect selection, please select a method");
			return null;
			
		}
		
				
//		String visibility = DisplayUtils.promptString("Visibility", "Please give the method visisbility(Public,Package,Private,Protected)");
//		String methodName = DisplayUtils.promptString("Method name", "Please give the method name");
//		String parameters = DisplayUtils.promptString("Parameters", "Please give the method parameter types, comma seperated");
//		String returnType = DisplayUtils.promptString("Return type", "Please give the method return type");
		
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

