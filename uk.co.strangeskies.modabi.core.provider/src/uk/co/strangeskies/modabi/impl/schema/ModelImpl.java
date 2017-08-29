package uk.co.strangeskies.modabi.impl.schema;

import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelImpl<T> extends NodeImpl<T> implements Model<T> {
  private final Schema schema;
  private final QualifiedName name;
  private final boolean export;

  protected ModelImpl(ModelBuilderImpl<T, ?> configurator) {
    super(configurator);

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
  public TypeToken<T> dataType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<Model<?>> baseModel() {
    // TODO Auto-generated method stub
    return null;
  }
}
