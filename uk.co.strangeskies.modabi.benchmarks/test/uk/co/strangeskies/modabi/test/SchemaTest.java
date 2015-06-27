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
package uk.co.strangeskies.modabi.test;

import uk.co.strangeskies.modabi.io.structured.BufferedStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.management.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.schema.node.Model;
import uk.co.strangeskies.modabi.xml.impl.XMLTarget;

public class SchemaTest {
	public static void main(String... args) {
		System.out.println("Creating SchemaManager...");
		new SchemaTest().run(new SchemaManagerImpl());
	}

	public void run(SchemaManager schemaManager) {
		System.out.println("Unbinding BaseSchema...");
		BufferingStructuredDataTarget out = new BufferingStructuredDataTarget();
		schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(), out,
				schemaManager.getBaseSchema());
		out.buffer().pipeNextChild(new XMLTarget(System.out));

		System.out.println();
		System.out.println();
		System.out.println("Unbinding MetaSchema...");
		out = new BufferingStructuredDataTarget();
		schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(), out,
				schemaManager.getMetaSchema());
		BufferedStructuredDataSource buffered = out.buffer();

		buffered.pipeNextChild(new XMLTarget(System.out));
		buffered.reset();

		System.out.println();
		System.out.println();
		System.out.println("Re-binding MetaSchema...");
		Schema metaSchema = schemaManager.bind(schemaManager.getMetaSchema()
				.getSchemaModel(), buffered);

		boolean success = metaSchema.equals(schemaManager.getMetaSchema());
		System.out.println("Success: " + success);
		if (!success) {
			for (Model<?> model : schemaManager.getMetaSchema().getModels()) {
				System.out.println("model: " + model + " && "
						+ metaSchema.getModels().get(model.getName()));
				System.out.println("  == "
						+ model.source().equals(
								metaSchema.getModels().get(model.getName()).source()));
			}

			@SuppressWarnings("unchecked")
			Model<Schema> schemaModel = (Model<Schema>) metaSchema.getModels().get(
					new QualifiedName("schema", MetaSchema.NAMESPACE));

			System.out.println();
			System.out.println();
			System.out.println("Re-unbinding MetaSchema...");
			out = new BufferingStructuredDataTarget();
			schemaManager.unbind(schemaModel, out, metaSchema);
			buffered = out.buffer();
			buffered.pipeNextChild(new XMLTarget(System.out));
			buffered.reset();

			System.out.println();
			System.out.println();
			System.out.println("Re-re-binding MetaSchema...");
			metaSchema = schemaManager.bind(schemaManager.getMetaSchema()
					.getSchemaModel(), buffered);

			@SuppressWarnings("unchecked")
			Model<Schema> schemaModel2 = (Model<Schema>) metaSchema.getModels().get(
					new QualifiedName("schema", MetaSchema.NAMESPACE));

			System.out.println();
			System.out.println();
			System.out.println("Re-re-unbinding MetaSchema...");
			out = new BufferingStructuredDataTarget();
			schemaManager.unbind(schemaModel2, out, metaSchema);
			buffered = out.buffer();
			buffered.pipeNextChild(new XMLTarget(System.out));
		}

		System.out.print("Profiling Preparation");
		for (int i = 1; i <= 80; i++) {
			if (i % 50 == 0)
				System.out.println();
			System.out.print(".");

			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(),
					new BufferingStructuredDataTarget(), schemaManager.getMetaSchema());

			buffered.reset();
			schemaManager.bind(schemaManager.getMetaSchema().getSchemaModel(),
					buffered);
		}
		System.out.println();

		int profileRounds = 40;

		System.out.print("Unbinding Profiling");
		long startTime = System.currentTimeMillis();
		for (int i = 1; i <= profileRounds; i++) {
			if (i % 50 == 0)
				System.out.println();
			System.out.print(".");

			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(),
					new BufferingStructuredDataTarget(), schemaManager.getMetaSchema());
		}
		long totalTimeUnbinding = System.currentTimeMillis() - startTime;
		System.out.println();

		System.out.print("Binding Profiling");
		startTime = System.currentTimeMillis();
		for (int i = 1; i <= profileRounds; i++) {
			if (i % 50 == 0)
				System.out.println();
			System.out.print(".");

			buffered.reset();
			schemaManager.bind(schemaManager.getMetaSchema().getSchemaModel(),
					buffered);
		}
		long totalTimeBinding = System.currentTimeMillis() - startTime;
		System.out.println();

		System.out.println("Time per unbind: " + (double) totalTimeUnbinding
				/ (profileRounds * 1000) + " seconds");
		System.out.println("Time per bind: " + (double) totalTimeBinding
				/ (profileRounds * 1000) + " seconds");
	}
}
