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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Binder;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.DataFormats;
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
import uk.co.strangeskies.modabi.Unbinder;
import uk.co.strangeskies.modabi.impl.processing.BindingProviders;
import uk.co.strangeskies.modabi.impl.processing.ProcessingContextImpl;
import uk.co.strangeskies.modabi.impl.processing.UnbindingProviders;
import uk.co.strangeskies.modabi.impl.schema.building.DataTypeBuilderImpl;
import uk.co.strangeskies.modabi.impl.schema.building.ModelBuilderImpl;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.BindingFutureBlocks;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.providers.DereferenceSource;
import uk.co.strangeskies.modabi.processing.providers.ImportSource;
import uk.co.strangeskies.modabi.processing.providers.ImportTarget;
import uk.co.strangeskies.modabi.processing.providers.IncludeTarget;
import uk.co.strangeskies.modabi.processing.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.modabi.schema.building.DataTypeBuilder;
import uk.co.strangeskies.modabi.schema.building.ModelBuilder;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

@Component(immediate = true)
public class SchemaManagerImpl implements SchemaManager {
	private final MultiMap<QualifiedName, BindingFuture<?>, Set<BindingFuture<?>>> bindingFutures;

	private final CoreSchemata coreSchemata;

	private final Provisions provisions;

	private final Models registeredModels;
	private final DataTypes registeredTypes;
	private final Schemata registeredSchemata;

	private final ModelBuilder modelBuilder;
	private final DataTypeBuilder dataTypeBuilder;

	private final BindingProviders bindingProviders;
	private final UnbindingProviders unbindingProviders;

	private final Map<String, StructuredDataFormat> dataInterfaces;
	private final ObservableImpl<StructuredDataFormat> dataInterfaceObservers;

	public SchemaManagerImpl() {
		this(new SchemaBuilderImpl(), new ModelBuilderImpl(), new DataTypeBuilderImpl());
	}

	public SchemaManagerImpl(SchemaBuilder schemaBuilder, ModelBuilder modelBuilder,
			DataTypeBuilder dataTypeBuilder /* TODO , Log log */) {
		this.modelBuilder = modelBuilder;
		this.dataTypeBuilder = dataTypeBuilder;

		bindingFutures = new MultiHashMap<>(HashSet::new); // TODO make synchronous

		coreSchemata = new CoreSchemata(schemaBuilder, modelBuilder, dataTypeBuilder);

		registeredSchemata = new Schemata();
		registeredModels = new Models();
		registeredTypes = new DataTypes();

		provisions = new ProvisionsImpl();

		/*
		 * Register model builder providers
		 */
		provisions().registerProvider(DataTypeBuilder.class, () -> dataTypeBuilder);
		provisions().registerProvider(ModelBuilder.class, () -> modelBuilder);
		provisions().registerProvider(SchemaBuilder.class, () -> schemaBuilder);

		/*
		 * Register collection providers
		 */
		provisions().registerProvider(new @Infer TypeToken<SortedSet<?>>() {}, TreeSet::new);
		provisions().registerProvider(new @Infer TypeToken<Set<?>>() {}, HashSet::new);
		provisions().registerProvider(new @Infer TypeToken<LinkedHashSet<?>>() {}, LinkedHashSet::new);
		provisions().registerProvider(new @Infer TypeToken<List<?>>() {}, ArrayList::new);
		provisions().registerProvider(new @Infer TypeToken<Map<?, ?>>() {}, HashMap::new);

		bindingProviders = new BindingProviders(this);
		unbindingProviders = new UnbindingProviders(this);

		dataInterfaces = new HashMap<>();
		dataInterfaceObservers = new ObservableImpl<>();

		registerSchema(coreSchemata.metaSchema());
	}

	public ProcessingContextImpl getBindingContext() {
		return new ProcessingContextImpl(this).withProvision(DereferenceSource.class, bindingProviders.dereferenceSource())
				.withProvision(IncludeTarget.class, bindingProviders.includeTarget())
				.withProvision(ImportSource.class, bindingProviders.importSource())
				.withProvision(DataLoader.class, bindingProviders.dataLoader())
				.withProvision(Imports.class, bindingProviders.imports()).withProvision(ProcessingContext.class, c -> c);
	}

	public ProcessingContextImpl getProcessingContext() {
		return new ProcessingContextImpl(this)
				.withProvision(new TypeToken<ReferenceTarget>() {}, unbindingProviders.referenceTarget())
				.withProvision(new TypeToken<ImportTarget>() {}, unbindingProviders.importTarget())
				.withProvision(new TypeToken<IncludeTarget>() {}, unbindingProviders.includeTarget())
				.withProvision(new TypeToken<ProcessingContext>() {}, c -> c);
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
			public BindingFuture<Schema> from(ThrowingSupplier<InputStream, ?> input) {
				return registerFuture(binder.from(input));
			}

			@Override
			public BindingFuture<Schema> from(String extension, ThrowingSupplier<InputStream, ?> input) {
				return registerFuture(binder.from(extension, input));
			}

			private BindingFuture<Schema> registerFuture(BindingFuture<Schema> future) {
				BindingFuture<Schema> wrappedFuture = new BindingFuture<Schema>() {
					@Override
					public boolean cancel(boolean mayInterruptIfRunning) {
						return future.cancel(mayInterruptIfRunning);
					}

					@Override
					public boolean isCancelled() {
						return future.isCancelled();
					}

					@Override
					public boolean isDone() {
						return future.isDone();
					}

					@Override
					public Future<Model<Schema>> getModelFuture() {
						return future.getModelFuture();
					}

					@Override
					public BindingFutureBlocks getBlocks() {
						return future.getBlocks();
					}

					@Override
					public Binding<Schema> get() {
						return register(future.get());
					}

					@Override
					public Binding<Schema> get(long timeout, TimeUnit unit) {
						return register(future.get(timeout, unit));
					}

					private Binding<Schema> register(Binding<Schema> binding) {
						registerSchemaImpl(binding.getData());
						return binding;
					}
				};

				new Thread(() -> wrappedFuture.resolve()).start();

				return wrappedFuture;
			}

			@Override
			public Binder<Schema> with(Consumer<Exception> errorHandler) {
				binder.with(errorHandler);
				return this;
			}

			@Override
			public Binder<Schema> with(ClassLoader classLoader) {
				binder.with(classLoader);
				return this;
			}
		};
	}

	void registerModel(Model<?> model) {
		synchronized (registeredModels) {
			registeredModels.add(model);
			registeredModels.notifyAll();
		}
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

	<T> BindingFuture<T> addBindingFuture(BindingFuture<T> binding) {
		new Thread(() -> {
			try {
				QualifiedName modelName = binding.getModelFuture().get().effective().getName();
				bindingFutures.add(modelName, binding);

				try {
					binding.get();
				} catch (Exception e) {
					bindingFutures.remove(modelName);
				}
			} catch (Exception e) {}
		}).start();

		return binding;
	}

	@Override
	public <T> Binder<T> bind(Model<T> model) {
		return new BinderImpl<>(this, input -> model.effective(), this::addBindingFuture);
	}

	private Model<?> waitForModel(QualifiedName modelName) {
		synchronized (registeredModels) {
			Model<?> model;
			while ((model = registeredModels.get(modelName)) == null) {
				try {
					registeredModels.wait();
				} catch (InterruptedException e) {
					throw new SchemaException("No model found to match the root element '" + modelName + "'", e);
				}
			}
			return model;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Binder<T> bind(TypeToken<T> dataClass) {
		return new BinderImpl<>(this, input -> {
			Model<?> model = waitForModel(input.peekNextChild());

			List<Model<T>> models = registeredModels.getModelsWithClass(dataClass);

			if (!models.contains(model)) {
				throw new IllegalArgumentException("None of the models '" + models + "' compatible with the class '" + dataClass
						+ "' match the root element '" + input.peekNextChild() + "'");
			}

			/*
			 * TODO model adapter to enforce possibly more specific generic type.
			 */

			return (Model<T>) model;
		}, this::addBindingFuture);
	}

	@Override
	public Binder<?> bind() {
		return new BinderImpl<>(this, input -> waitForModel(input.peekNextChild()).effective(), this::addBindingFuture);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Set<BindingFuture<T>> bindingFutures(Model<T> model) {
		Set<BindingFuture<?>> modelBindings = bindingFutures.get(model.effective().getName());

		if (modelBindings == null)
			return Collections.emptySet();
		else
			return modelBindings.stream().map(t -> (BindingFuture<T>) t).collect(Collectors.toSet());
	}

	@Override
	public <T> Unbinder<T> unbind(Model<T> model, T data) {
		return new UnbinderImpl<>(this, data, context -> Arrays.asList(model.effective()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Unbinder<T> unbind(T data) {
		return unbind((TypeToken<T>) TypeToken.over(data.getClass()), data);
	}

	@Override
	public <T> Unbinder<T> unbind(TypeToken<T> dataType, T data) {
		return new UnbinderImpl<>(this, data, context -> registeredModels().getModelsWithClass(dataType).stream()
				.map(n -> n.effective()).collect(Collectors.toList()));
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
	public GeneratedSchema generateSchema(QualifiedName name, Collection<? extends Schema> dependencies) {
		GeneratedSchemaImpl schema = new GeneratedSchemaImpl(this, name, dependencies);
		registerSchema(schema);
		return schema;
	}

	@Override
	public DataFormats dataFormats() {
		return new DataFormats() {
			@Override
			public void registerDataFormat(StructuredDataFormat loader) {
				synchronized (dataInterfaces) {
					dataInterfaces.put(loader.getFormatId(), loader);
					dataInterfaceObservers.fire(loader);
				}
			}

			@Override
			public void unregisterDataFormat(StructuredDataFormat loader) {
				synchronized (dataInterfaces) {
					dataInterfaces.remove(loader.getFormatId(), loader);
				}
			}

			@Override
			public Set<StructuredDataFormat> getRegistered() {
				synchronized (dataInterfaces) {
					return new HashSet<>(dataInterfaces.values());
				}
			}

			@Override
			public StructuredDataFormat getDataFormat(String id) {
				synchronized (dataInterfaces) {
					return dataInterfaces.get(id);
				}
			}

			@Override
			public Set<StructuredDataFormat> registerObserver(Consumer<? super StructuredDataFormat> listener) {
				synchronized (dataInterfaces) {
					dataInterfaceObservers.addWeakObserver(listener);
					return getRegistered();
				}
			}
		};
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unregisterDataInterface")
	void registerDataInterface(StructuredDataFormat loader) {
		dataFormats().registerDataFormat(loader);
	}

	void unregisterDataInterface(StructuredDataFormat loader) {
		dataFormats().unregisterDataFormat(loader);
	}

	@Override
	public Provisions provisions() {
		return provisions;
	}

}
