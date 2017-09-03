package uk.co.strangeskies.modabi.impl.schema;

import java.util.Collection;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelImpl<T> implements Model<T> {
  private final NodeImpl<T> rootNode;
  private final QualifiedName name;
  private final boolean export;
  private final Collection<Model<?>> baseModels;
  private final TypeToken<T> dataType;

  protected ModelImpl(ModelBuilderImpl<T> configurator) {
    rootNode = configurator.getRootNode().orElse(new NodeImpl<>());
    name = configurator.getName().get();
    export = configurator
        .overrideModelChildren(Model::export, ModelBuilder::getExport)
        .orDefault(true)
        .get();
    baseModels = null;
    dataType = null;
    // dataType = configurator.overrideModelChildren(Model::dataType,
    // ModelBuilder::getDataType).get();
  }

  @Override
  public QualifiedName name() {
    return name;
  }

  @Override
  public boolean export() {
    return export;
  }

  @Override
  public Node<T> rootNode() {
    return rootNode;
  }

  @Override
  public TypeToken<T> dataType() {
    return dataType;
  }

  @Override
  public Stream<Model<?>> baseModels() {
    return baseModels.stream();
  }
}
