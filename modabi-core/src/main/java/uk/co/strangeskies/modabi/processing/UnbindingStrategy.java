package uk.co.strangeskies.modabi.processing;

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
	 * The object which is being unbound should be passed as a parameter to the
	 * unbinding method, invoked on the provided instance of the unbinding class.
	 */
	PROVIDED,

	/**
	 * The schema binder should attempt to find a constructor to call on the
	 * requested unbinding class. The object being unbound should be passed to the
	 * constructor as a parameter. No unbinding method should be specified.
	 */
	CONSTRUCTOR,

	/**
	 * The schema binder should attempt to find the static factory method to call
	 * on the requested unbinding class named by the unbinding method. The object
	 * being unbound should be passed to the factory method as a parameter.
	 */
	STATIC_FACTORY;
}
