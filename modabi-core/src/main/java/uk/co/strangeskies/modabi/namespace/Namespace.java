package uk.co.strangeskies.modabi.namespace;

import java.util.Arrays;
import java.util.List;

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

	public List<String> split() {
		return Arrays.asList(namespace.split("\\."));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Namespace))
			return false;

		return namespace.equals(((Namespace) obj).namespace);
	}

	@Override
	public int hashCode() {
		return namespace.hashCode();
	}

	public static Namespace getDefault() {
		return DEFAULT;
	}
}
