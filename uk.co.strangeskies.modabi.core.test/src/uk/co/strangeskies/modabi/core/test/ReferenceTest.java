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

import java.util.List;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.utilities.classpath.ContextClassLoaderRunner;

public class ReferenceTest {
	private static final int TIMEOUT_MILLISECONDS = 2000;

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

	@Test(timeout = TIMEOUT_MILLISECONDS)
	public void referenceTypeTest() {
		SchemaManager schemaManager = getService(SchemaManager.class);

		new ContextClassLoaderRunner(getClass().getClassLoader()).run(() -> {
			SchemaConfigurator generatedSchema = schemaManager.getSchemaConfigurator()
					.qualifiedName(new QualifiedName("testReferences", Namespace.getDefault()));

			System.out.println("!");
			System.out.println("!");
			System.out.println("!");
			DataType<List<?>> intListType = generatedSchema.addDataType()
					.name("intReference",
							Schema.MODABI_NAMESPACE)
					.baseType(schemaManager.getBaseSchema().derivedTypes().listType())
					.addChild(e -> e.data().name("element").type(schemaManager.getBaseSchema().derivedTypes().referenceType())
							.addChild(p -> p.data().name("targetModel")
									.provideValue(new BufferingDataTarget()
											.put(Primitive.QUALIFIED_NAME, new QualifiedName("schema", Schema.MODABI_NAMESPACE)).buffer()))
							.addChild(p -> p.data().name("targetId")
									.provideValue(new BufferingDataTarget()
											.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", Schema.MODABI_NAMESPACE)).buffer()))
							.addChild(d -> d.data().name("data")))
					.create();

			// target model types
			System.out.println();

			System.out.println(((DataNode<?>) intListType.effective().child("element", "targetModel")).getPostInputType());
			System.out
					.println(((DataNode<?>) intListType.effective().child("element", "targetModel")).getPostInputType().infer());

			System.out.println(((DataNode<?>) intListType.effective().child("element", "targetModel")).getDataType());
			System.out.println(((DataNode<?>) intListType.effective().child("element", "targetModel")).getDataType().infer());

			// target id types
			System.out.println();

			System.out.println(((DataNode<?>) intListType.effective().child("element", "targetId")).getPostInputType());
			System.out
					.println(((DataNode<?>) intListType.effective().child("element", "targetId")).getPostInputType().infer());

			System.out.println(((DataNode<?>) intListType.effective().child("element", "targetId")).getDataType());
			System.out.println(((DataNode<?>) intListType.effective().child("element", "targetId")).getDataType().infer());

			// finishing move!
			System.out.println();

			System.out.println(intListType.effective().getDataType());
			System.out.println(intListType.effective().getDataType().infer());
		});
	}
}
