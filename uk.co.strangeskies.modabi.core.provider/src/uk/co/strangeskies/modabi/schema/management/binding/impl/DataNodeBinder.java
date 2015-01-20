package uk.co.strangeskies.modabi.schema.management.binding.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.ValueResolution;
import uk.co.strangeskies.modabi.schema.management.binding.BindingException;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public class DataNodeBinder {
	private final BindingContextImpl context;

	public DataNodeBinder(BindingContextImpl context) {
		this.context = context;
	}

	public <U> List<U> bind(DataNode.Effective<U> node) {
		DataSource dataSource;

		List<U> results = new ArrayList<>();

		if (node.isValueProvided()) {
			if (node.valueResolution() == ValueResolution.REGISTRATION_TIME) {
				results.addAll(node.providedValues());
			} else {
				DataSource providedValueBuffer = node.providedValueBuffer();
				BindingContextImpl context = this.context.withProvision(
						DataSource.class, () -> providedValueBuffer);
				results.addAll(bindList(context, node));
			}
		} else if (node.format() != null) {
			switch (node.format()) {
			case CONTENT:
				dataSource = context.input().readContent();

				if (dataSource != null || node.nullIfOmitted())
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
				BindingContextImpl context = this.context;

				while (node.getName().equals(context.input().peekNextChild())) {
					context.input().startNextChild(node.getName());

					dataSource = context.input().readContent();

					U result = bindWithDataSource(dataSource, context, node);
					results.add(result);

					if (node.isInMethodChained())
						context = this.context.withBindingTarget(result);

					context.input().endChild();
				}
			}
		} else
			results.addAll(bindList(context, node));

		if (results.isEmpty() && !node.optional()
				&& !node.occurrences().contains(0))
			throw new BindingException("Node '" + node.getName()
					+ "' must be bound data.", context);

		if (!results.isEmpty() && !node.occurrences().contains(results.size()))
			throw new BindingException("Node '" + node.getName()
					+ "' binding results '" + results
					+ "' must be bound data within range of '"
					+ Range.compose(node.occurrences()) + "' occurrences.", context);

		return results;
	}

	private static <U> List<U> bindList(BindingContextImpl context,
			DataNode.Effective<U> node) {
		context = context.withInput(null);

		List<U> results = new ArrayList<>();

		int count = 0;
		DataSource dataSource = null;
		int successfulIndex = 0;
		try {
			if (context.provisions().isProvided(DataSource.class))
				dataSource = context.provide(new TypeLiteral<>(DataSource.class));

			if (dataSource != null)
				successfulIndex = dataSource.index();

			while (!node.occurrences().isValueAbove(++count)) {
				results.add(bindExactNode(context, node));
				if (dataSource != null)
					successfulIndex = dataSource.index();
			}
		} catch (SchemaException e) {
			// TODO sometimes this exception is hidden
			if (dataSource != null) {
				dataSource.reset();
				while (dataSource.index() < successfulIndex)
					dataSource.get();
			}

			if (node.occurrences().isValueBelow(count))
				throw e;
		}

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
			ComputingMap<DataBindingType.Effective<? extends U>, DataNode.Effective<? extends U>> overrides = context
					.getDataNodeOverrides(node);

			if (overrides.isEmpty())
				throw new SchemaException("Unable to find type to satisfy data node '"
						+ node.getName() + "' with type '" + node.effective().type() + "'.");

			Property<U, U> result = new IdentityProperty<U>();

			context.attemptUntilSuccessful(
					overrides.keySet(),
					(c, n) -> result.set(new BindingNodeBinder(c).bind(overrides
							.putGet(n))),
					l -> new BindingException("Unable to bind data node '"
							+ node.getName()
							+ "' with type candidates '"
							+ overrides.keySet().stream()
									.map(m -> m.source().getName().toString())
									.collect(Collectors.joining(", ")) + "'.", context, l));

			return result.get();
		} else
			return new BindingNodeBinder(context).bind(node);
	}
}
