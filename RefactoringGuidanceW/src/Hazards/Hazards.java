package Hazards;

import java.util.List;

import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import WQL.W;


/**
 * WQL queries for the 'hazardous code patterns' by Evert Verduin
 * Grouped per microstep. 
 * Not all have been implemented, most have not have been checked for correctness whether they detect the right thing.
 * 
 * @author Wernsen
 *
 */
public class Hazards {
	
	
	
	public static void doAddClassAnalysis(W packageDestination, String newClassName) {
		W.clearAllMarkers();
			
		//AC-H1
		packageDestination.getClasses(newClassName).mark("Class is already defined.");
				
		
	}
	
	public static void doRemoveClassAnalysis(W classToRemove) {
		W.clearAllMarkers();
		
		
		//RC-H1-1
		W.universe()
				.nodes(XCSG.Instantiation)
				.getTypesOf()
				.intersection(classToRemove)
				.mark("RC-H1-1: Class is still in use. Instantiations (keyword new) of a class will become invalid.");
		
		
		//RC-H1-2
		
		classToRemove
		.predecessorsOn(
				W.universe().edges(XCSG.TypeOf))
		.nodes(XCSG.Instantiation)
		.mark("RC-H1-2: Class is still in use. Object reference to class will become invalid.");
		
		
		//RC-H1-3
		classToRemove
		.getMethods()
		.getCalledAt()
		.mark("RC-H1-3: Class is still in use. Caller to a method in a class is still present. ");
		
		//RC-H2-1
		classToRemove.getSuperclasses().mark("RC-H2-1: Class is removed from the inheritance tree. This is a superclass of the class to remove");
		
		//RC-H2-2
		classToRemove.getSubclassesT().mark("RC-H2-2: Class is removed from the inheritance tree. Subclass still present");
		
		//RC-H3
		classToRemove
		.children()
		.nodesTaggedWithAll(XCSG.Java.Class, "static") //InnerClass != static nested class
		.getMethods()
		.nodes(XCSG.ClassMethod) // a static method is a class method
		.getCallers()
		.mark("RC-H3: Static method from Inner static class still referenced from ALL, and the outerclass is going to be removed.");
		
		//RC-H4
		classToRemove.selectAbstractClasses().mark("RC-H4: Class is removed and possible design pattern is broken, due to abstract class definition.");
			
		
	}
	
	public static void doAddAttributeAnalysis(W classDestination, String attributeToAdd) {
		W.clearAllMarkers();
		
		
		//AA-H1
		classDestination
		.getFields()
		.fields(attributeToAdd)
		.mark("AA-H1: Attribute already defined");
		
		//AA-H2
		classDestination
		.parent()
		.selectClasses()
		.getFields()
		.fields(attributeToAdd)
		.dataflow(TraversalDirection.REVERSE) //all assignments to the fields
		.intersection(
			classDestination
			.contained()
			.nodes(XCSG.InstanceVariableAssignment)
		).mark("AA-H2:  Inner-/outer class attribute redefinition ( Attribute from outer class is used in inner class and new attribute with same  name and type is added to inner class )");
		
		//AA-H3
		classDestination
		.getSuperclasses()
		.getFields()
		.fields(attributeToAdd)
		.dataflow(TraversalDirection.REVERSE) //all assignments to the fields
		.intersection(
			classDestination
			.contained()
			.nodes(XCSG.InstanceVariableAssignment)

		).mark("AA-H3: Inheritance redefined attribute in Subclass ( Attribute from superclass is used in a sub class and new attribute with same name and type is added to that sub class)");
		
		
		
	}
	
	public static void doRemoveAttributeAnalysis(W attributeToRemove){
		W.clearAllMarkers();
		
		
		//RA-H1
		attributeToRemove
		.dataflow(TraversalDirection.REVERSE)
		.intersection(
			attributeToRemove
			.parent()
			.contained()
			.nodes(XCSG.InstanceVariableAssignment)
		).mark("RA-H1: Attribute still in use.");
		
		
		
	}
	
	public static void doAddMethodAnalysis(W classDestination, String methodName, List<String> parameterTypes) {
		W.clearAllMarkers();
		
		
		//AM-H1
		classDestination
		.getMethods()
		.selectMethodsWithSignature(methodName,parameterTypes)
		.mark("AM-H1: ");
		
		//AM-H2
		W MethodSupCallNoKeyAM =    classDestination
				.getSuperclassesT()
				.getMethods()
				.selectMethodsWithSignature(methodName, parameterTypes)
				.getCalledAt()
				.differenceR( s -> s.selectCallSitesWithSuper());

		W CallSubToSiginSupNoKeyAM = 	classDestination
					.getSubclassesT()
					.getCallers()
					.intersection(MethodSupCallNoKeyAM.getContainingFunctions());
		CallSubToSiginSupNoKeyAM.mark("AM-H1: Inheritance Redefined Method in Subclass. caller from subclass calls Subclass method instead of superclass method, after adding a method to the subclass.");
		
		
		
	}
	
	public static void doRemoveMethodAnalysis(W methodToRemove) {
		W.clearAllMarkers();
		
		
		//RM-H1
		
		methodToRemove
		.getCalledAt()
		.mark("RM-H1: Method call to methodToRemove still exists, Callers to method will be incorrect");
		
		//RM-H3
		methodToRemove
		.getOverridesT()
		.intersection(
			methodToRemove
			.getContainingPackages()
			.getInterfaces()
			.getMethods()
			

		).nodes(XCSG.ClassMethod, XCSG.Java.defaultMethod )
		.mark("RM-H3: Method definition in an interface.");
	}
	
	public static void doAddInterfaceAnalysis(String interfaceToAdd) {
		W.clearAllMarkers();
		
		
		//AI-H1
		W.universe()
		.types(interfaceToAdd)
		.selectInterfaces()
		.mark("AI-H1: Interface already defined.");
		
	}
	
	public static void doRemoveInterfaceAnalysis(W interfaceToRemove) {
		W.clearAllMarkers();
		
		
		//RI-H1
		interfaceToRemove
		.getImplementedBy()
		.selectClasses()
		.mark("RI-H1: Interface still in use by a class.");
		
		
		//RI-H2
		interfaceToRemove
		.getImplementedBy()
		.selectInterfaces()
		.mark("RI-H2: Interface still in use by another interface.");
		
		//RI-H3
		W.universe()
		.selectInterfaces()
		.getMethods()
		.nodesTaggedWithAny(XCSG.ClassMethod, XCSG.Java.defaultMethod)
		.getCalledAt()
		.mark("RI-H3: Interface holds static/default implementations .(that are used)");
		
		

	}
	
	public static void doAddParameterAnalysis(W methodDestination, String type, String name) {
		W.clearAllMarkers();
		
		
		//AP-H1
		methodDestination.getCalledAt().mark("AP-H1: method calls to method became incorrect after adding a parameter. ");
		
		String oldSignature = (String) methodDestination.getAttributeValuesAsMap("##signature").iterator().next();
		
		String newSignature = oldSignature.replace(")", "") + ", " +  type + ")";
		
		System.out.println(newSignature);
		
		//AP-H2
		methodDestination
		.parent()
		.getMethods()
		.selectMethodsWithSignature( newSignature)
		.mark("AP-H2: Method already defined after adding a parameter.");

		//AP-H3
		methodDestination
		.parent()
		.getSuperclassesT()
		.getMethods()
		.selectMethodsWithSignature( newSignature)
		.getCalledAt()
		.differenceR(s-> s.selectCallSitesWithSuper())
		.intersection(
				methodDestination
			.parent()
			.getSubclassesT()
			.contained().nodes(XCSG.CallSite)
			
		).mark("AP-H3: caller from subclass calls Subclass method instead of superclass method, after adding a parameter to the method in the subclass");
		
		//AP-H5
		methodDestination
		.getParameters()
		.selectNodesNamed(name)
		.mark("AP-H5: Parameter of method with the same name is already present");
		
		//AP-H6
		
		W.universe().fields(name)
		.dataflow(TraversalDirection.BIDIRECTIONAL)
		.intersection(
				methodDestination
			.contained()
			
		).mark("AP-H6: Field with the same name is already in use in Method");
	}
	
	public static void doRemoveParameterAnalysis(W parameterToRemove, W method) {
		W.clearAllMarkers();
		
		
		//RP-H1
		method.getCalledAt().mark("RP-H1: callers to method became incorrect after removing a parameter.");
		
		String oldSignature = (String) method.getAttributeValuesAsMap("##signature").iterator().next();
		
		String newSignature = ""; //TODO do some string trickery;
		
		//RP-H2
		method
		.parent()
		.getMethods()
		.selectMethodsWithSignature(newSignature);
		
		//RP-H5
		parameterToRemove
		.getDataflow(TraversalDirection.FORWARD) //REVERSE would be interprocedural dataflow to method
		.intersection(
				method.contained() 
		).mark("RP-H5: Parameter still in use in Method.");
		
	}
	
}
