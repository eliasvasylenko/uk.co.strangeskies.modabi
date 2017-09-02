package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildBindingPointConfigurationContext;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.InputInitializerBuilder;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.modabi.schema.OutputInitializerBuilder;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public class NodeBuilderImpl<T, E> implements NodeBuilder<T, E> {
  private final E parent;
  private final Boolean concrete;
  private final Boolean extensible;
  private final T provided;

  public NodeBuilderImpl(E parent) {
    this.parent = parent;
    this.concrete = null;
    this.extensible = null;
    this.provided = null;
  }

  public NodeBuilderImpl(E parent, Boolean concrete, Boolean extensible, T provided) {
    this.parent = parent;
    this.concrete = concrete;
    this.extensible = extensible;
    this.provided = provided;
  }

  @Override
  public InputInitializerBuilder initializeInput() {
    return new InputInitializerBuilderImpl();
  }

  @Override
  public OutputInitializerBuilder initializeOutput() {
    return new OutputInitializerBuilderImpl();
  }

  @Override
  public ChildBindingPointBuilder<?, NodeBuilder<T, E>> addChildBindingPoint() {
    ChildBindingPointConfigurationContext context = new ChildBindingPointConfigurationContext() {
      @Override
      public Namespace namespace() {
        return parent.getName().get().getNamespace();
      }

      @Override
      public Node<?> parentNode() {
        throw new UnsupportedOperationException();
      }

      @Override
      public Stream<ChildBindingPoint<?>> overrideChild(QualifiedName id) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public TypeToken<?> outputSourceType() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public TypeToken<?> inputTargetType() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Imports imports() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public BoundSet boundSet() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void addChildResult(ChildBindingPoint<?> result) {
        // TODO Auto-generated method stub

      }
    };

    return new ChildBindingPointBuilderImpl<>(context);
  }

  @Override
  public List<ChildBindingPointBuilder<?, NodeBuilder<T, E>>> getChildBindingPoints() {
    // TODO Auto-generated method stub
    return null;
  }

  public <U> OverrideBuilder<U> overrideChildren(
      Function<Node<T>, ? extends U> node,
      Function<NodeBuilder<T, ?>, Optional<? extends U>> builder) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NodeBuilder<T, E> concrete(boolean concrete) {
    return new NodeBuilderImpl<>(parent, concrete, extensible, provided);
  }

  @Override
  public Optional<Boolean> getConcrete() {
    return Optional.ofNullable(concrete);
  }

  @Override
  public NodeBuilder<T, E> extensible(boolean extensible) {
    return new NodeBuilderImpl<>(parent, concrete, extensible, provided);
  }

  @Override
  public Optional<Boolean> getExtensible() {
    return Optional.ofNullable(extensible);
  }

  @Override
  public E endNode() {
    // TODO Auto-generated method stub
    return parent;
  }

  @Override
  public NodeBuilder<T, E> provideValue(TypedObject<?> value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<T> getProvidedValue() {
    // TODO Auto-generated method stub
    return null;
  }
}
