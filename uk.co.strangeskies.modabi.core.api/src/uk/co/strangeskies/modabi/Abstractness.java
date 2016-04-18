package uk.co.strangeskies.modabi;

public enum Abstractness {
	/**
	 * The type is inferred, all input / output / binding / unbinding methods are
	 * resolved, and the unspecified properties of the type are instantiated with
	 * defaults.
	 */
	FINAL,

	/**
	 * The type is inferred, all input / output / binding / unbinding methods are
	 * resolved, and the unspecified properties of the type are instantiated with
	 * defaults.
	 */
	CONCRETE,

	/**
	 * The type is inferred, all input / output / binding / unbinding methods are
	 * resolved, and the unspecified properties of the type are instantiated with
	 * defaults.
	 */
	UNINFERRED,

	EXTENSIBLE,

	/**
	 * As with {@link #ABSTRACT}, except input / output methods are resolved
	 */
	RESOLVED,

	/**
	 * The node is completely abstract,
	 */
	ABSTRACT;

	public boolean isAtLeast(Abstractness abstractness) {
		return this.ordinal() >= abstractness.ordinal();
	}

	public boolean isAtMost(Abstractness abstractness) {
		return this.ordinal() <= abstractness.ordinal();
	}

	public boolean isLessThan(Abstractness abstractness) {
		return this.ordinal() < abstractness.ordinal();
	}

	public boolean isMoreThan(Abstractness abstractness) {
		return this.ordinal() > abstractness.ordinal();
	}
}
