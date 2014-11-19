package uk.co.strangeskies.modabi.schema.processing.unbinding;

public enum UnbindingStrategy {
	/**
	 * The schema binder should attempt to retrieve an reference to an instance of
	 * the data class directly from the result of the output method. No unbinding
	 * class or unbinding method should be specified. This is the default
	 * unbinding behaviour.
	 */
	SIMPLE,

	/**
	 * The schema binder should attempt to find an implementation of the unbinding
	 * class for which a factory has been provided externally, for example
	 * programmatically, through dependency injection, or as an OSGI service.
	 *
	 * The provided object should be passed as a parameter to the unbinding
	 * method, invoked on the object being unbound through the output method.
	 * 
	 * Any return value from the unbinding method will be ignored, and the
	 * provided object will be used as the output target for child nodes.
	 */
	ACCEPT_PROVIDED,

	/**
	 * The schema binder should attempt to find an implementation of the unbinding
	 * class for which a factory has been provided externally, for example
	 * programmatically, through dependency injection, or as an OSGI service.
	 *
	 * The object which is being unbound through the output method should be
	 * passed as a parameter to the unbinding method, invoked on the provided
	 * instance of the unbinding class.
	 * 
	 * Any return value from the unbinding method will be ignored, and the
	 * provided object will be used as the output target for child nodes.
	 */
	PASS_TO_PROVIDED,

	/**
	 * The schema binder should attempt to find an implementation of the unbinding
	 * class for which a factory has been provided externally, for example
	 * programmatically, through dependency injection, or as an OSGI service.
	 *
	 * The object which is being unbound through the output method should be
	 * passed as a parameter to the unbinding method, invoked on the provided
	 * instance of the unbinding class.
	 * 
	 * The return value from the unbinding method will be used as the output
	 * target for child nodes, and should be of the type of the unbinding class,
	 * if given, else the data class.
	 */
	PROVIDED_FACTORY,

	/**
	 * The schema binder should attempt to find the static factory method to call
	 * on the requested unbinding class named by the unbinding method. The object
	 * being unbound should be passed to the factory method as a parameter.
	 * 
	 * The class of the static method should be provided through
	 * staticFactoryClass, else is assumed to be the unbinding class.
	 */
	STATIC_FACTORY,

	/**
	 * The schema binder should attempt to find a constructor to call on the
	 * requested unbinding class. The object being unbound should be passed to the
	 * constructor as a parameter. No unbinding method should be specified.
	 * 
	 * The constructed object will be used as the output target for child nodes.
	 */
	CONSTRUCTOR;
}
