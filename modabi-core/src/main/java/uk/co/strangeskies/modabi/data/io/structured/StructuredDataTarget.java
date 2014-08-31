package uk.co.strangeskies.modabi.data.io.structured;

import java.util.Arrays;
import java.util.function.Function;

import uk.co.strangeskies.modabi.data.io.IOException;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface StructuredDataTarget {
	public enum State {
		UNSTARTED, EMPTY_ELEMENT, POPULATED_ELEMENT, ELEMENT_WITH_CONTENT, PROPERTY, CONTENT, FINISHED;

		public State enterState(State next) {
			switch (this) {
			case UNSTARTED:
				checkExitStateValid(next, EMPTY_ELEMENT);
				break;
			case EMPTY_ELEMENT:
				checkExitStateValid(next, EMPTY_ELEMENT, POPULATED_ELEMENT, PROPERTY,
						CONTENT);
				break;
			case POPULATED_ELEMENT:
				checkExitStateValid(next, EMPTY_ELEMENT, POPULATED_ELEMENT, FINISHED);
				break;
			case ELEMENT_WITH_CONTENT:
				checkExitStateValid(next, POPULATED_ELEMENT, FINISHED);
				break;
			case PROPERTY:
				checkExitStateValid(next, EMPTY_ELEMENT);
				break;
			case CONTENT:
				checkExitStateValid(next, ELEMENT_WITH_CONTENT);
				break;
			case FINISHED:
				checkExitStateValid(next);
				break;
			}

			return next;
		}

		private void checkExitStateValid(State exitState, State... validExitState) {
			if (!Arrays.asList(validExitState).contains(exitState))
				throw new IOException("Cannot move to state '" + exitState
						+ "' from state '" + this + "'.");
		}

		public void checkValid(State... validState) {
			if (!Arrays.asList(validState).contains(this))
				throw new IOException("Cannot perform action in state '" + this + "'.");
		}
	}

	public State currentState();

	/**
	 * This may help some data targets, e.g. XML, organise content a little more
	 * cleanly, by suggesting a default namespace for a document at the current
	 * child element (or the root element if none have been yet created).
	 * 
	 * The target should establish the namespace locally to the current child if
	 * possible, but may fall back to global scope if local scoping is not
	 * supported. The target may also fall back to equivalent behaviour to
	 * {@link StructuredDataTarget#registerNamespaceHint(Namespace)} if the
	 * concept of 'default' namespace doesn't apply.
	 * 
	 * If a DataTarget implementation does not support namespace hints this method
	 * should fail silently rather than throwing an exception.
	 *
	 * @param namespace
	 * @return
	 */
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace);

	public StructuredDataTarget registerNamespaceHint(Namespace namespace);

	public StructuredDataTarget nextChild(QualifiedName name);

	public TerminatingDataTarget property(QualifiedName name);

	public default StructuredDataTarget property(QualifiedName name,
			Function<TerminatingDataTarget, TerminatingDataTarget> targetOperation) {
		TerminatingDataTarget target = property(name);
		if (target != targetOperation.apply(target))
			throw new IllegalArgumentException();
		target.terminate();
		return this;
	}

	public TerminatingDataTarget content();

	public default StructuredDataTarget content(
			Function<TerminatingDataTarget, TerminatingDataTarget> targetOperation) {
		TerminatingDataTarget target = content();
		if (target != targetOperation.apply(target))
			throw new IllegalArgumentException();
		target.terminate();
		return this;
	}

	public StructuredDataTarget endChild();

	public StructuredDataTarget comment(String comment);
}
