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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.Range;
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
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.AbstractComplexNodeConfigurator;
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
import uk.co.strangeskies.modabi.schema.building.DataTypeBuilder;
import uk.co.strangeskies.modabi.schema.building.ModelBuilder;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;

public class MetaSchemaImpl implements MetaSchema {
	private interface TypeFactory {
		<T> DataType<T> apply(String name,
				Function<DataTypeConfigurator<Object>, DataTypeConfigurator<T>> type);
	}

	private interface ModelFactory {
		<T> Model<T> apply(String name,
				Function<ModelConfigurator<Object>, ModelConfigurator<T>> type);
	}

	private final Schema metaSchema;
	private final Model<Schema> schemaModel;

	public MetaSchemaImpl(SchemaBuilder schema, ModelBuilder modelBuilder,
			DataTypeBuilder dataTypeBuilder, DataLoader loader, BaseSchema base) {
		QualifiedName name = MetaSchema.QUALIFIED_NAME;
		Namespace namespace = name.getNamespace();

		/*
		 * Types
		 */
		Set<DataType<?>> typeSet = new LinkedHashSet<>();

		buildTypes(new TypeFactory() {
			@Override
			public <T> DataType<T> apply(String name,
					Function<DataTypeConfigurator<Object>, DataTypeConfigurator<T>> typeFunction) {
				DataType<T> type = typeFunction
						.apply(dataTypeBuilder.configure(loader).name(name, namespace))
						.create();
				typeSet.add(type);
				return type;
			}
		}, base, namespace);

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		schemaModel = buildModels(new ModelFactory() {
			@Override
			public <T> Model<T> apply(String name,
					Function<ModelConfigurator<Object>, ModelConfigurator<T>> modelFunction) {
				Model<T> model = modelFunction
						.apply(modelBuilder.configure(loader).name(name, namespace))
						.create();
				modelSet.add(model);
				return model;
			}
		}, base, namespace);

		/*
		 * Schema
		 */
		metaSchema = schema.configure().qualifiedName(name)
				.dependencies(Arrays.asList(base)).types(typeSet).models(modelSet)
				.create();
	}

	private void buildTypes(TypeFactory factory, BaseSchema base,
			Namespace namespace) {}

	private Model<Schema> buildModels(ModelFactory factory, BaseSchema base,
			Namespace namespace) {
		/* Node Models */

		Model<SchemaNode<?, ?>> nodeModel = factory.apply("node",
				m -> m.isAbstract(true).dataType(
						new TypeToken<SchemaNode<?, ?>>() {})
				.unbindingStrategy(UnbindingStrategy.SIMPLE)
				.addChild(n -> n.inputSequence().name("configure").isAbstract(true)
						.postInputType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}))
				.addChild(n -> n.data().format(Format.PROPERTY)
						.type(base.primitiveType(Primitive.QUALIFIED_NAME)).name("name")
						.inMethod("name").optional(true))
				.addChild(n -> n.data().format(Format.PROPERTY)
						.type(base.primitiveType(Primitive.BOOLEAN)).name("abstract")
						.inMethod("isAbstract").optional(true)));

		Model<ChildNode<?, ?>> childBaseModel = factory
				.apply("childBase",
						m -> m.isAbstract(true)
								.dataType(new TypeToken<ChildNode<?, ?>>() {})
								.bindingType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}));

		Model<SchemaNode<?, ?>> branchModel = factory.apply("branch",
				m -> m.isAbstract(true).baseModel(nodeModel)
						.addChild(n -> n.data().name("name"))
						.addChild(n -> n.data().name("abstract"))
						.addChild(n -> n.complex().name("child").outMethod("children")
								.inMethod("null").isAbstract(true).extensible(true)
								.baseModel(childBaseModel).occurrences(Range.between(0, null)))
				.addChild(n -> n.inputSequence().name("create").inMethodChained(true)));

		@SuppressWarnings("unchecked")
		Model<ChildNode<?, ?>> childModel = factory.apply("child",
				m -> m.baseModel(branchModel, childBaseModel).isAbstract(true)
						.dataType(new TypeToken<ChildNode<?, ?>>() {})
						.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
						.bindingType(
								new TypeToken<SchemaNodeConfigurator<?, ?>>() {}.getType())
				.addChild(c -> c.inputSequence().name("addChild").inMethodChained(true))
				.addChild(c -> c.inputSequence().name("configure").isAbstract(true)
						.inMethodChained(true)
						.postInputType(new TypeToken<ChildNodeConfigurator<?, ?>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.data().format(Format.PROPERTY).name("ordered")
						.type(base.primitiveType(Primitive.BOOLEAN)).optional(true))
				.addChild(n -> n.data().format(Format.PROPERTY).name("occurrences")
						.type(base.derivedTypes().rangeType()).optional(true))
				.addChild(n -> n.data().format(Format.PROPERTY)
						.type(base.derivedTypes().typeTokenType()).name("postInputType")
						.optional(true)));

		Model<BindingNode<?, ?, ?>> bindingNodeModel = factory.apply("binding",
				m -> m.baseModel(branchModel).isAbstract(true)
						.dataType(new TypeToken<BindingNode<?, ?, ?>>() {})
						.addChild(c -> c.inputSequence().name("configure").isAbstract(true)
								.postInputType(
										new TypeToken<BindingNodeConfigurator<?, ?, Object>>() {}))
						.addChild(n -> n.data().name("name"))
						.addChild(o -> o.data().format(Format.PROPERTY).name("dataType")
								.optional(true).type(base.derivedTypes().typeTokenType()))
						.addChild(o -> o.data().format(Format.PROPERTY)
								.name("bindingStrategy").type(base.derivedTypes().enumType())
								.dataType(BindingStrategy.class).optional(true))
						.addChild(n -> n.data().format(Format.PROPERTY).name("bindingType")
								.optional(true).type(base.derivedTypes().typeTokenType()))
						.addChild(o -> o.data().format(Format.PROPERTY)
								.name("unbindingStrategy").type(base.derivedTypes().enumType())
								.dataType(UnbindingStrategy.class).optional(true))
						.addChild(o -> o.data().format(Format.PROPERTY)
								.name("unbindingMethod").outMethod("getUnbindingMethodName")
								.type(base.primitiveType(Primitive.STRING)).optional(true))
						.addChild(
								n -> n.data().format(Format.PROPERTY).name("unbindingType")
										.optional(true).type(base.derivedTypes().typeTokenType()))
						.addChild(n -> n.data().format(Format.PROPERTY)
								.name("providedUnbindingMethodParameters").optional(true)
								.outMethod("getProvidedUnbindingMethodParameterNames")
								.inMethod("providedUnbindingMethodParameters")
								.type(base.derivedTypes().listType())
								.addChild(o -> o.data().name("element")
										.type(base.primitiveType(Primitive.QUALIFIED_NAME))))
						.addChild(n -> n.data().format(Format.PROPERTY)
								.name("unbindingFactoryType").optional(true)
								.type(base.derivedTypes().typeTokenType())));

		Model<InputNode<?, ?>> inputModel = factory
				.apply("input",
						m -> m.isAbstract(true).baseModel(
								childModel)
				.dataType(new TypeToken<InputNode<?, ?>>() {})
				.addChild(c -> c.inputSequence().name("configure").isAbstract(true)
						.postInputType(new TypeToken<InputNodeConfigurator<?, ?>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.data().format(Format.PROPERTY).name("inMethod")
						.outMethod("getInMethodName").optional(true)
						.type(base.primitiveType(Primitive.STRING)))
				.addChild(n -> n.data().format(Format.PROPERTY).name("inMethodChained")
						.optional(true).type(base.primitiveType(Primitive.BOOLEAN)))
				.addChild(n -> n.data().format(Format.PROPERTY).name("inMethodCast")
						.optional(true).type(base.primitiveType(Primitive.BOOLEAN)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethodUnchecked")
								.optional(true).type(base.primitiveType(Primitive.BOOLEAN))));

		@SuppressWarnings("unchecked")
		Model<BindingChildNode<?, ?, ?>> bindingChildNodeModel = factory.apply(
				"bindingChild",
				m -> m.isAbstract(true)
						.dataType(new TypeToken<BindingChildNode<?, ?, ?>>() {})
						.baseModel(inputModel, bindingNodeModel)
						.addChild(c -> c.inputSequence().name("configure").isAbstract(true)
								.postInputType(
										new TypeToken<BindingChildNodeConfigurator<?, ?, Object>>() {}))
						.addChild(n -> n.data().name("name"))
						.addChild(n -> n.data().format(Format.PROPERTY).name("extensible")
								.type(base.primitiveType(Primitive.BOOLEAN)).optional(true))
						.addChild(n -> n.data().format(Format.PROPERTY).name("outMethod")
								.outMethod("getOutMethodName").optional(true)
								.type(base.primitiveType(Primitive.STRING)))
						.addChild(
								n -> n.data().format(Format.PROPERTY).name("outMethodIterable")
										.optional(true).type(base.primitiveType(Primitive.BOOLEAN)))
						.addChild(
								n -> n.data().format(Format.PROPERTY).name("outMethodCast")
										.optional(true).type(base.primitiveType(Primitive.BOOLEAN)))
						.addChild(n -> n.data().format(Format.PROPERTY)
								.name("outMethodUnchecked").optional(true)
								.type(base.primitiveType(Primitive.BOOLEAN))));

		factory.apply("choice",
				m -> m.dataType(ChoiceNode.class).baseModel(childModel)
						.addChild(
								c -> c.inputSequence().name("configure").inMethod("choice"))
				.addChild(n -> n.data().name("name")));

		factory.apply("sequence",
				m -> m.dataType(SequenceNode.class).baseModel(childModel).addChild(
						c -> c.inputSequence().name("configure").inMethod("sequence")));

		@SuppressWarnings({ "unchecked", "unused" })
		Model<InputSequenceNode> inputSequenceModel = factory.apply("inputSequence",
				m -> m.dataType(InputSequenceNode.class)
						.baseModel(inputModel, childModel)
						.addChild(c -> c.inputSequence().name("configure")
								.inMethod("inputSequence")
								.postInputType(InputSequenceNodeConfigurator.class))
						.addChild(n -> n.data().name("name")));

		Model<AbstractComplexNode<?, ?, ?>> abstractModelModel = factory.apply(
				"abstractModel",
				m -> m.baseModel(bindingNodeModel).isAbstract(true)
						.dataType(new TypeToken<AbstractComplexNode<?, ?, ?>>() {})
						.addChild(c -> c.inputSequence().name("configure").isAbstract(true)
								.postInputType(
										new TypeToken<AbstractComplexNodeConfigurator<?, ?, Object>>() {}))
						.addChild(
								n -> n.data()
										.name("name"))
						.addChild(
								n -> n.data().format(Format.PROPERTY).name("baseModel")
										.optional(true).type(base.derivedTypes().listType())
										.outMethodUnchecked(
												true)
										.inMethodUnchecked(true)
										.addChild(o -> o.data().name("element")
												.type(base.derivedTypes().referenceType())
												.dataType(new TypeToken<Model<?>>() {})
												.addChild(p -> p.data().name("targetModel")
														.dataType(new TypeToken<Model<Model<?>>>() {})
														.provideValue(new BufferingDataTarget()
																.put(Primitive.QUALIFIED_NAME,
																		new QualifiedName("model", namespace))
																.buffer()))
												.addChild(p -> p.data().name("targetId")
														.provideValue(new BufferingDataTarget()
																.put(Primitive.QUALIFIED_NAME,
																		new QualifiedName("name", namespace))
																.buffer())))));

		Model<Model<?>> modelModel = factory.apply("model", m -> m
				.baseModel(abstractModelModel).dataType(new TypeToken<Model<?>>() {})
				.bindingType(ModelBuilder.class)
				.addChild(c -> c.inputSequence().name("configure").inMethodChained(true)
						.addChild(d -> d.data().dataType(DataLoader.class)
								.bindingStrategy(BindingStrategy.PROVIDED).name("configure")
								.outMethod("null")))
				.addChild(n -> n.data().name("name").optional(false)));

		@SuppressWarnings("unchecked")
		Model<ComplexNode<?>> abstractComplexModel = factory.apply(
				"abstractComplex",
				m -> m.isAbstract(true).dataType(new TypeToken<ComplexNode<?>>() {})
						.baseModel(abstractModelModel, bindingChildNodeModel)
						.addChild(c -> c.inputSequence().name("addChild"))
						.addChild(c -> c.inputSequence().name("configure")
								.inMethod("complex").inMethodChained(true).postInputType(
										new TypeToken<ComplexNodeConfigurator<Object>>() {}))
						.addChild(n -> n.data().name("name")).addChild(
								c -> c.data().name("inline").isAbstract(true).optional(true)
										.valueResolution(ValueResolution.REGISTRATION_TIME)
										.type(base.primitiveType(Primitive.BOOLEAN))));

		factory.apply("complex",
				m -> m.baseModel(
						abstractComplexModel)
				.addChild(c -> c.data().name("inline").optional(true).provideValue(
						new BufferingDataTarget().put(Primitive.BOOLEAN, false).buffer())));

		factory.apply("inline",
				m -> m.baseModel(
						abstractComplexModel)
				.addChild(c -> c.data().name("inline").optional(false).provideValue(
						new BufferingDataTarget().put(Primitive.BOOLEAN, true).buffer())));

		Model<DataNode<?>> typedDataModel = factory
				.apply("typedData",
						m -> m.baseModel(bindingChildNodeModel)
								.dataType(new TypeToken<DataNode<?>>() {})
								.isAbstract(
										true)
								.addChild(c -> c.inputSequence().name("addChild"))
								.addChild(
										c -> c.inputSequence().name("configure").inMethod("data")
												.inMethodChained(true))
								.addChild(n -> n.data().format(Format.PROPERTY).name("format")
										.optional(true)
										.valueResolution(ValueResolution.REGISTRATION_TIME)
										.isAbstract(true).type(base.derivedTypes().enumType())
										.dataType(Format.class)
										.postInputType(DataNodeConfigurator.class))
				.addChild(
						n -> n.data()
								.name("name")).addChild(
										n -> n.data().format(Format.PROPERTY).name("type")
												.optional(true)
												.type(base.derivedTypes().referenceType())
												.dataType(new TypeToken<DataType<?>>() {})
												.addChild(p -> p.data().name("targetModel")
														.valueResolution(
																ValueResolution.REGISTRATION_TIME)
														.provideValue(
																new BufferingDataTarget()
																		.put(Primitive.QUALIFIED_NAME,
																				new QualifiedName("type", namespace))
																		.buffer()))
												.addChild(p -> p.data().name("targetId")
														.valueResolution(ValueResolution.REGISTRATION_TIME)
														.provideValue(new BufferingDataTarget()
																.put(Primitive.QUALIFIED_NAME,
																		new QualifiedName("name", namespace))
																.buffer())))
				.addChild(n -> n.data().format(Format.PROPERTY).name("nullIfOmitted")
						.optional(true).type(base.primitiveType(Primitive.BOOLEAN)))
				.addChild(n -> n.data().format(Format.PROPERTY).name("valueResolution")
						.optional(true).type(base.derivedTypes().enumType())
						.dataType(ValueResolution.class))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("value").optional(true)
								.inMethod("provideValue").outMethod("providedValueBuffer")
								.type(base.derivedTypes().bufferedDataType())));

		factory.apply("content",
				m -> m.baseModel(typedDataModel)
						.addChild(n -> n.data().name("format").optional(false)
								.provideValue(new BufferingDataTarget()
										.put(Primitive.STRING, "CONTENT").buffer())));

		factory.apply("property",
				m -> m.baseModel(typedDataModel)
						.addChild(n -> n.data().name("format").optional(false)
								.provideValue(new BufferingDataTarget()
										.put(Primitive.STRING, "PROPERTY").buffer())));

		factory.apply("simple",
				m -> m.baseModel(typedDataModel)
						.addChild(n -> n.data().name("format").optional(false)
								.provideValue(new BufferingDataTarget()
										.put(Primitive.STRING, "SIMPLE").buffer())));

		factory.apply("data", m -> m.baseModel(typedDataModel).addChild(
				n -> n.data().name("format").occurrences(Range.between(0, 0))));

		/* Type Models */

		Model<DataType<?>> typeModel = factory.apply("type",
				m -> m.baseModel(bindingNodeModel)
						.dataType(
								new TypeToken<DataType<?>>() {})
				.bindingType(DataTypeBuilder.class)
				.addChild(c -> c.inputSequence().name("configure").inMethodChained(true)
						.addChild(d -> d.data().dataType(DataLoader.class)
								.bindingStrategy(BindingStrategy.PROVIDED).name("configure")
								.outMethod("null")))
						.addChild(
								n -> n.data().format(Format.PROPERTY).name("private")
										.inMethod("isPrivate").optional(true).type(
												base.primitiveType(Primitive.BOOLEAN)))
						.addChild(
								n -> n.data().format(Format.PROPERTY).name("baseType")
										.optional(true)
										.type(
												base.derivedTypes().referenceType())
										.dataType(new TypeToken<DataType<?>>() {})
										.addChild(
												p -> p.data()
														.name(
																"targetModel")
														.provideValue(new BufferingDataTarget()
																.put(Primitive.QUALIFIED_NAME,
																		new QualifiedName("type", namespace))
																.buffer()))
										.addChild(p -> p.data().name("targetId")
												.provideValue(new BufferingDataTarget()
														.put(Primitive.QUALIFIED_NAME,
																new QualifiedName("name", namespace))
														.buffer()))));

		/* Schema Models */

		return factory
				.apply("schema",
						m -> m.dataType(Schema.class).bindingType(SchemaBuilder.class)
								.addChild(c -> c.inputSequence().name("configure")
										.inMethodChained(true))
								.addChild(
										n -> n.data().format(Format.PROPERTY).name("name")
												.inMethod("qualifiedName")
												.outMethod("getQualifiedName").type(base
														.primitiveType(Primitive.QUALIFIED_NAME)))
								.addChild(
										i -> i.data().format(Format.SIMPLE).name("imports")
												.optional(true)
												.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
												.outMethod("this").dataType(Schema.class)
												.bindingType(SchemaConfigurator.class).inMethod("null")
												.addChild(n -> n.data().name("importsIn")
														.inMethod("imports").outMethod("null")
														.type(base.derivedTypes().setType())
														.addChild(e -> e.data().name("element")
																.type(base.derivedTypes().classType())
																.occurrences(Range.between(1, 1))))
												.addChild(
														n -> n.data().name("importsOut").inMethod("null")
																.optional(true).outMethod("getImports")
																.dataType(Imports.class)
																.addChild(s -> s.data().name("imports")
																		.outMethod("getImportedClasses").inMethod(
																				"null")
																.type(base.derivedTypes().setType())
																.addChild(e -> e.data().name("element")
																		.type(base.derivedTypes().classType())))))
				.addChild(n -> n.data().format(Format.SIMPLE).name("dependencies")
						.occurrences(Range.between(0, 1))
						.type(base.derivedTypes().setType())
						.addChild(o -> o.data().inMethod("add").name("element")
								.type(base.derivedTypes().importType()).dataType(Schema.class)
								.outMethod("this").occurrences(Range.between(0, null))
								.addChild(i -> i.data().name("import")
										.addChild(p -> p.data().name("targetModel")
												.provideValue(new BufferingDataTarget()
														.put(Primitive.QUALIFIED_NAME,
																new QualifiedName("schema", namespace))
														.buffer()))
										.addChild(p -> p.data().name("targetId")
												.provideValue(new BufferingDataTarget()
														.put(Primitive.QUALIFIED_NAME,
																new QualifiedName("name", namespace))
														.buffer())))
								.addChild(
										p -> p.data().name("dataTypes").inMethod("null")
												.type(base.derivedTypes().includeType())
												.bindingType(Schema.class)
												.addChild(
														q -> q.inputSequence().name("getDataTypes")
																.inMethodChained(true))
												.addChild(q -> q.data().name("targetModel")
														.provideValue(new BufferingDataTarget()
																.put(Primitive.QUALIFIED_NAME,
																		new QualifiedName("type", namespace))
																.buffer())))
								.addChild(
										p -> p.data().name("models").inMethod("null")
												.type(base.derivedTypes().includeType())
												.bindingType(Schema.class)
												.addChild(q -> q.inputSequence().name("getModels")
														.inMethodChained(true))
												.addChild(q -> q.data().name("targetModel")
														.provideValue(new BufferingDataTarget()
																.put(Primitive.QUALIFIED_NAME,
																		new QualifiedName("model", namespace))
																.buffer())))))
				.addChild(
						n -> n.complex().name("types").outMethod("getDataTypes")
								.occurrences(Range.between(0, 1))
								.dataType(
										new TypeToken<@Infer Set<?>>() {})
						.bindingType(new TypeToken<@Infer LinkedHashSet<?>>() {})
						.addChild(o -> o.complex().baseModel(typeModel).outMethod("this")
								.name("type").dataType(new TypeToken<DataType<?>>() {})
								.occurrences(Range.between(0, null)))).addChild(
										n -> n.complex().name("models")
												.occurrences(Range.between(0, 1)).dataType(
														new TypeToken<@Infer Set<?>>() {})
						.bindingType(new TypeToken<@Infer LinkedHashSet<?>>() {})
						.addChild(o -> o.complex().baseModel(modelModel).inMethod("add")
								.outMethod("this").occurrences(Range.between(0, null))))
				.addChild(n -> n.inputSequence().name("create").inMethodChained(true)));
	}

	@Override
	public QualifiedName getQualifiedName() {
		return metaSchema.getQualifiedName();
	}

	@Override
	public Schemata getDependencies() {
		return metaSchema.getDependencies();
	}

	@Override
	public DataTypes getDataTypes() {
		return metaSchema.getDataTypes();
	}

	@Override
	public Models getModels() {
		return metaSchema.getModels();
	}

	@Override
	public Model<Schema> getSchemaModel() {
		return schemaModel;
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
	public Imports getImports() {
		return Imports.empty().withImport(Schema.class);
	}
}