package uk.co.strangeskies.modabi.schema.impl;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Collection;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelImpl<T> implements Model<T> {
  private final NodeImpl rootNode;
  private final QualifiedName name;
  private final boolean export;
  private final Collection<Model<? super T>> baseModels;
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
    baseModels = configurator.getBaseModel().collect(toList());
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
  public Stream<Model<? super T>> baseModels() {
    return baseModels.stream();
  }

  @Override
  public String toString() {
    return Model.class.getSimpleName() + "(" + name() + ")";
  }
}
