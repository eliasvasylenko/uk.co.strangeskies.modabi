package uk.co.strangeskies.modabi.impl.processing;

import java.util.function.Function;

import uk.co.strangeskies.modabi.ReturningSchemaProcessor;
import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;

public abstract class ChildNodeBinder<T extends ChildNode.Effective<?, ?>> {
	private BindingContextImpl context;
	private final T node;

	public ChildNodeBinder(BindingContextImpl context, T node) {
		this.context = context;
		this.node = node;
	}

	protected void setContext(BindingContextImpl context) {
		this.context = context;
	}

	public BindingContextImpl getContext() {
		return context;
	}

	public T getNode() {
		return node;
	}

	protected void repeatNode(Function<Integer, Boolean> repeatAtCount) {
		int count = 0;
		try {
			do {
				if (!repeatAtCount.apply(count)) {
					break;
				}

				count++;
			} while (!node.occurrences().isValueAbove(count + 1));
		} catch (Exception e) {
			throw new BindingException(
					"Node '" + node.getName() + "' failed to bind on occurance '" + count
							+ "' of range '" + node.occurrences() + "'",
					context, e);
		}

		if (!node.occurrences().contains(count)) {
			throw new BindingException(
					"Node '" + node.getName() + "' occurrences '" + count
							+ "' should be within range '" + node.occurrences() + "'",
					getContext());
		}
	}

	public static BindingContextImpl bind(BindingContextImpl parentContext,
			ChildNode.Effective<?, ?> next) {
		BindingContextImpl context = parentContext.withBindingNode(next);

		ReturningSchemaProcessor<ChildNodeBinder<?>> childProcessor = new ReturningSchemaProcessor<ChildNodeBinder<?>>() {
			@Override
			public <U> ChildNodeBinder<?> accept(ComplexNode.Effective<U> node) {
				return new ComplexNodeBinder<>(context, node).bindToTarget();
			}

			@Override
			public <U> ChildNodeBinder<?> accept(DataNode.Effective<U> node) {
				return new DataNodeBinder<>(context, node).bindToTarget();
			}

			@Override
			public ChildNodeBinder<?> accept(InputSequenceNode.Effective node) {
				return new InputSequenceNodeBinder(context, node);
			}

			@Override
			public ChildNodeBinder<?> accept(SequenceNode.Effective node) {
				return new SequenceNodeBinder(context, node);
			}

			@Override
			public ChildNodeBinder<?> accept(ChoiceNode.Effective node) {
				return new ChoiceNodeBinder(context, node);
			}
		};

		try {
			return parentContext.withReplacementBindingTarget(
					next.process(childProcessor).getContext().bindingTarget());
		} catch (Exception e) {
			throw new BindingException("Failed to bind node '" + next + "'", context,
					e);
		}
	}
}
