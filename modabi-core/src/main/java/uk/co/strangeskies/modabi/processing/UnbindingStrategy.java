package uk.co.strangeskies.modabi.processing;

public enum UnbindingStrategy {
	/**
	 * The schema binder should attempt to retrieve an implementation of the
	 * requested class from the result of the first of any child nodes. This child
	 * must be an input node binding to a single class. No in method name should
	 * be specified on this child node.
	 */
	SIMPLE,

	/**
	 * The schema binder should attempt to find an implementation for which a
	 * factory has been provided externally, for example programmatically, through
	 * dependency injection, or as an OSGI service.
	 *
	 * The object which is being unbound should be passed
	 */
	PROVIDED,

	/**
	 * The schema binder should attempt to find a constructor to call on the
	 * requested class. The first of any child nodes must be an input node, though
	 * it may be an empty sequence, and any data it binds should be passed to the
	 * constructor as parameters. No in method name should be specified on this
	 * child node.
	 *
	 * This binding strategy is only valid when binding to concrete classes.
	 */
	CONSTRUCTOR,

	/**
	 * The schema binder should attempt to find the static factory method to call
	 * on the requested class named by the first of any child nodes. This child
	 * node must be an input node, though it may be an empty sequence, and any
	 * data it binds should be passed to the factory method as parameters.
	 */
	STATIC_FACTORY;
}
