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

import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeToken;

public class DataNodeImpl<T> extends BindingChildNodeImpl<T, DataNode<T>, DataNode.Effective<T>>
		implements DataNode<T> {
	public static class Effective<T> extends BindingChildNodeImpl.Effective<T, DataNode<T>, DataNode.Effective<T>>
			implements DataNode.Effective<T> {
		private final DataType.Effective<T> type;
		private final Format format;
		private final Boolean nullIfOmitted;
		private final DataSource providedBuffer;
		private final ValueResolution resolution;
		private List<T> provided;

		protected Effective(OverrideMerge<DataNode<T>, DataNodeConfiguratorImpl<T>> overrideMerge) {
			super(overrideMerge);

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
				throw new SchemaException("Node '" + getName() + "' must not provide a format.");

			nullIfOmitted = overrideMerge.getOverride(DataNode::nullIfOmitted).validate((n, o) -> o || !n).orDefault(false)
					.get();

			if (!isAbstract() && nullIfOmitted && (!occurrences().equals(Range.between(0, 1)) || format == Format.SIMPLE
					|| !overrideMerge.configurator().getContext().isInputExpected()))
				throw new SchemaException("'Null if omitted' property is not valid for node '" + getName() + "'");

			providedBuffer = overrideMerge.getOverride(DataNode::providedValueBuffer).tryGet();
			ValueResolution resolution = overrideMerge.getOverride(DataNode::valueResolution)
					.validate(
							(o, n) -> o == n || (o == ValueResolution.REGISTRATION_TIME && n == ValueResolution.POST_REGISTRATION))
					.orDefault(ValueResolution.PROCESSING_TIME).get();

			if (providedBuffer == null && !isAbstract() && !occurrences().contains(0)
					&& (resolution == ValueResolution.REGISTRATION_TIME || resolution == ValueResolution.POST_REGISTRATION))
				throw new SchemaException("Value must be provided at registration time for node '" + getName() + "'");

			if ((resolution == ValueResolution.REGISTRATION_TIME || resolution == ValueResolution.POST_REGISTRATION)
					&& providedBuffer != null) {
				provided = overrideMerge.configurator().getContext().dataLoader().loadData(DataNodeImpl.Effective.this,
						providedBuffer);

				/*
				 * Incorporate type information from provided data if possible
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * TODO lift this into BindingNodeImpl where it belongs
				 * 
				 * for things like "providedBuffer" and "provided", propagate results
				 * back up the constructor chain by passing down an IdentityProperty or
				 * something
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 * 
				 */
				for (T providedItem : provided) {
					Class<?> rawType = provided.iterator().next().getClass();

					if (resolution == ValueResolution.REGISTRATION_TIME && Reified.class.isAssignableFrom(rawType)) {
						TypeToken<?> providedType = ((Reified<?>) providedItem).getThisType();
						/*
						 * TODO Incorporate
						 */
					} else {
						/*
						 * Only a simple check is possible here as the actual reified type
						 * may not be available, or may not yet be properly proxied in the
						 * case of a post registration provision..
						 */
						TypeToken.over(rawType).withLooseCompatibility(getDataType());
					}
				}
			} else {
				provided = null;
			}

			this.resolution = resolution;
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
