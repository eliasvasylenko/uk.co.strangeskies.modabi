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
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.AnalyzerPlugin;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.utilities.ContextClassLoaderRunner;

public abstract class ModabiRegistration implements AnalyzerPlugin {
	private final StructuredDataFormat handler;
	private final SchemaManager manager;

	public ModabiRegistration(SchemaManager manager, StructuredDataFormat handler) {
		this.handler = handler;
		this.manager = manager;
		manager.registerDataInterface(handler);
	}

	public SchemaManager getManager() {
		return manager;
	}

	@Override
	public boolean analyzeJar(Analyzer analyzer) throws Exception {
		Jar jar = analyzer.getJar();

		scanSchemaAnnotations(jar, analyzer);

		registerSchemata(jar, analyzer);

		return false;
	}

	private void scanSchemaAnnotations(Jar jar, Analyzer analyzer) {}

	private void registerSchemata(Jar jar, Analyzer analyzer) {
		withJarOnBuildPath(analyzer, jar, "buildpath", () -> {
			Map<String, Resource> resources = jar.getDirectories()
					.get("META-INF/modabi");

			String newCapabilities = null;

			for (String resourceName : resources.keySet()) {
				Schema schema;
				try {
					schema = manager.bindSchema()
							.from(resources.get(resourceName).openInputStream()).resolve();
				} catch (Exception e) {
					throw new SchemaException(e);
				}

				String capability = "uk.co.strangeskies.modabi;schema:String=\""
						+ schema.getQualifiedName() + "\"";

				if (newCapabilities != null)
					newCapabilities += "," + capability;
				else
					newCapabilities = capability;
			}

			if (newCapabilities != null) {
				appendProperties(analyzer, Constants.REQUIRE_CAPABILITY,
						"osgi.service;" + "filter:=\"(&(objectClass="
								+ StructuredDataFormat.class.getTypeName() + ")(formatId="
								+ handler.getFormatId()
								+ "))\";resolution:=mandatory;effective:=active");

				appendProperties(analyzer, Constants.PROVIDE_CAPABILITY,
						newCapabilities);
			}
		});
	}

	private void withJarOnBuildPath(Analyzer analyzer, Jar jar, String jarName,
			Runnable run) {
		try {
			File tempJar = createDirs(
					analyzer.getBase() + File.separator + "generated", "tmp", "jar");

			if (tempJar == null)
				throw new RuntimeException(
						"Cannot create temporary build path jar, location '"
								+ analyzer.getBase() + "' does not exist");

			tempJar = new File(
					tempJar.getAbsolutePath() + File.separator + jarName + ".jar");

			jar.write(tempJar);
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

	private void appendProperties(Analyzer analyzer, String property,
			String append) {
		String capabilities = analyzer.getProperty(property);

		if (capabilities != null && !"".equals(capabilities.trim()))
			capabilities += "," + append;
		else
			capabilities = append;

		analyzer.setProperty(property, capabilities);
	}
}
