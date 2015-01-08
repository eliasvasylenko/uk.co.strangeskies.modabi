package uk.co.strangeskies.modabi.schema.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.modabi.schema.impl.CoreSchemata;
import uk.co.strangeskies.modabi.schema.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.schema.management.Provisions;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.management.binding.BindingFuture;
import uk.co.strangeskies.modabi.schema.management.binding.impl.SchemaBinder;
import uk.co.strangeskies.modabi.schema.management.unbinding.impl.SchemaUnbinder;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.ModelBuilder;
import uk.co.strangeskies.modabi.schema.node.model.Models;
import uk.co.strangeskies.modabi.schema.node.model.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypes;
import uk.co.strangeskies.modabi.schema.node.type.impl.DataBindingTypeBuilderImpl;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

import com.google.common.reflect.TypeToken;

@Component
public class SchemaManagerImpl implements SchemaManager {
	private final List<Function<TypeLiteral<?>, Object>> providers;
	private final MultiMap<Model<?>, BindingFuture<?>, Set<BindingFuture<?>>> bindingFutures;

	private final CoreSchemata coreSchemata;

	final Models registeredModels;
	final DataBindingTypes registeredTypes;
	private final Schemata registeredSchemata;

	public SchemaManagerImpl() {
		this(new SchemaBuilderImpl(), new ModelBuilderImpl(),
				new DataBindingTypeBuilderImpl());
	}

	public SchemaManagerImpl(SchemaBuilder schemaBuilder,
			ModelBuilder modelBuilder, DataBindingTypeBuilder dataTypeBuilder) {
		providers = new ArrayList<>();
		bindingFutures = new MultiHashMap<>(HashSet::new); // TODO make synchronous

		coreSchemata = new CoreSchemata(schemaBuilder, modelBuilder,
				dataTypeBuilder);

		registeredSchemata = new Schemata();
		registeredModels = new Models();
		registeredTypes = new DataBindingTypes();

		registerSchema(coreSchemata.baseSchema());
		registerSchema(coreSchemata.metaSchema());

		registerProvider(DataBindingTypeBuilder.class, () -> dataTypeBuilder);
		registerProvider(ModelBuilder.class, () -> modelBuilder);
		registerProvider(SchemaBuilder.class, () -> schemaBuilder);

		registerProvider(Set.class, HashSet::new);
		registerProvider(LinkedHashSet.class, LinkedHashSet::new);
		registerProvider(List.class, ArrayList::new);
		registerProvider(Map.class, HashMap::new);
	}

	@Override
	public void registerSchema(Schema schema) {
		if (registeredSchemata.add(schema)) {
			for (Schema dependency : schema.getDependencies())
				registerSchema(dependency);

			for (Model<?> model : schema.getModels())
				registerModel(model);

			for (DataBindingType<?> type : schema.getDataTypes())
				registerDataType(type);

			bindingFutures.add(coreSchemata.metaSchema().getSchemaModel(),
					BindingFuture.forData(coreSchemata.metaSchema().getSchemaModel(),
							schema));
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> BindingFuture<T> bindFuture(TypeLiteral<T> dataClass,
			StructuredDataSource input) {
		Model<?> model = registeredModels.get(input.peekNextChild());
		List<Model<T>> models = registeredModels.getModelsWithClass(dataClass);
		if (models.contains(model))
			throw new IllegalArgumentException("None of the models '" + model
					+ "' compatible with the class '" + dataClass
					+ "' match the root element '" + input.peekNextChild() + "'.");
		return (BindingFuture<T>) addBindingFuture(new SchemaBinder(this).bind(
				model.effective(), input));
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
		Set<BindingFuture<?>> modelBindings = bindingFutures.get(model);

		if (modelBindings == null)
			return new HashSet<>();
		else
			return new HashSet<>(modelBindings.stream()
					.map(t -> (BindingFuture<T>) t).collect(Collectors.toSet()));
	}

	@Override
	public <T> void unbind(Model<T> model, StructuredDataTarget output, T data) {
		new SchemaUnbinder(this).unbind(model.effective(), output, data);
	}

	@Override
	public void unbind(StructuredDataTarget output, Object data) {
		new SchemaUnbinder(this).unbind(output, data);
	}

	@Override
	public <T> void unbind(TypeLiteral<T> dataClass, StructuredDataTarget output,
			T data) {
		new SchemaUnbinder(this).unbind(output, dataClass, data);
	}

	@Override
	public <T> void registerProvider(TypeLiteral<T> providedClass,
			Supplier<T> provider) {
		registerProvider(c -> c.equals(providedClass) ? provider.get() : null);
	}

	@Override
	public void registerProvider(Function<TypeLiteral<?>, ?> provider) {
		providers.add(c -> {
			Object provided = provider.apply(c);
			if (provided != null
					&& !TypeToken.of(c.getType()).isAssignableFrom(provided.getClass()))
				throw new SchemaException("Invalid object provided for the class [" + c
						+ "] by provider [" + provider + "]");
			return provided;
		});
	}

	public Provisions provisions() {
		return new Provisions() {
			@Override
			@SuppressWarnings("unchecked")
			public <T> T provide(TypeLiteral<T> clazz) {
				return (T) providers
						.stream()
						.map(p -> p.apply(clazz))
						.filter(Objects::nonNull)
						.findFirst()
						.orElseThrow(
								() -> new SchemaException("No provider exists for the class "
										+ clazz));
			}

			@Override
			public boolean isProvided(TypeLiteral<?> clazz) {
				return providers.stream().map(p -> p.apply(clazz))
						.anyMatch(Objects::nonNull);
			}
		};
	}

	@Override
	public Schemata registeredSchemata() {
		return registeredSchemata;
	}

	@Override
	public Models registeredModels() {
		return registeredModels;
	}

	@Override
	public DataBindingTypes registeredTypes() {
		return registeredTypes;
	}
}
