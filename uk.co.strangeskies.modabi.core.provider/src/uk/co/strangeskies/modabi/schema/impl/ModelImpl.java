package uk.co.strangeskies.modabi.schema.impl;

import static java.util.Optional.of;
import static uk.co.strangeskies.modabi.schema.Permission.OPEN;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Permission;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.text.parsing.Parser;

public class ModelImpl<T> implements Model<T> {
  private final QualifiedName name;
  private final boolean partial;
  private final Permission permission;
  private final Model<? super T> baseModel;
  private final TypeToken<T> dataType;

  @SuppressWarnings("unchecked")
  protected ModelImpl(ModelBuilderImpl configurator) {
    name = configurator.getName().get();
    partial = configurator
        .overrideModelChildren(Model::partial, b -> of(b.isPartial()))
        .or(false)
        .get();
    permission = configurator
        .overrideModelChildren(Model::permission, ModelBuilder::getPermission)
        .or(OPEN)
        .get();
    dataType = configurator
        .overrideModelChildren(Model::type, ModelBuilder::getDataType)
        .mergeOverride((o, b) -> o.withConstraintTo(SUBTYPE, b))
        .or(forClass(Object.class))
        .tryGet()
        .map(t -> (TypeToken<T>) t)
        .get();
    Model<?> baseModel = configurator.getBaseModelImpl().orElse(null); // TODO some root model
    this.baseModel = (Model<? super T>) baseModel;
  }

  @Override
  public QualifiedName name() {
    return name;
  }

  @Override
  public boolean partial() {
    return partial;
  }

  @Override
  public Permission permission() {
    return permission;
  }

  @Override
  public TypeToken<T> type() {
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

  @Override
  public boolean anonymous() {
    return true;
  }

  @Override
  public Parser<?> parser() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<Child<?>> children() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<Child<?>> descendents(List<QualifiedName> names) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Child<?> child(QualifiedName name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<?> providedValue() {
    // TODO Auto-generated method stub
    return null;
  }
}
