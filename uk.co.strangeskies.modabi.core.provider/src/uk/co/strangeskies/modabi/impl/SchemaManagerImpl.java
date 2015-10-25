/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.function.Function;
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
import uk.co.strangeskies.modabi.io.structured.FileLoader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingContext;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.providers.DereferenceSource;
import uk.co.strangeskies.modabi.processing.providers.ImportSource;
import uk.co.strangeskies.modabi.processing.providers.IncludeTarget;
import uk.co.strangeskies.modabi.processing.providers.TypeParser;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.modabi.schema.building.DataTypeBuilder;
import uk.co.strangeskies.modabi.schema.building.ModelBuilder;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

@Component
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

	private final Set<FileLoader> fileLoaders;

	public SchemaManagerImpl() {
		this(new SchemaBuilderImpl(), new ModelBuilderImpl(),
				new DataTypeBuilderImpl());
	}

	public SchemaManagerImpl(SchemaBuilder schemaBuilder,
			ModelBuilder modelBuilder, DataTypeBuilder dataTypeBuilder) {
		this.modelBuilder = modelBuilder;
		this.dataTypeBuilder = dataTypeBuilder;

		providers = new ArrayList<>();
		bindingFutures = new MultiHashMap<>(HashSet::new); // TODO make
																												// synchronous

		coreSchemata = new CoreSchemata(schemaBuilder, modelBuilder,
				dataTypeBuilder);

		registeredSchemata = new Schemata();
		registeredModels = new Models();
		registeredTypes = new DataTypes();

		registerSchema(coreSchemata.baseSchema());
		registerSchema(coreSchemata.metaSchema());

		registerProvider(DataTypeBuilder.class, () -> dataTypeBuilder);
		registerProvider(ModelBuilder.class, () -> modelBuilder);
		registerProvider(SchemaBuilder.class, () -> schemaBuilder);

		registerProvider(new TypeToken<@Infer Set<?>>() {}, HashSet::new);
		registerProvider(new TypeToken<@Infer LinkedHashSet<?>>() {},
				LinkedHashSet::new);
		registerProvider(new TypeToken<@Infer List<?>>() {}, ArrayList::new);
		registerProvider(new TypeToken<@Infer Map<?, ?>>() {}, HashMap::new);

		bindingProviders = new BindingProviders(this);

		fileLoaders = new HashSet<>();
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
				.withProvision(TypeParser.class, bindingProviders.typeParser())
				.withProvision(BindingContext.class, c -> c);
	}

	ModelBuilder getModelBuilder() {
		return modelBuilder;
	}

	DataTypeBuilder getDataTypeBuilder() {
		return dataTypeBuilder;
	}

	@Override
	public void registerSchema(Schema schema) {
		if (registeredSchemata.add(schema)) {
			for (Schema dependency : schema.getDependencies())
				registerSchema(dependency);

			for (Model<?> model : schema.getModels())
				registerModel(model);

			for (DataType<?> type : schema.getDataTypes())
				registerDataType(type);

			bindingFutures.add(coreSchemata.metaSchema().getSchemaModel().getName(),
					BindingFuture.forBinding(new Binding<>(
							coreSchemata.metaSchema().getSchemaModel(), schema)));
		}
	}

	void registerModel(Model<?> model) {
		registeredModels.add(model);
	}

	void registerDataType(DataType<?> type) {
		registeredTypes.add(type);
	}

	@Override
	public void registerBinding(Binding<?> binding) {
		bindingFutures.add(binding.getModel().getName(),
				BindingFuture.forBinding(binding));
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
			public BindingFuture<T> from(File input) {
				String extension = input.getName();
				int lastDot = extension.lastIndexOf('.');
				if (lastDot > 0) {
					extension = extension.substring(lastDot);
				} else {
					extension = null;
				}

				try {
					InputStream fileStream = new FileInputStream(input);

					if (extension != null) {
						return from(fileStream, extension);
					} else {
						return from(fileStream);
					}
				} catch (FileNotFoundException e) {
					throw new IllegalArgumentException(e);
				}
			}

			@Override
			public BindingFuture<T> from(InputStream input) {
				return from(input, getRegisteredFileLoaders());
			}

			@Override
			public BindingFuture<T> from(InputStream input, String extension) {
				return from(input,
						getRegisteredFileLoaders().stream()
								.filter(l -> l.isValidForExtension(extension))
								.collect(Collectors.toList()));
			}

			private BindingFuture<T> from(InputStream input,
					Collection<FileLoader> loaders) {
				BufferedInputStream bufferedInput = new BufferedInputStream(input);
				bufferedInput.mark(4096);

				if (loaders.isEmpty())
					throw new IllegalArgumentException(
							"No valid file loader registered for input");

				Exception exception = null;

				for (FileLoader loader : loaders) {
					try {
						return from(loader.loadFile(bufferedInput));
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
			List<Model<T>> models = registeredModels.getModelsWithClass(dataClass);
			if (models.contains(model))
				throw new IllegalArgumentException("None of the models '" + model
						+ "' compatible with the class '" + dataClass
						+ "' match the root element '" + input.peekNextChild() + "'");
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
			return new HashSet<>();
		else
			return new HashSet<>(modelBindings.stream().map(t -> (BindingFuture<T>) t)
					.collect(Collectors.toSet()));
	}

	@Override
	public <T, U extends StructuredDataTarget> U unbind(Model<T> model, U output,
			T data) {
		new SchemaUnbinder(this).unbind(model.effective(), output, data);
		return output;
	}

	@Override
	public <U extends StructuredDataTarget> U unbind(U output, Object data) {
		new SchemaUnbinder(this).unbind(output, data);
		return output;
	}

	@Override
	public <T, U extends StructuredDataTarget> U unbind(TypeToken<T> dataType,
			U output, T data) {
		new SchemaUnbinder(this).unbind(output, dataType, data);
		return output;
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
			public <T> T provide(TypeToken<T> type) {
				return (T) providers.stream().map(p -> p.apply(type))
						.filter(Objects::nonNull).findFirst()
						.orElseThrow(() -> new SchemaException(
								"No provider exists for the type '" + type + "'"));
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
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unregisterFileLoader")
	public void registerFileLoader(FileLoader loader) {
		fileLoaders.add(loader);
	}

	@Override
	public void unregisterFileLoader(FileLoader loader) {
		fileLoaders.remove(loader);
	}

	@Override
	public Set<FileLoader> getRegisteredFileLoaders() {
		return Collections.unmodifiableSet(fileLoaders);
	}
}
