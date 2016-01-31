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
import java.util.Map;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.AnalyzerPlugin;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.utilities.ContextClassLoaderRunner;

/**
 * TODO replace all the magic string literals...
 * 
 * @author Elias N Vasylenko
 */
public abstract class ModabiRegistration implements AnalyzerPlugin {
	private final StructuredDataFormat handler;
	private final SchemaManager manager;

	public ModabiRegistration(SchemaManager manager, StructuredDataFormat handler) {
		this.handler = handler;
		this.manager = manager;
		manager.dataInterfaces().registerDataInterface(handler);
	}

	public SchemaManager getManager() {
		return manager;
	}

	@Override
	public boolean analyzeJar(Analyzer analyzer) throws Exception {
		scanSchemaAnnotations(analyzer);

		registerSchemata(analyzer);

		return false;
	}

	private void scanSchemaAnnotations(Analyzer analyzer) {}

	private void registerSchemata(Analyzer analyzer) {
		Map<String, Resource> resources = analyzer.getJar().getDirectories().get("META-INF/schemata");

		if (resources != null) {
			withJarOnBuildPath(analyzer, "buildpath", () -> {
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
					appendProperties(analyzer, Constants.REQUIRE_CAPABILITY,
							"osgi.service;" + "filter:=\"(&(objectClass=" + StructuredDataFormat.class.getTypeName() + ")(formatId="
									+ handler.getFormatId() + "))\";resolution:=mandatory;effective:=active");
					appendProperties(analyzer, Constants.REQUIRE_CAPABILITY, "osgi.service;" + "filter:=\"(objectClass="
							+ SchemaManager.class.getTypeName() + ")\";resolution:=mandatory;effective:=active");
					appendProperties(analyzer, Constants.REQUIRE_CAPABILITY, "osgi.extender;" + "filter:=\"(osgi.extender="
							+ Schema.class.getPackage().getName() + ")\";resolution:=mandatory;effective:=resolve");

					appendProperties(analyzer, Constants.PROVIDE_CAPABILITY, newCapabilities);

					appendProperties(analyzer, Constants.IMPORT_PACKAGE,
							"uk.co.strangeskies.modabi," + "uk.co.strangeskies.modabi.schema");
				}
			});
		}
	}

	private void withJarOnBuildPath(Analyzer analyzer, String jarName, Runnable run) {
		try {
			File tempJar = createDirs(analyzer.getBase() + File.separator + "generated", "tmp", "jar");

			if (tempJar == null)
				throw new RuntimeException(
						"Cannot create temporary build path jar, location '" + analyzer.getBase() + "' does not exist");

			tempJar = new File(tempJar.getAbsolutePath() + File.separator + jarName + ".jar");

			analyzer.getJar().write(tempJar);
			new ContextClassLoaderRunner(tempJar.toURI().toURL()).run(run);
		} catch (Exception e) {
			throw flattenMessage(e);
		}
	}

	private SchemaException flattenMessage(Throwable e) {
		String message = e.getMessage();

		while ((e = e.getCause()) != null) {
			message += ": " + e.getMessage();
		}

		return new SchemaException(message, e);
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

	private void appendProperties(Analyzer analyzer, String property, String append) {
		String capabilities = analyzer.getProperty(property);

		if (capabilities != null && !"".equals(capabilities.trim())) {
			capabilities += "," + append;
		} else {
			capabilities = append;
		}

		analyzer.setProperty(property, capabilities);
	}
}
