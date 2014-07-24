package uk.co.strangeskies.modabi.schema.processing;

import uk.co.strangeskies.modabi.schema.Schema;

/**
 * <p>
 * This enumeration describes the different ways a {@link Schema} can request a
 * {@link SchemaBinder} should provide implementations of classes and interfaces
 * to bind to. All binding strategies can be applied when binding to concrete
 * classes, abstract classes, or interfaces, unless otherwise specified.
 * </p>
 *
 * @author eli
 *
 */
public enum BindingStrategy {
	/**
	 * The schema binder should attempt to find an implementation for which a
	 * factory has been provided externally, for example programmatically, through
	 * dependency injection, or as an OSGI service. This is the default binding
	 * behaviour.
	 */
	PROVIDED,

	/**
	 * The schema binder should attempt to create a simple proxy implementation of
	 * an interface.
	 *
	 * This binding strategy is only valid when binding to interfaces.
	 */
	IMPLEMENT_IN_PLACE,

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
	 * on the requested binding class, named by the input method of the first
	 * child nodes. This child node must be an input node, though it may be an
	 * empty input sequence, and any data it binds should be passed to the factory
	 * method as parameters.
	 */
	STATIC_FACTORY,

	/**
	 * The schema binder should attempt to retrieve an implementation of the
	 * requested class from the result of the first of any child nodes. This child
	 * must be an input node binding to a single class. No in method name should
	 * be specified on this child node.
	 */
	SOURCE_ADAPTOR,

	/**
	 * The schema binder should attempt to retrieve an implementation of the
	 * requested class from the object being bound by the parent node. This is
	 * useful when the parent object is a factory implementation for the child
	 * object being bound. It is possible that objects produced from the parent
	 * don't need to be 'added' after, so it is optional to omit the'inMethod'
	 * property by setting it to 'void'.
	 *
	 * If a data node binds with this strategy and is set to resolve a provided
	 * value at registration time, the object being bound by the parent node
	 * during processing will not be available yet. In this case, an instance of
	 * {@link RegistrationTimeTargetAdapter} will be provided, so that the
	 * configurator stack can be reflected upon.
	 */
	TARGET_ADAPTOR;
}
