package uk.co.strangeskies.modabi.binding.impl;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.strangeskies.function.ThrowingSupplier;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.observable.Observable;

public class StructuredDataFuture<T extends StructuredDataReader> extends CompletableFuture<T> {
  private StructuredDataFuture(T structuredData) {
    complete(structuredData);
  }

  private <C extends AutoCloseable> StructuredDataFuture(
      BindingContextImpl context,
      DataFormats formats,
      String formatId,
      ThrowingSupplier<C, ?> input,
      Predicate<DataFormat> formatPredicate,
      boolean canRetry,
      BiFunction<DataFormat, C, T> s) {
    Set<Exception> exceptions = new HashSet<>();

    Function<DataFormat, Observable<T>> getReader = format -> {
      try (C inputStream = input.get()) {
        return Observable.of(s.apply(format, inputStream));
      } catch (Exception e) {
        if (canRetry) {
          exceptions.add(e);
          return Observable.of();
        } else {
          throw new RuntimeException(e);
        }
      }
    };

    formats
        .getAllFuture()
        .executeOn(newSingleThreadExecutor())
        .filter(formatPredicate)
        .concatMap(getReader)
        .getNext()
        .whenComplete((r, t) -> {
          if (r != null) {
            complete(r);
          } else {
            for (Exception e : exceptions)
              t.addSuppressed(e);

            completeExceptionally(
                new BindingException(
                    formatId == null
                        ? MESSAGES.noFormatFound()
                        : MESSAGES.noFormatFoundFor(formatId),
                    context,
                    t));
          }
        });
  }

  public static <T extends StructuredDataReader> StructuredDataFuture<T> forData(T structuredData) {
    return new StructuredDataFuture<>(structuredData);
  }

  public static StructuredDataFuture<StructuredDataReader> forDataReader(
      BindingContextImpl context,
      DataFormats formats,
      String formatId,
      ThrowingSupplier<ReadableByteChannel, ?> input,
      Predicate<DataFormat> formatPredicate,
      boolean canRetry) {
    return new StructuredDataFuture<>(
        context,
        formats,
        formatId,
        input,
        formatPredicate,
        canRetry,
        (f, c) -> f.readData(c));
  }

  public static StructuredDataFuture<StructuredDataWriter> forDataWriter(
      BindingContextImpl context,
      DataFormats formats,
      String formatId,
      ThrowingSupplier<WritableByteChannel, ?> input,
      Predicate<DataFormat> formatPredicate,
      boolean canRetry) {
    return new StructuredDataFuture<>(
        context,
        formats,
        formatId,
        input,
        formatPredicate,
        canRetry,
        (f, c) -> f.writeData(c));
  }
}
