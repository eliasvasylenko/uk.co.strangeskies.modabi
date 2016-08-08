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
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.RootNode;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeToken;

public class DataNodeImpl<T> extends BindingChildNodeImpl<T, DataNode<T>> implements DataNode<T> {
	private final DataType<? super T> type;
	private final Format format;
	private final DataSource providedBuffer;
	private final ValueResolution resolution;
	private final List<T> provided;

	private final InputNodeComponent<?, ?> inputNodeComponent;

	protected DataNodeImpl(DataNodeConfiguratorImpl<T> configurator) {
		super(configurator, false);

		@SuppressWarnings("unchecked")
		DataType<? super T> type = (DataType<? super T>) configurator
				.getOverride(DataNode::type, DataNodeConfigurator::getType).validateOverride((n, o) -> n.base().contains(o))
				.tryGet();
		this.type = type;

		format = configurator.getOverride(DataNode::format, DataNodeConfigurator::getFormat).tryGet();
		if (format != null && configurator.getContext().isInputDataOnly())
			throw new ModabiException(t -> t.cannotAcceptFormat(name()));

		providedBuffer = configurator.getOverride(DataNode::providedValueBuffer, DataNodeConfigurator::getProvidedValue)
				.tryGet();
		ValueResolution resolution = configurator
				.getOverride(DataNode::valueResolution, DataNodeConfigurator::getValueResolution)
				.validateOverride(
						(o, n) -> o == n || (o == ValueResolution.REGISTRATION_TIME && n == ValueResolution.POST_REGISTRATION))
				.orDefault(ValueResolution.PROCESSING_TIME).get();

		Range<Integer> occurrences = configurator
				.getOverride(BindingChildNode::occurrences, DataNodeConfigurator::getOccurrences).tryGet();

		/*
		 * Bind provided value:
		 */
		TypeToken<T> dataType = configurator.getEffectiveDataType();

		@SuppressWarnings("unchecked")
		Class<DataNode<T>> nodeType = (Class<DataNode<T>>) configurator.getNodeType().getRawType();
		if (providedBuffer == null && concrete() && (occurrences == null || !occurrences.contains(0))
				&& (resolution == ValueResolution.REGISTRATION_TIME || resolution == ValueResolution.POST_REGISTRATION))
			throw new ModabiException(t -> t.mustProvideValueForNonAbstract(DataNode::providedValueBuffer, nodeType));

		if ((resolution == ValueResolution.REGISTRATION_TIME || resolution == ValueResolution.POST_REGISTRATION)
				&& providedBuffer != null) {
			provided = configurator.getContext().dataLoader().loadData(this, providedBuffer);

			TypeToken<T> effectiveDataType = configurator.getEffectiveDataType();
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
				this.dataType = effectiveDataType;
			}
		} else {
			provided = null;
		}

		/*
		 * instantiation of this value is delayed until here so that provided value
		 * binding works without thinking the value has already been provided...
		 */
		this.resolution = resolution;

		integrateIO(configurator);
		inputNodeComponent = new InputNodeComponent<>(configurator, dataType());
	}

	@Override
	protected InputNodeComponent<?, ?> getInputNodeComponent() {
		return inputNodeComponent;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataNodeConfigurator<T> configurator() {
		return (DataNodeConfigurator<T>) super.configurator();
	}

	@Override
	public final DataType<? super T> type() {
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
	public RootNode<?, ?> root() {
		return parent().root();
	}
}
