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

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer.Navigable;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.testing.TestBase;
import uk.co.strangeskies.reflection.AnnotatedParameterizedTypes;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.AnnotatedWildcardTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.classpath.ContextClassLoaderRunner;

public class SchemaTest extends TestBase {
	private static final String INLINE_DATA = "inlineData";
	private static final TypeToken<IdentityProperty<DataSource>> INLINE_DATA_TYPE = new TypeToken<IdentityProperty<DataSource>>() {};

	private static final String INLINE_PROPERTY_RESOURCE = "InlineProperty";

	@Test
	public void abstractChoiceNodeTest() {
		SchemaManager schemaManager = getService(SchemaManager.class);
		schemaManager.getSchemaConfigurator().qualifiedName(new QualifiedName("choiceNodeTest")).addModel(
				"choiceNodeTestModel",
				m -> m.abstractness(Abstractness.ABSTRACT).addChild(c -> c.choice().abstractness(Abstractness.ABSTRACT)));
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void inlineDataSchemaTest() {
		Model<IdentityProperty<DataSource>> inlineData = getModel(INLINE_DATA, INLINE_DATA_TYPE);

		Assert.assertNotNull(inlineData);
	}

	@Test(timeout = 5000)
	public void schemaTest() throws InterruptedException {
		Thread.sleep(1000);

		System.out.println();
		System.out.println();
		System.out.println("*");
		System.out.println();
		System.out.println();
		
		manager().bindSchema().withClassLoader(getClass().getClassLoader()).from(() -> this.getResouce("SchemaTest"))
				.resolve(5000);
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void inlinePropertyTest() {
		Property<DataSource, DataSource> inlineProperty = manager().bind(getModel(INLINE_DATA, INLINE_DATA_TYPE))
				.withProvider(Provider.over(INLINE_DATA_TYPE, () -> new IdentityProperty<>()))
				.from(() -> this.getResouce(INLINE_PROPERTY_RESOURCE)).resolve(1000);

		DataSource expectedValue = new BufferingDataTarget()

				.put(Primitive.STRING, "test")

				.put(Primitive.STRING, "value")

				.put(Primitive.STRING, "list").buffer();

		Assert.assertNotNull(inlineProperty);
		Assert.assertEquals(expectedValue, inlineProperty.get());
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void schemaManagerServiceTest() {
		Assert.assertNotNull(manager());
	}

	@Test
	public void schemaUnbindingTest() {
		new ContextClassLoaderRunner(getClass().getClassLoader()).run(() -> {
			SchemaManager schemaManager = getService(SchemaManager.class);

			System.out.println("Unbinding BaseSchema...");
			Navigable out = StructuredDataBuffer.singleBuffer();
			NavigableStructuredDataSource buffered = out.getBuffer();
			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(), schemaManager.getBaseSchema()).to(out);
			buffered.pipeNextChild(schemaManager.registeredFormats().get("xml").saveData(System.out));

			System.out.println();
			System.out.println();
			System.out.println("Unbinding MetaSchema...");
			out = StructuredDataBuffer.singleBuffer();
			buffered = out.getBuffer();
			schemaManager.unbind(schemaManager.getMetaSchema().getSchemaModel(), schemaManager.getMetaSchema()).to(out);

			try {
				System.out.println(Class.forName("uk.co.strangeskies.modabi.schema.SchemaNode"));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

			buffered.pipeNextChild(schemaManager.registeredFormats().get("xml").saveData(System.out));
			buffered.reset();

			System.out.println();
			System.out.println();
			System.out.println("Re-binding MetaSchema...");
			Schema metaSchema = schemaManager.bind(schemaManager.getMetaSchema().getSchemaModel()).from(buffered).resolve();

			boolean success = metaSchema.equals(schemaManager.getMetaSchema());
			System.out.println("Success: " + success);

			@SuppressWarnings("unchecked")
			Model<Schema> schemaModel = (Model<Schema>) metaSchema.models()
					.get(new QualifiedName("schema", MetaSchema.QUALIFIED_NAME.getNamespace()));

			System.out.println();
			System.out.println();
			System.out.println("Re-unbinding MetaSchema...");
			out = StructuredDataBuffer.singleBuffer();
			buffered = out.getBuffer();
			schemaManager.unbind(schemaModel, metaSchema).to(out);
			buffered.pipeNextChild(schemaManager.registeredFormats().get("xml").saveData(System.out));
			buffered.reset();

			System.out.println();
			System.out.println();
			System.out.println("Re-re-binding MetaSchema...");
			metaSchema = schemaManager.bind(schemaManager.getMetaSchema().getSchemaModel()).from(buffered).resolve();

			@SuppressWarnings("unchecked")
			Model<Schema> schemaModel2 = (Model<Schema>) metaSchema.models()
					.get(new QualifiedName("schema", MetaSchema.QUALIFIED_NAME.getNamespace()));

			System.out.println();
			System.out.println();
			System.out.println("Re-re-unbinding MetaSchema...");
			out = StructuredDataBuffer.singleBuffer();
			buffered = out.getBuffer();
			schemaManager.unbind(schemaModel2, metaSchema).to(out);
			buffered.pipeNextChild(schemaManager.registeredFormats().get("xml").saveData(System.out));
		});
	}

	@Test(timeout = TEST_TIMEOUT_MILLISECONDS)
	public void manualSchemaCreationTest() {
		SchemaManager schemaManager = getService(SchemaManager.class);

		SchemaConfigurator generatedSchema = schemaManager.getSchemaConfigurator()
				.qualifiedName(new QualifiedName("testExtentions", Namespace.getDefault()));

		DataType<List<?>> intListType = generatedSchema.addDataType().name("intSet", Namespace.getDefault())
				.baseType(schemaManager.getBaseSchema().derivedTypes().listType())
				.addChild(e -> e.data().name("element").type(schemaManager.getBaseSchema().primitiveType(Primitive.INT)))
				.create();
		System.out.println(intListType.effective().dataType());

		Map<String, Integer> stringIntMap = new HashMap<>();
		stringIntMap.put("first", 1);
		stringIntMap.put("second", 2);
		stringIntMap.put("third", 3);

		@SuppressWarnings("unchecked")
		Model<Map<?, ?>> stringIntMapModel = generatedSchema.addModel().name("stringIntMap", Namespace.getDefault())
				.baseModel(schemaManager.getBaseSchema().baseModels().mapModel())
				.addChild(s -> s.complex().name("entrySet").addChild(e -> e.complex().name("entry")
						.addChild(k -> k.data().name("key").type(schemaManager.getBaseSchema().primitiveType(Primitive.STRING)))
						.addChild(v -> v.complex().name("value")
								.<Object> model((Model<Object>) schemaManager.getBaseSchema().baseModels().simpleModel()).addChild(
										c -> c.data().name("content").type(schemaManager.getBaseSchema().primitiveType(Primitive.INT))))))
				.create();
		System.out.println(stringIntMapModel.effective().dataType());
		System.out.println("    ~# " + stringIntMapModel.effective().dataType().getResolver().getBounds());

		schemaManager.unbind(stringIntMapModel, stringIntMap)
				.to(schemaManager.registeredFormats().get("xml").saveData(System.out));
		System.out.println();

		/*-
		 * TODO
		 * 
		 * The following should be inferred as having type:
		 * java.util.Map<java.util.List<byte[]>, java.util.Map<java.lang.String, java.lang.Integer>>
		 */
		AnnotatedType annotatedMapEntry = AnnotatedParameterizedTypes.from(
				AnnotatedTypes.over(Map.Entry.class, Annotations.from(Infer.class)),
				Arrays.asList(AnnotatedWildcardTypes.unbounded(), AnnotatedWildcardTypes.unbounded()));
		TypeToken<?> inferredMapEntry = TypeToken.over(annotatedMapEntry);
		TypeToken<?> inferredMapEntrySet = TypeToken
				.over(AnnotatedParameterizedTypes.from(AnnotatedTypes.over(Set.class, Annotations.from(Infer.class)),
						Arrays.asList(AnnotatedWildcardTypes.upperBounded(annotatedMapEntry))));
		@SuppressWarnings("unchecked")
		Model<Map<?, ?>> mapModel3 = generatedSchema.addModel()
				.name("map3",
						Namespace
								.getDefault())
				.dataType(
						new @Infer TypeToken<Map<?, ?>>() {})
				.addChild(
						e -> e.complex().name("entrySet").inline(true).inMethod("null")
								.dataType(
										inferredMapEntrySet)
								.bindingStrategy(
										BindingStrategy.TARGET_ADAPTOR)
								.addChild(
										s -> s.inputSequence().name("entrySet")
												.inMethodChained(true))
								.addChild(
										f -> f.complex().name("entry").occurrences(Range.between(0, null)).inMethod("add").outMethod("this")
												.bindingStrategy(BindingStrategy.IMPLEMENT_IN_PLACE).bindingType(BaseSchema.class)
												.unbindingMethod("mapEntry").dataType(inferredMapEntry)
												.addChild(
														k -> k.data().name("key").inMethod("null").format(DataNode.Format.PROPERTY)
																.type(schemaManager.getBaseSchema().derivedTypes().listType()).addChild(
																		l -> l.data().name("element")
																				.type(schemaManager.getBaseSchema().primitiveType(Primitive.BINARY))))
												.addChild(
														v -> v.complex().name("value").inMethod("null")
																.model(
																		schemaManager.getBaseSchema().baseModels()
																				.mapModel())
																.addChild(
																		s -> s.complex()
																				.name(
																						"entrySet")
																				.addChild(ee -> ee.complex().name("entry")
																						.addChild(k -> k.data().name("key")
																								.type(schemaManager.getBaseSchema()
																										.primitiveType(Primitive.STRING)))
																						.addChild(vv -> vv.complex().name("value")
																								.<Object> model((Model<Object>) schemaManager.getBaseSchema()
																										.baseModels().simpleModel())
																								.addChild(cc -> cc.data().name("content").type(
																										schemaManager.getBaseSchema().primitiveType(Primitive.INT)))))))))
				.create();
		System.out.println(mapModel3.effective().dataType());
		System.out.println(mapModel3.effective().dataType().getResolver().getBounds());
	}
}
