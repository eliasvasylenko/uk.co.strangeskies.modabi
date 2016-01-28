package uk.co.strangeskies.modabi;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.processing.BindingFuture;

public interface Binder<T> {
	BindingFuture<T> from(StructuredDataSource input);

	// BindingFuture<T> from(RewritableStructuredData input);

	default BindingFuture<T> from(File input) {
		return from(input.toURI());
	}

	default BindingFuture<T> from(URI input) {
		try {
			return from(input.toURL());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	BindingFuture<T> from(URL input);

	BindingFuture<T> from(InputStream input);

	BindingFuture<T> from(String extension, InputStream input);

	// Binder<T> updatable();

	Binder<T> with(ClassLoader classLoader);

	/*
	 * Errors which are rethrown will be passed to the next error handler if
	 * present, or dealt with as normal. Otherwise, a best effort is made at
	 * binding.
	 */
	Binder<T> with(Consumer<Exception> errorHandler);
}
