/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.json.provider.
 *
 * uk.co.strangeskies.modabi.json.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.json.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.json.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.json.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;

import net.minidev.json.JSONObject;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.NamespaceAliases;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTargetImpl;

public class JsonTarget extends StructuredDataTargetImpl<JsonTarget> {
	private final PrintWriter out;

	private final Deque<JSONObject> jsonObjectStack;

	private Namespace defaultNamespace;
	private final NamespaceAliases namespaces;

	private boolean skipRoot;

	public JsonTarget(OutputStream out) {
		this(out, false);
	}

	public JsonTarget(OutputStream out, boolean skipRoot) {
		this.out = new PrintWriter(out);

		jsonObjectStack = new ArrayDeque<>();
		jsonObjectStack.push(new JSONObject());

		namespaces = new NamespaceAliases();

		this.skipRoot = skipRoot;
	}

	@Override
	protected void registerDefaultNamespaceHintImpl(Namespace namespace) {
		if (defaultNamespace != null)
			namespaces.addNamespace(namespace);
		else
			defaultNamespace = namespace;
	}

	@Override
	protected void registerNamespaceHintImpl(Namespace namespace) {
		namespaces.addNamespace(namespace);
	}

	private String formatQualifiedName(QualifiedName name) {
		if (name.getNamespace().equals(defaultNamespace)) {
			return name.getName();
		} else {
			String alias = namespaces.addNamespace(name.getNamespace());

			return alias + "." + name.getName();
		}
	}

	@Override
	protected void commentImpl(String comment) {}

	@Override
	protected void nextChildImpl(QualifiedName name) {
		JSONObject object = new JSONObject();
		jsonObjectStack.peek().put(formatQualifiedName(name), object);
		jsonObjectStack.push(object);
	}

	@Override
	protected DataTarget writePropertyImpl(QualifiedName name) {
		return writePropertyImpl(formatQualifiedName(name));
	}

	@Override
	protected DataTarget writeContentImpl() {
		return writePropertyImpl("");
	}

	private DataTarget writePropertyImpl(String name) {
		return DataTarget.composeList(l -> {
			Object o = l;
			if (l.size() == 1)
				o = l.get(0);
			jsonObjectStack.peek().put(name, o);
		} , this::formatQualifiedName);
	}

	@Override
	protected void endChildImpl() {
		JSONObject object = jsonObjectStack.pop();
		if (jsonObjectStack.size() == 1) {
			if (!skipRoot) {
				object = jsonObjectStack.pop();
				object.put("", defaultNamespace.toString());
				for (Namespace namespace : namespaces.getNamespaces())
					object.put(namespaces.getAlias(namespace), namespace.toString());
			}

			try {
				object.writeJSONString(out);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
