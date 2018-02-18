package uk.co.strangeskies.modabi.schema.impl;

import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelImpl<T> implements Model<T> {
  private final NodeImpl rootNode;
  private final QualifiedName name;
  private final boolean export;
  private final Model<? super T> baseModel;
  private final TypeToken<T> dataType;

  @SuppressWarnings("unchecked")
  protected ModelImpl(ModelBuilderImpl<T> configurator) {
    rootNode = configurator.getRootNodeImpl().orElse(new NodeImpl());
    name = configurator.getName().get();
    export = configurator
        .overrideModelChildren(Model::export, ModelBuilder::getExport)
        .or(true)
        .get();
    dataType = configurator
        .overrideModelChildren(Model::dataType, ModelBuilder::getDataType)
        .mergeOverride((o, b) -> o.withConstraintTo(SUBTYPE, b))
        .or(forClass(Object.class))
        .tryGet()
        .map(t -> (TypeToken<T>) t)
        .get();
    baseModel = configurator.getBaseModel().orElse(null); // TODO some root model
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
  public Node rootNode() {
    return rootNode;
  }

  @Override
  public TypeToken<T> dataType() {
    return dataType;
  }

  @Override
  public Model<? super T> baseModel() {
    return baseModel;
  }

  @Override
  public String toString() {
    return Model.class.getSimpleName() + "(" + name() + ")";
  }
}
