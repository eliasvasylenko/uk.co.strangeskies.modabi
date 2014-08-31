package uk.co.strangeskies.modabi.data.io.structured;

import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTargetDecorator;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.utilities.Decorator;

public class StructuredDataTargetDecorator extends
		Decorator<StructuredDataTarget> implements StructuredDataTarget {
	private State currentState;
	private int depth;

	public StructuredDataTargetDecorator(StructuredDataTarget component) {
		super(component);

		currentState = State.UNSTARTED;
		depth = 0;
	}

	@Override
	public State currentState() {
		return currentState;
	}

	private void enterState(State exitState) {
		currentState = currentState.enterState(exitState);
	}

	@Override
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace) {
		currentState().checkValid(State.UNSTARTED, State.EMPTY_ELEMENT);
		return getComponent().registerDefaultNamespaceHint(namespace);
	}

	@Override
	public StructuredDataTarget registerNamespaceHint(Namespace namespace) {
		currentState().checkValid(State.UNSTARTED, State.EMPTY_ELEMENT);
		return getComponent().registerNamespaceHint(namespace);
	}

	@Override
	public StructuredDataTarget comment(String comment) {
		currentState().checkValid(State.UNSTARTED, State.EMPTY_ELEMENT,
				State.POPULATED_ELEMENT);
		return getComponent().comment(comment);
	}

	@Override
	public StructuredDataTarget nextChild(QualifiedName name) {
		enterState(State.EMPTY_ELEMENT);
		return getComponent().nextChild(name);
	}

	@Override
	public TerminatingDataTarget property(QualifiedName name) {
		enterState(State.PROPERTY);
		return new TerminatingDataTargetDecorator(getComponent().property(name)) {
			@Override
			public void terminate() {
				super.terminate();
				enterState(StructuredDataTarget.State.EMPTY_ELEMENT);
			}
		};
	}

	@Override
	public TerminatingDataTarget content() {
		enterState(State.CONTENT);
		return new TerminatingDataTargetDecorator(getComponent().content()) {
			@Override
			public void terminate() {
				super.terminate();
				enterState(StructuredDataTarget.State.ELEMENT_WITH_CONTENT);
			}
		};
	}

	@Override
	public StructuredDataTarget endChild() {
		if (--depth == 0)
			enterState(State.FINISHED);
		else
			enterState(State.POPULATED_ELEMENT);
		return getComponent().endChild();
	}
}
