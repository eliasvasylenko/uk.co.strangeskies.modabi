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

	public String toHttpString() {
		return namespace.toHttpString() + name;
	}

	public static QualifiedName parseString(String string) {
		int splitIndex = string.lastIndexOf(':');

		return new QualifiedName(string.substring(splitIndex + 1),
				Namespace.parseString(string.substring(0, splitIndex)));
	}

	public static QualifiedName parseHttpString(String string) {
		int splitIndex = string.lastIndexOf('/');

		return new QualifiedName(string.substring(splitIndex + 1),
				Namespace.parseHttpString(string.substring(0, splitIndex + 1)));
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
