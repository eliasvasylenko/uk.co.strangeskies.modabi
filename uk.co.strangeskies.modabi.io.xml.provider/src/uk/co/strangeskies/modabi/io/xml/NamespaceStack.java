package uk.co.strangeskies.modabi.io.xml;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.NamespaceAliases;
import uk.co.strangeskies.modabi.QualifiedName;

class NamespaceStack implements NamespaceContext {
	private Namespace defaultNamespace;
	private NamespaceAliases aliasSet;

	private NamespaceStack next;

	public NamespaceStack() {
		aliasSet = new NamespaceAliases();
	}

	private NamespaceStack(NamespaceStack from) {
		setFrom(from);
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

	public void setDefaultNamespace(Namespace namespace) {
		defaultNamespace = namespace;
	}

	public Namespace getDefaultNamespace() {
		return defaultNamespace;
	}

	public String addNamespace(Namespace namespace) {
		String alias = getNamespaceAlias(namespace);
		if (alias != null)
			return alias;

		return aliasSet.addNamespace(namespace);
	}

	public Set<Namespace> getNamespaces() {
		return aliasSet.getNamespaces();
	}

	public String getNamespaceAlias(Namespace namespace) {
		if (namespace.equals(defaultNamespace))
			return XMLConstants.DEFAULT_NS_PREFIX;

		if (getNamespaces().contains(namespace))
			return aliasSet.getAlias(namespace);

		if (next != null)
			return next.getNamespaceAlias(namespace);

		return null;
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return aliasSet.getNamespace(prefix).toHttpString();
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
