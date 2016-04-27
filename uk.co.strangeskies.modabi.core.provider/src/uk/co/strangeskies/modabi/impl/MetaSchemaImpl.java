/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl;

import static uk.co.strangeskies.mathematics.Range.between;
import static uk.co.strangeskies.modabi.Abstractness.ABSTRACT;
import static uk.co.strangeskies.modabi.ValueResolution.REGISTRATION_TIME;
import static uk.co.strangeskies.modabi.io.Primitive.BOOLEAN;
import static uk.co.strangeskies.modabi.io.Primitive.STRING;
import static uk.co.strangeskies.modabi.processing.BindingStrategy.STATIC_FACTORY;
import static uk.co.strangeskies.modabi.processing.BindingStrategy.TARGET_ADAPTOR;
import static uk.co.strangeskies.modabi.schema.DataNode.Format.CONTENT;
import static uk.co.strangeskies.modabi.schema.DataNode.Format.PROPERTY;
import static uk.co.strangeskies.modabi.schema.DataNode.Format.SIMPLE;

import java.util.Arrays;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.DataTypes;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNode.Format;
import uk.co.strangeskies.modabi.schema.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public class MetaSchemaImpl implements MetaSchema {
	private interface TypeFactory {
		<T> DataType<T> apply(String name, Function<DataTypeConfigurator<Object>, DataTypeConfigurator<T>> type);
	}

	private interface ModelFactory {
		<T> Model<T> apply(String name, Function<ModelConfigurator<Object>, ModelConfigurator<T>> type);
	}

	private final Schema metaSchema;
	private Model<Schema> schemaModel;
	private Model<Model<?>> metaModel;
	private Model<DataType<?>> dataTypeModel;

	public MetaSchemaImpl(SchemaBuilder schema, DataLoader loader, BaseSchema base) {
		QualifiedName name = QUALIFIED_NAME;
		Namespace namespace = name.getNamespace();

		/*
		 * Schema
		 */
		SchemaConfigurator schemaConfigurator = schema.configure(loader).qualifiedName(name)
				.dependencies(Arrays.asList(base));

		/*
		 * Types
		 */
		buildTypes(new TypeFactory() {
			@Override
			public <T> DataType<T> apply(String name,
					Function<DataTypeConfigurator<Object>, DataTypeConfigurator<T>> typeFunction) {
				return typeFunction.apply(schemaConfigurator.addDataType().name(name, namespace)).create();
			}
		}, base, namespace);

		/*
		 * Models
		 */
		buildModels(new ModelFactory() {
			@Override
			public <T> Model<T> apply(String name, Function<ModelConfigurator<Object>, ModelConfigurator<T>> modelFunction) {
				return modelFunction.apply(schemaConfigurator.addModel().name(name, namespace)).create();
			}
		}, base, namespace);

		metaSchema = schemaConfigurator.create();
	}

	private void buildTypes(TypeFactory factory, BaseSchema base, Namespace namespace) {}

	private void buildModels(ModelFactory factory, BaseSchema base, Namespace namespace) {
		/* Node Models */

		Model<SchemaNode<?, ?>> nodeModel = factory.apply("node",
				m -> m.abstractness(ABSTRACT).dataType(new TypeToken<SchemaNode<?, ?>>() {})
						.unbindingStrategy(UnbindingStrategy.SIMPLE)
						.addChild(n -> n.inputSequence().name("configure").abstractness(ABSTRACT)
								.postInputType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}))
						.addChild(n -> n.data().format(PROPERTY).type(base.primitiveType(Primitive.QUALIFIED_NAME)).name("name")
								.inMethod("name").optional(true))
						.addChild(n -> n.data().format(PROPERTY).type(base.derivedTypes().enumType()).dataType(Abstractness.class)
								.name("abstractness").optional(true)));

		Model<ChildNode<?, ?>> childBaseModel = factory.apply("childBase", m -> m.abstractness(ABSTRACT)
				.dataType(new TypeToken<ChildNode<?, ?>>() {}).bindingType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}));

		Model<SchemaNode<?, ?>> branchModel = factory.apply("branch",
				m -> m.abstractness(ABSTRACT).baseModel(nodeModel).addChild(n -> n.data().name("name"))
						.addChild(n -> n.data().name("abstractness"))
						.addChild(n -> n.complex().name("child").outMethod("children").inMethod("null").abstractness(ABSTRACT)
								.extensible(true).model(childBaseModel).occurrences(Range.between(0, null)))
						.addChild(n -> n.inputSequence().name("create").inMethodChained(true)));

		@SuppressWarnings("unchecked")
		Model<ChildNode<?, ?>> childModel = factory.apply("child",
				m -> m.baseModel(branchModel, childBaseModel).abstractness(ABSTRACT)
						.dataType(new TypeToken<ChildNode<?, ?>>() {}).bindingStrategy(TARGET_ADAPTOR)
						.bindingType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}.getType())
						.addChild(c -> c.inputSequence().name("addChild").inMethodChained(true))
						.addChild(c -> c.inputSequence().name("configure").abstractness(ABSTRACT).inMethodChained(true)
								.postInputType(new TypeToken<ChildNodeConfigurator<?, ?>>() {}))
						.addChild(n -> n.data().name("name"))
						.addChild(n -> n.data().format(PROPERTY).name("ordered").type(base.primitiveType(BOOLEAN)).optional(true))
						.addChild(
								n -> n.data().format(PROPERTY).name("occurrences").type(base.derivedTypes().rangeType()).optional(true))
						.addChild(n -> n.data().format(PROPERTY).type(base.primitiveType(STRING)).name("postInputType")
								.optional(true).outMethod("postInputTypeString")));

		Model<BindingNode<?, ?, ?>> bindingNodeModel = factory.apply("binding",
				m -> m.baseModel(branchModel).abstractness(ABSTRACT).dataType(new TypeToken<BindingNode<?, ?, ?>>() {})
						.addChild(c -> c.inputSequence().name("configure").abstractness(ABSTRACT)
								.postInputType(new TypeToken<BindingNodeConfigurator<?, ?, Object>>() {}))
						.addChild(n -> n.data().name("name"))
						.addChild(o -> o.data().format(PROPERTY).name("dataType").optional(true).type(base.primitiveType(STRING))
								.outMethod("dataTypeString"))
						.addChild(o -> o.data().format(PROPERTY).name("bindingStrategy").type(base.derivedTypes().enumType())
								.dataType(BindingStrategy.class).optional(true))
						.addChild(n -> n.data().format(PROPERTY).name("bindingType").optional(true).type(base.primitiveType(STRING))
								.outMethod("bindingTypeString"))
						.addChild(o -> o.data().format(PROPERTY).name("unbindingStrategy").type(base.derivedTypes().enumType())
								.dataType(UnbindingStrategy.class).optional(true))
						.addChild(o -> o.data().format(PROPERTY).name("unbindingMethod").outMethod("unbindingMethodName")
								.type(base.primitiveType(STRING)).optional(true))
						.addChild(n -> n.data().format(PROPERTY).name("unbindingType").optional(true)
								.type(base.primitiveType(STRING)).outMethod("unbindingTypeString"))
						.addChild(n -> n.data().format(PROPERTY).name("providedUnbindingMethodParameters").optional(true)
								.outMethod("providedUnbindingMethodParameterNames").inMethod("providedUnbindingMethodParameters")
								.type(base.derivedTypes().listType())
								.addChild(o -> o.data().name("element").type(base.primitiveType(Primitive.QUALIFIED_NAME))))
						.addChild(n -> n.data().format(PROPERTY).name("unbindingFactoryType").optional(true)
								.type(base.primitiveType(STRING)).outMethod("unbindingFactoryTypeString")));

		Model<InputNode<?, ?>> inputModel = factory.apply("input", m -> m.abstractness(ABSTRACT).baseModel(childModel)
				.dataType(new TypeToken<InputNode<?, ?>>() {})
				.addChild(c -> c.inputSequence().name("configure").abstractness(ABSTRACT)
						.postInputType(new TypeToken<InputNodeConfigurator<?, ?>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.data().format(PROPERTY).name("inMethod").outMethod("inMethodName").optional(true)
						.type(base.primitiveType(STRING)))
				.addChild(
						n -> n.data().format(PROPERTY).name("inMethodChained").optional(true).type(base.primitiveType(BOOLEAN)))
				.addChild(n -> n.data().format(PROPERTY).name("inMethodCast").optional(true).type(base.primitiveType(BOOLEAN)))
				.addChild(
						n -> n.data().format(PROPERTY).name("inMethodUnchecked").optional(true).type(base.primitiveType(BOOLEAN))));

		@SuppressWarnings("unchecked")
		Model<BindingChildNode<?, ?, ?>> bindingChildNodeModel = factory.apply("bindingChild", m -> m.abstractness(ABSTRACT)
				.dataType(new TypeToken<BindingChildNode<?, ?, ?>>() {}).baseModel(inputModel, bindingNodeModel)
				.addChild(c -> c.inputSequence().name("configure").abstractness(ABSTRACT)
						.postInputType(new TypeToken<BindingChildNodeConfigurator<?, ?, Object>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.data().format(PROPERTY).name("extensible").type(base.primitiveType(BOOLEAN)).optional(true))
				.addChild(n -> n.data().format(PROPERTY).name("synchronous").optional(true).type(base.primitiveType(BOOLEAN)))
				.addChild(n -> n.data().format(PROPERTY).name("outMethod").outMethod("outMethodName").optional(true)
						.type(base.primitiveType(STRING)))
				.addChild(
						n -> n.data().format(PROPERTY).name("outMethodIterable").optional(true).type(base.primitiveType(BOOLEAN)))
				.addChild(n -> n.data().format(PROPERTY).name("outMethodCast").optional(true).type(base.primitiveType(BOOLEAN)))
				.addChild(n -> n.data().format(PROPERTY).name("outMethodUnchecked").optional(true)
						.type(base.primitiveType(BOOLEAN))));

		factory.apply("choice",
				m -> m.dataType(ChoiceNode.class).baseModel(childModel)
						.addChild(c -> c.inputSequence().name("configure").inMethod("choice"))
						.addChild(n -> n.data().name("name")));

		factory.apply("sequence", m -> m.dataType(SequenceNode.class).baseModel(childModel)
				.addChild(c -> c.inputSequence().name("configure").inMethod("sequence")));

		@SuppressWarnings({ "unchecked", "unused" })
		Model<InputSequenceNode> inputSequenceModel = factory.apply("inputSequence",
				m -> m.dataType(InputSequenceNode.class)
						.baseModel(inputModel, childModel).addChild(c -> c.inputSequence().name("configure")
								.inMethod("inputSequence").postInputType(InputSequenceNodeConfigurator.class))
						.addChild(n -> n.data().name("name")));

		/*
		 * configure a node to bind a list of base models
		 */
		Function<DataNodeConfigurator<Object>, SchemaNodeConfigurator<?, ?>> baseModelConfiguration = n -> n
				.format(PROPERTY).optional(true).type(base.derivedTypes().listType()).outMethodUnchecked(true)
				.inMethodUnchecked(true)
				.addChild(o -> o.data().name("element").type(base.derivedTypes().referenceType())
						.dataType(new TypeToken<Model<?>>() {}).addChild(
								p -> p.data().name("targetModel").dataType(new TypeToken<Model<Model<?>>>() {})
										.provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace)).buffer()))
						.addChild(p -> p.data().name("targetId").provideValue(new BufferingDataTarget()
								.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer())));

		metaModel = factory.apply("model",
				m -> m.baseModel(bindingNodeModel).dataType(new TypeToken<Model<?>>() {}).bindingType(SchemaConfigurator.class)
						.bindingStrategy(TARGET_ADAPTOR)
						.addChild(c -> c.inputSequence().name("configure").inMethodChained(true).inMethod("addModel"))
						.addChild(n -> n.data().name("name").optional(false))
						.addChild(n -> baseModelConfiguration.apply(n.data().name("baseModel"))));

		Model<ComplexNode<?>> abstractComplexModel = factory.apply("abstractComplex",
				m -> m.abstractness(ABSTRACT).dataType(new TypeToken<ComplexNode<?>>() {}).baseModel(bindingChildNodeModel)
						.addChild(c -> c.inputSequence().name("addChild"))
						.addChild(c -> c.inputSequence().name("configure").inMethod("complex").inMethodChained(true)
								.postInputType(new TypeToken<ComplexNodeConfigurator<Object>>() {}))
						.addChild(n -> n.data().name("name")).addChild(n -> baseModelConfiguration.apply(n.data().name("model")))
						.addChild(c -> c.data().name("inline").abstractness(ABSTRACT).optional(true)
								.valueResolution(REGISTRATION_TIME).type(base.primitiveType(BOOLEAN))));

		factory.apply("complex", m -> m.baseModel(abstractComplexModel).addChild(c -> c.data().name("inline").optional(true)
				.provideValue(new BufferingDataTarget().put(BOOLEAN, false).buffer())));

		factory.apply("inline", m -> m.baseModel(abstractComplexModel).addChild(c -> c.data().name("inline").optional(false)
				.provideValue(new BufferingDataTarget().put(BOOLEAN, true).buffer())));

		Model<DataNode<?>> typedDataModel = factory
				.apply("typedData",
						m -> m.baseModel(bindingChildNodeModel).dataType(new TypeToken<DataNode<?>>() {}).abstractness(ABSTRACT)
								.addChild(
										c -> c.inputSequence().name("addChild"))
								.addChild(
										c -> c.inputSequence().name("configure").inMethod("data")
												.inMethodChained(
														true))
								.addChild(n -> n.data().format(PROPERTY).name("format").optional(true)
										.valueResolution(REGISTRATION_TIME).abstractness(ABSTRACT).type(base.derivedTypes().enumType())
										.dataType(Format.class).postInputType(DataNodeConfigurator.class))
								.addChild(n -> n.data().name("name"))
								.addChild(n -> n.data().format(PROPERTY).name("type").optional(true)
										.type(base.derivedTypes().referenceType()).dataType(new TypeToken<DataType<?>>() {})
										.addChild(p -> p.data().name("targetModel").valueResolution(REGISTRATION_TIME)
												.provideValue(new BufferingDataTarget()
														.put(Primitive.QUALIFIED_NAME, new QualifiedName("type", namespace)).buffer()))
										.addChild(p -> p.data().name("targetId").valueResolution(REGISTRATION_TIME)
												.provideValue(new BufferingDataTarget()
														.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer())))
								.addChild(n -> n.data().format(PROPERTY).name("nullIfOmitted").optional(true)
										.type(base.primitiveType(BOOLEAN)))
								.addChild(n -> n.data().format(PROPERTY).name("valueResolution").optional(true)
										.type(base.derivedTypes().enumType()).dataType(ValueResolution.class))
								.addChild(n -> n.complex().name("valueInline").inMethod("provideValue").outMethod("providedValueBuffer")
										.dataType(DataSource.class).unbindingType(DataSource.class).bindingType(Function.class)
										.bindingStrategy(STATIC_FACTORY).inline(true).optional(true)
										.addChild(o -> o.inputSequence().name("identity"))
										.addChild(o -> o.choice().name("valueFormat")
												.addChild(p -> p.data().format(PROPERTY).name("value").inMethodChained(true)
														.type(base.derivedTypes().bufferedDataType()).outMethod("copy").inMethod("apply"))
												.addChild(p -> p.data().format(CONTENT).name("valueContent").inMethodChained(true)
														.type(base.derivedTypes().bufferedDataType()).outMethod("copy").inMethod("apply")))));

		factory.apply("content", m -> m.baseModel(typedDataModel).addChild(n -> n.data().name("format").optional(false)
				.provideValue(new BufferingDataTarget().put(STRING, CONTENT.toString()).buffer())));

		factory.apply("property", m -> m.baseModel(typedDataModel).addChild(n -> n.data().name("format").optional(false)
				.provideValue(new BufferingDataTarget().put(STRING, PROPERTY.toString()).buffer())));

		factory.apply("simple", m -> m.baseModel(typedDataModel).addChild(n -> n.data().name("format").optional(false)
				.provideValue(new BufferingDataTarget().put(STRING, SIMPLE.toString()).buffer())));

		factory.apply("data",
				m -> m.baseModel(typedDataModel).addChild(n -> n.data().name("format").occurrences(between(0, 0))));

		/* Type Models */

		dataTypeModel = factory
				.apply("type",
						m -> m.baseModel(bindingNodeModel)
								.dataType(new TypeToken<DataType<?>>() {}).bindingType(SchemaConfigurator.class).bindingStrategy(
										TARGET_ADAPTOR)
								.addChild(c -> c.inputSequence().name("configure")
										.inMethodChained(
												true)
										.inMethod(
												"addDataType"))
								.addChild(
										n -> n.data().format(PROPERTY).name("private").inMethod("isPrivate")
												.optional(
														true)
												.type(base.primitiveType(BOOLEAN)))
								.addChild(n -> n.data().format(PROPERTY).name("baseType").optional(true)
										.type(base.derivedTypes().referenceType()).dataType(new TypeToken<DataType<?>>() {})
										.addChild(p -> p.data().name("targetModel")
												.provideValue(new BufferingDataTarget()
														.put(Primitive.QUALIFIED_NAME, new QualifiedName("type", namespace)).buffer()))
										.addChild(p -> p.data().name("targetId").provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer()))));

		/* Schema Models */

		schemaModel = factory
				.apply("schema",
						m -> m
								.dataType(
										Schema.class)
								.bindingType(SchemaConfigurator.class)
								.addChild(
										n -> n.data().format(PROPERTY).name("name").inMethod("qualifiedName").outMethod("qualifiedName")
												.type(base.primitiveType(Primitive.QUALIFIED_NAME)))
								.addChild(
										n -> n.data()
												.format(
														SIMPLE)
												.name("dependencies").occurrences(Range.between(0, 1)).type(base.derivedTypes().setType())
												.addChild(o -> o.data().inMethod("add").name("element").type(base.derivedTypes().importType())
														.dataType(Schema.class).outMethod("this").occurrences(Range.between(0, null))
														.addChild(i -> i.data().name("import")
																.addChild(p -> p.data().name("targetModel")
																		.provideValue(new BufferingDataTarget()
																				.put(Primitive.QUALIFIED_NAME, new QualifiedName("schema", namespace))
																				.buffer()))
																.addChild(p -> p.data().name("targetId")
																		.provideValue(new BufferingDataTarget()
																				.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer())))
														.addChild(p -> p.data().name("dataTypes").inMethod("null")
																.type(base.derivedTypes().includeType()).bindingType(Schema.class)
																.addChild(q -> q.inputSequence().name("dataTypes").inMethodChained(true))
																.addChild(q -> q.data().name("targetModel")
																		.provideValue(new BufferingDataTarget()
																				.put(Primitive.QUALIFIED_NAME, new QualifiedName("type", namespace)).buffer())))
														.addChild(p -> p.data().name("models").inMethod("null")
																.type(base.derivedTypes().includeType()).bindingType(Schema.class)
																.addChild(q -> q.inputSequence().name("models").inMethodChained(true))
																.addChild(q -> q.data().name("targetModel").provideValue(new BufferingDataTarget()
																		.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace)).buffer())))))
								.addChild(
										i -> i.data().format(SIMPLE).name("imports").optional(true).bindingStrategy(TARGET_ADAPTOR)
												.outMethod("this").dataType(Schema.class).bindingType(SchemaConfigurator.class).inMethod("null")
												.addChild(n -> n.data().name("importsIn").inMethod("imports").outMethod("null")
														.type(base.derivedTypes().setType()).addChild(
																e -> e.data().name("element").type(base.derivedTypes().classType())))
												.addChild(
														n -> n.data().name("importsOut").inMethod("null").optional(true)
																.outMethod(
																		"imports")
																.dataType(
																		Imports.class)
																.addChild(s -> s.data().name("imports").outMethod("getImportedClasses").inMethod("null")
																		.type(
																				base.derivedTypes().setType())
																		.addChild(e -> e.data()
																				.name(
																						"element")
																				.type(base.derivedTypes().classType())))))
								.addChild(n -> n.complex().name("types").outMethod("this").occurrences(Range.between(0, 1))
										.dataType(Schema.class).bindingType(SchemaConfigurator.class).inMethod("null")
										.bindingStrategy(TARGET_ADAPTOR)
										.addChild(o -> o.complex().model(dataTypeModel).inMethod("null").bindingStrategy(TARGET_ADAPTOR)
												.outMethod("dataTypes").name("type").dataType(new TypeToken<DataType<?>>() {})
												.occurrences(Range.between(0, null))))
								.addChild(n -> n.complex().name("models").outMethod("this").occurrences(Range.between(0, 1))
										.dataType(Schema.class).bindingType(SchemaConfigurator.class).inMethod("null")
										.bindingStrategy(TARGET_ADAPTOR)
										.addChild(o -> o.complex().model(metaModel).inMethod("null").bindingStrategy(TARGET_ADAPTOR)
												.outMethod("models").occurrences(Range.between(0, null))))
								.addChild(n -> n.inputSequence().name("create").inMethodChained(true)));
	}

	@Override
	public QualifiedName qualifiedName() {
		return metaSchema.qualifiedName();
	}

	@Override
	public Schemata dependencies() {
		return metaSchema.dependencies();
	}

	@Override
	public DataTypes dataTypes() {
		return metaSchema.dataTypes();
	}

	@Override
	public Models models() {
		return metaSchema.models();
	}

	@Override
	public Model<Schema> getSchemaModel() {
		return schemaModel;
	}

	@Override
	public Model<Model<?>> getMetaModel() {
		return metaModel;
	}

	@Override
	public Model<DataType<?>> getDataTypeModel() {
		return dataTypeModel;
	}

	@Override
	public boolean equals(Object obj) {
		return metaSchema.equals(obj);
	}

	@Override
	public int hashCode() {
		return metaSchema.hashCode();
	}

	@Override
	public Imports imports() {
		return Imports.empty().withImport(Schema.class);
	}

	@Override
	public String toString() {
		return qualifiedName().toString();
	}
}
