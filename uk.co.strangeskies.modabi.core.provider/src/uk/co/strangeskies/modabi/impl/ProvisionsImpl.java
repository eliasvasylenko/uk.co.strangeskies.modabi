/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl;

import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;
import uk.co.strangeskies.utilities.collection.ObservableSet;
import uk.co.strangeskies.utilities.collection.ScopedObservableSet;

public final class ProvisionsImpl extends ScopedObservableSet<Provisions, Provider> implements Provisions {
	private final ProvisionsImpl parent;

	protected ProvisionsImpl(ProvisionsImpl parent) {
		super(ObservableSet.over(new HashSet<Provider>()).synchronizedView());
		this.parent = parent;
	}

	public ProvisionsImpl() {
		this(null);
	}

	@Override
	public <T> TypedObject<T> provide(TypeToken<T> type, ProcessingContext state) {
		return typedObject(type,
				visiblePriovidersStream()
						.map(p -> p.provide(type, state))
						.filter(Objects::nonNull)
						.findFirst()
						.<ModabiException>orElseThrow(() -> new ProcessingException(t -> t.noProviderFound(type), state)));
	}

	@Override
	public boolean isProvided(TypeToken<?> type, ProcessingContext state) {
		return visiblePriovidersStream().map(p -> p.provide(type, state)).anyMatch(Objects::nonNull);
	}

	protected Stream<Provider> visiblePriovidersStream() {
		if (parent == null)
			return stream();
		else
			return Stream.concat(stream(), parent.visiblePriovidersStream());
	}

	@Override
	public Optional<Provisions> getParentScope() {
		return Optional.ofNullable(parent);
	}

	@Override
	public void collapseIntoParentScope() {
		Objects.requireNonNull(parent);
		parent.addAll(this);
		this.clear();
	}

	@Override
	public Provisions nestChildScope() {
		return new ProvisionsImpl(this);
	}

	@Override
	public ProvisionsImpl copy() {
		ProvisionsImpl parentCopy = parent != null ? parent.copy() : null;
		ProvisionsImpl copy = new ProvisionsImpl(parentCopy);
		copy.addAll(this);
		return copy;
	}
}
