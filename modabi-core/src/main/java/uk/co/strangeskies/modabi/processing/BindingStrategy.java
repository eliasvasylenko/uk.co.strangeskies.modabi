package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.Schema;

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
	 * The schema binder should attempt to create a simple proxy implementation of
	 * an interface.
	 *
	 * This binding strategy is only valid when binding to interfaces.
	 */
	IN_PLACE,

	/**
	 * The schema binder should attempt to find an implementation for which a
	 * factory has been provided externally, for example programmatically, through
	 * dependency injection, or as an OSGI service.
	 */
	REQUIRE_PROVIDED,

	/**
	 * The schema binder should behave as if {@link #REQUIRE_PROVIDED} were
	 * selected where possible, and otherwise should fall back to
	 * {@link #IN_PLACE} behaviour.
	 *
	 * This binding strategy is only valid when binding to interfaces.
	 */
	PREFER_PROVIDED,

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
	STATIC_FACTORY,

	/**
	 * The schema binder should attempt to retrieve an implementation of the
	 * requested class from the result of the first of any child nodes. This child
	 * must be an input node binding to a single class. No in method name should
	 * be specified on this child node.
	 */
	ADAPTOR;
}
