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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import aQute.bnd.header.Attrs;
import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.AnalyzerPlugin;
import aQute.bnd.service.Plugin;
import aQute.bnd.version.Version;
import aQute.bnd.version.VersionRange;
import aQute.service.reporter.Reporter;
import uk.co.strangeskies.bnd.ReporterLog;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.classpath.Classpath;
import uk.co.strangeskies.utilities.classpath.ContextClassLoaderRunner;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

/**
 * TODO replace all the magic string literals...
 * 
 * @author Elias N Vasylenko
 */
public abstract class ModabiRegistration implements AnalyzerPlugin, Plugin {
	private static final String VERSION = "version";

	private static final Object SOURCES_PROPERTY = "sources";
	private static final String DEFAULT_SOURCE = "META-INF/schemata/*";

	private final StructuredDataFormat handler;
	private final SchemaManager manager;

	private final Set<String> sources;

	private Log log = (l, m) -> {};

	public ModabiRegistration(SchemaManager manager, StructuredDataFormat handler) {
		this.handler = handler;
		this.manager = manager;
		manager.dataInterfaces().registerDataInterface(handler);

		sources = new HashSet<>();
		sources.add(DEFAULT_SOURCE);
	}

	public SchemaManager getManager() {
		return manager;
	}

	@Override
	public boolean analyzeJar(Analyzer analyzer) throws Exception {
		scanSchemaAnnotations(analyzer);

		return registerSchemata(analyzer);
	}

	private Attrs getPackageImportAttributes(String packageName) {
		Manifest manifest = Classpath.getManifest(getClass());
		Map<String, Map<String, String>> privatePackages = Classpath
				.parseManifestEntry(manifest.getMainAttributes().getValue(Constants.PRIVATE_PACKAGE));

		Version versionFrom = Version.parseVersion(privatePackages.get(packageName).get("version"));

		Attrs attributes = new Attrs();
		attributes.put(VERSION,
				new VersionRange(true, versionFrom, new Version(versionFrom.getMajor() + 1, 0, 0), false).toString());

		return attributes;
	}

	private void scanSchemaAnnotations(Analyzer analyzer) {}

	private boolean registerSchemata(Analyzer analyzer) throws Exception {
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
				Map<String, Resource> directoryResources = analyzer.getJar().getDirectories().get(directorySource);

				if (directoryResources == null || directoryResources.isEmpty()) {
					log.log(Level.WARN, "Cannot find Modabi documents in source directory: " + source);
				} else {
					resources.putAll(directoryResources);
				}
			} else {
				Resource resource = analyzer.getJar().getResource(source);

				if (resource == null) {
					log.log(Level.WARN, "Cannot find Modabi document at source location: " + source);
				} else {
					resources.put(source, resource);
				}
			}
		}

		boolean changed = false;

		if (resources != null) {
			changed = withJarOnBuildPath(analyzer, "buildpath", () -> {
				String newCapabilities = null;

				for (String resourceName : resources.keySet()) {
					Schema schema;
					try {
						schema = manager.bindSchema().from(resources.get(resourceName).openInputStream()).resolve();
					} catch (Exception e) {
						throw new SchemaException(e);
					}

					String capability = Schema.class.getPackage().getName() + ";schema:String=\"" + schema.getQualifiedName()
							+ "\";resource:String=\"" + resourceName + "\"";

					if (newCapabilities != null)
						newCapabilities += "," + capability;
					else
						newCapabilities = capability;
				}

				if (newCapabilities != null) {
					prependProperties(analyzer, Constants.PROVIDE_CAPABILITY, newCapabilities);

					prependProperties(analyzer, Constants.REQUIRE_CAPABILITY,
							"osgi.service;" + "filter:=\"(&(objectClass=" + StructuredDataFormat.class.getTypeName() + ")(formatId="
									+ handler.getFormatId() + "))\";resolution:=mandatory;effective:=active");
					prependProperties(analyzer, Constants.REQUIRE_CAPABILITY, "osgi.service;" + "filter:=\"(objectClass="
							+ SchemaManager.class.getTypeName() + ")\";resolution:=mandatory;effective:=active");
					prependProperties(analyzer, Constants.REQUIRE_CAPABILITY, "osgi.extender;" + "filter:=\"(osgi.extender="
							+ Schema.class.getPackage().getName() + ")\";resolution:=mandatory;effective:=resolve");

					List<String> packages = Arrays.asList("uk.co.strangeskies.modabi", "uk.co.strangeskies.modabi.schema");
					for (String packageName : packages) {
						prependProperties(analyzer, Constants.IMPORT_PACKAGE,
								packageName + ";" + getPackageImportAttributes(packageName));
					}

					return true;
				} else {
					return false;
				}
			});
		}

		return changed;
	}

	private boolean withJarOnBuildPath(Analyzer analyzer, String jarName, ThrowingSupplier<Boolean, ?> run)
			throws Exception {
		try {
			File tempJar = createDirs(analyzer.getBase() + File.separator + "generated", "tmp", "jar");

			if (tempJar == null)
				throw new RuntimeException(
						"Cannot create temporary build path jar, location '" + analyzer.getBase() + "' does not exist");

			tempJar = new File(tempJar.getAbsolutePath() + File.separator + jarName + ".jar");

			analyzer.getJar().write(tempJar);

			return new ContextClassLoaderRunner(tempJar.toURI().toURL()).runThrowing(run);
		} catch (Exception e) {
			log.log(Level.ERROR, "Failed to process bundle", e);
			throw e;
		}
	}

	private File createDirs(String baseDirectory, String... directories) {
		File file = new File(baseDirectory);
		if (!file.exists() || !file.isDirectory())
			return null;

		for (String directory : directories) {
			file = new File(file.getAbsolutePath() + File.separator + directory);
			file.mkdir();
		}

		return file;
	}

	private void prependProperties(Analyzer analyzer, String property, String append) {
		String capabilities = analyzer.getProperty(property);

		if (capabilities == null || "".equals(capabilities.trim())) {
			capabilities = append;
		} else if (!Arrays.stream(capabilities.split(",")).anyMatch(c -> c.trim().equals(append.trim()))) {
			capabilities = append + "," + capabilities;
		}

		analyzer.setProperty(property, capabilities);
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
}
