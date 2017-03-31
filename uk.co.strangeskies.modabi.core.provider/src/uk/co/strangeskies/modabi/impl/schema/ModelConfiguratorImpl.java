package uk.co.strangeskies.modabi.impl.schema;

import java.util.Collection;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelConfiguratorImpl<T> extends BindingPointConfiguratorImpl<T, ModelConfigurator<T>>
		implements ModelConfigurator<T> {
	private final Schema schema;

	public ModelConfiguratorImpl(DataLoader loader, Schema schema, Imports imports) {
		this.schema = schema;
	}

	@Override
	public ModelConfigurator<T> copy() {
		// TODO Auto-generated method stub
		return null;
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
}
