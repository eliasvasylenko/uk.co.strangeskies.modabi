package uk.co.strangeskies.modabi.impl.schema;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.ModelFactory;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelConfiguratorImpl extends BindingPointConfiguratorImpl<ModelConfigurator>
    implements ModelConfigurator {
  private final Schema schema;

  public ModelConfiguratorImpl(DataLoader loader, Schema schema, Imports imports) {
    this.schema = schema;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> ModelConfigurator<V> dataType(TypeToken<V> dataType) {
    return (ModelConfigurator<V>) super.dataType(dataType);
  }

  @Override
  public ModelConfigurator<?> baseModel(Collection<? extends Model<?>> baseModel) {
    return (ModelConfigurator<?>) super.baseModel(baseModel);
  }

  @Override
  public Model<T> create() {
    return new ModelImpl<>(this);
  }

  @Override
  protected Stream<Model<?>> getOverriddenBindingPoints() {
    return getBaseModel();
  }

  public Schema getSchema() {
    return schema;
  }

  @Override
  public Optional<QualifiedName> getNode() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <V> SchemaNodeConfigurator<V, ModelFactory<V>> type(TypeToken<V> dataType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <V> ModelFactory<V> withoutNode(TypeToken<V> dataType) {
    // TODO Auto-generated method stub
    return null;
  }
}
