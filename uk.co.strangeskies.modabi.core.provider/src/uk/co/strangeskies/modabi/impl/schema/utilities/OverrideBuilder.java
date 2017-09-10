package uk.co.strangeskies.modabi.impl.schema.utilities;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static uk.co.strangeskies.collection.stream.StreamUtilities.upcastOptional;
import static uk.co.strangeskies.modabi.ModabiException.MESSAGES;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.collection.stream.StreamUtilities;
import uk.co.strangeskies.modabi.ModabiException;

/**
 * A class for building override rules to determine the value of a property of a
 * schema node. A "property", in this context, may include for example the
 * {@link S#name() name} of a node or the {@link BindingNode#dataType() type} of
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
  private final Set<T> inheritedValues;
  private final Optional<T> override;

  private final BiFunction<? super T, ? super T, ? extends T> mergeOverride;
  private final boolean concrete;
  private final Supplier<String> propertyName;

  public OverrideBuilder(
      Collection<? extends T> overridden,
      Optional<? extends T> override,
      Supplier<String> propertyName) {
    this.inheritedValues = new HashSet<>(overridden);
    if (!override.isPresent() && inheritedValues.size() == 1) {
      this.override = of(inheritedValues.iterator().next());
    } else {
      this.override = upcastOptional(override);
    }

    this.mergeOverride = validateOverrideFunction(Objects::equals);
    this.concrete = true;
    this.propertyName = propertyName;
  }

  private OverrideBuilder(
      Supplier<String> propertyName,
      Set<T> inheritedValues,
      Optional<T> override,
      BiFunction<? super T, ? super T, ? extends T> mergeOverride,
      boolean concrete) {
    this.propertyName = propertyName;
    this.inheritedValues = inheritedValues;
    this.override = override;

    this.mergeOverride = mergeOverride;
    this.concrete = concrete;
  }

  /**
   * @return all unique values inherited from overridden and base nodes, or given
   *         directly from their configurators
   */
  public Set<T> getValues() {
    return inheritedValues;
  }

  public OverrideBuilder<T> orDefault(T value) {
    if (concrete) {
      return or(value);
    } else {
      return this;
    }
  }

  public OverrideBuilder<T> orMerged(Function<? super Collection<? extends T>, ? extends T> merge) {
    if (!inheritedValues.isEmpty() && !isOverridden()) {
      T merged = merge.apply(inheritedValues);

      if (merged == null) {
        throw new ModabiException(
            MESSAGES.cannotMergeIncompatibleProperties(propertyName.get(), inheritedValues));
      }

      return or(merged);
    } else {
      return this;
    }
  }

  public OverrideBuilder<T> orMerged(BinaryOperator<T> merge) {
    return orMerged(
        s -> StreamUtilities.<T>upcastStream(s.stream()).reduce(merge).orElseThrow(
            () -> new ModabiException(
                MESSAGES.cannotMergeIncompatibleProperties(propertyName.get(), inheritedValues))));
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
   * @param mergeOverride
   *          the override merge function
   * @return a new builder instance incorporating the given override behavior
   */
  public OverrideBuilder<T> validateOverride(BiPredicate<? super T, ? super T> validation) {
    return mergeOverride(validateOverrideFunction(validation));
  }

  private BiFunction<? super T, ? super T, ? extends T> validateOverrideFunction(
      BiPredicate<? super T, ? super T> validation) {
    return (a, b) -> {
      if (!validation.test(a, b)) {
        throw new ModabiException(
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
      BiFunction<? super T, ? super T, ? extends T> mergeOverride) {
    return new OverrideBuilder<>(propertyName, inheritedValues, override, mergeOverride, concrete);
  }

  public OverrideBuilder<T> or(T value) {
    return isOverridden()
        ? this
        : new OverrideBuilder<>(
            propertyName,
            inheritedValues,
            ofNullable(value),
            mergeOverride,
            concrete);
  }

  public Optional<T> tryGet() {
    T value = override.get();

    if (isOverridden() && mergeOverride != null) {
      for (T inheritedValue : inheritedValues) {
        value = mergeOverride.apply(value, inheritedValue);
      }
    }

    return Optional.ofNullable(value);
  }

  public T get() {
    T value = tryGet().orElse(null);

    if (value == null && concrete) {
      if (inheritedValues.isEmpty()) {
        throw new ModabiException(MESSAGES.mustProvideValueForNonAbstract(propertyName.get()));
      } else {
        throw new ModabiException(
            MESSAGES.mustOverrideIncompatibleProperties(propertyName.get(), inheritedValues));
      }
    }

    return value;
  }

  private boolean isOverridden() {
    return override.isPresent();
  }
}
