package uk.co.strangeskies.modabi.schema.impl;

import static uk.co.strangeskies.modabi.schema.ModabiSchemaException.MESSAGES;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.schema.ModabiSchemaException;

/**
 * A class for building override rules to determine the value of a property of a
 * schema node. A "property", in this context, may include for example the
 * {@link S#name() name} of a node or the {@link BindingNode#type() type} of
 * a binding node.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the value of the property to override
 * @param <N>
 *          the type of the node
 */
public class OverrideBuilder<T> {
  private final Supplier<String> propertyName;

  private final T inherited;
  private final T override;

  private final BiFunction<? super T, ? super T, ? extends T> overrideFunction;
  private final boolean concrete;

  public OverrideBuilder(
      Supplier<String> propertyName,
      Optional<? extends T> inherited,
      Optional<? extends T> override) {
    this.propertyName = propertyName;

    if (!override.isPresent()) {
      this.inherited = null;
      this.override = inherited.orElse(null);
    } else {
      this.inherited = inherited.orElse(null);
      this.override = override.orElse(null);
    }

    this.overrideFunction = validatingOverrideFunction(Objects::equals);
    this.concrete = true;
  }

  private OverrideBuilder(
      Supplier<String> propertyName,
      T inherited,
      T override,
      BiFunction<? super T, ? super T, ? extends T> mergeOverride,
      boolean concrete) {
    this.propertyName = propertyName;
    this.inherited = inherited;
    this.override = override;

    this.overrideFunction = mergeOverride;
    this.concrete = concrete;
  }

  public OverrideBuilder<T> or(T value) {
    return or(() -> value);
  }

  public OverrideBuilder<T> or(Supplier<T> value) {
    return override == null
        ? new OverrideBuilder<>(propertyName, inherited, value.get(), overrideFunction, concrete)
        : this;
  }

  public OverrideBuilder<T> orDefault(T value) {
    return concrete ? or(value) : this;
  }

  public OverrideBuilder<T> orDefault(Supplier<T> value) {
    return concrete ? or(value) : this;
  }

  /**
   * The override validation predicate takes two parameters, the first is a
   * potential overriding value, and the second is a value it must override. The
   * predicate passes if the override relationship cannot be satisfied.
   * 
   * <p>
   * If the predicate does not pass then an exception will be thrown
   * automatically.
   * 
   * @param overrideFunction
   *          the override merge function
   * @return a new builder instance incorporating the given override behavior
   */
  public OverrideBuilder<T> validateOverride(BiPredicate<? super T, ? super T> validationFunction) {
    return mergeOverride(validatingOverrideFunction(validationFunction));
  }

  private BiFunction<? super T, ? super T, ? extends T> validatingOverrideFunction(
      BiPredicate<? super T, ? super T> validation) {
    return (a, b) -> {
      if (!validation.test(a, b)) {
        throw new ModabiSchemaException(
            MESSAGES.cannotOverrideIncompatibleProperty(propertyName.get(), b, a));
      }

      return a;
    };
  }

  /**
   * The override merge function takes two parameters, the first is a potential
   * overriding value, and the second is a value it must override. The function
   * returns an overriding value which satisfies this relationship, or throws an
   * exception if the override relationship cannot be satisfied.
   * 
   * @param mergeOverride
   *          the override merge function
   * @return a new builder instance incorporating the given override behavior
   */
  public OverrideBuilder<T> mergeOverride(
      BiFunction<? super T, ? super T, ? extends T> overrideFunction) {
    return new OverrideBuilder<>(propertyName, inherited, override, overrideFunction, concrete);
  }

  public Optional<T> tryGet() {
    if (override == null)
      return Optional.empty();

    if (inherited == null)
      return Optional.of(override);

    return Optional.of(overrideFunction.apply(override, inherited));
  }

  public T get() {
    T value = tryGet().orElse(null);

    if (value == null && concrete) {
      throw new ModabiSchemaException(MESSAGES.mustProvideValueForNonAbstract(propertyName.get()));
    }

    return value;
  }
}
