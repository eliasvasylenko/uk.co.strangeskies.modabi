package uk.co.strangeskies.modabi.impl.schema;

import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelImpl<T> implements Model<T> {
  private final NodeImpl<T> rootNode;
  private final Schema schema;
  private final QualifiedName name;
  private final boolean export;

  protected ModelImpl(ModelBuilderImpl<T> configurator) {
    rootNode = configurator.getRootNode();
    schema = configurator.getSchema();
    name = configurator.getName().get();
    export = configurator.overrideModelChildren(Model::export, ModelBuilder::getExport).get();
  }

  @Override
  public Schema schema() {
    return schema;
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<Model<?>> baseModels() {
    // TODO Auto-generated method stub
    return null;
  }
}
