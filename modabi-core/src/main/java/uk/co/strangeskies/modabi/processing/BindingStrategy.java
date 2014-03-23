package uk.co.strangeskies.modabi.processing;

import java.lang.reflect.Modifier;

import uk.co.strangeskies.modabi.Schema;

/**
 * <p>
 * This enumeration describes the different ways a {@link Schema} can request a
 * {@link SchemaBinder} should provide implementations of classes and interfaces
 * to bind to.
 * </p>
 *
 * @author eli
 *
 */
public enum BindingStrategy {
	/**
	 * The schema binder should attempt to create a simple proxy implementation of
	 * an interface.
	 */
	IN_PLACE(false, false, true),

	/**
	 * The schema binder should attempt to find an implementation for which a
	 * factory has been provided externally, for example programmatically, through
	 * dependency injection, or as an OSGI service.
	 */
	REQUIRE_PROVIDED(false, true, true),

	/**
	 * The schema binder should behave as if {@link #REQUIRE_PROVIDED} were
	 * selected where possible, and otherwise should fall back to
	 * {@link #IN_PLACE} behaviour.
	 */
	PREFER_PROVIDED(false, false, true),

	/**
	 * The schema binder should attempt to find a constructor to call on the
	 * requested class. The first of any child nodes must be an input node, though
	 * it may be an empty sequence, and any data it binds should be passed to the
	 * constructor as parameters. No in method name should be specified on this
	 * child node.
	 */
	CONSTRUCTOR(true, true, true),

	/**
	 * The schema binder should attempt to find the static factory method to call
	 * on the requested class named by the first of any child nodes. This child
	 * node must be an input node, though it may be an empty sequence, and any
	 * data it binds should be passed to the factory method as parameters.
	 */
	STATIC_FACTORY(true, true, true),

	/**
	 * The schema binder should attempt to retrieve an implementation of the
	 * requested class from the result of the first of any child nodes. This child
	 * must be an input node binding to a single class. No in method name should
	 * be specified on this child node.
	 */
	ADAPTOR(true, true, true);

	private final boolean validForClass;
	private final boolean validForAbstractClass;
	private final boolean validForInterface;

	BindingStrategy(boolean validForClass, boolean validForAbstractClass,
			boolean validForInterface) {
		this.validForClass = validForClass;
		this.validForAbstractClass = validForAbstractClass;
		this.validForInterface = validForInterface;
	}

	/**
	 * Returns <code>true</code> if the binding strategy is valid when binding to
	 * the given class, based on whether it is a concrete class, an abstract
	 * class, or an interface.
	 *
	 * @return <code>true</code> if the binding strategy is valid when binding to
	 *         the given class, <code>false</code> otherwise.
	 */
	public boolean isValidForClass(Class<?> clazz) {
		if (clazz.isInterface())
			return validForInterface;
		else if (Modifier.isAbstract(clazz.getModifiers()))
			return validForAbstractClass;
		return validForClass;
	}
}
