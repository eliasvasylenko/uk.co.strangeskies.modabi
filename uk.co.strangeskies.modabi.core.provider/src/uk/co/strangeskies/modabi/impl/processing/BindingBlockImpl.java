package uk.co.strangeskies.modabi.impl.processing;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.BindingBlock;

public class BindingBlockImpl implements BindingBlock {
	private final QualifiedName namespace;
	private final DataSource id;
	private final boolean internal;

	private boolean complete;
	private Throwable failure;

	public BindingBlockImpl(QualifiedName namespace, DataSource id, boolean internal) {
		this.namespace = namespace;
		this.id = id;
		this.internal = internal;

		complete = false;
		failure = null;
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
		synchronized (this) {
			return complete;
		}
	}

	@Override
	public Throwable getFailure() {
		synchronized (this) {
			return failure;
		}
	}

	@Override
	public boolean isInternal() {
		return internal;
	}

	@Override
	public void complete() {
		synchronized (this) {
			complete = true;
			notifyAll();
		}
	}

	@Override
	public boolean fail(Throwable cause) {
		synchronized (this) {
			if (complete || failure != null) {
				return false;
			} else {
				failure = cause;
				notifyAll();
				return true;
			}
		}
	}

	@Override
	public void waitUntilComplete() throws InterruptedException, ExecutionException {
		synchronized (this) {
			while (!complete) {
				wait();
				if (failure != null) {
					throw new ExecutionException(failure);
				}
			}
		}
	}

	@Override
	public void waitUntilComplete(long timeoutMilliseconds)
			throws InterruptedException, TimeoutException, ExecutionException {
		long startTime = System.currentTimeMillis();

		synchronized (this) {
			while (!complete) {
				long wait = timeoutMilliseconds + startTime - System.currentTimeMillis();
				if (wait < 0) {
					throw new TimeoutException("Timed out waiting for blocking dependency " + this);
				}
				wait();
				if (failure != null) {
					throw new ExecutionException("Failed to resolve blocking dependency " + this, failure);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "(" + namespace + ", " + id + ", " + (internal ? "internal" : "external") + ")";
	}
}
