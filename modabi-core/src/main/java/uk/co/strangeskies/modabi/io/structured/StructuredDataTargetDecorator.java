package uk.co.strangeskies.modabi.io.structured;

import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.DataTargetDecorator;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.utilities.Decorator;

public class StructuredDataTargetDecorator extends
		Decorator<StructuredDataTarget> implements StructuredDataTarget {
	private StructuredDataState currentState;
	private int depth;

	public StructuredDataTargetDecorator(StructuredDataTarget component) {
		super(component);

		currentState = StructuredDataState.UNSTARTED;
		depth = 0;
	}

	@Override
	public StructuredDataState currentState() {
		return currentState;
	}

	private void enterState(StructuredDataState exitState) {
		currentState = currentState.enterState(exitState);
	}

	@Override
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace) {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START);
		return getComponent().registerDefaultNamespaceHint(namespace);
	}

	@Override
	public StructuredDataTarget registerNamespaceHint(Namespace namespace) {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START);
		return getComponent().registerNamespaceHint(namespace);
	}

	@Override
	public StructuredDataTarget comment(String comment) {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START,
				StructuredDataState.POPULATED_ELEMENT);
		return getComponent().comment(comment);
	}

	@Override
	public StructuredDataTarget nextChild(QualifiedName name) {
		depth++;
		enterState(StructuredDataState.ELEMENT_START);
		return getComponent().nextChild(name);
	}

	@Override
	public DataTarget writeProperty(QualifiedName name) {
		enterState(StructuredDataState.PROPERTY);
		return new DataTargetDecorator(getComponent().writeProperty(name)) {
			@Override
			public void terminate() {
				super.terminate();
				enterState(StructuredDataState.ELEMENT_START);
			}
		};
	}

	@Override
	public DataTarget writeContent() {
		enterState(StructuredDataState.CONTENT);
		return new DataTargetDecorator(getComponent().writeContent()) {
			@Override
			public void terminate() {
				super.terminate();
				enterState(StructuredDataState.ELEMENT_WITH_CONTENT);
			}
		};
	}

	@Override
	public StructuredDataTarget endChild() {
		if (--depth == 0)
			enterState(StructuredDataState.FINISHED);
		else
			enterState(StructuredDataState.POPULATED_ELEMENT);
		return getComponent().endChild();
	}
}
