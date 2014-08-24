package uk.co.strangeskies.modabi.schema.processing.impl;

import java.time.LocalDate;
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
import uk.co.strangeskies.modabi.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.InputNode;
import uk.co.strangeskies.modabi.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.SchemaConfigurator;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

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
		Set<DataBindingType<?>> typeSet = new HashSet<>();

		DataBindingType<?> typeType = dataType.configure(loader)
				.name("type", namespace).dataClass(DataBindingType.class)
				.bindingClass(DataBindingTypeConfigurator.class).create();
		typeSet.add(typeType);

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
						n -> n.data().format(Format.PROPERTY)
								.type(base.primitiveType(DataType.QUALIFIED_NAME)).name("name")
								.optional(true)).create();
		modelSet.add(nodeModel);

		Model<InputNode> inputModel = model
				.configure(loader)
				.name("input", namespace)
				.isAbstract(true)
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
				.name("branch", namespace)
				.isAbstract(true)
				.baseModel(nodeModel)
				.dataClass(SchemaNode.class)
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.element().name("child").outMethod("children")
								.isAbstract(true).isExtensible(true).baseModel(nodeModel)
								.outMethodIterable(true).occurances(Range.create(0, null)))
				.create();
		modelSet.add(branchModel);

		Model<BindingNode> bindingNodeModel = model
				.configure(loader)
				.name("binding", namespace)
				.baseModel(nodeModel, branchModel)
				.isAbstract(true)
				.dataClass(BindingNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("abstract")
								.type(base.primitiveType(DataType.BOOLEAN)).optional(true))
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
								.type(base.derivedTypes().enumType())
								.dataClass(UnbindingStrategy.class).optional(true))
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
				.name("bindingChild", namespace)
				.isAbstract(true)
				.dataClass(BindingChildNode.class)
				.baseModel(inputModel, bindingNodeModel)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("extensible")
								.type(base.primitiveType(DataType.BOOLEAN)).optional(true))
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
				.bindingClass(ChoiceNodeConfigurator.class)
				.baseModel(branchModel)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("mandatory")
								.type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(choiceModel);

		Model<SequenceNode> sequenceModel = model.configure(loader)
				.name("sequence", namespace).dataClass(SequenceNode.class)
				.bindingClass(SequenceNodeConfigurator.class).baseModel(branchModel)
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(sequenceModel);

		Model<InputSequenceNode> inputSequenceModel = model.configure(loader)
				.name("inputSequence", namespace).dataClass(InputSequenceNode.class)
				.bindingClass(InputSequenceNodeConfigurator.class)
				.baseModel(inputModel, branchModel)
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(inputSequenceModel);

		Model<BindingChildNode> repeatableModel = model
				.configure(loader)
				.name("repeatable", namespace)
				.isAbstract(true)
				.baseModel(nodeModel)
				.dataClass(BindingChildNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("occurances")
								.type(base.derivedTypes().rangeType())).create();
		modelSet.add(repeatableModel);

		Model<AbstractModel> abstractModelModel = model
				.configure(loader)
				.name("abstractModel", namespace)
				.baseModel(bindingNodeModel)
				.isAbstract(true)
				.dataClass(AbstractModel.class)
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
																.name("targetModel")
																.isAbstract(true)
																.provideValue(
																		new BufferingDataTarget().put(
																				DataType.STRING, "model").buffer()))
												.addChild(
														p -> p
																.data()
																.name("targetId")
																.provideValue(
																		new BufferingDataTarget().put(
																				DataType.STRING, "name").buffer()))))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(abstractModelModel);

		Model<Model> modelModel = model.configure(loader).name("model", namespace)
				.baseModel(abstractModelModel).dataClass(Model.class)
				.addChild(n -> n.data().name("name").optional(false))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(modelModel);

		Model<ElementNode> elementModel = model.configure(loader)
				.name("element", namespace).dataClass(ElementNode.class)
				.bindingClass(ElementNodeConfigurator.class)
				.baseModel(bindingChildNodeModel, repeatableModel, abstractModelModel)
				.addChild(o -> o.data().name("dataClass"))
				.addChild(n -> n.element().name("child")).create();
		modelSet.add(elementModel);

		Model<DataNode> typedDataModel = model
				.configure(loader)
				.baseModel(bindingChildNodeModel)
				.name("typedData", namespace)
				.dataClass(DataNode.class)
				.isAbstract(true)
				.bindingClass(DataNodeConfigurator.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("type").type(typeType))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("optional")
								.type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("format")
								.type(base.derivedTypes().enumType()).dataClass(Format.class))
				.addChild(
						n -> n.data().format(Format.SIMPLE_ELEMENT).name("value")
								.outMethod("providedValueBuffer").optional(true)
								.type(base.derivedTypes().bufferedDataType())).create();
		modelSet.add(typedDataModel);

		Model<DataNode> contentModel = model
				.configure(loader)
				.name("content", namespace)
				.baseModel(typedDataModel)
				.addChild(
						n -> n
								.data()
								.name("format")
								.valueResolution(ValueResolution.REGISTRATION_TIME)
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
								.valueResolution(ValueResolution.REGISTRATION_TIME)
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING, "PROPERTY")
												.buffer())).create();
		modelSet.add(propertyModel);

		Model<DataNode> simpleElementModel = model
				.configure(loader)
				.name("simpleElement", namespace)
				.baseModel(typedDataModel)
				.addChild(
						n -> n
								.data()
								.name("format")
								.isAbstract(false)
								.outMethod("format")
								.valueResolution(ValueResolution.REGISTRATION_TIME)
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING,
												"SIMPLE_ELEMENT").buffer())).create();
		modelSet.add(simpleElementModel);

		Model<DataNode> dataModel = model.configure(loader).name("data", namespace)
				.baseModel(typedDataModel).create();
		modelSet.add(dataModel);

		/* Type Models */

		Model<DataBindingType> typeModel = model
				.configure(loader)
				.baseModel(bindingNodeModel)
				.dataClass(DataBindingType.class)
				.name("type", namespace)
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
												.isAbstract(true)
												.name("targetModel")
												.provideValue(
														new BufferingDataTarget().put(DataType.STRING,
																"type").buffer()))
								.addChild(
										p -> p
												.data()
												.name("targetId")
												.provideValue(
														new BufferingDataTarget().put(DataType.STRING,
																"name").buffer()))).create();
		modelSet.add(typeModel);

		/* Schema Models */

		Model<Set> modelsModel = model
				.configure(loader)
				.name("models", namespace)
				.dataClass(Set.class)
				.addChild(
						n -> n.element().baseModel(modelModel).outMethodIterable(true)
								.outMethod("this").occurances(Range.create(0, null))).create();
		modelSet.add(modelsModel);

		schemaModel = model
				.configure(loader)
				.name("schemaModel", namespace)
				.dataClass(Schema.class)
				.bindingClass(SchemaConfigurator.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("qualifiedName")
								.type(base.primitiveType(DataType.QUALIFIED_NAME)))
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
