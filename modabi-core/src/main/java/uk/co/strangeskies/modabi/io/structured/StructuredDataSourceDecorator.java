package uk.co.strangeskies.modabi.io.structured;

import java.util.Set;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataSourceDecorator;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.utilities.Decorator;

public class StructuredDataSourceDecorator extends
		Decorator<StructuredDataSource> implements StructuredDataSource {
	private StructuredDataState currentState;

	public StructuredDataSourceDecorator(StructuredDataSource component) {
		super(component);

		currentState = StructuredDataState.UNSTARTED;
	}

	@Override
	public StructuredDataState currentState() {
		return currentState;
	}

	private void enterState(StructuredDataState exitState) {
		currentState = currentState.enterState(exitState);
	}

	@Override
	public QualifiedName peekNextChild() {
		return getComponent().peekNextChild();
	}

	@Override
	public Namespace getDefaultNamespaceHint() {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START);
		return getComponent().getDefaultNamespaceHint();
	}

	@Override
	public Set<Namespace> getNamespaceHints() {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START);
		return getComponent().getNamespaceHints();
	}

	@Override
	public Set<String> getComments() {
		currentState().checkValid(StructuredDataState.UNSTARTED,
				StructuredDataState.ELEMENT_START,
				StructuredDataState.POPULATED_ELEMENT);
		return getComponent().getComments();
	}

	@Override
	public QualifiedName startNextChild() {
		enterState(StructuredDataState.ELEMENT_START);
		return getComponent().startNextChild();
	}

	@Override
	public DataSource readProperty(QualifiedName name) {
		return new DataSourceDecorator(getComponent().readProperty(name));
	}

	@Override
	public DataSource readContent() {
		return new DataSourceDecorator(getComponent().readContent());
	}

	@Override
	public void endChild() {
		if (depth() == 1)
			enterState(StructuredDataState.FINISHED);
		else
			enterState(StructuredDataState.POPULATED_ELEMENT);
		getComponent().endChild();
	}

	@Override
	public Set<QualifiedName> getProperties() {
		return getComponent().getProperties();
	}

	@Override
	public boolean hasNextChild() {
		return getComponent().hasNextChild();
	}

	@Override
	public int depth() {
		return getComponent().depth();
	}

	@Override
	public int indexAtDepth() {
		return getComponent().indexAtDepth();
	}
}
