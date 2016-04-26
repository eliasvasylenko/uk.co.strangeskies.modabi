/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.test.
 *
 * uk.co.strangeskies.modabi.core.test is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.test.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.core.test;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;

public class ReferenceTest {
	private static final int TIMEOUT_MILLISECONDS = 2000;
	protected static final String XML_POSTFIX = ".xml";

	private <T> T getService(Class<T> clazz) {
		try {
			BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

			ServiceTracker<T, T> st = new ServiceTracker<>(context, clazz, null);
			st.open();
			try {
				return st.waitForService(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@SuppressWarnings("unchecked")
	private Model<NamedValue> getNamedValueModel(SchemaManager manager) {
		return (Model<NamedValue>) manager.registeredModels().get(new QualifiedName("namedValue", Schema.MODABI_NAMESPACE));
	}

	@SuppressWarnings("unchecked")
	private DataType<List<NamedValue>> getStringReferencesType(SchemaManager manager) {
		return (DataType<List<NamedValue>>) manager.registeredTypes()
				.get(new QualifiedName("stringReferences", Schema.MODABI_NAMESPACE));
	}

	@Test
	public void loadScriptTestSchema() {
		SchemaManager manager = getService(SchemaManager.class);

		Assert.assertNotNull(getNamedValueModel(manager));
		Assert.assertNotNull(getStringReferencesType(manager));
	}

	private InputStream getResouce(String resource) {
		return getClass().getResourceAsStream(resource + XML_POSTFIX);
	}
}
