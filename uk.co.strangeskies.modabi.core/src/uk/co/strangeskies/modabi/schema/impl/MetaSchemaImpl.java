package uk.co.strangeskies.modabi.schema.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataType;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.management.ValueResolution;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.node.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.ChoiceNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.DataNode.Format;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.AbstractModelConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.InputNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.ModelBuilder;
import uk.co.strangeskies.modabi.schema.node.model.Models;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypes;

public class MetaSchemaImpl implements MetaSchema {
	private final Schema metaSchema;
	private final Model<Schema> schemaModel;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MetaSchemaImpl(SchemaBuilder schema, ModelBuilder model,
			DataBindingTypeBuilder dataType, DataLoader loader, BaseSchema base) {
		Namespace namespace = new Namespace(MetaSchema.class.getPackage(),
				LocalDate.of(2014, 1, 1));
		QualifiedName name = new QualifiedName(MetaSchema.class.getSimpleName(),
				namespace);

		/*
		 * Types
		 */
		Set<DataBindingType<?>> typeSet = new LinkedHashSet<>();

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		/* Node Models */

		Model<SchemaNode> nodeModel = model
				.configure(loader)
				.name("node", namespace)
				.isAbstract(true)
				.dataClass(SchemaNode.class)
				.addChild(
						n -> n.inputSequence().name("configure").isAbstract(true)
								.postInputType(SchemaNodeConfigurator.class))
				.addChild(
						n -> n.data().format(Format.PROPERTY)
								.type(base.primitiveType(DataType.QUALIFIED_NAME)).name("name")
								.optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY)
								.type(base.primitiveType(DataType.BOOLEAN)).name("abstract")
								.inMethod("isAbstract").optional(true)).create();
		modelSet.add(nodeModel);

		Model<SchemaNode> branchModel = model
				.configure(loader)
				.name("branch", namespace)
				.isAbstract(true)
				.baseModel(nodeModel)
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.data().name("abstract"))
				.addChild(
						n -> n.complex().name("child").outMethod("children")
								.inMethod("null").extensible(true).baseModel(nodeModel)
								.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
								.bindingType(SchemaNodeConfigurator.class)
								.dataClass(ChildNode.class).outMethodIterable(true)
								.occurrences(Range.create(0, null)))
				.addChild(n -> n.inputSequence().name("create").inMethodChained(true))
				.create();
		modelSet.add(branchModel);

		Model<ChildNode> childModel = model
				.configure(loader)
				.name("child", namespace)
				.baseModel(branchModel)
				.isAbstract(true)
				.dataClass(ChildNode.class)
				.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
				.bindingType(SchemaNodeConfigurator.class)
				.addChild(c -> c.inputSequence().name("addChild").inMethodChained(true))
				.addChild(
						c -> c.inputSequence().name("configure").isAbstract(true)
								.inMethodChained(true)
								.postInputType(ChildNodeConfigurator.class))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY)
								.type(base.derivedTypes().classType()).name("postInputClass")
								.optional(true)).create();
		modelSet.add(childModel);

		Model<InputNode> inputModel = model
				.configure(loader)
				.name("input", namespace)
				.isAbstract(true)
				.baseModel(childModel)
				.dataClass(InputNode.class)
				.addChild(
						c -> c.inputSequence().name("configure").isAbstract(true)
								.postInputType(InputNodeConfigurator.class))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethod")
								.outMethod("getInMethodName").optional(true)
								.type(base.primitiveType(DataType.STRING)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethodChained")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("isInMethodCast")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.create();
		modelSet.add(inputModel);

		Model<BindingNode> bindingNodeModel = model
				.configure(loader)
				.name("binding", namespace)
				.baseModel(branchModel)
				.isAbstract(true)
				.dataClass(BindingNode.class)
				.addChild(
						c -> c.inputSequence().name("configure").isAbstract(true)
								.postInputType(BindingNodeConfigurator.class))
				.addChild(n -> n.data().name("name"))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("dataClass")
								.type(base.derivedTypes().classType()).optional(true))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("bindingStrategy")
								.type(base.derivedTypes().enumType())
								.dataClass(BindingStrategy.class).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("bindingClass")
								.optional(true).type(base.derivedTypes().classType()))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("unbindingStrategy")
								.type(base.derivedTypes().enumType())
								.dataClass(UnbindingStrategy.class).optional(true))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("unbindingMethod")
								.outMethod("getUnbindingMethodName")
								.type(base.primitiveType(DataType.STRING)).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("unbindingClass")
								.optional(true).type(base.derivedTypes().classType()))
				.addChild(
						n -> n
								.data()
								.format(Format.PROPERTY)
								.name("providedUnbindingMethodParameters")
								.optional(true)
								.outMethod("getProvidedUnbindingMethodParameterNames")
								.type(base.derivedTypes().listType())
								.addChild(
										o -> o.data().name("element")
												.type(base.primitiveType(DataType.QUALIFIED_NAME))))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("unbindingFactoryClass")
								.optional(true).type(base.derivedTypes().classType())).create();
		modelSet.add(bindingNodeModel);

		Model<BindingChildNode> bindingChildNodeModel = model
				.configure(loader)
				.name("bindingChild", namespace)
				.isAbstract(true)
				.dataClass(BindingChildNode.class)
				.baseModel(inputModel, bindingNodeModel)
				.addChild(
						c -> c.inputSequence().name("configure").isAbstract(true)
								.postInputType(BindingChildNodeConfigurator.class))
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
				.create();
		modelSet.add(bindingChildNodeModel);

		Model<ChoiceNode> choiceModel = model
				.configure(loader)
				.name("choice", namespace)
				.dataClass(ChoiceNode.class)
				.baseModel(childModel)
				.addChild(c -> c.inputSequence().name("configure").inMethod("choice"))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("mandatory")
								.type(base.primitiveType(DataType.BOOLEAN))).create();
		modelSet.add(choiceModel);

		Model<SequenceNode> sequenceModel = model
				.configure(loader)
				.name("sequence", namespace)
				.dataClass(SequenceNode.class)
				.baseModel(childModel)
				.addChild(c -> c.inputSequence().name("configure").inMethod("sequence"))
				.create();
		modelSet.add(sequenceModel);

		Model<InputSequenceNode> inputSequenceModel = model
				.configure(loader)
				.name("inputSequence", namespace)
				.dataClass(InputSequenceNode.class)
				.baseModel(inputModel, childModel)
				.addChild(
						c -> c.inputSequence().name("configure").inMethod("inputSequence"))
				.create();
		modelSet.add(inputSequenceModel);

		Model<AbstractComplexNode> abstractModelModel = model
				.configure(loader)
				.name("abstractModel", namespace)
				.baseModel(bindingNodeModel)
				.isAbstract(true)
				.dataClass(AbstractComplexNode.class)
				.addChild(
						c -> c.inputSequence().name("configure").isAbstract(true)
								.postInputType(AbstractModelConfigurator.class))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n
								.data()
								.format(Format.PROPERTY)
								.name("baseModel")
								.optional(true)
								.type(base.derivedTypes().listType())
								.addChild(
										o -> o
												.data()
												.name("element")
												.type(base.derivedTypes().referenceType())
												.dataClass(Model.class)
												.addChild(
														p -> p
																.data()
																.name("targetModel")
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

		Model<Model> modelModel = model
				.configure(loader)
				.name("model", namespace)
				.baseModel(abstractModelModel)
				.dataClass(Model.class)
				.bindingType(ModelBuilder.class)
				.addChild(
						c -> c
								.inputSequence()
								.name("configure")
								.inMethodChained(true)
								.addChild(
										d -> d.data().dataClass(DataLoader.class)
												.bindingStrategy(BindingStrategy.PROVIDED)
												.name("configure").outMethod("null")))
				.addChild(n -> n.data().name("name").optional(false)).create();
		modelSet.add(modelModel);

		Model<ComplexNode> abstractComplexModel = model
				.configure(loader)
				.name("abstractComplex", namespace)
				.isAbstract(true)
				.dataClass(ComplexNode.class)
				.baseModel(abstractModelModel, bindingChildNodeModel)
				.addChild(c -> c.inputSequence().name("addChild"))
				.addChild(
						c -> c.inputSequence().name("configure").inMethod("complex")
								.inMethodChained(true))
				.addChild(n -> n.data().name("name"))
				.addChild(
						c -> c.data().name("inline").isAbstract(true).optional(true)
								.valueResolution(ValueResolution.REGISTRATION_TIME)
								.type(base.primitiveType(DataType.BOOLEAN))).create();
		modelSet.add(abstractComplexModel);

		Model<ComplexNode> complexModel = model
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

		Model<ComplexNode> inlineModel = model
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

		Model<DataNode> typedDataModel = model
				.configure(loader)
				.baseModel(bindingChildNodeModel)
				.name("typedData", namespace)
				.dataClass(DataNode.class)
				.isAbstract(true)
				.addChild(c -> c.inputSequence().name("addChild"))
				.addChild(
						c -> c.inputSequence().name("configure").inMethod("data")
								.inMethodChained(true))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n
								.data()
								.format(Format.PROPERTY)
								.name("type")
								.optional(true)
								.type(base.derivedTypes().referenceType())
								.dataClass(DataBindingType.class)
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
						n -> n.data().format(Format.PROPERTY).name("format").optional(true)
								.valueResolution(ValueResolution.REGISTRATION_TIME)
								.isAbstract(true).type(base.derivedTypes().enumType())
								.dataClass(Format.class)
								.postInputType(DataNodeConfigurator.class))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("valueResolution")
								.optional(true).type(base.derivedTypes().enumType())
								.dataClass(ValueResolution.class))
				/*
				 * TODO Figure out how to have value output itself as a SIMPLE_ELEMENT
				 * if there are no 'child' elements. Perhaps can work something out once
				 * 'choice' nodes are fully implemented.
				 */
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("value")
								.inMethod("provideValue").outMethod("providedValueBuffer")
								.optional(true).type(base.derivedTypes().bufferedDataType()))
				.create();
		modelSet.add(typedDataModel);

		Model<DataNode> contentModel = model
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

		Model<DataNode> propertyModel = model
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

		Model<DataNode> simpleModel = model
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

		Model<DataNode> dataModel = model.configure(loader).name("data", namespace)
				.baseModel(typedDataModel).addChild(n -> n.data().name("format"))
				.create();
		modelSet.add(dataModel);

		/* Type Models */

		Model<DataBindingType> typeModel = model
				.configure(loader)
				.baseModel(bindingNodeModel)
				.name("type", namespace)
				.dataClass(DataBindingType.class)
				.bindingType(DataBindingTypeBuilder.class)
				.addChild(
						c -> c
								.inputSequence()
								.name("configure")
								.inMethodChained(true)
								.addChild(
										d -> d.data().dataClass(DataLoader.class)
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
								.dataClass(DataBindingType.class)
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
				.dataClass(Schema.class)
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
								.dataClass(Set.class)
								.addChild(
										o -> o
												.data()
												.format(Format.SIMPLE)
												.inMethod("add")
												.name("dependency")
												.type(base.derivedTypes().importType())
												.dataClass(Schema.class)
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
								.dataClass(Set.class)
								.bindingType(LinkedHashSet.class)
								.addChild(
										o -> o.complex().baseModel(typeModel).outMethod("this")
												.name("type").outMethodIterable(true)
												.dataClass(DataBindingType.class)
												.occurrences(Range.create(0, null))))
				.addChild(
						n -> n
								.complex()
								.name("models")
								.occurrences(Range.create(0, 1))
								.dataClass(Set.class)
								.bindingType(LinkedHashSet.class)
								.addChild(
										o -> o.complex().baseModel(modelModel)
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
}
