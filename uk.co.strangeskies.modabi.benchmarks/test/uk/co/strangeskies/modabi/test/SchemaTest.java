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

import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.GeneratedSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.impl.BaseSchemaImpl;
import uk.co.strangeskies.modabi.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.io.json.impl.JsonTarget;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer.Navigable;
import uk.co.strangeskies.modabi.io.xml.XmlTarget;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.DataNode.Format;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.AnnotatedParameterizedTypes;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.AnnotatedWildcardTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;

public class SchemaTest {
	public static void main(String... args) {
		System.out.println("Creating SchemaManager...");
		new SchemaTest().run(new SchemaManagerImpl());
	}

	public void run(SchemaManager schemaManager) {
		manualSchemaCreationTest(schemaManager);

		System.out.println("Unbinding BaseSchema...");
		Navigable out = StructuredDataBuffer.singleBuffer();
		NavigableStructuredDataSource buffered = out.getBuffer();
		schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(), out,
				schemaManager.getBaseSchema());
		buffered.pipeNextChild(new XmlTarget(System.out));

		System.out.println();
		System.out.println();
		System.out.println("Unbinding MetaSchema...");
		out = StructuredDataBuffer.singleBuffer();
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
		out = StructuredDataBuffer.singleBuffer();
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
		out = StructuredDataBuffer.singleBuffer();
		buffered = out.getBuffer();
		schemaManager.unbind(schemaModel2, out, metaSchema);
		buffered.pipeNextChild(new XmlTarget(System.out));

		System.out.print("Profiling Preparation");
		for (int i = 1; i <= 60; i++) {
			if (i % 50 == 0)
				System.out.println();
			System.out.print(".");

			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(),
					StructuredDataBuffer.singleBuffer(), schemaManager.getMetaSchema());

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
					StructuredDataBuffer.singleBuffer(), schemaManager.getMetaSchema());
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

	private void manualSchemaCreationTest(SchemaManager schemaManager) {
		GeneratedSchema generatedSchema = schemaManager.generateSchema(
				new QualifiedName("testExtentions", Namespace.getDefault()));

		DataType<List<?>> intListType = generatedSchema
				.buildDataType(
						n -> n.name("intSet", Namespace.getDefault())
								.baseType(
										schemaManager.getBaseSchema().derivedTypes().listType())
				.addChild(e -> e.data().name("element")
						.type(schemaManager.getBaseSchema().primitiveType(Primitive.INT))));
		System.out.println(intListType.effective().getDataType());

		Map<String, Integer> stringIntMap = new HashMap<>();
		stringIntMap.put("first", 1);
		stringIntMap.put("second", 2);
		stringIntMap.put("third", 3);

		Model<Map<?, ?>> stringIntMapModel = generatedSchema.buildModel(n -> n
				.name("stringIntMap", Namespace.getDefault())
				.baseModel(schemaManager.getBaseSchema().models().mapModel())
				.addChild(s -> s.complex().name("entrySet")
						.addChild(e -> e.complex().name("entry")
								.addChild(k -> k.data().name("key").type(schemaManager
										.getBaseSchema().primitiveType(Primitive.STRING)))
						.addChild(v -> v.complex().name("value")
								.baseModel(schemaManager.getBaseSchema().models().simpleModel())
								.addChild(c -> c.data().name("content").type(schemaManager
										.getBaseSchema().primitiveType(Primitive.INT)))))));
		System.out.println(stringIntMapModel.effective().getDataType());
		System.out.println("    ~# " + stringIntMapModel.effective().getDataType()
				.getResolver().getBounds());

		schemaManager.unbind(stringIntMapModel, new XmlTarget(System.out),
				stringIntMap);
		schemaManager.unbind(stringIntMapModel, new JsonTarget(System.out, true),
				stringIntMap);
		System.out.println();

		Model<Map<?, ?>> mapmap = generatedSchema
				.buildModel(n -> n
						.name("mapmap", Namespace
								.getDefault())
				.baseModel(schemaManager.getBaseSchema().models().mapModel())
				.addChild(s -> s.complex().name("entrySet").addChild(e -> e.complex()
						.name("entry")
						.addChild(k -> k.data().name("key")
								.type(schemaManager.getBaseSchema().derivedTypes().setType())
								.addChild(c -> c.data().name("element")
										.type(schemaManager.getBaseSchema()
												.primitiveType(Primitive.LONG))))
						.addChild(
								v -> v.complex().name("value")
										.baseModel(
												schemaManager.getBaseSchema().models().mapModel())
										.addChild(ss -> ss.complex().name("entrySet")
												.addChild(ee -> ee.complex().name("entry")
														.addChild(kk -> kk.data().name("key")
																.type(schemaManager.getBaseSchema()
																		.primitiveType(Primitive.BOOLEAN)))
														.addChild(
																vv -> vv.complex().name("value")
																		.baseModel(schemaManager.getBaseSchema()
																				.models().simpleModel())
																		.addChild(cc -> cc.data().name("content")
																				.type(schemaManager.getBaseSchema()
																						.derivedTypes()
																						.classType())))))))));
		System.out.println(stringIntMapModel.effective().getDataType());
		System.out.println("    ~# " + stringIntMapModel.effective().getDataType()
				.getResolver().getBounds());

		schemaManager.unbind(mapmap, new XmlTarget(System.out), stringIntMap);

		/*-
		 * The following should be inferred as having type:
		 * java.util.Map<java.util.List<byte[]>, java.util.Map<java.lang.String, java.lang.Integer>>
		 */
		AnnotatedType annotatedMapEntry = AnnotatedParameterizedTypes.from(
				AnnotatedTypes.over(Map.Entry.class, Annotations.from(Infer.class)),
				Arrays.asList(AnnotatedWildcardTypes.unbounded(),
						AnnotatedWildcardTypes.unbounded()));
		TypeToken<?> inferredMapEntry = TypeToken.over(annotatedMapEntry);
		TypeToken<?> inferredMapEntrySet = TypeToken
				.over(AnnotatedParameterizedTypes.from(
						AnnotatedTypes.over(Set.class, Annotations.from(Infer.class)),
						Arrays.asList(
								AnnotatedWildcardTypes.upperBounded(annotatedMapEntry))));
		Model<Map<?, ?>> mapModel3 = generatedSchema
				.buildModel(n -> n.name("map3", Namespace.getDefault())
						.dataType(new TypeToken<@Infer Map<?, ?>>() {})
						.addChild(e -> e.complex().name("entrySet").inline(true)
								.inMethod("null").dataType(inferredMapEntrySet)
								.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
								.addChild(s -> s.inputSequence().name("entrySet")
										.inMethodChained(true))
						.addChild(
								f -> f.complex().name("entry").outMethodIterable(true)
										.occurrences(Range.create(0, null)).inMethod("add")
										.outMethod("this")
										.bindingStrategy(BindingStrategy.IMPLEMENT_IN_PLACE)
										.bindingType(BaseSchemaImpl.class)
										.unbindingMethod("mapEntry").dataType(inferredMapEntry)
										.addChild(k -> k.data().name("key").inMethod("null")
												.format(Format.PROPERTY)
												.type(schemaManager.getBaseSchema().derivedTypes()
														.listType())
										.addChild(l -> l.data().name("element")
												.type(schemaManager.getBaseSchema()
														.primitiveType(Primitive.BINARY))))
								.addChild(v -> v.complex().name("value").inMethod("null")
										.baseModel(
												schemaManager.getBaseSchema().models().mapModel())
										.addChild(s -> s.complex().name("entrySet")
												.addChild(ee -> ee.complex().name("entry")
														.addChild(k -> k.data().name("key")
																.type(schemaManager.getBaseSchema()
																		.primitiveType(Primitive.STRING)))
														.addChild(vv -> vv.complex().name("value")
																.baseModel(schemaManager.getBaseSchema()
																		.models().simpleModel())
																.addChild(cc -> cc.data().name("content")
																		.type(schemaManager.getBaseSchema()
																				.primitiveType(Primitive.INT))))))))));
		System.out.println(mapModel3.effective().getDataType());
	}
}
