package uk.co.strangeskies.modabi.impl.processing;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.BindingBlock;

public class BlockImpl implements BindingBlock {
	private final QualifiedName namespace;
	private final DataSource id;

	public BlockImpl(QualifiedName namespace, DataSource id) {
		this.namespace = namespace;
		this.id = id;
	}

	@Override
	public QualifiedName namespace() {
		return namespace;
	}

	@Override
	public DataSource id() {
		return id;
	}

	@Override
	public boolean isComplete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInternal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void complete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void waitFor() throws InterruptedException {
		// TODO Auto-generated method stub

	}
}
