package uk.co.strangeskies.modabi;

public interface SchemaManagerScope extends SchemaManager {
	/**
	 * Collapse scope into parent schema manager. This means all bindings, schema,
	 * provisions etc. in this scope will be shunted up to the parent scope.
	 */
	void collapseScope();
}
