package uk.co.strangeskies.modabi;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingFuture;

public interface Unbinder<T> {
	<U extends StructuredDataTarget> U to(U output);

	// BindingFuture<T> to(RewritableStructuredData output);

	default BindingFuture<T> to(File output) {
		return to(output.toURI());
	}

	default BindingFuture<T> to(URI output) {
		try {
			return to(output.toURL());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	default BindingFuture<T> to(URL output) {
		String extension = output.getQuery();
		int lastDot = extension.lastIndexOf('.');
		if (lastDot > 0) {
			extension = extension.substring(lastDot);
		} else {
			throw new IllegalArgumentException(
					"No recognisable extension for file'" + output + "', data interface cannot be selected");
		}

		try (OutputStream fileStream = output.openConnection().getOutputStream()) {
			BindingFuture<T> binding = to(extension, fileStream);
			fileStream.flush();
			return binding;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	BindingFuture<T> to(String extension, OutputStream output);

	// Unbinder<T> updatable();

	/*
	 * Errors which are rethrown will be passed to the next error handler if
	 * present, or dealt with as normal. Otherwise, a best effort is made at
	 * unbinding, and the exception information will be serialised as a comment.
	 */
	Unbinder<T> with(Consumer<Exception> errorHandler);
}
