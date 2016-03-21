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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.service.AnalyzerPlugin;
import aQute.bnd.service.Plugin;
import aQute.service.reporter.Reporter;
import uk.co.strangeskies.bnd.ReporterLog;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.plugin.ModabiRegistration;
import uk.co.strangeskies.modabi.plugin.RegistrationContext;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;

/**
 * @author Elias N Vasylenko
 */
public abstract class ModabiBndPlugin implements AnalyzerPlugin, Plugin {
	private static final Object SOURCES_PROPERTY = "sources";
	private static final String DEFAULT_SOURCE = "META-INF/schemata/*";

	private final StructuredDataFormat format;
	private final Set<String> sources;
	private Log log = (l, m) -> {};

	public ModabiBndPlugin(StructuredDataFormat format) {
		this.format = format;

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

	@Override
	public synchronized boolean analyzeJar(Analyzer analyzer) throws Exception {
		try {
			scanSchemaAnnotations(analyzer);

			RegistrationContext context = new BndRegistrationContext(log, analyzer, format, sources);
			return new ModabiRegistration().registerSchemata(context);
		} catch (Throwable t) {
			log.log(Level.ERROR, "Failed to register modabi schemata " + sources, t);
			throw t;
		}
	}

	private void scanSchemaAnnotations(Analyzer analyzer) {}
}
