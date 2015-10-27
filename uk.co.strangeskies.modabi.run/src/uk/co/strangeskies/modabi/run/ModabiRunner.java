/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.benchmarks.
 *
 * uk.co.strangeskies.modabi.benchmarks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.benchmarks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.benchmarks.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.run;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.io.xml.XmlInterface;
import uk.co.strangeskies.utilities.ContextClassLoaderRunner;

@Component
public class ModabiRunner {
	private SchemaManager manager;

	@Activate
	public void run(BundleContext context) throws BundleException {
		new ContextClassLoaderRunner(getClass().getClassLoader()).run(() -> {
			try {
				manager = new SchemaManagerImpl();
				manager.registerDataInterface(new XmlInterface());

				System.out.println("Binding benchmark schema...");

				Schema schema = manager.bindSchema().from(getClass()
						.getResource("/META-INF/modabi/BenchmarkSchema.xml").openStream())
						.resolve(500);

				System.out.println("Done! " + schema.getQualifiedName());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});

		context.getBundle(0).stop();
	}
}
