package menuHandlers;



import java.util.Arrays;
import java.util.List;

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
import RefactoringDangersWQL.QueryLibraryWQL;




public class AddMethodAnalysis extends AbstractHandler {
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		W.clearAllMarkers();
		

		
	
		W selection = W.toW(SelectionUtil.getLastSelectionEvent().getSelection().eval());
		
		W classes = selection.nodes(XCSG.Java.Class,XCSG.Java.Interface);
		
		W classDestination = W.empty();
		if(classes.nodeSize() == 1) {
			classDestination = classes;
			
			
		} else {
			DisplayUtils.showMessage("Incorrect selection, please select a class");
			return null;
			
		}
		
		String visibility = DisplayUtils.promptString("Visibility", "Please give the method visisbility(Public,Package,Private,Protected)");
		String methodName = DisplayUtils.promptString("Method name", "Please give the method name");
		String parameters = DisplayUtils.promptString("Parameters", "Please give the method parameter types, comma seperated");
		String returnType = DisplayUtils.promptString("Return type", "Please give the method return type");

		if (methodName != null && parameters != null && visibility !=null && returnType != null) {
			List<String> parameterTypes = Arrays.asList();
			
			if(!parameters.trim().isEmpty()) {
				 parameterTypes = Arrays.asList(parameters.split(",", 0));
				
			}
			
			W returnTypeW = W.U().types(returnType);
			
			QueryLibraryWQL queries = new QueryLibraryWQL();
			
			queries.Query1(classDestination, methodName, parameterTypes).mark("Method signature already present");
			
			
			
			queries.Query2(classDestination.selectAbstractClasses(), methodName, parameterTypes).mark("Concrete subclass does not have an implementation for the given method signature");

			queries.Query3(classDestination.selectConcreteClasses(), visibility, methodName, parameterTypes,  returnTypeW).mark("Method will override this method, possibly changing behaviour");
			
			queries.Query4(classDestination.selectClasses(), visibility, methodName, parameterTypes, returnTypeW).mark("Method will be overridden by this method");
			
			
		} else {
			
			DisplayUtils.showMessage("Incorrect input, please provide required details");
		}
		
		
		
		
		
						
		
		return null;
		
		
		
		
	}
	

}

