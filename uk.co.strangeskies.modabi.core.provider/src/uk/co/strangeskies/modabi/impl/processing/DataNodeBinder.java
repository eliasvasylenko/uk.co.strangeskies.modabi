/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public class DataNodeBinder extends ChildNodeBinder {
	public DataNodeBinder(BindingContextImpl context) {
		super(context);
	}

	public <U> List<U> bind(DataNode.Effective<U> node) {
		BindingContextImpl context = getParentContext();
		DataSource dataSource;

		List<U> results = new ArrayList<>();

		if (node.isValueProvided()
				&& (node.valueResolution() == ValueResolution.REGISTRATION_TIME
						|| node.valueResolution() == ValueResolution.POST_REGISTRATION)) {
			/*
			 * Value is already provided and bound
			 */
			results.addAll(node.providedValues());
		} else {
			/*
			 * Value is not yet bound, so we must determind the data source
			 */

			if (node.isValueProvided()) {
				/*
				 * Value is already provided, but not bound
				 */
				DataSource providedValueBuffer = node.providedValueBuffer();
				results.addAll(bindList(
						context.withProvision(DataSource.class, () -> providedValueBuffer),
						node));
			} else if (node.format() != null) {
				switch (node.format()) {
				case CONTENT:
					dataSource = context.input().readContent();

					if (dataSource != null)
						results.add(bindWithDataSource(dataSource, context, node));
					else if (node.nullIfOmitted())
						results.add(null);

					break;
				case PROPERTY:
					dataSource = context.input().readProperty(node.getName());

					if (dataSource != null)
						results.add(bindWithDataSource(dataSource, context, node));
					else if (node.nullIfOmitted())
						results.add(null);

					break;
				case SIMPLE:
					while (node.getName().equals(context.input().peekNextChild())) {
						context.input().startNextChild(node.getName());

						dataSource = context.input().readContent();

						U result = bindWithDataSource(dataSource, context, node);
						results.add(result);

						if (node.isInMethodChained())
							context = getParentContext().withBindingTarget(result);

						context.input().endChild();
					}
				}

				validateResults(node, results, null);
			} else {
				results.addAll(bindList(context, node));
			}
		}

		//for (U item : results)
			//invokeInMethod(node, context.bindingTarget(), item);

		return results;
	}

	private void validateResults(DataNode.Effective<?> node, List<?> results,
			Exception cause) {
		if (results.isEmpty() && !node.occurrences().contains(0)
				&& !node.occurrences().contains(0)) {
			String message = "Node '" + node.getName() + "' must be bound data.";
			if (cause != null)
				throw new BindingException(message, getParentContext(), cause);
			else
				throw new BindingException(message, getParentContext());
		}

		if (!results.isEmpty() && !node.occurrences().contains(results.size())) {
			String message = "Node '" + node.getName() + "' binding results '"
					+ results + "' must be bound data within range of '"
					+ Range.compose(node.occurrences()) + "' occurrences.";
			if (cause != null)
				throw new BindingException(message, getParentContext(), cause);
			else
				throw new BindingException(message, getParentContext());
		}
	}

	private <U> List<U> bindList(BindingContextImpl context,
			DataNode.Effective<U> node) {
		context = context.withInput(null);

		List<U> results = new ArrayList<>();

		Exception optionalException = null;

		int count = 0;
		DataSource dataSource = null;
		int successfulIndex = 0;
		try {
			if (context.provisions().isProvided(DataSource.class))
				dataSource = context.provisions()
						.provide(TypeToken.over(DataSource.class));

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
			}

			optionalException = e;
		}

		validateResults(node, results, optionalException);

		return results;
	}

	private static <U> U bindWithDataSource(DataSource dataSource,
			BindingContextImpl context, DataNode.Effective<U> node) {
		context = context.withProvision(DataSource.class, () -> dataSource);

		return bindExactNode(context, node);
	}

	private static <U> U bindExactNode(BindingContextImpl context,
			DataNode.Effective<U> node) {
		if (node.isExtensible()) {
			ComputingMap<DataType<? extends U>, DataNode.Effective<? extends U>> overrides = context
					.getDataNodeOverrides(node);

			if (overrides.isEmpty())
				throw new SchemaException("Unable to find type to satisfy data node '"
						+ node.getName() + "' with type '" + node.effective().type() + "'");

			Property<U, U> result = new IdentityProperty<U>();

			context
					.attemptBindingUntilSuccessful(
							overrides
									.keySet(),
							(c, n) -> result
									.set(
											new BindingNodeBinder(c).bind(
													overrides.putGet(n))),
							l -> new BindingException(
									"Unable to bind data node '" + node.getName()
											+ "' with type candidates '"
											+ overrides.keySet().stream()
													.map(m -> m.effective().getName().toString())
													.collect(Collectors.joining(", "))
											+ "'",
									context, l));

			return result.get();
		} else
			return new BindingNodeBinder(context).bind(node);
	}
}
