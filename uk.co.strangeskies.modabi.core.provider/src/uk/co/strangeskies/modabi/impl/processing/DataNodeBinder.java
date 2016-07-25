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
package uk.co.strangeskies.modabi.impl.processing;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.ChildNodeBinding;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public class DataNodeBinder<U> extends InputNodeBinder<DataNode<U>> {
	private final List<ChildNodeBinding<? extends U, ?>> bindings;

	public DataNodeBinder(ProcessingContext context, DataNode<U> node) {
		super(context, node);

		bindings = bind();
	}

	public DataNodeBinder<U> bindToTarget() {
		for (ChildNodeBinding<? extends U, ?> item : getBinding())
			invokeInMethod(item.getData());

		return this;
	}

	public List<ChildNodeBinding<? extends U, ?>> getBinding() {
		return bindings;
	}

	private List<ChildNodeBinding<? extends U, ?>> bind() {
		ProcessingContextImpl context = getContext();
		DataNode<U> node = getNode();

		DataSource dataSource;

		List<ChildNodeBinding<? extends U, ?>> results = new ArrayList<>();

		if (node.isValueProvided() && (node.valueResolution() == ValueResolution.REGISTRATION_TIME
				|| node.valueResolution() == ValueResolution.POST_REGISTRATION)) {
			/*
			 * Value is already provided and bound
			 */
			results.addAll(node.providedValues().stream().map(b -> new ChildNodeBinding<>(node, b)).collect(toList()));
		} else {
			/*
			 * Value is not yet bound, so we must determine the data source
			 */

			if (node.isValueProvided()) {
				/*
				 * Value is already provided, but not bound
				 */
				DataSource providedValueBuffer = node.providedValueBuffer();
				context = context.withNestedProvisionScope().forceExhausting();
				context.provisions().add(Provider.over(DataSource.class, () -> providedValueBuffer));
				results.addAll(bindList(context, node));
			} else if (node.format() != null) {
				switch (node.format()) {
				case CONTENT:
					dataSource = context.input().get().readContent();

					if (dataSource != null)
						results.add(bindWithDataSource(dataSource, context, node));
					else if (node.nullIfOmitted())
						results.add(new ChildNodeBinding<>(node, null));

					break;
				case PROPERTY:
					dataSource = context.input().get().readProperty(node.name());

					if (dataSource != null)
						results.add(bindWithDataSource(dataSource, context, node));
					else if (node.nullIfOmitted())
						results.add(new ChildNodeBinding<>(node, null));

					break;
				case SIMPLE:
					dataSource = null;
					while (node.name().equals(context.input().get().peekNextChild())) {
						context.input().get().startNextChild(node.name());

						try {
							dataSource = context.input().get().readContent();

							ChildNodeBinding<? extends U, ?> result = bindWithDataSource(dataSource, context, node);
							results.add(result);
						} finally {
							context.input().get().endChild();
						}
					}
					break;
				default:
					throw new AssertionError();
				}

				validateResults(node, results, null);
			} else {
				results.addAll(bindList(context, node));
			}
		}

		return results;
	}

	private void validateResults(DataNode<?> node, List<?> results, Exception cause) {
		if (results.isEmpty() && !node.occurrences().contains(0) && !node.occurrences().contains(0)) {
			throw new ProcessingException(t -> t.mustHaveData(node.name()), getContext(), cause);
		}

		if (!results.isEmpty() && !node.occurrences().contains(results.size())) {
			throw new ProcessingException(t -> t.mustHaveDataWithinRange(node), getContext(), cause);
		}
	}

	private List<ChildNodeBinding<? extends U, ?>> bindList(ProcessingContextImpl context, DataNode<U> node) {
		context = context.withInput(null);

		List<ChildNodeBinding<? extends U, ?>> results = new ArrayList<>();

		Exception optionalException = null;

		int count = 0;
		DataSource dataSource = null;
		int successfulIndex = 0;
		try {
			if (context.isProvided(DataSource.class))
				dataSource = context.provide(TypeToken.over(DataSource.class)).getObject();

			if (dataSource != null)
				successfulIndex = dataSource.index();

			while (!node.occurrences().isValueAbove(++count)) {
				results.add(bindExactNode(context, node));
				if (dataSource != null)
					successfulIndex = dataSource.index();
			}
		} catch (ModabiException e) {
			if (dataSource != null) {
				dataSource.reset();
				while (dataSource.index() < successfulIndex)
					dataSource.get();

				if (context.isExhaustive() && !dataSource.isComplete()) {
					DataSource dataSourceFinal = dataSource;
					throw new ProcessingException(t -> t.cannotBindRemainingData(dataSourceFinal), context, e);
				}
			}

			optionalException = e;
		}

		validateResults(node, results, optionalException);

		return results;
	}

	private static <U> ChildNodeBinding<? extends U, ?> bindWithDataSource(DataSource dataSource,
			ProcessingContextImpl context, DataNode<U> node) {
		context = context.withNestedProvisionScope().forceExhausting();
		context.provisions().add(Provider.over(DataSource.class, () -> dataSource));

		ChildNodeBinding<? extends U, ?> binding = bindExactNode(context, node);

		return binding;
	}

	private static <U> ChildNodeBinding<? extends U, ?> bindExactNode(ProcessingContextImpl context, DataNode<U> node) {
		if (node.extensible()) {
			ComputingMap<DataType<? extends U>, DataNode<? extends U>> overrides = context.getDataNodeOverrides(node);

			if (overrides.isEmpty())
				throw new ModabiException(
						"Unable to find type to satisfy data node '" + node.name() + "' with type '" + node.type() + "'");

			IdentityProperty<ChildNodeBinding<? extends U, ?>> result = new IdentityProperty<>();

			context.attemptBindingUntilSuccessful(overrides.keySet(), (c, n) -> {
				DataNode<? extends U> exactNode = overrides.putGet(n);
				result.set(getNodeBinding(c, exactNode));
			}, l -> new ProcessingException(
					"Unable to bind data node '" + node.name() + "' with type candidates '"
							+ overrides.keySet().stream().map(m -> m.name().toString()).collect(Collectors.joining(", ")) + "'",
					context, l));

			return result.get();
		} else
			return new ChildNodeBinding<>(node, new BindingNodeBinder(context).bind(node));
	}

	private static <U> ChildNodeBinding<U, ?> getNodeBinding(ProcessingContextImpl context, DataNode<U> exactNode) {
		return new ChildNodeBinding<>(exactNode, new BindingNodeBinder(context).bind(exactNode));
	}

	public static DataLoader dataLoader(ProcessingContextImpl context) {
		return new DataLoader() {
			@Override
			public <U> List<U> loadData(DataNode<U> node, DataSource data) {
				ProcessingContextImpl derivedContext = context.withNestedProvisionScope().forceExhausting();
				derivedContext.provisions().add(Provider.over(DataSource.class, () -> data));
				return new DataNodeBinder<>(derivedContext, node).getBinding().stream().map(ChildNodeBinding::getData)
						.collect(toList());
			}
		};
	}
}
