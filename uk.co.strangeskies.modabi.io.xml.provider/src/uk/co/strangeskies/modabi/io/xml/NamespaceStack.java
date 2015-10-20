package uk.co.strangeskies.modabi.io.xml;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.NamespaceAliases;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.utilities.Copyable;

class NamespaceStack implements NamespaceContext, Copyable<NamespaceStack> {
	private Namespace defaultNamespace;
	private NamespaceAliases aliasSet;

	private NamespaceStack next;

	public NamespaceStack() {
		aliasSet = new NamespaceAliases();
	}

	private NamespaceStack(NamespaceStack from) {
		setFrom(from);
	}

	@Override
	public NamespaceStack copy() {
		NamespaceStack copy = new NamespaceStack(this);

		copy.aliasSet = copy.aliasSet.copy();
		if (copy.next != null) {
			copy.next = copy.next.copy();
		}

		return copy;
	}

	private void setFrom(NamespaceStack from) {
		defaultNamespace = from.defaultNamespace;
		aliasSet = from.aliasSet;
		next = from.next;
	}

	public String getNameString(QualifiedName name) {
		if (defaultNamespace != null
				&& defaultNamespace.equals(name.getNamespace()))
			return name.getName();

		String alias = aliasSet.getAlias(name.getNamespace());
		if (alias != null)
			return alias + ":" + name.getName();

		if (!isBase())
			return next.getNameString(name);

		return name.toString();
	}

	public void push() {
		next = new NamespaceStack(this);
		aliasSet = new NamespaceAliases();
		defaultNamespace = null;
	}

	public void pop() {
		if (isBase())
			throw new AssertionError();

		setFrom(next);
	}

	public boolean isBase() {
		return next == null;
	}

	public boolean setDefaultNamespace(Namespace namespace) {
		if (!XMLConstants.DEFAULT_NS_PREFIX.equals(getNamespaceAlias(namespace))) {
			defaultNamespace = namespace;
			return true;
		} else {
			return false;
		}
	}

	public String addNamespace(Namespace namespace) {
		String alias = getNamespaceAlias(namespace);

		if (alias == null) {
			alias = aliasSet.addNamespace(namespace);
		}

		return alias;
	}

	public boolean addNamespace(Namespace namespace, String alias) {
		if (XMLConstants.DEFAULT_NS_PREFIX.equals(alias)) {
			return setDefaultNamespace(namespace);
		} else if (getNamespaceAlias(namespace) != null) {
			return false;
		} else {
			aliasSet.addNamespace(namespace);

			return true;
		}
	}

	public Set<Namespace> getNamespaces() {
		return aliasSet.getNamespaces();
	}

	public NamespaceAliases getAliasSet() {
		return aliasSet;
	}

	public String getNamespaceAlias(Namespace namespace) {
		if (namespace.equals(defaultNamespace)) {
			return XMLConstants.DEFAULT_NS_PREFIX;
		} else if (getNamespaces().contains(namespace)) {
			return aliasSet.getAlias(namespace);
		} else if (next != null) {
			return next.getNamespaceAlias(namespace);
		} else {
			return null;
		}
	}

	public String getDefaultNamespaceURI() {
		return getDefaultNamespace().toHttpString();
	}

	public Namespace getDefaultNamespace() {
		Namespace namespace = defaultNamespace;

		if (namespace == null && next != null) {
			namespace = next.getDefaultNamespace();
		}

		return namespace;
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return getNamespace(prefix).toHttpString();
	}

	public Namespace getNamespace(String prefix) {
		Namespace namespace;
		if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
			namespace = defaultNamespace;
		} else {
			namespace = aliasSet.getNamespace(prefix);
		}

		if (namespace == null && next != null) {
			namespace = next.getNamespace(prefix);
		}

		return namespace;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		return getNamespaceAlias(Namespace.parseHttpString(namespaceURI));
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		return Arrays.asList(getPrefix(namespaceURI)).iterator();
	}
}
