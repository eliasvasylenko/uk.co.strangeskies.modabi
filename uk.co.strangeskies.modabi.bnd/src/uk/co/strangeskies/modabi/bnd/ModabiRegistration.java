/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.AnalyzerPlugin;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;

public abstract class ModabiRegistration implements AnalyzerPlugin {
	private final String handlerId;
	private final SchemaManager manager;

	public ModabiRegistration(SchemaManager manager, String handlerId) {
		this.handlerId = handlerId;
		this.manager = manager;
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
		ClassLoader threadClassLoader = Thread.currentThread()
				.getContextClassLoader();

		try {
			File tempJar = createDirs(
					analyzer.getBase() + File.separator + "generated", "tmp", "jar");

			if (tempJar == null)
				throw new RuntimeException(
						"Cannot create temporary build path jar, location '"
								+ analyzer.getBase() + "' does not exist");

			tempJar = new File(
					tempJar.getAbsolutePath() + File.separator + "buildpath.jar");

			jar.write(tempJar);
			Thread.currentThread().setContextClassLoader(new URLClassLoader(
					new URL[] { new URL("file:" + tempJar) }, threadClassLoader));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		try {
			Map<String, Resource> resources = jar.getDirectories()
					.get("META-INF/modabi");

			String newCapabilities = null;

			for (String resourceName : resources.keySet()) {
				Schema schema;
				try {
					schema = manager.bindSchema()
							.from(resources.get(resourceName).openInputStream()).resolve();

					String capability = "uk.co.strangeskies.modabi;schema:String=\""
							+ schema.getQualifiedName() + "\"";

					if (newCapabilities != null)
						newCapabilities += "," + capability;
					else
						newCapabilities = capability;
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new IllegalStateException(
							"Schema resource cannot be bound, ensure all participating classes and FileLoader implementations are on build path",
							e);
				}
			}

			if (newCapabilities != null) {
				appendProperties(analyzer, Constants.REQUIRE_CAPABILITY,
						"osgi.service;"
								+ "filter:=\"(&(objectClass=uk.co.strangeskies.modabi.io.structured.FileLoader)(id="
								+ handlerId + "))\";" + "resolution:=mandatory");

				appendProperties(analyzer, Constants.PROVIDE_CAPABILITY,
						newCapabilities);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Thread.currentThread().setContextClassLoader(threadClassLoader);
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
