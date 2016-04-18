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

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.impl.schema.BindingNodeImpl.Effective.Callback;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeToken;

class ProvidedValueTypeCallback<T> implements Callback<T, DataNodeConfiguratorImpl<T>, DataNode<T>> {
	DataSource providedBuffer;
	List<T> provided;
	ValueResolution resolution;

	public TypeToken<T> callback(DataNode<T> node,
			OverrideMerge<DataNode<T>, DataNodeConfiguratorImpl<T>> overrideMerge) {
		DataNode<T> effective = node.effective();
		TypeToken<T> dataType = overrideMerge.configurator().getEffectiveDataType();

		providedBuffer = overrideMerge.getOverride(DataNode::providedValueBuffer).tryGet();
		resolution = overrideMerge.getOverride(DataNode::valueResolution)
				.validate(
						(o, n) -> o == n || (o == ValueResolution.REGISTRATION_TIME && n == ValueResolution.POST_REGISTRATION))
				.orDefault(ValueResolution.PROCESSING_TIME).get();

		Range<Integer> occurrences = overrideMerge.getOverride(BindingChildNode::occurrences).tryGet();

		if (providedBuffer == null && effective.abstractness().isAtMost(Abstractness.UNINFERRED)
				&& (occurrences == null || !occurrences.contains(0))
				&& (resolution == ValueResolution.REGISTRATION_TIME || resolution == ValueResolution.POST_REGISTRATION))
			throw new SchemaException("Value must be provided at registration time for node '" + effective.name() + "'");

		if ((resolution == ValueResolution.REGISTRATION_TIME || resolution == ValueResolution.POST_REGISTRATION)
				&& providedBuffer != null) {
			provided = overrideMerge.configurator().getContext().dataLoader().loadData(effective, providedBuffer);

			TypeToken<T> effectiveDataType = overrideMerge.configurator().getEffectiveDataType();
			List<TypeToken<?>> providedTypes = new ArrayList<>();

			for (T providedItem : provided) {
				Class<?> rawType = provided.iterator().next().getClass();

				if (resolution == ValueResolution.REGISTRATION_TIME && Reified.class.isAssignableFrom(rawType)) {
					TypeToken<?> providedType = ((Reified<?>) providedItem).getThisType();
					providedTypes.add(providedType);

					effectiveDataType = effectiveDataType.withLooseCompatibilityFrom(providedType);
				} else {
					/*
					 * Only a simple check is possible here as the actual reified type may
					 * not be available, or may not yet be properly proxied in the case of
					 * a post registration provision..
					 */
					dataType.withLooseCompatibilityFrom(rawType);
				}
			}

			if (!providedTypes.isEmpty()) {
				dataType = effectiveDataType;
			}
		} else {
			provided = null;
		}

		return dataType;
	}
}

public class DataNodeImpl<T> extends BindingChildNodeImpl<T, DataNode<T>, DataNode.Effective<T>>
		implements DataNode<T> {
	public static class Effective<T> extends BindingChildNodeImpl.Effective<T, DataNode<T>, DataNode.Effective<T>>
			implements DataNode.Effective<T> {
		private final DataType.Effective<T> type;
		private final Format format;
		private final Boolean nullIfOmitted;
		private final DataSource providedBuffer;
		private final ValueResolution resolution;
		private final List<T> provided;

		private Effective(OverrideMerge<DataNode<T>, DataNodeConfiguratorImpl<T>> overrideMerge) {
			this(overrideMerge, new ProvidedValueTypeCallback<>());
		}

		private Effective(OverrideMerge<DataNode<T>, DataNodeConfiguratorImpl<T>> overrideMerge,
				ProvidedValueTypeCallback<T> dto) {
			super(overrideMerge, dto);

			DataType<T> type = overrideMerge.getOverride(DataNode::type).validate((n, o) -> {
				DataType<?> p = n.effective();
				do
					if (p == o.effective())
						return true;
				while ((p = p.baseType().effective()) != null);
				return false;
			}).tryGet();
			this.type = type == null ? null : type.effective();

			format = overrideMerge.getOverride(DataNode::format).tryGet();
			if (format != null && overrideMerge.configurator().getContext().isInputDataOnly())
				throw new SchemaException("Node '" + name() + "' must not provide a format.");

			/*
			 * Determine effective 'null if omitted' property. Must be true for nodes
			 * which form part of an inputSequence, or which bind their data into a
			 * constructor or static factory.
			 */
			boolean mustBeNullIfOmitted = !overrideMerge.configurator().getContext().isInputExpected()
					|| overrideMerge.configurator().getContext().isConstructorExpected()
					|| overrideMerge.configurator().getContext().isStaticMethodExpected();
			nullIfOmitted = overrideMerge.getOverride(DataNode::nullIfOmitted).validate((n, o) -> o || !n)
					.orDefault(mustBeNullIfOmitted).get();
			if (nullIfOmitted != null && !nullIfOmitted && mustBeNullIfOmitted) {
				throw new SchemaException("'Null if omitted' property must be true for node '" + name() + "'");
			}

			providedBuffer = dto.providedBuffer;
			provided = dto.provided;
			resolution = dto.resolution;
		}

		@Override
		public final DataType.Effective<T> type() {
			return type;
		}

		@Override
		public final Format format() {
			return format;
		}

		@Override
		public final Boolean nullIfOmitted() {
			return nullIfOmitted;
		}

		@Override
		public DataSource providedValueBuffer() {
			return providedBuffer == null ? null : providedBuffer.copy().reset();
		}

		@Override
		public List<T> providedValues() {
			return provided;
		}

		@Override
		public ValueResolution valueResolution() {
			return resolution;
		}

		@Override
		public BindingNode.Effective<?, ?, ?> root() {
			return parent().root();
		}
	}

	private final Effective<T> effective;

	private final DataType<T> type;
	private final Format format;
	private final Boolean nullIfOmitted;
	private final DataSource providedBuffer;
	private final ValueResolution resolution;

	DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
		super(configurator);

		format = configurator.getFormat();
		type = configurator.getType();
		nullIfOmitted = configurator.getNullIfOmitted();

		providedBuffer = configurator.getProvidedBufferedValue();
		resolution = configurator.getResolution();

		effective = new Effective<>(DataNodeConfiguratorImpl.overrideMerge(this, configurator));
	}

	@Override
	public final DataType<T> type() {
		return type;
	}

	@Override
	public final Format format() {
		return format;
	}

	@Override
	public final Boolean nullIfOmitted() {
		return nullIfOmitted;
	}

	@Override
	public DataSource providedValueBuffer() {
		return providedBuffer == null ? null : providedBuffer.copy().reset();
	}

	@Override
	public ValueResolution valueResolution() {
		return resolution;
	}

	@Override
	public DataNodeImpl.Effective<T> effective() {
		return effective;
	}

	@Override
	public BindingNode<?, ?, ?> root() {
		return parent().root();
	}
}
