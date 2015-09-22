/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
	private final Map<Namespace, String> namespaceAliases;
	private final Map<String, Namespace> aliasedNamespaces;

	public NamespaceAliases() {
		namespaceAliases = new HashMap<>();
		aliasedNamespaces = new HashMap<>();
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

	public String addNamespace(Namespace namespace) {
		String alias = getAlias(namespace);
		if (alias != null)
			return alias;

		alias = generateAlias(namespace);
		namespaceAliases.put(namespace, alias);
		aliasedNamespaces.put(alias, namespace);

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
}
