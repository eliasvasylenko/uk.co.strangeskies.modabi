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
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeToken;

public class DataNodeImpl<T> extends BindingChildNodeImpl<T, DataNode<T>, DataNode.Effective<T>>
		implements DataNode<T> {
	public static class Effective<T> extends BindingChildNodeImpl.Effective<T, DataNode<T>, DataNode.Effective<T>>
			implements DataNode.Effective<T> {
		private final DataType.Effective<? super T> type;
		private final Format format;
		private final DataSource providedBuffer;
		private final ValueResolution resolution;
		private final List<T> provided;

		private Effective(OverrideMerge<DataNode<T>, DataNodeConfiguratorImpl<T>> overrideMerge) {
			super(overrideMerge, false);

			DataType<? super T> type = overrideMerge.getOverride(DataNode::type)
					.validate((n, o) -> n.effective().base().contains(o.effective())).tryGet();
			this.type = type == null ? null : type.effective();

			format = overrideMerge.getOverride(DataNode::format).tryGet();
			if (format != null && overrideMerge.configurator().getContext().isInputDataOnly())
				throw new SchemaException("Node '" + name() + "' must not provide a format.");

			providedBuffer = overrideMerge.getOverride(DataNode::providedValueBuffer).tryGet();
			ValueResolution resolution = overrideMerge.getOverride(DataNode::valueResolution)
					.validate(
							(o, n) -> o == n || (o == ValueResolution.REGISTRATION_TIME && n == ValueResolution.POST_REGISTRATION))
					.orDefault(ValueResolution.PROCESSING_TIME).get();

			Range<Integer> occurrences = overrideMerge.getOverride(BindingChildNode::occurrences).tryGet();

			/*
			 * Bind provided value:
			 */
			TypeToken<T> dataType = overrideMerge.configurator().getEffectiveDataType();

			if (providedBuffer == null && abstractness().isAtMost(Abstractness.UNINFERRED)
					&& (occurrences == null || !occurrences.contains(0))
					&& (resolution == ValueResolution.REGISTRATION_TIME || resolution == ValueResolution.POST_REGISTRATION))
				throw new SchemaException("Value must be provided at registration time for node '" + name() + "'");

			if ((resolution == ValueResolution.REGISTRATION_TIME || resolution == ValueResolution.POST_REGISTRATION)
					&& providedBuffer != null) {
				provided = overrideMerge.configurator().getContext().dataLoader().loadData(this, providedBuffer);

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
						 * Only a simple check is possible here as the actual reified type
						 * may not be available, or may not yet be properly proxied in the
						 * case of a post registration provision..
						 */
						dataType.withLooseCompatibilityFrom(rawType);
					}
				}

				if (!providedTypes.isEmpty()) {
					this.dataType = effectiveDataType;
				}
			} else {
				provided = null;
			}

			/*
			 * instantiation of this value is delayed until here so that provided
			 * value binding works without thinking the value has already been
			 * provided...
			 */
			this.resolution = resolution;

			integrateIO(overrideMerge);
		}

		@Override
		public final DataType.Effective<? super T> type() {
			return type;
		}

		@Override
		public final Format format() {
			return format;
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
	private final DataSource providedBuffer;
	private final ValueResolution resolution;

	DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
		super(configurator);

		format = configurator.getFormat();
		type = configurator.getType();

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
