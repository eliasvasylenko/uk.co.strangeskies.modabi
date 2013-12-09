package uk.co.strangeskies.modabi.processing;

public class QualifiedName {
	private final String name;
	private final Namespace namespace;

	public QualifiedName(String name, Namespace namespace) {
		this.name = name;
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	@Override
	public String toString() {
		return namespace + name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QualifiedName))
			return false;

		return namespace.equals(((QualifiedName) obj).namespace)
				&& name.equals(((QualifiedName) obj).name);
	}
}
