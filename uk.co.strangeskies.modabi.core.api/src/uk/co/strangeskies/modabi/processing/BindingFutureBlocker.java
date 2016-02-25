package uk.co.strangeskies.modabi.processing;

import java.util.function.Supplier;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;

public interface BindingFutureBlocker {
	<T> T blockAndWaitFor(Supplier<T> blockingSupplier, QualifiedName namespace, DataSource id);

	default void blockFor(Supplier<?> blockingSupplier, QualifiedName namespace, DataSource id) {
		new Thread(() -> {
			blockAndWaitFor(blockingSupplier, namespace, id);
		}).start();
	}
}
