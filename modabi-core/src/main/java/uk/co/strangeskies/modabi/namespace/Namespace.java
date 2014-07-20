package uk.co.strangeskies.modabi.namespace;

public class Namespace {
	private final String namespace;

	private final static Namespace DEFAULT = new Namespace("");

	public Namespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String toString() {
		return namespace;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Namespace))
			return false;

		return namespace.equals(((Namespace) obj).namespace);
	}

	public static Namespace getDefault() {
		return DEFAULT;
	}
}
