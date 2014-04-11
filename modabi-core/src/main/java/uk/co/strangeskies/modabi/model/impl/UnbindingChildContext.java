package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import uk.co.strangeskies.modabi.data.BufferedDataTarget;
import uk.co.strangeskies.modabi.data.TerminatingDataTarget;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.impl.ElementNodeConfiguratorImpl.ElementNodeImpl;
import uk.co.strangeskies.modabi.processing.UnbindingContext;

public class UnbindingChildContext {
	private final List<BufferedDataTarget> bufferedSimpleElements;
	private final List<ElementNodeImpl<?>> queuedElements;
	private final Deque<Object> bindingStack;
	private final UnbindingContext<?> context;

	private TerminatingDataTarget sink;

	public UnbindingChildContext(UnbindingContext<?> context) {
		bufferedSimpleElements = new ArrayList<>();
		queuedElements = new ArrayList<>();
		bindingStack = new ArrayDeque<>();
		this.context = context;

		pushUnbindingTarget(context.data());
	}

	public TerminatingDataTarget simpleElement(String id) {
		BufferedDataTarget buffer = new BufferedDataTarget();
		bufferedSimpleElements.add(buffer);
		return sink = buffer;
	}

	public TerminatingDataTarget property(String id) {
		return sink = context.output().property(id);
	}

	public Object getUnbindingTarget() {
		return bindingStack.peek();
	}

	public void pushUnbindingTarget(Object target) {
		bindingStack.push(target);
	}

	public void popUnbindingTarget() {
		bindingStack.pop();
	}

	public TerminatingDataTarget getOpenDataTarget() {
		return sink;
	}

	public void endData() {
		sink.end();
		sink = null;
	}

	public TerminatingDataTarget content() {
		return sink = context.output().content();
	}

	public <U> List<Model<? extends U>> getMatchingModels(
			AbstractModel<U> element, Class<?> dataClass) {
		return context.getMatchingModels(element, dataClass);
	}

	public void queueElement(ElementNodeImpl<?> elementNode) {
	}

	public void processChildren(List<ChildNodeImpl> children) {
		for (ChildNodeImpl child : children)
			child.unbind(this);

		for (BufferedDataTarget bufferedSimpleElement : bufferedSimpleElements) {
		}

		for (ElementNodeImpl<?> node : queuedElements)
			node.unbindQueued(this);
	}

	public void beginElement(String id) {
		// TODO Auto-generated method stub

	}

	public void endElement() {
		// TODO Auto-generated method stub

	}
}
