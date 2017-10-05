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

import static uk.co.strangeskies.log.Log.Level.ERROR;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.service.AnalyzerPlugin;
import aQute.bnd.service.Plugin;
import aQute.service.reporter.Reporter;
import uk.co.strangeskies.log.Log;
import uk.co.strangeskies.log.Log.Level;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.impl.SchemaManagerService;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.plugin.ModabiRegistration;
import uk.co.strangeskies.modabi.plugin.RegistrationContext;

/**
 * @author Elias N Vasylenko
 */
public abstract class ModabiBndPlugin implements AnalyzerPlugin, Plugin {
  private static SchemaManager MANAGER;
  private static final Object SOURCES_PROPERTY = "sources";
  private static final String DEFAULT_SOURCE = "META-INF/schemata/*";

  private final DataFormat format;
  private final Set<String> sources;
  private Log log;

  public ModabiBndPlugin(DataFormat format) {
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
    log = new Log() {
      @Override
      public void log(Level level, String message, Throwable exception) {
        if (level == ERROR)
          processor.exception(exception, message);
        else
          log(level, message + exception.getMessage());
      }

      @Override
      public void log(Level level, Throwable exception) {
        if (level == ERROR)
          processor.exception(exception, exception.getMessage());
        else
          log(level, exception.getMessage());
      }

      @Override
      public void log(Level level, String message) {
        switch (level) {
        case ERROR:
          processor.error(message);
        case WARN:
          processor.warning(message);
        default:
          processor.trace(message);
        }
      }

    };
  }

  @Override
  public synchronized boolean analyzeJar(Analyzer analyzer) throws Exception {
    try {
      if (MANAGER == null) {
        MANAGER = new SchemaManagerService();
      }

      scanSchemaAnnotations(analyzer);

      RegistrationContext context = new BndRegistrationContext(
          MANAGER,
          log,
          analyzer,
          format,
          sources);
      return new ModabiRegistration().registerSchemata(context);
    } catch (Throwable t) {
      log.log(Level.ERROR, "Failed to register modabi schemata " + sources, t);
      throw t;
    }
  }

  private void scanSchemaAnnotations(Analyzer analyzer) {}
}
