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

import java.util.HashMap;
import java.util.Map;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.io.structured.BufferedStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget.StructuredDataTargetBuffer;
import uk.co.strangeskies.modabi.json.impl.JsonTarget;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.xml.impl.XmlTarget;

public class SchemaTest {
	public static void main(String... args) {
		System.out.println("Creating SchemaManager...");
		new SchemaTest().run(new SchemaManagerImpl());
	}

	public void run(SchemaManager schemaManager) {
		Map<String, Integer> stringIntMap = new HashMap<>();
		stringIntMap.put("first", 1);
		stringIntMap.put("second", 2);
		stringIntMap.put("third", 3);
		@SuppressWarnings("unchecked")
		Model<Map<?, ?>> stringIntMapModel = (Model<Map<?, ?>>) schemaManager
				.getBaseSchema().getModels().get(new QualifiedName("stringIntMap",
						BaseSchema.QUALIFIED_NAME.getNamespace()));
		schemaManager.unbind(stringIntMapModel, new XmlTarget(System.out),
				stringIntMap);
		schemaManager.unbind(stringIntMapModel, new JsonTarget(System.out, true),
				stringIntMap);

		System.out.println("Unbinding BaseSchema...");
		StructuredDataTargetBuffer out = BufferingStructuredDataTarget
				.singleBuffer();
		BufferedStructuredDataSource buffered = out.getBuffer();
		schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(), out,
				schemaManager.getBaseSchema());
		buffered.pipeNextChild(new XmlTarget(System.out));

		System.out.println();
		System.out.println();
		System.out.println("Unbinding MetaSchema...");
		out = BufferingStructuredDataTarget.singleBuffer();
		buffered = out.getBuffer();
		schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(), out,
				schemaManager.getMetaSchema());

		buffered.pipeNextChild(new XmlTarget(System.out));
		buffered.reset();

		System.out.println();
		System.out.println();
		System.out.println("Re-binding MetaSchema...");
		Schema metaSchema = schemaManager
				.bind(schemaManager.getMetaSchema().getSchemaModel(), buffered);

		boolean success = metaSchema.equals(schemaManager.getMetaSchema());
		System.out.println("Success: " + success);

		@SuppressWarnings("unchecked")
		Model<Schema> schemaModel = (Model<Schema>) metaSchema.getModels().get(
				new QualifiedName("schema", MetaSchema.QUALIFIED_NAME.getNamespace()));

		System.out.println();
		System.out.println();
		System.out.println("Re-unbinding MetaSchema...");
		out = BufferingStructuredDataTarget.singleBuffer();
		buffered = out.getBuffer();
		schemaManager.unbind(schemaModel, out, metaSchema);
		buffered.pipeNextChild(new XmlTarget(System.out));
		buffered.reset();

		System.out.println();
		System.out.println();
		System.out.println("Re-re-binding MetaSchema...");
		metaSchema = schemaManager
				.bind(schemaManager.getMetaSchema().getSchemaModel(), buffered);

		@SuppressWarnings("unchecked")
		Model<Schema> schemaModel2 = (Model<Schema>) metaSchema.getModels().get(
				new QualifiedName("schema", MetaSchema.QUALIFIED_NAME.getNamespace()));

		System.out.println();
		System.out.println();
		System.out.println("Re-re-unbinding MetaSchema...");
		out = BufferingStructuredDataTarget.singleBuffer();
		buffered = out.getBuffer();
		schemaManager.unbind(schemaModel2, out, metaSchema);
		buffered.pipeNextChild(new XmlTarget(System.out));

		System.out.print("Profiling Preparation");
		for (int i = 1; i <= 60; i++) {
			if (i % 50 == 0)
				System.out.println();
			System.out.print(".");

			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(),
					BufferingStructuredDataTarget.singleBuffer(),
					schemaManager.getMetaSchema());

			buffered.reset();
			schemaManager.bind(schemaManager.getMetaSchema().getSchemaModel(),
					buffered);
		}
		System.out.println();

		int profileRounds = 20;

		System.out.print("Unbinding Profiling");
		long startTime = System.currentTimeMillis();
		for (int i = 1; i <= profileRounds; i++) {
			if (i % 50 == 0)
				System.out.println();
			System.out.print(".");

			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(),
					BufferingStructuredDataTarget.singleBuffer(),
					schemaManager.getMetaSchema());
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

		System.out.println("Time per unbind: "
				+ (double) totalTimeUnbinding / (profileRounds * 1000) + " seconds");
		System.out.println("Time per bind: "
				+ (double) totalTimeBinding / (profileRounds * 1000) + " seconds");
	}
}
