package uk.co.strangeskies.modabi;

import uk.co.strangeskies.utilities.Self;

/**
 * A general interface describing a system with a hierarchical scope for
 * visibility of the contents of that system. Child scopes have visibility over
 * everything visible to their parents, but parents do not have visibility over
 * the contents of their children.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 */
public interface Scoped<T extends Scoped<T>> extends Self<T> {
	/**
	 * @return the parent scope if one exists, otherwise null
	 */
	T getParentScope();

	/**
	 * Collapse this scope into its parent. This will result in the contents of
	 * this scope becoming visible to the parent scope, and all the rest of that
	 * scope's children.
	 * 
	 * @throws NullPointerException
	 *           if the parent scope doesn't exist
	 */
	void collapseIntoParentScope();

	/**
	 * @return a new child scope, with the receiver as its parent
	 */
	T deriveChildScope();
}
