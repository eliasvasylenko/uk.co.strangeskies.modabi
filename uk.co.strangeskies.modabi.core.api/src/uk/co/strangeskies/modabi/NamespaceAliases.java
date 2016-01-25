/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NamespaceAliases {
	private Namespace defaultNamespace;
	private final Map<Namespace, String> namespaceAliases;
	private final Map<String, Namespace> aliasedNamespaces;

	public NamespaceAliases() {
		namespaceAliases = new HashMap<>();
		aliasedNamespaces = new HashMap<>();
	}

	public NamespaceAliases(NamespaceAliases namespaceAliases) {
		this.defaultNamespace = namespaceAliases.defaultNamespace;
		this.namespaceAliases = new HashMap<>(namespaceAliases.namespaceAliases);
		this.aliasedNamespaces = new HashMap<>(namespaceAliases.aliasedNamespaces);
	}

	public String getNameString(QualifiedName name) {
		String alias = namespaceAliases.get(name.getNamespace());
		if (alias != null)
			return alias + ":" + name.getName();

		return name.toString();
	}

	private String generateAlias(Namespace namespace) {
		Set<String> existingAliases = new HashSet<>(namespaceAliases.values());

		String alias = "";
		do {
			alias += "a";
		} while (existingAliases.contains(alias));

		return alias;
	}

	public boolean addNamespace(Namespace namespace, String alias) {
		if (getAlias(namespace) != null)
			return false;

		namespaceAliases.put(namespace, alias);
		aliasedNamespaces.put(alias, namespace);

		return true;
	}

	public String addNamespace(Namespace namespace) {
		String alias = getAlias(namespace);

		if (alias == null) {
			alias = generateAlias(namespace);

			addNamespace(namespace, alias);
		}

		return alias;
	}

	public Set<Namespace> getNamespaces() {
		return namespaceAliases.keySet();
	}

	public String getAlias(Namespace namespace) {
		if (namespaceAliases.containsKey(namespace))
			return namespaceAliases.get(namespace);

		return null;
	}

	public Namespace getNamespace(String alias) {
		if (aliasedNamespaces.containsKey(alias))
			return aliasedNamespaces.get(alias);

		return null;
	}

	public NamespaceAliases copy() {
		return new NamespaceAliases(this);
	}

	public void setDefaultNamespace(Namespace namespace) {
		defaultNamespace = namespace;
	}

	public Namespace getDefaultNamespace() {
		return defaultNamespace;
	}
}
