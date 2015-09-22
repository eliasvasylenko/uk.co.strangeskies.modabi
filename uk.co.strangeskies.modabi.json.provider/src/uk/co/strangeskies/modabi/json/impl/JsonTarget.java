/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.xml.provider.
 *
 * uk.co.strangeskies.modabi.xml.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.xml.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.xml.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.json.impl;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTargetImpl;

@Component(property = "format=json")
public class JsonTarget extends StructuredDataTargetImpl<JsonTarget> {
	@Override
	protected void registerDefaultNamespaceHintImpl(Namespace namespace) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void registerNamespaceHintImpl(Namespace namespace) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void commentImpl(String comment) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void nextChildImpl(QualifiedName name) {
		// TODO Auto-generated method stub

	}

	@Override
	protected DataTarget writePropertyImpl(QualifiedName name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DataTarget writeContentImpl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void endChildImpl() {
		// TODO Auto-generated method stub

	}
}
