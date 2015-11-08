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
package uk.co.strangeskies.modabi.io.structured;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.DataTargetDecorator;

public abstract class StructuredDataTargetImpl<S extends StructuredDataTargetImpl<S>>
		implements StructuredDataTarget {
	private StructuredDataState currentState;
	private Deque<Integer> index;

	public StructuredDataTargetImpl() {
		currentState = StructuredDataState.UNSTARTED;
		index = new ArrayDeque<>();
		index.push(0);
	}

	@SuppressWarnings("unchecked")
	protected S getThis() {
		return (S) this;
	}

	@Override
	public StructuredDataState currentState() {
		return currentState;
	}

	private void enterState(StructuredDataState exitState) {
		currentState = currentState.enterState(exitState);
	}

	@Override
	public S writeProperty(QualifiedName name,
			Function<DataTarget, DataTarget> targetOperation) {
		StructuredDataTarget.super.writeProperty(name, targetOperation);
		return getThis();
	}

	@Override
	public S writeContent(Function<DataTarget, DataTarget> targetOperation) {
		StructuredDataTarget.super.writeContent(targetOperation);
		return getThis();
	}

	@Override
	public S registerDefaultNamespaceHint(Namespace namespace) {
		currentState().assertValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START);
		registerDefaultNamespaceHintImpl(namespace);

		return getThis();
	}

	protected abstract void registerDefaultNamespaceHintImpl(Namespace namespace);

	@Override
	public S registerNamespaceHint(Namespace namespace) {
		if (currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START)) {
			registerNamespaceHintImpl(namespace);
		}

		return getThis();
	}

	protected abstract void registerNamespaceHintImpl(Namespace namespace);

	@Override
	public S comment(String comment) {
		currentState().assertValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START,
				StructuredDataState.POPULATED_ELEMENT);
		commentImpl(comment);

		return getThis();
	}

	protected abstract void commentImpl(String comment);

	@Override
	public S nextChild(QualifiedName name) {
		index.push(0);
		enterState(StructuredDataState.ELEMENT_START);
		nextChildImpl(name);

		return getThis();
	}

	protected abstract void nextChildImpl(QualifiedName name);

	@Override
	public DataTarget writeProperty(QualifiedName name) {
		enterState(StructuredDataState.PROPERTY);
		return new DataTargetDecorator(writePropertyImpl(name)) {
			@Override
			public void terminate() {
				super.terminate();
				enterState(StructuredDataState.ELEMENT_START);
			}
		};
	}

	protected abstract DataTarget writePropertyImpl(QualifiedName name);

	@Override
	public DataTarget writeContent() {
		enterState(StructuredDataState.CONTENT);
		return new DataTargetDecorator(writeContentImpl()) {
			@Override
			public void terminate() {
				super.terminate();
				enterState(StructuredDataState.ELEMENT_WITH_CONTENT);
			}
		};
	}

	protected abstract DataTarget writeContentImpl();

	@Override
	public S endChild() {
		index.pop();
		index.push(index.pop() + 1);
		if (index.size() == 1)
			enterState(StructuredDataState.FINISHED);
		else
			enterState(StructuredDataState.POPULATED_ELEMENT);
		endChildImpl();

		return getThis();
	}

	protected abstract void endChildImpl();

	@Override
	public List<Integer> index() {
		return new ArrayList<>(index);
	}
}
