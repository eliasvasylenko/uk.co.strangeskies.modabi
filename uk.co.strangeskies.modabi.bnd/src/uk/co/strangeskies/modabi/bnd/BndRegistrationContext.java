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
import static uk.co.strangeskies.log.Log.forwardingLog;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Resource;
import uk.co.strangeskies.log.Log;
import uk.co.strangeskies.log.Log.Level;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.plugin.ModabiRegistration;
import uk.co.strangeskies.modabi.plugin.RegistrationContext;
import uk.co.strangeskies.reflection.resource.Attribute;
import uk.co.strangeskies.reflection.resource.ManifestAttributeParser;

final class BndRegistrationContext implements RegistrationContext {
  private final Log log;
  private final Analyzer analyzer;
  private final DataFormat format;

  private final ClassLoader classLoader;
  private final Map<String, Resource> resources;
  private final Map<QualifiedName, Resource> availableDependencies;
  private final SchemaManager manager;

  public BndRegistrationContext(
      SchemaManager manager,
      Log log,
      Analyzer analyzer,
      DataFormat format,
      Set<String> sources) {
    this.log = log;
    this.analyzer = analyzer;
    this.format = format;

    classLoader = createClassLoader(analyzer);
    resources = collectSchemaResources(analyzer.getJar(), sources);
    availableDependencies = collectSchemaResources(analyzer.getClasspath());

    this.manager = manager;

    manager.registeredFormats().add(format);
  }

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
    return classLoader;
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
    return availableDependencies.keySet();
  }

  @Override
  public InputStream openDependency(QualifiedName name) throws Exception {
    availableDependencies();
    return availableDependencies.get(name).openInputStream();
  }

  public Log getLog() {
    return forwardingLog(() -> log);
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
          getLog().log(Level.WARN, "Cannot find Modabi documents in source directory: " + source);
        } else {
          resources.putAll(directoryResources);
        }
      } else {
        Resource resource = jar.getResource(source);

        if (resource == null) {
          getLog().log(Level.WARN, "Cannot find Modabi document at source location: " + source);
        } else {
          resources.put(source, resource);
        }
      }
    }
    return resources;
  }

  private Map<QualifiedName, Resource> collectSchemaResources(Collection<? extends Jar> jars) {
    Map<QualifiedName, Resource> resources = new HashMap<>();

    getLog().log(Level.TRACE, "Jars on build path: " + jars);

    for (Jar jar : jars) {
      Manifest manifest = null;
      try {
        manifest = jar.getManifest();
      } catch (Exception e) {}

      if (manifest != null) {
        String provides = manifest.getMainAttributes().getValue(Constants.PROVIDE_CAPABILITY);

        getLog().log(Level.TRACE, "Jar " + jar.getName() + " provides: " + provides);

        if (provides != null) {
          List<Attribute> attributes = new ManifestAttributeParser().parseAttributes(provides);

          attributes = attributes
              .stream()
              .filter(a -> a.name().equals(Schema.class.getPackage().getName()))
              .collect(toList());

          for (Attribute attribute : attributes) {
            QualifiedName schema = QualifiedName.parseString(
                attribute.properties().get(ModabiRegistration.SCHEMA).composeValueString());
            Resource resource = jar.getResource(
                attribute.properties().get(ModabiRegistration.RESOURCE).composeValueString());

            resources.put(schema, resource);
          }
        }
      }
    }

    return resources;
  }

  private ClassLoader createClassLoader(Analyzer analyzer) {
    List<URL> jarPaths;
    try {
      jarPaths = getJarPaths(analyzer);
    } catch (MalformedURLException e) {
      getLog().log(
          Level.ERROR,
          "Failed to load build path for bundle " + analyzer.getBundleSymbolicName(),
          e);
      throw new ModabiException(
          "Failed to load build path for bundle " + analyzer.getBundleSymbolicName(),
          e);
    }

    getLog().log(Level.INFO, "Classpath: " + jarPaths);

    ClassLoader classpathLoader = new URLClassLoader(
        jarPaths.toArray(new URL[jarPaths.size()]),
        getClass().getClassLoader());

    getLog().log(Level.TRACE, "ModabiBndPlugin.class loader: " + getClass().getClassLoader());
    getLog().log(Level.TRACE, "Classpath loader: " + classpathLoader);

    return classpathLoader;
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
        if (!new ManifestAttributeParser().parseAttributes(capabilities).stream().anyMatch(
            c -> c.equals(attribute))) {
          capabilities = attribute.toString() + "," + capabilities;
        }
      }
    }

    analyzer.setProperty(property, capabilities);
  }
}
