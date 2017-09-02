package uk.co.strangeskies.modabi.impl.processing;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.observable.Observable;

public class DataReaderFuture extends CompletableFuture<StructuredDataReader> {
  private static final QualifiedName FORMAT_BLOCK_NAMESPACE = new QualifiedName(
      "structuredDataFormat",
      Schema.MODABI_NAMESPACE);

  public DataReaderFuture(StructuredDataReader dataReader) {
    complete(dataReader);
  }

  public DataReaderFuture(
      ProcessingContextImpl context,
      DataFormats formats,
      String formatId,
      ThrowingSupplier<InputStream, ?> input,
      Predicate<DataFormat> formatPredicate,
      boolean canRetry) {
    Set<Exception> exceptions = new HashSet<>();

    Function<DataFormat, Observable<StructuredDataReader>> getReader = format -> {
      try (InputStream inputStream = input.get()) {
        StructuredDataReader source = format.loadData(inputStream);

        return Observable.of(source);
      } catch (Exception e) {
        if (canRetry) {
          exceptions.add(e);
          return Observable.of();
        } else {
          throw new RuntimeException(e);
        }
      }
    };

    context
        .bindingBlocker()
        .block(
            FORMAT_BLOCK_NAMESPACE,
            formatId,
            false,
            formats
                .getAllFuture()
                .executeOn(newSingleThreadExecutor())
                .filter(formatPredicate)
                .flatMap(getReader)
                .getNext())
        .thenAccept(this::complete)
        .exceptionally(t -> {
          for (Exception e : exceptions)
            t.addSuppressed(e);

          completeExceptionally(
              new ProcessingException(
                  formatId == null ? MESSAGES.noFormatFound() : MESSAGES.noFormatFoundFor(formatId),
                  context,
                  t));

          return null;
        });
  }
}
