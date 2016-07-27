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
package uk.co.strangeskies.modabi.impl.schema;

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class BindingChildNodeConfiguratorImpl<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T, N>, T>
		extends BindingNodeConfiguratorImpl<S, N, T> implements BindingChildNodeConfigurator<S, N, T> {
	private final SchemaNodeConfigurationContext context;

	private TypeToken<?> postInputClass;
	private Range<Integer> occurrences;
	private Boolean optional;
	private Boolean nullIfOmitted;
	private Boolean ordered;
	private Boolean outMethodIterable;
	private Boolean outMethodCast;
	private Boolean outMethodUnchecked;
	private String outMethodName;
	private String inMethodName;
	private Boolean inMethodChained;
	private Boolean inMethodCast;
	private Boolean inMethodUnchecked;
	private Boolean extensible;
	private Boolean synchronous;

	public BindingChildNodeConfiguratorImpl(SchemaNodeConfigurationContext parent) {
		this.context = parent;
	}

	@Override
	public final S nullIfOmitted(boolean nullIfOmitted) {
		this.nullIfOmitted = nullIfOmitted;

		return getThis();
	}

	public Boolean getNullIfOmitted() {
		return nullIfOmitted;
	}

	@Override
	public S synchronous(boolean synchronous) {
		this.synchronous = synchronous;

		return getThis();
	}

	public Boolean getSynchronous() {
		return synchronous;
	}

	protected final SchemaNodeConfigurationContext getContext() {
		return context;
	}

	@Override
	protected Namespace getNamespace() {
		return getName() != null ? getName().getNamespace() : getContext().namespace();
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().dataLoader();
	}

	@Override
	protected Imports getImports() {
		return getContext().imports();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingChildNodeConfigurator<?, ?, V> dataType(TypeToken<? extends V> dataClass) {
		return (BindingChildNodeConfigurator<?, ?, V>) super.dataType(dataClass);
	}

	@Override
	public final S occurrences(Range<Integer> range) {
		occurrences = range;

		return getThis();
	}

	public Range<Integer> getOccurrences() {
		return occurrences;
	}

	@Override
	public S optional(boolean optional) {
		this.optional = optional;

		return BindingChildNodeConfigurator.super.optional(optional);
	}

	@Override
	public Boolean getOptional() {
		return optional;
	}

	@Override
	public final S inMethod(String inMethodName) {
		if (!getContext().isInputExpected() && !inMethodName.equals("null"))
			throw new ModabiException(t -> t.cannotDefineInputInContext(getName()));

		this.inMethodName = inMethodName;

		return getThis();
	}

	public String getInMethod() {
		return inMethodName;
	}

	@Override
	public final S inMethodChained(boolean chained) {
		this.inMethodChained = chained;
		return getThis();
	}

	public Boolean getInMethodChained() {
		return inMethodChained;
	}

	@Override
	public final S inMethodCast(boolean allowInMethodResultCast) {
		this.inMethodCast = allowInMethodResultCast;

		return getThis();
	}

	public Boolean getInMethodCast() {
		return inMethodCast;
	}

	@Override
	public final S inMethodUnchecked(boolean unchecked) {
		inMethodUnchecked = unchecked;

		return getThis();
	}

	public Boolean getInMethodUnchecked() {
		return inMethodUnchecked;
	}

	@Override
	public final S outMethod(String outMethodName) {
		this.outMethodName = outMethodName;
		return getThis();
	}

	public String getOutMethod() {
		return outMethodName;
	}

	@Override
	public final S outMethodIterable(boolean iterable) {
		this.outMethodIterable = iterable;
		return getThis();
	}

	public Boolean getOutMethodIterable() {
		return outMethodIterable;
	}

	@Override
	public final S outMethodCast(boolean cast) {
		this.outMethodCast = cast;
		return getThis();
	}

	public Boolean getOutMethodCast() {
		return outMethodCast;
	}

	@Override
	public S outMethodUnchecked(boolean unchecked) {
		this.outMethodUnchecked = unchecked;
		return getThis();
	}

	public Boolean getOutMethodUnchecked() {
		return outMethodUnchecked;
	}

	@Override
	public final S extensible(boolean extensible) {
		this.extensible = extensible;

		return getThis();
	}

	public Boolean getExtensible() {
		return extensible;
	}

	@Override
	public final S ordered(boolean ordered) {
		this.ordered = ordered;

		return getThis();
	}

	public Boolean getOrdered() {
		return ordered;
	}

	public <U extends BindingChildNode<? super T, ?>> List<U> getOverriddenNodes(TypeToken<U> type) {
		return getName() == null ? Collections.emptyList() : getContext().overrideChild(getName(), type);
	}

	@Override
	protected final boolean isChildContextAbstract() {
		return super.isChildContextAbstract() || getContext().isAbstract() || extensible != null && extensible;
	}

	@Override
	public S postInputType(String postInputType) {
		return postInputType(parseTypeWithSubstitutedBrackets(postInputType, getImports()));
	}

	@Override
	public S postInputType(TypeToken<?> postInputClass) {
		this.postInputClass = postInputClass;

		return getThis();
	}

	public TypeToken<?> getPostInputType() {
		return postInputClass;
	}

	@Override
	protected TypeToken<?> getInputTargetForTargetAdapter() {
		return getContext().inputTargetType();
	}

	@Override
	public TypeToken<T> getExpectedType() {
		System.out.println(getOverride(BindingChildNode::inMethod, (Invokable<?, ?>) null).tryGet());

		System.out.println(getOverride(BindingChildNode::outMethod, (Invokable<?, ?>) null).tryGet());

		return null;
	}
}
