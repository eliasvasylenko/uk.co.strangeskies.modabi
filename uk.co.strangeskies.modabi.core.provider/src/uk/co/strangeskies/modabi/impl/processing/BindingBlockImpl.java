package uk.co.strangeskies.modabi.impl.processing;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.BindingBlock;

public class BindingBlockImpl implements BindingBlock {
	private final QualifiedName namespace;
	private final DataSource id;
	private final boolean internal;

	private boolean complete;
	private Throwable failure;

	private final Consumer<BindingBlock> startListener;
	private final Consumer<BindingBlock> endListener;

	public BindingBlockImpl(QualifiedName namespace, DataSource id, boolean internal,
			Consumer<BindingBlock> startThreadBlockListener, Consumer<BindingBlock> endThreadBlockListener) {
		this.namespace = namespace;
		this.id = id;
		this.internal = internal;

		complete = false;
		failure = null;

		startListener = startThreadBlockListener;
		endListener = endThreadBlockListener;
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
	public synchronized boolean isComplete() {
		return complete;
	}

	@Override
	public synchronized Throwable getFailure() {
		return failure;
	}

	@Override
	public boolean isInternal() {
		return internal;
	}

	@Override
	public synchronized void complete() {
		complete = true;
		endListener.accept(this);
		notifyAll();
	}

	@Override
	public synchronized boolean fail(Throwable cause) {
		if (complete || failure != null) {
			return false;
		} else {
			failure = cause;
			endListener.accept(this);
			notifyAll();
			return true;
		}
	}

	@Override
	public synchronized void waitUntilComplete() throws InterruptedException, ExecutionException {
		startListener.accept(this);

		while (!complete) {
			wait();
			if (failure != null) {
				throw new ExecutionException(failure);
			}
		}
	}

	@Override
	public synchronized void waitUntilComplete(long timeoutMilliseconds)
			throws InterruptedException, TimeoutException, ExecutionException {
		startListener.accept(this);

		long startTime = System.currentTimeMillis();

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

	@Override
	public String toString() {
		return "(" + namespace + ", " + id + ", " + (internal ? "internal" : "external") + ")";
	}
}
