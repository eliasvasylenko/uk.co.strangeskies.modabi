package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.data.DataBindingTypes;
import uk.co.strangeskies.modabi.data.impl.DataBindingTypeBuilderImpl;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.Models;
import uk.co.strangeskies.modabi.schema.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.schema.model.building.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.schema.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.schemata.CoreSchemata;
import uk.co.strangeskies.utilities.collection.HashSetMultiHashMap;
import uk.co.strangeskies.utilities.collection.SetMultiMap;

public class SchemaManagerImpl implements SchemaManager {
	private final List<Function<Class<?>, Object>> providers;
	private final SetMultiMap<Model<?>, BindingFuture<?>> bindingFutures;

	private final CoreSchemata coreSchemata;

	final Models registeredModels;
	final DataBindingTypes registeredTypes;
	private final Schemata registeredSchema;

	public SchemaManagerImpl() {
		this(new SchemaBuilderImpl(), new ModelBuilderImpl(),
				new DataBindingTypeBuilderImpl());
	}

	public SchemaManagerImpl(SchemaBuilder schemaBuilder,
			ModelBuilder modelBuilder, DataBindingTypeBuilder dataTypeBuilder) {
		providers = new ArrayList<>();
		bindingFutures = new HashSetMultiHashMap<>(); // TODO make synchronous

		coreSchemata = new CoreSchemata(schemaBuilder, modelBuilder,
				dataTypeBuilder);

		registeredSchema = new Schemata();
		registeredModels = new Models();
		registeredTypes = new DataBindingTypes();

		registerSchema(coreSchemata.baseSchema());
		registerSchema(coreSchemata.metaSchema());

		registerProvider(DataBindingTypeBuilder.class, () -> dataTypeBuilder);
		registerProvider(ModelBuilder.class, () -> modelBuilder);
		registerProvider(SchemaBuilder.class, () -> schemaBuilder);

		registerProvider(Set.class, HashSet::new);
		registerProvider(List.class, ArrayList::new);
		registerProvider(Map.class, HashMap::new);
	}

	@Override
	public void registerSchema(Schema schema) {
		if (registeredSchema.add(schema)) {
			for (Schema dependency : schema.getDependencies())
				registerSchema(dependency);

			for (Model<?> model : schema.getModels())
				registerModel(model);

			for (DataBindingType<?> type : schema.getDataTypes())
				registerDataType(type);
		}
	}

	private void registerModel(Model<?> model) {
		registeredModels.add(model);
	}

	private void registerDataType(DataBindingType<?> type) {
		registeredTypes.add(type);
	}

	@Override
	public void registerBinding(Binding<?> binding) {
		// TODO Auto-generated method stub

	}

	public Set<BindingFuture<?>> getBindings(Model<?> model) {
		return bindingFutures.get(model);
	}

	@Override
	public MetaSchema getMetaSchema() {
		return coreSchemata.metaSchema();
	}

	@Override
	public BaseSchema getBaseSchema() {
		return coreSchemata.baseSchema();
	}

	@Override
	public <T> BindingFuture<T> bindFuture(Model<T> model,
			StructuredDataSource input) {
		return addBindingFuture(new SchemaBinder(this).bind(model.effective(),
				input));
	}

	@Override
	public BindingFuture<?> bindFuture(StructuredDataSource input) {
		return addBindingFuture(new SchemaBinder(this).bind(
				registeredModels.get(input.peekNextChild()).effective(), input));
	}

	private <T> BindingFuture<T> addBindingFuture(BindingFuture<T> binding) {
		bindingFutures.add(binding.getModel(), binding);
		new Thread(() -> {
			try {
				binding.get();
			} catch (CancellationException e) {
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			bindingFutures.remove(binding);
		});
		return binding;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Set<BindingFuture<T>> bindingFutures(Model<T> model) {
		return new HashSet<>(bindingFutures.get(model).stream()
				.map(t -> (BindingFuture<T>) t).collect(Collectors.toSet()));
	}

	@Override
	public <T> void unbind(Model<T> model, StructuredDataTarget output, T data) {
		new SchemaUnbinder<>(this, model, output, data);
	}

	// TODO disallow provider registrations overriding built-in providers
	@Override
	public <T> void registerProvider(Class<T> providedClass, Supplier<T> provider) {
		registerProvider(c -> c.equals(providedClass) ? provider.get() : null);
	}

	@Override
	public void registerProvider(Function<Class<?>, ?> provider) {
		providers.add(c -> {
			Object provided = provider.apply(c);
			if (provided != null && !c.isInstance(provided))
				throw new SchemaException("Invalid object provided for the class [" + c
						+ "] by provider [" + provider + "]");
			return provided;
		});
	}

	@SuppressWarnings("unchecked")
	protected <T> T provide(Class<T> clazz) {
		return (T) providers
				.stream()
				.map(p -> p.apply(clazz))
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow(
						() -> new SchemaException("No provider exists for the class "
								+ clazz));
	}
}
