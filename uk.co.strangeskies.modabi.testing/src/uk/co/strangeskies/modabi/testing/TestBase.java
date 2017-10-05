/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.testing.
 *
 * uk.co.strangeskies.modabi.testing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.testing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.testing.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.testing;

import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.ServiceTracker;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.schema.Model;

/**
 * A simple superclass for testing over schema processing, providing some
 * convenience methods for common uses.
 * 
 * @author Elias N Vasylenko
 */
public abstract class TestBase {
  protected static final int SERVICE_TIMEOUT_MILLISECONDS = 2000;
  protected static final int TEST_TIMEOUT_MILLISECONDS = 3000;
  protected static final String XML_POSTFIX = ".xml";

  private final Namespace defaultNamespace;
  private final int serviceTimeoutMilliseconds;

  public TestBase() {
    this(Schema.MODABI_NAMESPACE);
  }

  public TestBase(Namespace defaultNamespace) {
    this(defaultNamespace, SERVICE_TIMEOUT_MILLISECONDS);
  }

  public TestBase(Namespace defaultNamespace, int serviceTimeoutMilliseconds) {
    this.serviceTimeoutMilliseconds = serviceTimeoutMilliseconds;
    this.defaultNamespace = defaultNamespace;
  }

  public Namespace getDefaultNamespace() {
    return defaultNamespace;
  }

  public int getServiceTimeoutMilliseconds() {
    return serviceTimeoutMilliseconds;
  }

  protected ClassLoader getClassLoader() {
    return FrameworkUtil.getBundle(getClass()).adapt(BundleWiring.class).getClassLoader();
  }

  protected <T> T getService(Class<T> clazz) {
    try {
      BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

      ServiceTracker<T, T> st = new ServiceTracker<>(context, clazz, null);
      st.open();
      try {
        return st.waitForService(getServiceTimeoutMilliseconds());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
  }

  protected SchemaManager manager() {
    return getService(SchemaManager.class);
  }

  protected Model<?> getModel(String name) {
    return getModel(new QualifiedName(name, getDefaultNamespace()));
  }

  protected Model<?> getModel(QualifiedName name) {
    return manager().registeredModels().getFuture(name).join();
  }

  protected ReadableByteChannel getResouce(String resource) {
    return Channels.newChannel(getClass().getResourceAsStream(resource + XML_POSTFIX));
  }
}
