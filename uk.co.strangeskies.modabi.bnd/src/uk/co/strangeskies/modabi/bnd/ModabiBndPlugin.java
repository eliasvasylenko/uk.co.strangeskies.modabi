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

import static java.util.stream.Collectors.toList;

import java.io.InputStream;
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
import java.util.jar.Manifest;

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
import uk.co.strangeskies.modabi.plugin.ModabiRegistration;
import uk.co.strangeskies.modabi.plugin.RegistrationContext;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.classpath.Attribute;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;

/**
 * @author Elias N Vasylenko
 */
public abstract class ModabiBndPlugin implements AnalyzerPlugin, Plugin {
	private static final Object SOURCES_PROPERTY = "sources";
	private static final String DEFAULT_SOURCE = "META-INF/schemata/*";

	private final SchemaManager manager;
	private final StructuredDataFormat format;

	private final Set<String> sources;

	private Log log = (l, m) -> {};

	public ModabiBndPlugin(StructuredDataFormat handler) {
		this.manager = new SchemaManagerImpl();
		this.format = handler;

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
	public synchronized boolean analyzeJar(Analyzer analyzer) throws Exception {
		try {
			scanSchemaAnnotations(analyzer);

			return new ModabiRegistration(createRegistrationContext(analyzer)).registerSchemata();
		} catch (Throwable t) {
			log.log(Level.ERROR, "Oh no.", t);
			throw t;
		}
	}

	private RegistrationContext createRegistrationContext(Analyzer analyzer) {
		return new RegistrationContext() {
			Map<String, Resource> resources = collectSchemaResources(analyzer.getJar(), sources);
			Map<QualifiedName, Resource> availableDependencies;

			@Override
			public SchemaManager schemaManager() {
				return manager;
			}

			@Override
			public String formatId() {
				return format.getFormatId();
			}

			@Override
			public ClassLoader classLoader() {
				return getClassLoader(analyzer);
			}

			@Override
			public void addAttributes(String attributeName, List<Attribute> attributes) {
				prependProperties(analyzer, attributeName, attributes);
			}

			@Override
			public Set<String> sources() {
				return resources.keySet();
			}

			@Override
			public InputStream openSource(String sourceLocation) throws Exception {
				return resources.get(sourceLocation).openInputStream();
			}

			@Override
			public Set<QualifiedName> availableDependencies() {
				if (availableDependencies == null) {
					availableDependencies = collectSchemaResources(analyzer.getClasspath());
				}
				return availableDependencies.keySet();
			}

			@Override
			public InputStream openDependency(QualifiedName name) throws Exception {
				availableDependencies();
				return availableDependencies.get(name).openInputStream();
			}

			@Override
			public void log(Level level, String message) {
				log.log(level, message);
			}

			@Override
			public void log(Level level, String message, Throwable throwable) {
				log.log(level, message, throwable);
			}
		};
	}

	private void scanSchemaAnnotations(Analyzer analyzer) {}

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

	private Map<QualifiedName, Resource> collectSchemaResources(Collection<? extends Jar> jars) {
		Map<QualifiedName, Resource> resources = new HashMap<>();

		log.log(Level.TRACE, "Jars on build path: " + jars);

		for (Jar jar : jars) {
			Manifest manifest = null;
			try {
				manifest = jar.getManifest();
			} catch (Exception e) {}

			if (manifest != null) {
				String provides = manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);

				log.log(Level.TRACE, "Jar " + jar.getName() + " provides: " + provides);

				if (provides != null) {
					List<Attribute> attributes = ManifestUtilities.parseAttributes(provides);

					attributes = attributes.stream().filter(a -> a.name().equals(Schema.class.getPackage().getName()))
							.collect(toList());

					for (Attribute attribute : attributes) {
						QualifiedName schema = QualifiedName
								.parseString(attribute.properties().get(ModabiRegistration.SCHEMA).composeValueString());
						Resource resource = jar
								.getResource(attribute.properties().get(ModabiRegistration.RESOURCE).composeValueString());

						resources.put(schema, resource);
					}
				}
			}
		}

		return resources;
	}

	private ClassLoader getClassLoader(Analyzer analyzer) {
		List<URL> jarPaths;
		try {
			jarPaths = getJarPaths(analyzer);
		} catch (MalformedURLException e) {
			log.log(Level.ERROR, "Failed to load build path for bundle " + analyzer.getBundleSymbolicName(), e);
			throw new SchemaException("Failed to load build path for bundle " + analyzer.getBundleSymbolicName(), e);
		}

		return new URLClassLoader(jarPaths.toArray(new URL[jarPaths.size()]), Schema.class.getClassLoader());
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
