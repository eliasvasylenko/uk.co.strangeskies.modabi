/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.plugin.
 *
 * uk.co.strangeskies.modabi.plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.plugin;

import static java.util.stream.Collectors.toSet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.framework.Constants;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.classpath.Attribute;
import uk.co.strangeskies.utilities.classpath.AttributeProperty;
import uk.co.strangeskies.utilities.classpath.PropertyType;
import uk.co.strangeskies.utilities.collection.ObservableSet;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class ModabiRegistration {
	public static final String SCHEMA = "schema";
	public static final String RESOURCE = "resource";

	private static final String EXTENDER = "osgi.extender";
	private static final String EXTENDER_FILTER = "(" + EXTENDER + "=" + Schema.class.getPackage().getName() + ")";

	private static final String SERVICE = "osgi.service";
	private static final String STRUCTUREDDATAFORMAT_SERVICE_FILTER = "(" + Constants.OBJECTCLASS + "="
			+ StructuredDataFormat.class.getTypeName() + ")";
	private static final String SCHEMAMANAGER_SERVICE_FILTER = "(" + Constants.OBJECTCLASS + "="
			+ SchemaManager.class.getTypeName() + ")";

	private RegistrationContext context;

	/*
	 * The schemata provided by the building bundle, mapped to their jar resource
	 * locations:
	 */
	private final Map<BindingFuture<Schema>, String> providedSchemata = new HashMap<>();
	/*
	 * The names of required schemata from build dependencies:
	 */
	private final Set<QualifiedName> requiredSchemata = Collections.synchronizedSet(new HashSet<>());
	/*
	 * All encountered schemata which are loading or have loaded:
	 */
	private final Set<BindingFuture<Schema>> resolvingSchemata = new HashSet<>();

	private QualifiedName getDependencyNamespace() {
		return context.schemaManager().getMetaSchema().getSchemaModel().name();
	}

	public synchronized boolean registerSchemata(RegistrationContext context) {
		this.context = context;

		requiredSchemata.clear();
		providedSchemata.clear();
		resolvingSchemata.clear();

		Model<Schema> schemaModel = context.schemaManager().getMetaSchema().getSchemaModel();
		ObservableSet<?, Binding<Schema>> schemaBindingChanges = context.schemaManager().getBindings(schemaModel);
		schemaBindingChanges.changes().addObserver(c -> {
			new Thread(() -> {
				synchronized (resolvingSchemata) {
					resolvingSchemata.removeAll(context.schemaManager().getBindingFutures(schemaModel).stream()
							.filter(BindingFuture::isDone).collect(Collectors.toSet()));
				}
				detectDeadlock();
			}).start();
		});

		if (!context.sources().isEmpty()) {
			registerSchemaResources();
			return true;
		} else {
			return false;
		}
	}

	private void registerSchemaResources() {
		try {
			context.log(Level.TRACE, "Available dependencies: " + context.availableDependencies());

			/*
			 * Begin binding schemata concurrently
			 */
			for (String resourceName : context.sources()) {
				providedSchemata.put(registerSchemaResource(() -> context.openSource(resourceName)), resourceName);
			}

			/*
			 * Resolve schemata
			 */
			for (BindingFuture<Schema> schemaFuture : providedSchemata.keySet()) {
				schemaFuture.resolve();
			}

			addProvisions();
			addRequirements();
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}

	private BindingFuture<Schema> registerSchemaResource(ThrowingSupplier<InputStream, ?> inputStream) {
		BindingFuture<Schema> bindingFuture = context.schemaManager().bindSchema().withClassLoader(context.classLoader())
				.from(context.formatId(), inputStream);

		/*
		 * Resolve dependencies
		 */
		synchronized (resolvingSchemata) {
			bindingFuture.blocks().addObserver(event -> {
				synchronized (resolvingSchemata) {
					switch (event.type()) {
					case STARTED:
						resolveDependency(event.block());
						break;
					case THREAD_BLOCKED:
						detectDeadlock();
						break;
					case THREAD_UNBLOCKED:
						break;
					case ENDED:
						break;
					}
				}
			});

			resolvingSchemata.add(bindingFuture);

			for (BindingBlock block : bindingFuture.blocks().getBlocks()) {
				resolveDependency(block);
			}
		}
		detectDeadlock();

		new Thread(() -> {
			try {
				bindingFuture.resolve();
			} catch (Exception e) {
				synchronized (resolvingSchemata) {
					resolvingSchemata.remove(bindingFuture);
				}
				detectDeadlock();
			}
		}).start();

		return bindingFuture;
	}

	private void detectDeadlock() {
		Set<BindingFuture<Schema>> resolvingSchemata;
		synchronized (this.resolvingSchemata) {
			resolvingSchemata = this.resolvingSchemata;
		}

		if (resolvingSchemata.stream().allMatch(f -> f.blocks().isBlocked())) {
			Set<BindingBlock> missingDependencies = resolvingSchemata.stream().flatMap(f -> f.blocks().getBlocks().stream())
					.collect(toSet());

			SchemaException deadlockException = new SchemaException(
					"Cannot bind " + providedSchemata.keySet() + "; Cannot resolve dependencies " + missingDependencies);
			for (BindingFuture<?> future : resolvingSchemata) {
				for (BindingBlock block : future.blocks().getBlocks()) {
					block.fail(deadlockException);
				}
			}

			throw deadlockException;
		}
	}

	private void resolveDependency(BindingBlock block) {
		if (!block.namespace().equals(getDependencyNamespace())) {
			/*-
			 * TODO put this error aside and only register it if we deadlock
			SchemaException exception = new SchemaException(
					"Cannot resolve " + block + "; Only those external dependencies in the " + getDependencyNamespace()
							+ " namespace may be considered at build time");
			block.fail(exception);
			throw exception;
			 */
		} else {
			QualifiedName id = block.id().get(Primitive.QUALIFIED_NAME);

			if (context.availableDependencies().contains(id)) {
				boolean added = requiredSchemata.add(id);

				if (added) {
					registerSchemaResource(() -> context.openDependency(id));
				}
			}
		}
	}

	private void addProvisions() {
		List<Attribute> providedCapabilities = new ArrayList<>();

		for (BindingFuture<Schema> schemaFuture : providedSchemata.keySet()) {
			List<AttributeProperty<?>> properties = new ArrayList<>();

			properties.add(AttributeProperty.untyped(SCHEMA, schemaFuture.resolve().qualifiedName().toString()));
			properties.add(AttributeProperty.untyped(RESOURCE, providedSchemata.get(schemaFuture)));

			providedCapabilities.add(new Attribute(Schema.class.getPackage().getName(), properties));
		}

		context.addAttributes(Constants.PROVIDE_CAPABILITY, providedCapabilities);
	}

	private void addRequirements() {
		List<Attribute> requiredCapabilities = new ArrayList<>();

		for (QualifiedName schema : requiredSchemata) {
			List<AttributeProperty<?>> properties = new ArrayList<>();

			properties.add(AttributeProperty.untyped(SCHEMA, schema.toString()));

			/*
			 * Modabi schema requirement attributes
			 */
			requiredCapabilities.add(new Attribute(Schema.class.getPackage().getName(), properties));
		}

		AttributeProperty<?> mandatoryResolution = new AttributeProperty<>(Constants.RESOLUTION_DIRECTIVE,
				PropertyType.DIRECTIVE, Constants.MANDATORY_DIRECTIVE);

		AttributeProperty<?> resolveEffective = new AttributeProperty<>(Constants.EFFECTIVE_DIRECTIVE,
				PropertyType.DIRECTIVE, Constants.EFFECTIVE_RESOLVE);

		AttributeProperty<?> activeEffective = new AttributeProperty<>(Constants.EFFECTIVE_DIRECTIVE,
				PropertyType.DIRECTIVE, Constants.EFFECTIVE_ACTIVE);

		/*
		 * StructuredDataFormat service requirement attribute
		 */
		requiredCapabilities.add(new Attribute(SERVICE,
				new AttributeProperty<>(Constants.FILTER_DIRECTIVE, PropertyType.DIRECTIVE,
						"(&" + STRUCTUREDDATAFORMAT_SERVICE_FILTER + "(formatId=" + context.formatId() + "))"),
				mandatoryResolution, activeEffective));

		/*
		 * SchemaManager service requirement attribute
		 */
		requiredCapabilities.add(new Attribute(SERVICE,
				new AttributeProperty<>(Constants.FILTER_DIRECTIVE, PropertyType.DIRECTIVE, SCHEMAMANAGER_SERVICE_FILTER),
				mandatoryResolution, activeEffective));

		/*
		 * Modabi extender attribute
		 */
		requiredCapabilities.add(new Attribute(EXTENDER,
				new AttributeProperty<>(Constants.FILTER_DIRECTIVE, PropertyType.DIRECTIVE, EXTENDER_FILTER),
				mandatoryResolution, resolveEffective));

		context.addAttributes(Constants.REQUIRE_CAPABILITY, requiredCapabilities);
	}
}
