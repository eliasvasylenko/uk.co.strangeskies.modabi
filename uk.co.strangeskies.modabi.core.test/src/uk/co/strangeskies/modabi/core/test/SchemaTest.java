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

import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.GeneratedSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer.Navigable;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.AnnotatedParameterizedTypes;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.AnnotatedWildcardTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.utilities.ContextClassLoaderRunner;

public class SchemaTest {
	private <T> T getService(Class<T> clazz) {
		try {
			BundleContext context = FrameworkUtil.getBundle(this.getClass())
					.getBundleContext();

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

	@Test
	public void abstractChoiceNodeTest() {
		SchemaManager schemaManager = getService(SchemaManager.class);
		schemaManager.generateSchema(new QualifiedName("choiceNodeTest"))
				.buildModel("choiceNodeTestModel",
						m -> m.isAbstract(true).addChild(c -> c.choice().isAbstract(true)));
	}

	@Test
	public void schemaManagerServiceTest() {
		Assert.assertNotNull(getService(SchemaManager.class));
	}

	@Test
	public void schemaUnbindingTest() {
		new ContextClassLoaderRunner(getClass().getClassLoader()).run(() -> {
			SchemaManager schemaManager = getService(SchemaManager.class);

			System.out.println("Unbinding BaseSchema...");
			Navigable out = StructuredDataBuffer.singleBuffer();
			NavigableStructuredDataSource buffered = out.getBuffer();
			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(),
					schemaManager.getBaseSchema()).to(out);
			buffered.pipeNextChild(
					schemaManager.getDataInterface("xml").saveData(System.out));

			System.out.println();
			System.out.println();
			System.out.println("Unbinding MetaSchema...");
			out = StructuredDataBuffer.singleBuffer();
			buffered = out.getBuffer();
			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(),
					schemaManager.getMetaSchema()).to(out);

			try {
				System.out.println(
						Class.forName("uk.co.strangeskies.modabi.schema.SchemaNode"));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

			buffered.pipeNextChild(
					schemaManager.getDataInterface("xml").saveData(System.out));
			buffered.reset();

			System.out.println();
			System.out.println();
			System.out.println("Re-binding MetaSchema...");
			Schema metaSchema = schemaManager
					.bind(schemaManager.getMetaSchema().getSchemaModel()).from(buffered)
					.resolve();

			boolean success = metaSchema.equals(schemaManager.getMetaSchema());
			System.out.println("Success: " + success);

			@SuppressWarnings("unchecked")
			Model<Schema> schemaModel = (Model<Schema>) metaSchema.getModels()
					.get(new QualifiedName("schema",
							MetaSchema.QUALIFIED_NAME.getNamespace()));

			System.out.println();
			System.out.println();
			System.out.println("Re-unbinding MetaSchema...");
			out = StructuredDataBuffer.singleBuffer();
			buffered = out.getBuffer();
			schemaManager.unbind(schemaModel, metaSchema).to(out);
			buffered.pipeNextChild(
					schemaManager.getDataInterface("xml").saveData(System.out));
			buffered.reset();

			System.out.println();
			System.out.println();
			System.out.println("Re-re-binding MetaSchema...");
			metaSchema = schemaManager
					.bind(schemaManager.getMetaSchema().getSchemaModel()).from(buffered)
					.resolve();

			@SuppressWarnings("unchecked")
			Model<Schema> schemaModel2 = (Model<Schema>) metaSchema.getModels()
					.get(new QualifiedName("schema",
							MetaSchema.QUALIFIED_NAME.getNamespace()));

			System.out.println();
			System.out.println();
			System.out.println("Re-re-unbinding MetaSchema...");
			out = StructuredDataBuffer.singleBuffer();
			buffered = out.getBuffer();
			schemaManager.unbind(schemaModel2, metaSchema).to(out);
			buffered.pipeNextChild(
					schemaManager.getDataInterface("xml").saveData(System.out));
		});
	}

	@Test
	public void manualSchemaCreationTest() {
		SchemaManager schemaManager = getService(SchemaManager.class);

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

		schemaManager.unbind(stringIntMapModel, stringIntMap)
				.to(schemaManager.getDataInterface("xml").saveData(System.out));
		System.out.println();

		/*-
		 * TODO
		 * 
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
								f -> f.complex().name("entry")
										.occurrences(Range.between(0, null)).inMethod("add")
										.outMethod("this")
										.bindingStrategy(BindingStrategy.IMPLEMENT_IN_PLACE)
										.bindingType(BaseSchema.class).unbindingMethod("mapEntry")
										.dataType(inferredMapEntry)
										.addChild(k -> k.data().name("key").inMethod("null")
												.format(DataNode.Format.PROPERTY)
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
