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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Constants;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.AnalyzerPlugin;
import aQute.bnd.service.Plugin;
import aQute.service.reporter.Reporter;
import uk.co.strangeskies.bnd.ReporterLog;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.classpath.Attribute;
import uk.co.strangeskies.utilities.classpath.AttributeProperty;
import uk.co.strangeskies.utilities.classpath.ContextClassLoaderRunner;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;
import uk.co.strangeskies.utilities.classpath.PropertyType;
import uk.co.strangeskies.utilities.function.ThrowingRunnable;

/**
 * @author Elias N Vasylenko
 */
public abstract class ModabiRegistration implements AnalyzerPlugin, Plugin {
	private static final Object SOURCES_PROPERTY = "sources";
	private static final String DEFAULT_SOURCE = "META-INF/schemata/*";

	private static final String SCHEMA = "schema";
	private static final String RESOURCE = "resource";

	private static final String EXTENDER = "osgi.extender";
	private static final String EXTENDER_FILTER = "(" + EXTENDER + "=" + Schema.class.getPackage().getName() + ")";

	private static final String SERVICE = "osgi.service";
	private static final String STRUCTUREDDATAFORMAT_SERVICE_FILTER = "(" + Constants.OBJECTCLASS + "="
			+ StructuredDataFormat.class.getTypeName() + ")";
	private static final String SCHEMAMANAGER_SERVICE_FILTER = "(" + Constants.OBJECTCLASS + "="
			+ SchemaManager.class.getTypeName() + ")";

	private final SchemaManager manager;
	private final StructuredDataFormat handler;

	private final Set<String> sources;

	private Log log = (l, m) -> {};

	public ModabiRegistration(StructuredDataFormat handler) {
		this.manager = new SchemaManagerImpl();
		this.handler = handler;

		manager.dataFormats().registerDataFormat(handler);

		sources = new HashSet<>();
		sources.add(DEFAULT_SOURCE);
	}

	@Override
	public void setProperties(Map<String, String> map) throws Exception {
		sources.clear();
		if (map.containsKey(SOURCES_PROPERTY)) {
			String sourcesString = map.get(SOURCES_PROPERTY);
			sources.addAll(Arrays.asList(sourcesString.split(",")));
		} else {
			sources.add(DEFAULT_SOURCE);
		}
	}

	@Override
	public void setReporter(Reporter processor) {
		log = new ReporterLog(processor);
	}

	public SchemaManager getManager() {
		return manager;
	}

	@Override
	public boolean analyzeJar(Analyzer analyzer) throws Exception {
		scanSchemaAnnotations(analyzer);

		return registerSchemata(analyzer);
	}

	private void scanSchemaAnnotations(Analyzer analyzer) {}

	private boolean registerSchemata(Analyzer analyzer) throws Exception {
		Map<String, Resource> resources = collectSchemaResources(analyzer.getJar(), sources);

		if (!resources.isEmpty()) {
			withBuildPath(analyzer, () -> registerSchemaResources(analyzer, resources));
			return true;
		} else {
			return false;
		}
	}

	private void registerSchemaResources(Analyzer analyzer, Map<String, Resource> resources) {
		List<Attribute> newCapabilities = new ArrayList<>();

		/*
		 * Begin binding schemata concurrently
		 */
		Map<String, BindingFuture<Schema>> schemaFutures = new HashMap<>();
		for (String resourceName : resources.keySet()) {
			schemaFutures.put(resourceName,
					manager.bindSchema().from(handler.getFormatId(), resources.get(resourceName)::openInputStream));
		}

		Map<QualifiedName, Resource> dependencyResources = collectSchemaResources(analyzer.getClasspath());
		Map<QualifiedName, BindingFuture<?>> dependencySchemata = new HashMap<>();

		/*
		 * Resolve all schemata
		 * 
		 * TODO resolve schema dependencies from jars on build path, and terminate
		 * on dependencies which cannot be resolved.
		 */
		for (String resourceName : schemaFutures.keySet()) {
			Schema schema = schemaFutures.get(resourceName).resolve();

			List<AttributeProperty<?>> properties = new ArrayList<>();

			properties.add(AttributeProperty.untyped(SCHEMA, schema.getQualifiedName().toString()));
			properties.add(AttributeProperty.untyped(RESOURCE, resourceName));

			newCapabilities.add(new Attribute(Schema.class.getPackage().getName(), properties));
		}

		prependProperties(analyzer, Constants.PROVIDE_CAPABILITY, newCapabilities);

		addGeneralRequirements(analyzer);
	}

	private void addGeneralRequirements(Analyzer analyzer) {
		AttributeProperty<?> mandatoryResolution = new AttributeProperty<>(Constants.RESOLUTION_DIRECTIVE,
				PropertyType.DIRECTIVE, Constants.MANDATORY_DIRECTIVE);

		AttributeProperty<?> resolveEffective = new AttributeProperty<>(Constants.EFFECTIVE_DIRECTIVE,
				PropertyType.DIRECTIVE, Constants.EFFECTIVE_RESOLVE);

		AttributeProperty<?> activeEffective = new AttributeProperty<>(Constants.EFFECTIVE_DIRECTIVE,
				PropertyType.DIRECTIVE, Constants.EFFECTIVE_ACTIVE);

		prependProperties(analyzer, Constants.REQUIRE_CAPABILITY,
				/*
				 * StructuredDataFormat service requirement attribute
				 */
				new Attribute(SERVICE,
						new AttributeProperty<>(Constants.FILTER_DIRECTIVE, PropertyType.DIRECTIVE,
								"(&" + STRUCTUREDDATAFORMAT_SERVICE_FILTER + "(formatId=" + handler.getFormatId() + "))"),
						mandatoryResolution, activeEffective),

				/*
				 * SchemaManager service requirement attribute
				 */
				new Attribute(SERVICE,
						new AttributeProperty<>(Constants.FILTER_DIRECTIVE, PropertyType.DIRECTIVE, SCHEMAMANAGER_SERVICE_FILTER),
						mandatoryResolution, activeEffective),

				/*
				 * Modabi extender attribute
				 */
				new Attribute(EXTENDER,
						new AttributeProperty<>(Constants.FILTER_DIRECTIVE, PropertyType.DIRECTIVE, EXTENDER_FILTER),
						mandatoryResolution, resolveEffective));
	}

	private Map<QualifiedName, Resource> collectSchemaResources(Collection<? extends Jar> jar) {
		/*
		 * TODO get sources from jar manifests, then call:
		 * 
		 * #collectSchemaResources(Jar jar, Set<String> sources)
		 */
		return null;
	}

	private Map<String, Resource> collectSchemaResources(Jar jar, Set<String> sources) {
		Map<String, Resource> resources = new HashMap<>();

		for (String source : sources) {
			source = source.trim();

			String directorySource = null;
			if (source.equals("*")) {
				directorySource = "";
			} else if (source.endsWith("/*")) {
				directorySource = source.substring(0, source.length() - 2);
			}

			if (directorySource != null) {
				Map<String, Resource> directoryResources = jar.getDirectories().get(directorySource);

				if (directoryResources == null || directoryResources.isEmpty()) {
					log.log(Level.WARN, "Cannot find Modabi documents in source directory: " + source);
				} else {
					resources.putAll(directoryResources);
				}
			} else {
				Resource resource = jar.getResource(source);

				if (resource == null) {
					log.log(Level.WARN, "Cannot find Modabi document at source location: " + source);
				} else {
					resources.put(source, resource);
				}
			}
		}
		return resources;
	}

	private <E extends Exception> void withBuildPath(Analyzer analyzer, ThrowingRunnable<E> run) throws E {
		List<URL> jarPaths;
		try {
			jarPaths = getJarPaths(analyzer);
		} catch (MalformedURLException e) {
			log.log(Level.ERROR, "Failed to load build path for bundle " + analyzer.getBundleSymbolicName(), e);
			throw new SchemaException("Failed to load build path for bundle " + analyzer.getBundleSymbolicName(), e);
		}

		ClassLoader targetClassloader = new URLClassLoader(jarPaths.toArray(new URL[jarPaths.size()]),
				Schema.class.getClassLoader());

		new ContextClassLoaderRunner(targetClassloader).runThrowing(run);
	}

	private List<URL> getJarPaths(Analyzer analyzer) throws MalformedURLException {
		List<URL> jarPaths = new ArrayList<>();

		for (Jar jar : analyzer.getClasspath()) {
			if (jar.getSource() != null) {
				jarPaths.add(jar.getSource().toURI().toURL());
			}
		}

		return jarPaths;
	}

	private void prependProperties(Analyzer analyzer, String property, Attribute... prepend) {
		prependProperties(analyzer, property, Arrays.asList(prepend));
	}

	private void prependProperties(Analyzer analyzer, String property, List<Attribute> prepend) {
		String capabilities = analyzer.getProperty(property);

		for (Attribute attribute : prepend) {
			if (capabilities == null || "".equals(capabilities.trim())) {
				capabilities = attribute.toString();
			} else {
				if (!ManifestUtilities.parseAttributes(capabilities).stream().anyMatch(c -> c.equals(attribute))) {
					capabilities = attribute.toString() + "," + capabilities;
				}
			}
		}

		analyzer.setProperty(property, capabilities);
	}
}
