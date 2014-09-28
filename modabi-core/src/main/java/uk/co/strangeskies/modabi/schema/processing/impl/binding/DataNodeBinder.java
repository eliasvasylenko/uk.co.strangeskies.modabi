package uk.co.strangeskies.modabi.schema.processing.impl.binding;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public class DataNodeBinder {
	private final BindingContext context;

	public DataNodeBinder(BindingContext context) {
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
				BindingContext context = this.context.withProvision(DataSource.class,
						() -> providedValueBuffer);
				results.addAll(bindList(context, node));
			}
		} else if (node.format() != null) {
			switch (node.format()) {
			case CONTENT:
				dataSource = context.input().readContent();

				if (dataSource != null)
					results.add(bindWithDataSource(dataSource, context, node));

				break;
			case PROPERTY:
				dataSource = context.input().readProperty(node.getName());

				if (dataSource != null)
					results.add(bindWithDataSource(dataSource, context, node));

				break;
			case SIMPLE_ELEMENT:
				BindingContext context = this.context;

				while (node.getName().equals(context.input().peekNextChild())) {
					context.input().startNextChild(node.getName());

					dataSource = context.input().readContent();

					U result = bindWithDataSource(dataSource, context, node);
					results.add(result);

					if (node.isInMethodChained())
						context = context.withReplacedBindingTarget(result);

					context.input().endChild();
				}
			}
		} else
			results.addAll(bindList(context, node));

		if (results.isEmpty() && !node.optional() && !node.occurances().contains(0))
			throw context.exception("Node '" + node.getName()
					+ "' must be bound data.");

		if (!results.isEmpty() && !node.occurances().contains(results.size()))
			throw context.exception("Node '" + node.getName() + "' binding results '"
					+ results + "' must be bound data within range of '"
					+ Range.compose(node.occurances()) + "' occurances.");

		return results;
	}

	private static <U> List<U> bindList(BindingContext context,
			DataNode.Effective<U> node) {
		context = context.withInput(null);

		List<U> results = new ArrayList<>();

		int count = 0;
		DataSource dataSource = null;
		int successfulIndex = 0;
		try {
			if (context.isProvided(DataSource.class))
				dataSource = context.provide(DataSource.class);

			if (dataSource != null)
				successfulIndex = dataSource.index();

			while (!node.occurances().isValueAbove(++count)) {
				results.add(new BindingNodeBinder(context).bind(node));
				if (dataSource != null)
					successfulIndex = dataSource.index();
			}
		} catch (SchemaException e) {
			if (dataSource != null) {
				dataSource.reset();
				while (dataSource.index() < successfulIndex)
					dataSource.get();
			}

			if (node.occurances().isValueBelow(count))
				throw e;
		}

		return results;
	}

	private static <U> U bindWithDataSource(DataSource dataSource,
			BindingContext context, DataNode.Effective<U> node) {
		context = context.withProvision(DataSource.class, () -> dataSource);

		return new BindingNodeBinder(context).bind(node);
	}
}
