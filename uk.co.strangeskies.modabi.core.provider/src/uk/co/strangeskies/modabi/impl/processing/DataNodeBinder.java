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
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.SchemaException;
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

public class DataNodeBinder<U> extends InputNodeBinder<DataNode.Effective<U>> {
	private final List<NodeBinding<U>> bindings;

	public DataNodeBinder(ProcessingContext context, DataNode<U> node) {
		super(context, node.effective());

		bindings = bind();
	}

	public DataNodeBinder<U> bindToTarget() {
		for (NodeBinding<U> item : getBinding())
			invokeInMethod(item.getBinding());

		return this;
	}

	public List<NodeBinding<U>> getBinding() {
		return bindings;
	}

	private List<NodeBinding<U>> bind() {
		ProcessingContextImpl context = getContext();
		DataNode.Effective<U> node = getNode();

		DataSource dataSource;

		List<NodeBinding<U>> results = new ArrayList<>();

		if (node.isValueProvided() && (node.valueResolution() == ValueResolution.REGISTRATION_TIME
				|| node.valueResolution() == ValueResolution.POST_REGISTRATION)) {
			/*
			 * Value is already provided and bound
			 */
			results.addAll(node.providedValues().stream().map(b -> new NodeBinding<>(b, node)).collect(toList()));
		} else {
			/*
			 * Value is not yet bound, so we must determind the data source
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
						results.add(null);

					break;
				case PROPERTY:
					dataSource = context.input().get().readProperty(node.getName());

					if (dataSource != null)
						results.add(bindWithDataSource(dataSource, context, node));
					else if (node.nullIfOmitted())
						results.add(null);

					break;
				case SIMPLE:
					dataSource = null;
					while (node.getName().equals(context.input().get().peekNextChild())) {
						context.input().get().startNextChild(node.getName());

						dataSource = context.input().get().readContent();

						NodeBinding<U> result = bindWithDataSource(dataSource, context, node);
						results.add(result);

						context.input().get().endChild();
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

	private void validateResults(DataNode.Effective<?> node, List<?> results, Exception cause) {
		if (results.isEmpty() && !node.occurrences().contains(0) && !node.occurrences().contains(0)) {
			String message = "Node '" + node.getName() + "' must be bound data.";
			if (cause != null)
				throw new ProcessingException(message, getContext(), cause);
			else
				throw new ProcessingException(message, getContext());
		}

		if (!results.isEmpty() && !node.occurrences().contains(results.size())) {
			String message = "Node '" + node.getName() + "' binding results '" + results
					+ "' must be bound data within range of '" + Range.compose(node.occurrences()) + "' occurrences.";
			if (cause != null)
				throw new ProcessingException(message, getContext(), cause);
			else
				throw new ProcessingException(message, getContext());
		}
	}

	private List<NodeBinding<U>> bindList(ProcessingContextImpl context, DataNode.Effective<U> node) {
		context = context.withInput(null);

		List<NodeBinding<U>> results = new ArrayList<>();

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
		} catch (SchemaException e) {
			if (dataSource != null) {
				dataSource.reset();
				while (dataSource.index() < successfulIndex)
					dataSource.get();

				if (context.isExhaustive() && !dataSource.isComplete()) {
					throw new ProcessingException(
							"Failed to bind all of data source, with ["
									+ dataSource.stream().map(Objects::toString).collect(Collectors.joining(", ")) + "] remaining",
							context, e);
				}
			}

			optionalException = e;
		}

		validateResults(node, results, optionalException);

		return results;
	}

	private static <U> NodeBinding<U> bindWithDataSource(DataSource dataSource, ProcessingContextImpl context,
			DataNode.Effective<U> node) {
		context = context.withNestedProvisionScope().forceExhausting();
		context.provisions().add(Provider.over(DataSource.class, () -> dataSource));

		NodeBinding<U> binding = bindExactNode(context, node);

		return binding;
	}

	private static <U> NodeBinding<U> bindExactNode(ProcessingContextImpl context, DataNode.Effective<U> node) {
		if (node.isExtensible()) {
			ComputingMap<DataType<? extends U>, DataNode.Effective<? extends U>> overrides = context
					.getDataNodeOverrides(node);

			if (overrides.isEmpty())
				throw new SchemaException("Unable to find type to satisfy data node '" + node.getName() + "' with type '"
						+ node.effective().type() + "'");

			IdentityProperty<NodeBinding<U>> result = new IdentityProperty<>();

			context.attemptBindingUntilSuccessful(overrides.keySet(), (c, n) -> {
				DataNode.Effective<? extends U> exactNode = overrides.putGet(n);
				result.set(new NodeBinding<>(new BindingNodeBinder(c).bind(exactNode), exactNode));
			}, l -> new ProcessingException("Unable to bind data node '" + node.getName() + "' with type candidates '"
					+ overrides.keySet().stream().map(m -> m.effective().getName().toString()).collect(Collectors.joining(", "))
					+ "'", context, l));

			return result.get();
		} else
			return new NodeBinding<>(new BindingNodeBinder(context).bind(node), node);
	}

	public static DataLoader dataLoader(ProcessingContextImpl context) {
		return new DataLoader() {
			@Override
			public <U> List<U> loadData(DataNode<U> node, DataSource data) {
				ProcessingContextImpl derivedContext = context.withNestedProvisionScope().forceExhausting();
				derivedContext.provisions().add(Provider.over(DataSource.class, () -> data));
				return new DataNodeBinder<>(derivedContext, node.effective()).getBinding().stream().map(NodeBinding::getBinding)
						.collect(toList());
			}
		};
	}
}
