/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.DataBindingTypes;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataType;
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
import uk.co.strangeskies.modabi.schema.DataBindingType;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNode.Format;
import uk.co.strangeskies.modabi.schema.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.modabi.schema.building.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.modabi.schema.building.ModelBuilder;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;

public class MetaSchemaImpl implements MetaSchema {
	private final Schema metaSchema;
	private final Model<Schema> schemaModel;

	@SuppressWarnings("unchecked")
	public MetaSchemaImpl(SchemaBuilder schema, ModelBuilder model,
			DataBindingTypeBuilder dataType, DataLoader loader, BaseSchema base) {
		Namespace namespace = MetaSchema.NAMESPACE;
		QualifiedName name = MetaSchema.QUALIFIED_NAME;

		/*
		 * Types
		 */
		Set<DataBindingType<?>> typeSet = new LinkedHashSet<>();

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		/* Node Models */

		Model<SchemaNode<?, ?>> nodeModel = model
				.configure(loader)
				.name("node", namespace)
				.isAbstract(true)
				.dataType(new TypeToken<SchemaNode<?, ?>>() {})
				.unbindingStrategy(UnbindingStrategy.SIMPLE)
				.addChild(
						n -> n
								.inputSequence()
								.name("configure")
								.isAbstract(true)
								.postInputType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}))
				.addChild(
						n -> n.data().format(Format.PROPERTY)
								.type(base.primitiveType(DataType.QUALIFIED_NAME)).name("name")
								.inMethod("name").optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY)
								.type(base.primitiveType(DataType.BOOLEAN)).name("abstract")
								.inMethod("isAbstract").optional(true)).create();
		modelSet.add(nodeModel);

		Model<ChildNode<?, ?>> childBaseModel = model
				.configure(loader)
				.name("childBase", namespace)
				.isAbstract(true)
				.dataType(new TypeToken<ChildNode<?, ?>>() {})
				.bindingType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}.getType())
				.create();
		modelSet.add(childBaseModel);

		Model<SchemaNode<?, ?>> branchModel = model
				.configure(loader)
				.name("branch", namespace)
				.isAbstract(true)
				.baseModel(nodeModel)
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.data().name("abstract"))
				.addChild(
						n -> n.complex().name("child").outMethod("children")
								.inMethod("null").isAbstract(true).extensible(true)
								.baseModel(childBaseModel).outMethodIterable(true)
								.occurrences(Range.create(0, null)))
				.addChild(n -> n.inputSequence().name("create").inMethodChained(true))
				.create();
		modelSet.add(branchModel);

		Model<ChildNode<?, ?>> childModel = model
				.configure(loader)
				.name("child", namespace)
				.baseModel(branchModel, childBaseModel)
				.isAbstract(true)
				.dataType(new TypeToken<ChildNode<?, ?>>() {})
				.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
				.bindingType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}.getType())
				.addChild(c -> c.inputSequence().name("addChild").inMethodChained(true))
				.addChild(
						c -> c.inputSequence().name("configure").isAbstract(true)
								.inMethodChained(true)
								.postInputType(new TypeToken<ChildNodeConfigurator<?, ?>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY)
								.type(base.derivedTypes().typeTokenType())
								.name("postInputType").optional(true)).create();
		modelSet.add(childModel);

		Model<BindingNode<?, ?, ?>> bindingNodeModel = model
				.configure(loader)
				.name("binding", namespace)
				.baseModel(branchModel)
				.isAbstract(true)
				.dataType(new TypeToken<BindingNode<?, ?, ?>>() {})
				.addChild(
						c -> c
								.inputSequence()
								.name("configure")
								.isAbstract(true)
								.postInputType(
										new TypeToken<BindingNodeConfigurator<?, ?, Object>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("dataType")
								.optional(true).type(base.derivedTypes().typeTokenType()))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("bindingStrategy")
								.type(base.derivedTypes().enumType())
								.dataType(BindingStrategy.class).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("bindingType")
								.optional(true).type(base.derivedTypes().typeTokenType()))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("unbindingStrategy")
								.type(base.derivedTypes().enumType())
								.dataType(UnbindingStrategy.class).optional(true))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("unbindingMethod")
								.outMethod("getUnbindingMethodName")
								.type(base.primitiveType(DataType.STRING)).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("unbindingType")
								.optional(true).type(base.derivedTypes().typeTokenType()))
				.addChild(
						n -> n
								.data()
								.format(Format.PROPERTY)
								.name("providedUnbindingMethodParameters")
								.optional(true)
								.outMethod("getProvidedUnbindingMethodParameterNames")
								.inMethod("providedUnbindingMethodParameters")
								.type(base.derivedTypes().listType())
								.addChild(
										o -> o.data().name("element")
												.type(base.primitiveType(DataType.QUALIFIED_NAME))))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("unbindingFactoryType")
								.optional(true).type(base.derivedTypes().typeTokenType()))
				.create();
		modelSet.add(bindingNodeModel);

		Model<InputNode<?, ?>> inputModel = model
				.configure(loader)
				.name("input", namespace)
				.isAbstract(true)
				.baseModel(childModel)
				.dataType(new TypeToken<InputNode<?, ?>>() {})
				.addChild(
						c -> c.inputSequence().name("configure").isAbstract(true)
								.postInputType(new TypeToken<InputNodeConfigurator<?, ?>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethod")
								.outMethod("getInMethodName").optional(true)
								.type(base.primitiveType(DataType.STRING)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethodChained")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethodCast")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethodUnchecked")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.create();
		modelSet.add(inputModel);

		Model<BindingChildNode<?, ?, ?>> bindingChildNodeModel = model
				.configure(loader)
				.name("bindingChild", namespace)
				.isAbstract(true)
				.dataType(new TypeToken<BindingChildNode<?, ?, ?>>() {})
				.baseModel(inputModel, bindingNodeModel)
				.addChild(
						c -> c
								.inputSequence()
								.name("configure")
								.isAbstract(true)
								.postInputType(
										new TypeToken<BindingChildNodeConfigurator<?, ?, Object>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("extensible")
								.type(base.primitiveType(DataType.BOOLEAN)).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("ordered")
								.type(base.primitiveType(DataType.BOOLEAN)).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("occurrences")
								.type(base.derivedTypes().rangeType()).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("outMethod")
								.outMethod("getOutMethodName").optional(true)
								.type(base.primitiveType(DataType.STRING)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("outMethodIterable")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("outMethodCast")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("outMethodUnchecked")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.create();
		modelSet.add(bindingChildNodeModel);

		Model<ChoiceNode> choiceModel = model
				.configure(loader)
				.name("choice", namespace)
				.dataType(ChoiceNode.class)
				.baseModel(childModel)
				.addChild(c -> c.inputSequence().name("configure").inMethod("choice"))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("mandatory")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.create();
		modelSet.add(choiceModel);

		Model<SequenceNode> sequenceModel = model
				.configure(loader)
				.name("sequence", namespace)
				.dataType(SequenceNode.class)
				.baseModel(childModel)
				.addChild(c -> c.inputSequence().name("configure").inMethod("sequence"))
				.create();
		modelSet.add(sequenceModel);

		Model<InputSequenceNode> inputSequenceModel = model
				.configure(loader)
				.name("inputSequence", namespace)
				.dataType(InputSequenceNode.class)
				.baseModel(inputModel, childModel)
				.addChild(
						c -> c.inputSequence().name("configure").inMethod("inputSequence")
								.postInputType(InputSequenceNodeConfigurator.class))
				.addChild(n -> n.data().name("name")).create();
		modelSet.add(inputSequenceModel);

		Model<AbstractComplexNode<?, ?, ?>> abstractModelModel = model
				.configure(loader)
				.name("abstractModel", namespace)
				.baseModel(bindingNodeModel)
				.isAbstract(true)
				.dataType(new TypeToken<AbstractComplexNode<?, ?, ?>>() {})
				.addChild(
						c -> c
								.inputSequence()
								.name("configure")
								.isAbstract(true)
								.postInputType(
										new TypeToken<AbstractComplexNodeConfigurator<?, ?, Object>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n
								.data()
								.format(Format.PROPERTY)
								.name("baseModel")
								.optional(true)
								.type(base.derivedTypes().listType())
								.outMethodUnchecked(true)
								.inMethodUnchecked(true)
								.addChild(
										o -> o
												.data()
												.name("element")
												.type(base.derivedTypes().referenceType())
												.dataType(new TypeToken<Model<?>>() {})
												.addChild(
														p -> p
																.data()
																.name("targetModel")
																.dataType(new TypeToken<Model<Model<?>>>() {})
																.provideValue(
																		new BufferingDataTarget().put(
																				DataType.QUALIFIED_NAME,
																				new QualifiedName("model", namespace))
																				.buffer()))
												.addChild(
														p -> p
																.data()
																.name("targetId")
																.provideValue(
																		new BufferingDataTarget().put(
																				DataType.QUALIFIED_NAME,
																				new QualifiedName("name", namespace))
																				.buffer())))).create();
		modelSet.add(abstractModelModel);

		Model<Model<?>> modelModel = model
				.configure(loader)
				.name("model", namespace)
				.baseModel(abstractModelModel)
				.dataType(new TypeToken<Model<?>>() {})
				.bindingType(ModelBuilder.class)
				.addChild(
						c -> c
								.inputSequence()
								.name("configure")
								.inMethodChained(true)
								.addChild(
										d -> d.data().dataType(DataLoader.class)
												.bindingStrategy(BindingStrategy.PROVIDED)
												.name("configure").outMethod("null")))
				.addChild(n -> n.data().name("name").optional(false)).create();
		modelSet.add(modelModel);

		Model<ComplexNode<?>> abstractComplexModel = model
				.configure(loader)
				.name("abstractComplex", namespace)
				.isAbstract(true)
				.dataType(new TypeToken<ComplexNode<?>>() {})
				.baseModel(abstractModelModel, bindingChildNodeModel)
				.addChild(c -> c.inputSequence().name("addChild"))
				.addChild(
						c -> c
								.inputSequence()
								.name("configure")
								.inMethod("complex")
								.inMethodChained(true)
								.postInputType(
										new TypeToken<ComplexNodeConfigurator<Object>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(
						c -> c.data().name("inline").isAbstract(true).optional(true)
								.valueResolution(ValueResolution.REGISTRATION_TIME)
								.type(base.primitiveType(DataType.BOOLEAN))).create();
		modelSet.add(abstractComplexModel);

		Model<ComplexNode<?>> complexModel = model
				.configure(loader)
				.name("complex", namespace)
				.baseModel(abstractComplexModel)
				.addChild(
						c -> c
								.data()
								.name("inline")
								.optional(true)
								.provideValue(
										new BufferingDataTarget().put(DataType.BOOLEAN, false)
												.buffer())).create();
		modelSet.add(complexModel);

		Model<ComplexNode<?>> inlineModel = model
				.configure(loader)
				.name("inline", namespace)
				.baseModel(abstractComplexModel)
				.addChild(
						c -> c
								.data()
								.name("inline")
								.optional(false)
								.provideValue(
										new BufferingDataTarget().put(DataType.BOOLEAN, true)
												.buffer())).create();
		modelSet.add(inlineModel);

		Model<DataNode<?>> typedDataModel = model
				.configure(loader)
				.baseModel(bindingChildNodeModel)
				.name("typedData", namespace)
				.dataType(new TypeToken<DataNode<?>>() {})
				.isAbstract(true)
				.addChild(c -> c.inputSequence().name("addChild"))
				.addChild(
						c -> c.inputSequence().name("configure").inMethod("data")
								.inMethodChained(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("format").optional(true)
								.valueResolution(ValueResolution.REGISTRATION_TIME)
								.isAbstract(true).type(base.derivedTypes().enumType())
								.dataType(Format.class)
								.postInputType(DataNodeConfigurator.class))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n
								.data()
								.format(Format.PROPERTY)
								.name("type")
								.optional(true)
								.type(base.derivedTypes().referenceType())
								.dataType(new TypeToken<DataBindingType<?>>() {})
								.addChild(
										p -> p
												.data()
												.name("targetModel")
												.valueResolution(ValueResolution.REGISTRATION_TIME)
												.provideValue(
														new BufferingDataTarget().put(
																DataType.QUALIFIED_NAME,
																new QualifiedName("type", namespace)).buffer()))
								.addChild(
										p -> p
												.data()
												.name("targetId")
												.valueResolution(ValueResolution.REGISTRATION_TIME)
												.provideValue(
														new BufferingDataTarget().put(
																DataType.QUALIFIED_NAME,
																new QualifiedName("name", namespace)).buffer())))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("optional")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("nullIfOmitted")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("valueResolution")
								.optional(true).type(base.derivedTypes().enumType())
								.dataType(ValueResolution.class))
				.addChild(
						n -> n
								.choice()
								.name("providedValue")
								.mandatory(false)
								.addChild(
										o -> o.data().format(Format.PROPERTY).name("value")
												.inMethod("provideValue")
												.outMethod("providedValueBuffer")
												.type(base.derivedTypes().bufferedDataType())))
				.create();
		modelSet.add(typedDataModel);

		Model<DataNode<?>> contentModel = model
				.configure(loader)
				.name("content", namespace)
				.baseModel(typedDataModel)
				.addChild(
						n -> n
								.data()
								.name("format")
								.optional(false)
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING, "CONTENT")
												.buffer())).create();
		modelSet.add(contentModel);

		Model<DataNode<?>> propertyModel = model
				.configure(loader)
				.name("property", namespace)
				.baseModel(typedDataModel)
				.addChild(
						n -> n
								.data()
								.name("format")
								.optional(false)
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING, "PROPERTY")
												.buffer())).create();
		modelSet.add(propertyModel);

		Model<DataNode<?>> simpleModel = model
				.configure(loader)
				.name("simple", namespace)
				.baseModel(typedDataModel)
				.addChild(
						n -> n
								.data()
								.name("format")
								.optional(false)
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING, "SIMPLE")
												.buffer())).create();
		modelSet.add(simpleModel);

		Model<DataNode<?>> dataModel = model.configure(loader)
				.name("data", namespace).baseModel(typedDataModel)
				.addChild(n -> n.data().name("format").occurrences(Range.create(0, 0)))
				.create();
		modelSet.add(dataModel);

		/* Type Models */

		Model<DataBindingType<?>> typeModel = model
				.configure(loader)
				.baseModel(bindingNodeModel)
				.name("type", namespace)
				.dataType(new TypeToken<DataBindingType<?>>() {})
				.bindingType(DataBindingTypeBuilder.class)
				.addChild(
						c -> c
								.inputSequence()
								.name("configure")
								.inMethodChained(true)
								.addChild(
										d -> d.data().dataType(DataLoader.class)
												.bindingStrategy(BindingStrategy.PROVIDED)
												.name("configure").outMethod("null")))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("private")
								.inMethod("isPrivate").optional(true)
								.type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(
						n -> n
								.data()
								.format(Format.PROPERTY)
								.name("baseType")
								.optional(true)
								.type(base.derivedTypes().referenceType())
								.dataType(new TypeToken<DataBindingType<?>>() {})
								.addChild(
										p -> p
												.data()
												.name("targetModel")
												.provideValue(
														new BufferingDataTarget().put(
																DataType.QUALIFIED_NAME,
																new QualifiedName("type", namespace)).buffer()))
								.addChild(
										p -> p
												.data()
												.name("targetId")
												.provideValue(
														new BufferingDataTarget().put(
																DataType.QUALIFIED_NAME,
																new QualifiedName("name", namespace)).buffer())))
				.create();
		modelSet.add(typeModel);

		/* Schema Models */

		schemaModel = model
				.configure(loader)
				.name("schema", namespace)
				.dataType(Schema.class)
				.bindingType(SchemaBuilder.class)
				.addChild(
						c -> c.inputSequence().name("configure").inMethodChained(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("name")
								.inMethod("qualifiedName").outMethod("getQualifiedName")
								.type(base.primitiveType(DataType.QUALIFIED_NAME)))
				.addChild(
						n -> n
								.complex()
								.name("dependencies")
								.occurrences(Range.create(0, 1))
								.dataType(new TypeToken<Set<Schema>>() {})
								.addChild(
										o -> o
												.data()
												.format(Format.SIMPLE)
												.inMethod("add")
												.name("dependency")
												.type(base.derivedTypes().importType())
												.dataType(Schema.class)
												.outMethodIterable(true)
												.outMethod("this")
												.occurrences(Range.create(0, null))
												.addChild(
														i -> i
																.data()
																.name("import")
																.addChild(
																		p -> p
																				.data()
																				.name("targetModel")
																				.provideValue(
																						new BufferingDataTarget().put(
																								DataType.QUALIFIED_NAME,
																								new QualifiedName("schema",
																										namespace)).buffer()))
																.addChild(
																		p -> p
																				.data()
																				.name("targetId")
																				.provideValue(
																						new BufferingDataTarget().put(
																								DataType.QUALIFIED_NAME,
																								new QualifiedName("name",
																										namespace)).buffer())))
												.addChild(
														p -> p
																.data()
																.name("dataTypes")
																.inMethod("null")
																.type(base.derivedTypes().includeType())
																.bindingType(Schema.class)
																.addChild(
																		q -> q.inputSequence().name("getDataTypes")
																				.inMethodChained(true))
																.addChild(
																		q -> q
																				.data()
																				.name("targetModel")
																				.provideValue(
																						new BufferingDataTarget().put(
																								DataType.QUALIFIED_NAME,
																								new QualifiedName("type",
																										namespace)).buffer())))
												.addChild(
														p -> p
																.data()
																.name("models")
																.inMethod("null")
																.type(base.derivedTypes().includeType())
																.bindingType(Schema.class)
																.addChild(
																		q -> q.inputSequence().name("getModels")
																				.inMethodChained(true))
																.addChild(
																		q -> q
																				.data()
																				.name("targetModel")
																				.provideValue(
																						new BufferingDataTarget().put(
																								DataType.QUALIFIED_NAME,
																								new QualifiedName("model",
																										namespace)).buffer())))))
				.addChild(
						n -> n
								.complex()
								.name("types")
								.outMethod("getDataTypes")
								.occurrences(Range.create(0, 1))
								.dataType(new TypeToken<@Infer Set<?>>() {})
								.bindingType(new TypeToken<@Infer LinkedHashSet<?>>() {})
								.addChild(
										o -> o.complex().baseModel(typeModel).outMethod("this")
												.name("type").outMethodIterable(true)
												.dataType(new TypeToken<DataBindingType<?>>() {})
												.occurrences(Range.create(0, null))))
				.addChild(
						n -> n
								.complex()
								.name("models")
								.occurrences(Range.create(0, 1))
								.dataType(new TypeToken<@Infer Set<?>>() {})
								.bindingType(new TypeToken<@Infer LinkedHashSet<?>>() {})
								.addChild(
										o -> o.complex().baseModel(modelModel).inMethod("add")
												.outMethodIterable(true).outMethod("this")
												.occurrences(Range.create(0, null))))
				.addChild(n -> n.inputSequence().name("create").inMethodChained(true))
				.create();
		modelSet.add(schemaModel);

		/*
		 * Schema
		 */
		metaSchema = schema.configure().qualifiedName(name)
				.dependencies(Arrays.asList(base)).types(typeSet).models(modelSet)
				.create();
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
	public DataBindingTypes getDataTypes() {
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
}
