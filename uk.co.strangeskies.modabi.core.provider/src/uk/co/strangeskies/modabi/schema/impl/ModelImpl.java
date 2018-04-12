package uk.co.strangeskies.modabi.schema.impl;

import static java.util.stream.Stream.concat;
import static uk.co.strangeskies.modabi.schema.Permission.OPEN;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.ModabiSchemaException;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Permission;
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

    partial = configurator.isPartial();

    permission = configurator
        .getPermission()
        .or(() -> configurator.getBaseModelImpl().map(Model::permission))
        .orElse(OPEN);

    TypeToken<?> dataType = concat(
        configurator.getDataType().stream(),
        configurator.getBaseModelImpl().map(Model::type).stream())
            .reduce((o, b) -> o.withConstraintTo(SUBTYPE, b))
            .orElseThrow(() -> new ModabiSchemaException(""));
    this.dataType = (TypeToken<T>) dataType;

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
