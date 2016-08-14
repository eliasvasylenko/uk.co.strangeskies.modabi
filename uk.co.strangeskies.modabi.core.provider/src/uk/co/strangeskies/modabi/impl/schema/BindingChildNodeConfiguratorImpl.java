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
import java.util.function.Function;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingChildNode.OutputMemberType;
import uk.co.strangeskies.modabi.schema.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputNode.InputMemberType;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class BindingChildNodeConfiguratorImpl<S extends BindingChildNodeConfigurator<S, N, T>, N extends BindingChildNode<T, N>, T>
		extends BindingNodeConfiguratorImpl<S, N, T>
		implements BindingChildNodeConfigurator<S, N, T>, InputNodeConfiguratorImpl<S, N> {
	private final SchemaNodeConfigurationContext context;

	private TypeToken<?> postInputClass;
	private Range<Integer> occurrences;
	private Boolean optional;
	private Boolean nullIfOmitted;
	private Boolean ordered;

	private Boolean iterableOutput;
	private Boolean castOutput;
	private Boolean uncheckedOutput;
	private OutputMemberType outputMemberType;
	private String outputMember;

	private String inputMember;
	private InputMemberType inputMemberType;
	private Boolean chainedInput;
	private Boolean castIntput;
	private Boolean uncheckedInput;
	private Boolean extensible;
	private Boolean synchronous;

	public BindingChildNodeConfiguratorImpl(SchemaNodeConfigurationContext parent) {
		this.context = parent;
	}

	public BindingChildNodeConfiguratorImpl(BindingChildNodeConfiguratorImpl<S, N, T> copy) {
		super(copy);

		this.context = copy.context;

		this.postInputClass = copy.postInputClass;
		this.occurrences = copy.occurrences;
		this.optional = copy.optional;
		this.nullIfOmitted = copy.nullIfOmitted;
		this.ordered = copy.ordered;

		this.iterableOutput = copy.iterableOutput;
		this.castOutput = copy.castOutput;
		this.uncheckedOutput = copy.uncheckedOutput;
		this.outputMemberType = copy.outputMemberType;
		this.outputMember = copy.outputMember;

		this.inputMember = copy.inputMember;
		this.inputMemberType = copy.inputMemberType;
		this.chainedInput = copy.chainedInput;
		this.castIntput = copy.castIntput;
		this.uncheckedInput = copy.uncheckedInput;
		this.extensible = copy.extensible;
		this.synchronous = copy.synchronous;
	}

	@Override
	public N create() {
		N node = super.create();

		getContext().addChildResult(node);

		return node;
	}

	@Override
	public final S nullIfOmitted(boolean nullIfOmitted) {
		this.nullIfOmitted = nullIfOmitted;

		return getThis();
	}

	@Override
	public Boolean getNullIfOmitted() {
		return nullIfOmitted;
	}

	@Override
	public S synchronous(boolean synchronous) {
		this.synchronous = synchronous;

		return getThis();
	}

	@Override
	public Boolean getSynchronous() {
		return synchronous;
	}

	@Override
	public final SchemaNodeConfigurationContext getContext() {
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

	@Override
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
	public final S inputMethod(String methodName) {
		checkInputAllowed();
		this.inputMember = methodName;
		this.inputMemberType = InputMemberType.METHOD;
		return getThis();
	}

	@Override
	public final S inputField(String fieldName) {
		checkInputAllowed();
		this.inputMember = fieldName;
		this.inputMemberType = InputMemberType.FIELD;
		return getThis();
	}

	@Override
	public S inputNone() {
		this.inputMemberType = InputMemberType.NONE;
		return getThis();
	}

	@Override
	public String getInputMember() {
		return inputMember;
	}

	@Override
	public InputMemberType getInputMemberType() {
		return inputMemberType;
	}

	@Override
	public final S chainedInput(boolean chained) {
		this.chainedInput = chained;
		return getThis();
	}

	@Override
	public Boolean getChainedInput() {
		return chainedInput;
	}

	@Override
	public final S castInput(boolean allowInMethodResultCast) {
		this.castIntput = allowInMethodResultCast;

		return getThis();
	}

	@Override
	public Boolean getCastInput() {
		return castIntput;
	}

	@Override
	public final S uncheckedInput(boolean unchecked) {
		uncheckedInput = unchecked;

		return getThis();
	}

	@Override
	public Boolean getUncheckedInput() {
		return uncheckedInput;
	}

	@Override
	public final S outputMethod(String methodName) {
		this.outputMember = methodName;
		this.outputMemberType = OutputMemberType.METHOD;
		return getThis();
	}

	@Override
	public S outputField(String fieldName) {
		this.outputMember = fieldName;
		this.outputMemberType = OutputMemberType.FIELD;
		return getThis();
	}

	@Override
	public S outputNone() {
		this.outputMember = null;
		this.outputMemberType = OutputMemberType.NONE;
		return getThis();
	}

	@Override
	public S outputSelf() {
		this.outputMember = null;
		this.outputMemberType = OutputMemberType.SELF;
		return getThis();
	}

	@Override
	public String getOutputMember() {
		return outputMember;
	}

	@Override
	public OutputMemberType getOutputMemberType() {
		return outputMemberType;
	}

	@Override
	public final S iterableOutput(boolean iterable) {
		this.iterableOutput = iterable;
		return getThis();
	}

	@Override
	public Boolean getIterableOutput() {
		return iterableOutput;
	}

	@Override
	public final S castOutput(boolean cast) {
		this.castOutput = cast;
		return getThis();
	}

	@Override
	public Boolean getCastOutput() {
		return castOutput;
	}

	@Override
	public S uncheckedOutput(boolean unchecked) {
		this.uncheckedOutput = unchecked;
		return getThis();
	}

	@Override
	public Boolean getUncheckedOutput() {
		return uncheckedOutput;
	}

	@Override
	public final S extensible(boolean extensible) {
		this.extensible = extensible;

		return getThis();
	}

	@Override
	public Boolean getExtensible() {
		return extensible;
	}

	@Override
	public final S ordered(boolean ordered) {
		this.ordered = ordered;

		return getThis();
	}

	@Override
	public Boolean getOrdered() {
		return ordered;
	}

	protected <U extends BindingChildNode<? super T, ?>> List<U> getOverriddenNodes(TypeToken<U> type) {
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

	@Override
	public TypeToken<?> getPostInputType() {
		return postInputClass;
	}

	@Override
	protected TypeToken<?> getInputTargetForTargetAdapter() {
		return getContext().inputTargetType();
	}

	@Override
	public TypeToken<T> getExpectedType() {
		System.out.println(getOverride(BindingChildNode::inputExecutable, c -> null).tryGet());

		System.out.println(getOverride(BindingChildNode::outputMethod, c -> null).tryGet());

		return null;
	}

	@Override
	public abstract List<N> getOverriddenNodes();

	protected <U> OverrideBuilder<U, ?, ?> getOverride(Function<N, U> valueFunction, Function<S, U> givenValueFunction) {
		return new OverrideBuilder<>(this, getResult(), BindingChildNodeConfiguratorImpl::getOverriddenNodes, valueFunction,
				givenValueFunction.compose(SchemaNodeConfiguratorImpl::getThis));
	}

	protected <U> OverrideBuilder<U, ?, ?> getOverride(Function<S, U> givenValueFunction) {
		return new OverrideBuilder<>(this, getResult(), BindingChildNodeConfiguratorImpl::getOverriddenNodes, n -> null,
				givenValueFunction.compose(SchemaNodeConfiguratorImpl::getThis));
	}
}
