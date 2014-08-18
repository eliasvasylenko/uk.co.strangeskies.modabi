package uk.co.strangeskies.modabi.namespace;

public class QualifiedName {
	private final String name;
	private final Namespace namespace;

	public QualifiedName(String name, Namespace namespace) {
		this.name = name;
		this.namespace = namespace;
	}

	public QualifiedName(String name) {
		this(name, Namespace.getDefault());
	}

	public QualifiedName(String name, String namespace) {
		this(name, new Namespace(namespace));
	}

	public String getName() {
		return name;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	@Override
	public String toString() {
		return namespace + ":" + name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QualifiedName))
			return false;

		return namespace.equals(((QualifiedName) obj).namespace)
				&& name.equals(((QualifiedName) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ namespace.hashCode();
	}
}
