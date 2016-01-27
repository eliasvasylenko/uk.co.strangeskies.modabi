/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.DataTypes;
import uk.co.strangeskies.modabi.GeneratedSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.impl.processing.BindingContextImpl;
import uk.co.strangeskies.modabi.impl.processing.BindingProviders;
import uk.co.strangeskies.modabi.impl.processing.SchemaBinder;
import uk.co.strangeskies.modabi.impl.processing.SchemaUnbinder;
import uk.co.strangeskies.modabi.impl.schema.building.DataTypeBuilderImpl;
import uk.co.strangeskies.modabi.impl.schema.building.ModelBuilderImpl;
import uk.co.strangeskies.modabi.io.structured.RewritableStructuredData;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingContext;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.BindingState;
import uk.co.strangeskies.modabi.processing.UnbindingState;
import uk.co.strangeskies.modabi.processing.providers.DereferenceSource;
import uk.co.strangeskies.modabi.processing.providers.ImportSource;
import uk.co.strangeskies.modabi.processing.providers.IncludeTarget;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.modabi.schema.building.DataTypeBuilder;
import uk.co.strangeskies.modabi.schema.building.ModelBuilder;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.reflection.TypedObject;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;

@Component(immediate = true)
public class SchemaManagerImpl implements SchemaManager {
	private final List<Function<TypeToken<?>, Object>> providers;
	private final MultiMap<QualifiedName, BindingFuture<?>, Set<BindingFuture<?>>> bindingFutures;

	private final CoreSchemata coreSchemata;

	private final Models registeredModels;
	private final DataTypes registeredTypes;
	private final Schemata registeredSchemata;

	private final ModelBuilder modelBuilder;
	private final DataTypeBuilder dataTypeBuilder;

	private final BindingProviders bindingProviders;

	private final Map<String, StructuredDataFormat> dataInterfaces;

	public SchemaManagerImpl() {
		this(new SchemaBuilderImpl(), new ModelBuilderImpl(),
				new DataTypeBuilderImpl());
	}

	public SchemaManagerImpl(SchemaBuilder schemaBuilder,
			ModelBuilder modelBuilder, DataTypeBuilder dataTypeBuilder) {
		this.modelBuilder = modelBuilder;
		this.dataTypeBuilder = dataTypeBuilder;

		providers = new ArrayList<>();
		bindingFutures = new MultiHashMap<>(HashSet::new); // TODO make synchronous

		coreSchemata = new CoreSchemata(schemaBuilder, modelBuilder,
				dataTypeBuilder);

		registeredSchemata = new Schemata();
		registeredModels = new Models();
		registeredTypes = new DataTypes();

		registerProvider(DataTypeBuilder.class, () -> dataTypeBuilder);
		registerProvider(ModelBuilder.class, () -> modelBuilder);
		registerProvider(SchemaBuilder.class, () -> schemaBuilder);

		registerProvider(new TypeToken<@Infer SortedSet<?>>() {}, TreeSet::new);
		registerProvider(new TypeToken<@Infer Set<?>>() {}, HashSet::new);
		registerProvider(new TypeToken<@Infer LinkedHashSet<?>>() {},
				LinkedHashSet::new);
		registerProvider(new TypeToken<@Infer List<?>>() {}, ArrayList::new);
		registerProvider(new TypeToken<@Infer Map<?, ?>>() {}, HashMap::new);

		bindingProviders = new BindingProviders(this);

		dataInterfaces = new HashMap<>();

		registerSchema(coreSchemata.metaSchema());
	}

	SchemaBinder getSchemaBinder() {
		return new SchemaBinder(getBindingContext());
	}

	BindingContextImpl getBindingContext() {
		return new BindingContextImpl(this)
				.withProvision(DereferenceSource.class,
						bindingProviders.dereferenceSource())
				.withProvision(IncludeTarget.class, bindingProviders.includeTarget())
				.withProvision(ImportSource.class, bindingProviders.importSource())
				.withProvision(DataLoader.class, bindingProviders.dataLoader())
				.withProvision(Imports.class, bindingProviders.imports())
				.withProvision(BindingContext.class, c -> c);
	}

	ModelBuilder getModelBuilder() {
		return modelBuilder;
	}

	DataTypeBuilder getDataTypeBuilder() {
		return dataTypeBuilder;
	}

	private boolean registerSchemaImpl(Schema schema) {
		if (registeredSchemata.add(schema)) {
			for (Model<?> model : schema.getModels())
				registerModel(model);

			for (DataType<?> type : schema.getDataTypes())
				registerDataType(type);

			for (Schema dependency : schema.getDependencies())
				registerSchema(dependency);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean registerSchema(Schema schema) {
		if (registerSchemaImpl(schema)) {
			bindingFutures.add(coreSchemata.metaSchema().getSchemaModel().getName(),
					registerBinding(coreSchemata.metaSchema().getSchemaModel(), schema));

			return true;
		} else {
			return false;
		}
	}

	@Override
	public Binder<Schema> bindSchema() {
		Binder<Schema> binder = bind(getMetaSchema().getSchemaModel());

		return new Binder<Schema>() {
			@Override
			public BindingFuture<Schema> from(StructuredDataSource input) {
				return registerFuture(binder.from(input));
			}

			@Override
			public BindingFuture<Schema> from(URL input) {
				return registerFuture(binder.from(input));
			}

			@Override
			public BindingFuture<Schema> from(InputStream input) {
				return registerFuture(binder.from(input));
			}

			@Override
			public BindingFuture<Schema> from(String extension, InputStream input) {
				return registerFuture(binder.from(extension, input));
			}

			private BindingFuture<Schema> registerFuture(
					BindingFuture<Schema> schema) {
				return new BindingFuture<Schema>() {
					@Override
					public boolean cancel(boolean mayInterruptIfRunning) {
						return schema.cancel(mayInterruptIfRunning);
					}

					@Override
					public boolean isCancelled() {
						return schema.isCancelled();
					}

					@Override
					public boolean isDone() {
						return schema.isDone();
					}

					@Override
					public QualifiedName getName() {
						return schema.getName();
					}

					@Override
					public Model<Schema> getModel() {
						return schema.getModel();
					}

					@Override
					public Set<BindingFuture<?>> getBlockingBindings() {
						return schema.getBlockingBindings();
					}

					@Override
					public Binding<Schema> get() {
						return register(schema.get());
					}

					@Override
					public Binding<Schema> get(long timeout, TimeUnit unit) {
						return register(schema.get(timeout, unit));
					}

					private Binding<Schema> register(Binding<Schema> binding) {
						registerSchemaImpl(schema.resolve());
						bindingFutures.add(
								coreSchemata.metaSchema().getSchemaModel().getName(), schema);
						return binding;
					}
				};
			}

			@Override
			public Binder<Schema> with(Consumer<Exception> errorHandler) {
				binder.with(errorHandler);
				return this;
			}
		};
	}

	void registerModel(Model<?> model) {
		registeredModels.add(model);
	}

	void registerDataType(DataType<?> type) {
		registeredTypes.add(type);
	}

	@Override
	public <T> BindingFuture<T> registerBinding(Model<T> model, T data) {
		return registerBindingImpl(new Binding<T>() {
			@Override
			public Model<T> getModel() {
				return model;
			}

			@Override
			public T getData() {
				return data;
			}
		});
	}

	protected <T> BindingFuture<T> registerBindingImpl(Binding<T> binding) {
		BindingFuture<T> future = BindingFuture.forBinding(binding);
		bindingFutures.add(binding.getModel().getName(), future);
		return future;
	}

	@Override
	public MetaSchema getMetaSchema() {
		return coreSchemata.metaSchema();
	}

	@Override
	public BaseSchema getBaseSchema() {
		return coreSchemata.baseSchema();
	}

	private <T> Binder<T> createBinder(
			Function<StructuredDataSource, BindingFuture<T>> bindingFunction) {
		return new Binder<T>() {
			@Override
			public BindingFuture<T> from(StructuredDataSource input) {
				return bindingFunction.apply(input);
			}

			@Override
			public BindingFuture<T> from(URL input) {
				String extension = input.getPath();
				int lastSlash = extension.lastIndexOf('/');
				if (lastSlash > 0) {
					extension = extension.substring(lastSlash);

					int lastDot = extension.lastIndexOf('.');
					if (lastDot > 0) {
						extension = extension.substring(lastDot + 1);
					} else {
						extension = null;
					}
				} else {
					extension = null;
				}

				try (InputStream fileStream = input.openStream()) {
					if (extension != null) {
						return from(extension, fileStream);
					} else {
						return from(fileStream);
					}
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
			}

			@Override
			public BindingFuture<T> from(InputStream input) {
				return from(input, getRegisteredDataInterfaces());
			}

			@Override
			public BindingFuture<T> from(String extension, InputStream input) {
				return from(input, getDataInterfaces(extension));
			}

			private BindingFuture<T> from(InputStream input,
					Collection<? extends StructuredDataFormat> loaders) {
				BufferedInputStream bufferedInput = new BufferedInputStream(input);
				bufferedInput.mark(4096);

				if (loaders.isEmpty())
					throw new IllegalArgumentException(
							"No valid file loader registered for input");

				Exception exception = null;

				for (StructuredDataFormat loader : loaders) {
					try {
						return from(loader.loadData(bufferedInput));
					} catch (Exception e) {
						exception = e;
					}
					try {
						bufferedInput.reset();
					} catch (IOException e) {
						throw new IllegalArgumentException(
								"Problem buffering input for binding", e);
					}
				}

				throw new IllegalArgumentException(
						"Could not bind input with any registered file loaders", exception);
			}

			@Override
			public Binder<T> with(Consumer<Exception> errorHandler) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public <T> Binder<T> bind(Model<T> model) {
		return createBinder(input -> addBindingFuture(
				getSchemaBinder().bind(model.effective(), input)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Binder<T> bind(TypeToken<T> dataClass) {
		return createBinder(input -> {
			Model<?> model = registeredModels.get(input.peekNextChild());

			if (model == null) {
				throw new IllegalArgumentException(
						"No model found to match the root element '" + input.peekNextChild()
								+ "'");
			}

			List<Model<T>> models = registeredModels.getModelsWithClass(dataClass);

			if (!models.contains(model)) {
				throw new IllegalArgumentException("None of the models '" + models
						+ "' compatible with the class '" + dataClass
						+ "' match the root element '" + input.peekNextChild() + "'");
			}

			return (BindingFuture<T>) addBindingFuture(
					getSchemaBinder().bind(model.effective(), input));
		});
	}

	@Override
	public Binder<?> bind() {
		return createBinder(input -> addBindingFuture(getSchemaBinder()
				.bind(registeredModels.get(input.peekNextChild()).effective(), input)));
	}

	private <T> BindingFuture<T> addBindingFuture(BindingFuture<T> binding) {
		bindingFutures.add(binding.getModel().effective().getName(), binding);

		new Thread(() -> {
			try {
				binding.get();
			} catch (Exception e) {
				bindingFutures.remove(binding.getModel().effective().getName());
			}
		}).start();

		return binding;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Set<BindingFuture<T>> bindingFutures(Model<T> model) {
		Set<BindingFuture<?>> modelBindings = bindingFutures
				.get(model.effective().getName());

		if (modelBindings == null)
			return Collections.emptySet();
		else
			return modelBindings.stream().map(t -> (BindingFuture<T>) t)
					.collect(Collectors.toSet());
	}

	private <T> Unbinder<T> createUnbinder(
			Consumer<StructuredDataTarget> unbindingFunction) {
		return new Unbinder<T>() {
			@Override
			public BindingFuture<T> to(String extension, OutputStream output) {
				unbindingFunction.accept(getDataInterface(extension).saveData(output));

				return null;
			}

			@Override
			public <U extends StructuredDataTarget> U to(U output) {
				unbindingFunction.accept(output);

				return null;
			}

			@Override
			public Unbinder<T> with(Consumer<Exception> errorHandler) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public <T> Unbinder<T> unbind(Model<T> model, T data) {
		return createUnbinder(output -> new SchemaUnbinder(this)
				.unbind(model.effective(), output, data));
	}

	@Override
	public <T> Unbinder<T> unbind(T data) {
		return createUnbinder(
				output -> new SchemaUnbinder(this).unbind(output, data));
	}

	@Override
	public <T> Unbinder<T> unbind(TypeToken<T> dataType, T data) {
		return createUnbinder(
				output -> new SchemaUnbinder(this).unbind(output, dataType, data));
	}

	@Override
	public <T> void registerProvider(TypeToken<T> providedType,
			Supplier<T> provider) {
		registerProvider(c -> canEqual(c, providedType) ? provider.get() : null);
	}

	private boolean canEqual(TypeToken<?> first, TypeToken<?> second) {
		try {
			first.withEquality(second);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void registerProvider(Function<TypeToken<?>, ?> provider) {
		providers.add(c -> {
			Object provided = provider.apply(c);
			if (provided != null && !c.isAssignableFrom(provided.getClass()))
				throw new SchemaException("Invalid object provided for the class [" + c
						+ "] by provider [" + provider + "]");
			return provided;
		});
	}

	@Override
	public Provisions provisions() {
		return new Provisions() {
			@Override
			@SuppressWarnings("unchecked")
			public <T> TypedObject<T> provide(TypeToken<T> type) {
				return new TypedObject<>(type,
						(T) providers.stream().map(p -> p.apply(type))
								.filter(Objects::nonNull).findFirst()
								.orElseThrow(() -> new SchemaException(
										"No provider exists for the type '" + type + "'")));
			}

			@Override
			public boolean isProvided(TypeToken<?> type) {
				return providers.stream().map(p -> p.apply(type))
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
	public DataTypes registeredTypes() {
		return registeredTypes;
	}

	@Override
	public GeneratedSchema generateSchema(QualifiedName name,
			Collection<? extends Schema> dependencies) {
		GeneratedSchemaImpl schema = new GeneratedSchemaImpl(this, name,
				dependencies);
		registerSchema(schema);
		return schema;
	}

	@Override
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unregisterDataInterface")
	public void registerDataInterface(StructuredDataFormat loader) {
		dataInterfaces.put(loader.getFormatId(), loader);
	}

	@Override
	public void unregisterDataInterface(StructuredDataFormat loader) {
		dataInterfaces.remove(loader.getFormatId(), loader);
	}

	@Override
	public Set<StructuredDataFormat> getRegisteredDataInterfaces() {
		return new HashSet<>(dataInterfaces.values());
	}

	@Override
	public StructuredDataFormat getDataInterface(String id) {
		return dataInterfaces.get(id);
	}

	@Override
	public Set<StructuredDataFormat> getDataInterfaces(String extension) {
		return getRegisteredDataInterfaces().stream()
				.filter(l -> l.getFileExtensions().contains(extension))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
