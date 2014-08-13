package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.data.DataBindingTypeConfigurator;
import uk.co.strangeskies.modabi.data.DataBindingTypes;
import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.InputNode;
import uk.co.strangeskies.modabi.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.SchemaConfigurator;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public class MetaSchemaImpl implements MetaSchema {
	private final Schema metaSchema;

	private final Model<Schema> schemaModel;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MetaSchemaImpl(SchemaBuilder schema, ModelBuilder model,
			DataBindingTypeBuilder dataType, DataLoader loader, BaseSchema base) {
		QualifiedName name = new QualifiedName(MetaSchema.class.getName(),
				new Namespace(BaseSchema.class.getPackage().getName()));

		/*
		 * Types
		 */
		Set<DataBindingType<?>> typeSet = new HashSet<>();

		DataBindingType<?> typeType = dataType.configure(loader).name("type")
				.dataClass(DataBindingType.class)
				.bindingClass(DataBindingTypeConfigurator.class).create();
		typeSet.add(typeType);

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		/* Node Models */

		Model<SchemaNode> nodeModel = model
				.configure(loader)
				.name("node")
				.isAbstract(true)
				.dataClass(SchemaNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY)
								.type(base.primitiveType(DataType.STRING)).name("name")
								.optional(true)).create();
		modelSet.add(nodeModel);

		Model<InputNode> inputModel = model
				.configure(loader)
				.name("input")
				.baseModel(nodeModel)
				.dataClass(InputNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethod")
								.outMethod("getInMethodName").optional(true)
								.type(base.primitiveType(DataType.STRING)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethodChained")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.create();
		modelSet.add(inputModel);

		Model<SchemaNode> branchModel = model
				.configure(loader)
				.name("branch")
				.baseModel(nodeModel)
				.dataClass(SchemaNode.class)
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.element().name("child").outMethod("children")
								.baseModel(nodeModel).outMethodIterable(true)
								.occurances(Range.create(0, null))).create();
		modelSet.add(branchModel);

		Model<BindingNode> bindingNodeModel = model
				.configure(loader)
				.name("binding")
				.baseModel(nodeModel, branchModel)
				.dataClass(BindingNode.class)
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("dataClass")
								.type(base.derivedTypes().classType()).optional(true))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("bindingStrategy")
								.type(base.derivedTypes().enumType())
								.dataClass(BindingStrategy.class).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("bindingClass")
								.type(base.derivedTypes().classType()))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("unbindingStrategy")
								.type(base.derivedTypes().referenceType()).optional(true))
				.addChild(
						o -> o.data().format(Format.PROPERTY).name("unbindingMethod")
								.outMethod("getUnbindingMethodName")
								.type(base.primitiveType(DataType.STRING)).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("unbindingClass")
								.type(base.derivedTypes().classType())).create();
		modelSet.add(bindingNodeModel);

		Model<BindingChildNode> bindingChildNodeModel = model
				.configure(loader)
				.name("bindingChild")
				.dataClass(BindingChildNode.class)
				.baseModel(inputModel, bindingNodeModel)
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
				.name("choice")
				.isAbstract(false)
				.dataClass(ChoiceNode.class)
				.bindingClass(ChoiceNodeConfigurator.class)
				.baseModel(branchModel)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("mandatory")
								.type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(choiceModel);

		Model<InputSequenceNode> sequenceModel = model.configure(loader)
				.name("sequence").isAbstract(false).dataClass(InputSequenceNode.class)
				.bindingClass(InputSequenceNodeConfigurator.class)
				.baseModel(inputModel, branchModel)
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(sequenceModel);

		Model<BindingChildNode> repeatableModel = model
				.configure(loader)
				.name("repeatable")
				.baseModel(nodeModel)
				.dataClass(BindingChildNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("occurances")
								.type(base.derivedTypes().rangeType())).create();
		modelSet.add(repeatableModel);

		Model<AbstractModel> abstractModelModel = model
				.configure(loader)
				.name("abstractModel")
				.baseModel(bindingNodeModel)
				.dataClass(AbstractModel.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("abstract")
								.type(base.primitiveType(DataType.BOOLEAN)).optional(true))
				.addChild(
						n -> n
								.data()
								.format(Format.PROPERTY)
								.name("baseModel")
								.optional(true)
								.type(base.derivedTypes().setType())
								.addChild(
										o -> o
												.data()
												.name("element")
												.type(base.derivedTypes().referenceType())
												.dataClass(Model.class)
												.addChild(
														p -> p
																.data()
																.name("targetDomain")
																.provideValue(
																		new BufferingDataTarget().put(
																				DataType.STRING, "model").buffer()))
												.addChild(
														p -> p
																.data()
																.name("id")
																.provideValue(
																		new BufferingDataTarget().put(
																				DataType.STRING, "name").buffer()))))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(abstractModelModel);

		Model<Model> modelModel = model
				.configure(loader)
				.name("model")
				.baseModel(abstractModelModel)
				.isAbstract(false)
				.dataClass(Model.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("name").optional(false))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(modelModel);

		Model<ElementNode> elementModel = model.configure(loader).name("element")
				.dataClass(ElementNode.class)
				.bindingClass(ElementNodeConfigurator.class)
				.baseModel(bindingChildNodeModel, repeatableModel, abstractModelModel)
				.isAbstract(false).addChild(o -> o.data().name("dataClass"))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(elementModel);

		Model<DataNode> typedDataModel = model
				.configure(loader)
				.baseModel(bindingChildNodeModel)
				.name("typedData")
				.dataClass(DataNode.class)
				.bindingClass(DataNodeConfigurator.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("type").type(typeType))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("format")
								.type(base.derivedTypes().enumType()).dataClass(Format.class))
				.addChild(
						n -> n.data().format(Format.SIMPLE_ELEMENT).name("providedValue")
								.outMethod("providedValueBuffer").optional(true)
								.type(base.derivedTypes().bufferedDataType())).create();
		modelSet.add(typedDataModel);

		Model<DataNode> optionalModel = model
				.configure(loader)
				.name("optional")
				.baseModel(nodeModel)
				.dataClass(DataNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("optional")
								.type(base.primitiveType(DataType.BOOLEAN))).create();
		modelSet.add(optionalModel);

		Model<DataNode> contentModel = model
				.configure(loader)
				.name("content")
				.baseModel(typedDataModel, optionalModel)
				.isAbstract(false)
				.dataClass(DataNode.class)
				.addChild(
						n -> n
								.data()
								.name("format")
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING, "CONTENT")
												.buffer())
								.valueResolution(ValueResolution.REGISTRATION_TIME)).create();
		modelSet.add(contentModel);

		Model<DataNode> propertyModel = model
				.configure(loader)
				.name("property")
				.isAbstract(false)
				.baseModel(typedDataModel, optionalModel)
				.dataClass(DataNode.class)
				.addChild(
						n -> n
								.data()
								.name("format")
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING, "PROPERTY")
												.buffer())
								.valueResolution(ValueResolution.REGISTRATION_TIME)).create();
		modelSet.add(propertyModel);

		Model<DataNode> simpleElementModel = model
				.configure(loader)
				.name("simpleElement")
				.isAbstract(false)
				.baseModel(typedDataModel, optionalModel)
				.dataClass(DataNode.class)
				.addChild(
						n -> n
								.data()
								.name("format")
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING,
												"SIMPLE_ELEMENT").buffer())
								.valueResolution(ValueResolution.REGISTRATION_TIME)).create();
		modelSet.add(simpleElementModel);

		/* Type Models */

		Model<DataBindingType> typeModel = model
				.configure(loader)
				.baseModel(bindingNodeModel)
				.dataClass(DataBindingType.class)
				.isAbstract(false)
				.name("type")
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("abstract")
								.type(base.primitiveType(DataType.BOOLEAN)).optional(true))
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
												.name("targetDomain")
												.provideValue(
														new BufferingDataTarget().put(DataType.STRING,
																"type").buffer()))
								.addChild(
										p -> p
												.data()
												.name("id")
												.provideValue(
														new BufferingDataTarget().put(DataType.STRING,
																"name").buffer()))).create();
		modelSet.add(typeModel);

		/* Schema Models */

		Model<Set> modelsModel = model
				.configure(loader)
				.name("models")
				.dataClass(Set.class)
				.addChild(
						n -> n.element().baseModel(modelModel).outMethodIterable(true)
								.outMethod("this").occurances(Range.create(0, null))).create();
		modelSet.add(modelsModel);

		schemaModel = model
				.configure(loader)
				.name("schemaModel")
				.dataClass(Schema.class)
				.bindingClass(SchemaConfigurator.class)
				.addChild(
						n -> n
								.element()
								.name("dependencies")
								.occurances(Range.create(0, 1))
								.dataClass(Set.class)
								.addChild(
										o -> o.element().name("dependency").dataClass(Schema.class)
												.baseModel(base.models().includeModel())
												.outMethodIterable(true).outMethod("this")
												.occurances(Range.create(0, null))))
				.addChild(
						n -> n
								.element()
								.name("types")
								.outMethod("getDataTypes")
								.occurances(Range.create(0, 1))
								.dataClass(Set.class)
								.addChild(
										o -> o.element().baseModel(typeModel).outMethod("this")
												.name("type").outMethodIterable(true)
												.dataClass(DataBindingType.class)
												.occurances(Range.create(0, null))))
				.addChild(
						n -> n.element().baseModel(modelsModel)
								.occurances(Range.create(0, 1))).create();
		modelSet.add(schemaModel);

		/*
		 * Schema
		 */
		metaSchema = schema.configure().qualifiedName(name).types(typeSet)
				.models(modelSet).create();
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
