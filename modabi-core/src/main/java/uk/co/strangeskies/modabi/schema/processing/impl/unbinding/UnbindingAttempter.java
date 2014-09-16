package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode.Effective;
import uk.co.strangeskies.utilities.MultiException;

public class UnbindingAttempter {
	private final UnbindingContext context;

	public UnbindingAttempter(UnbindingContext context) {
		this.context = new UnbindingContext() {
			@Override
			public Object unbindingSource() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<Effective<?, ?>> unbindingNodeStack() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <U> U provide(Class<U> clazz) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public StructuredDataTarget output() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> List<Model<? extends T>> getMatchingModels(
					uk.co.strangeskies.modabi.schema.model.nodes.ElementNode.Effective<T> element,
					Class<?> dataClass) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Bindings bindings() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	public <I extends SchemaNode.Effective<?, ?>> void tryForEach(
			List<I> unbindingItems, BiConsumer<UnbindingContext, I> unbindingMethod,
			Function<List<SchemaException>, MultiException> onFailure) {
		if (unbindingItems.isEmpty())
			throw new IllegalArgumentException(
					"Must supply items for unbinding attempt.");

		List<SchemaException> failures = new ArrayList<>();
		Deque<SchemaNode<?, ?>> nodeStack = new ArrayDeque<>(this.nodeStack);
		Deque<Object> bindingStack = new ArrayDeque<>(this.bindingStack);

		BufferingDataTarget dataTarget = null;
		StructuredDataTarget output = null;
		for (I item : unbindingItems) {
			// mark output! (by redirecting to a new buffer)
			if (this.dataTarget != null) {
				dataTarget = this.dataTarget;
				this.dataTarget = new BufferingDataTarget();
			}
			output = this.output;
			this.output = new BufferingStructuredDataTarget();

			try {
				unbindingMethod.accept(context, item);
				failures.clear();
				break;
			} catch (SchemaException e) {
				failures.add(e);

				// reset output to mark! (by discarding buffer)
				this.dataTarget = dataTarget;
				this.output = output;
				this.nodeStack.clear();
				this.nodeStack.addAll(nodeStack);
				this.bindingStack.clear();
				this.bindingStack.addAll(bindingStack);
			}
		}

		if (failures.isEmpty()) {
			// remove mark! (by flushing buffer into output)
			if (dataTarget != null)
				this.dataTarget = this.dataTarget.buffer().pipe(dataTarget);
			this.output = ((BufferingStructuredDataTarget) this.output).buffer()
					.pipeNextChild(output);

			this.nodeStack = nodeStack;
			this.bindingStack = bindingStack;
		} else
			throw onFailure.apply(failures);
	}
}
