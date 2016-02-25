package uk.co.strangeskies.modabi.processing;

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.tuple.Pair;

public interface BindingFutureBlocks extends Observable<Pair<QualifiedName, DataSource>> {
	Set<QualifiedName> waitingForNamespaces();

	List<DataSource> waitingForIds(QualifiedName namespace);

	void waitFor(QualifiedName namespace, DataSource id) throws InterruptedException;

	void waitFor(QualifiedName namespace, DataSource id, long timeoutMilliseconds) throws InterruptedException;

	void waitForAll(QualifiedName namespace) throws InterruptedException;

	void waitForAll(QualifiedName namespace, long timeoutMilliseconds) throws InterruptedException;

	boolean isBlocked();
}
