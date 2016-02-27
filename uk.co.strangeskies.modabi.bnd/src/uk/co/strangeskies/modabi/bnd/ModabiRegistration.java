/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.bnd.
 *
 * uk.co.strangeskies.modabi.bnd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.bnd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.bnd.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.bnd;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Constants;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.utilities.classpath.Attribute;
import uk.co.strangeskies.utilities.classpath.AttributeProperty;
import uk.co.strangeskies.utilities.classpath.ContextClassLoaderRunner;
import uk.co.strangeskies.utilities.classpath.PropertyType;
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

	private final RegistrationContext context;
	private final Map<BindingFuture<Schema>, String> providedSchemata;
	private final Set<QualifiedName> requiredSchemata;

	public ModabiRegistration(RegistrationContext context) {
		this.context = context;
		requiredSchemata = new HashSet<>();
		providedSchemata = new HashMap<>();
	}

	public synchronized boolean registerSchemata() throws Exception {
		requiredSchemata.clear();
		providedSchemata.clear();

		if (!context.sources().isEmpty()) {
			new ContextClassLoaderRunner(context.classLoader()).run(() -> registerSchemaResources());
			return true;
		} else {
			return false;
		}
	}

	private void registerSchemaResources() {
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
	}

	private BindingFuture<Schema> registerSchemaResource(ThrowingSupplier<InputStream, ?> inputStream) {
		BindingFuture<Schema> bindingFuture = context.schemaManager().bindSchema().from(context.formatId(), inputStream);

		/*
		 * Locate resources for available dependencies
		 */
		Map<QualifiedName, BindingFuture<?>> dependencySchemata = new HashMap<>();

		/*
		 * Resolve dependencies
		 */
		bindingFuture.getBlocks().addObserver(p -> {
			resolveDependency(p.getLeft(), p.getRight().get(Primitive.QUALIFIED_NAME), dependencySchemata);
		});

		for (QualifiedName namespace : bindingFuture.getBlocks().waitingForNamespaces()) {
			for (DataSource data : bindingFuture.getBlocks().waitingForIds(namespace)) {
				resolveDependency(namespace, data.get(Primitive.QUALIFIED_NAME), dependencySchemata);
			}
		}

		return bindingFuture;
	}

	private void resolveDependency(QualifiedName namespace, QualifiedName dependency,
			Map<QualifiedName, BindingFuture<?>> dependencySchemata) {
		if (!namespace.equals(Schema.class.getPackage().getName())) {
			throw new SchemaException("Unsatisfiable dependency " + namespace + ": " + dependency);
		}

		if (context.availableDependencies().contains(dependency)) {
			boolean added;
			synchronized (requiredSchemata) {
				added = requiredSchemata.add(dependency);
			}
			if (added) {
				registerSchemaResource(() -> context.openDependency(dependency));
			}
		}
	}

	private void addProvisions() {
		List<Attribute> providedCapabilities = new ArrayList<>();

		for (BindingFuture<Schema> schemaFuture : providedSchemata.keySet()) {
			List<AttributeProperty<?>> properties = new ArrayList<>();

			properties.add(AttributeProperty.untyped(SCHEMA, schemaFuture.resolve().getQualifiedName().toString()));
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
