package uk.co.strangeskies.modabi.schema.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

public class MetaSchemaImpl implements MetaSchema {
	private final Schema metaSchema;

	private final Model<Schema> schemaModel;

	@SuppressWarnings("unchecked")
	public MetaSchemaImpl(SchemaBuilder schema, ModelBuilder model,
			DataBindingTypeBuilder dataType, BaseSchema base) {
		DataLoader loader = null;

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
				.id("node")
				.isAbstract(true)
				.dataClass(SchemaNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY)
								.type(base.primitiveType(DataType.STRING)).id("id")
								.optional(true)).create();
		modelSet.add(nodeModel);

		Model<InputNode> inputModel = model
				.configure(loader)
				.id("input")
				.baseModel(nodeModel)
				.dataClass(InputNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("inMethod")
								.outMethod("getInMethodName").optional(true)
								.type(base.primitiveType(DataType.STRING)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("inMethodChained")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.create();
		modelSet.add(inputModel);

		@SuppressWarnings("rawtypes")
		Model<BindingChildNode> dataModel = model
				.configure(loader)
				.id("data")
				.baseModel(inputModel)
				.dataClass(BindingChildNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("dataClass")
								.type(base.derivedTypes().classType()).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("outMethod")
								.outMethod("getOutMethodName").optional(true)
								.type(base.primitiveType(DataType.STRING)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("outMethodIterable")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.create();
		modelSet.add(dataModel);

		Model<SchemaNode> branchModel = model
				.configure(loader)
				.id("branch")
				.baseModel(nodeModel)
				.dataClass(SchemaNode.class)
				.addChild(
						n -> n.element().id("child").outMethod("getChildren")
								.baseModel(nodeModel).outMethodIterable(true)
								.occurances(Range.create(0, null))).create();
		modelSet.add(branchModel);

		Model<ChoiceNode> choiceModel = model
				.configure(loader)
				.id("choice")
				.isAbstract(false)
				.dataClass(ChoiceNode.class)
				.bindingClass(ChoiceNodeConfigurator.class)
				.baseModel(branchModel)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("mandatory")
								.type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(choiceModel);

		Model<InputSequenceNode> sequenceModel = model.configure(loader)
				.id("sequence").isAbstract(false).dataClass(InputSequenceNode.class)
				.bindingClass(InputSequenceNodeConfigurator.class)
				.baseModel(inputModel, branchModel)
				.addChild(n -> n.data().format(Format.PROPERTY).id("id"))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(sequenceModel);

		@SuppressWarnings("rawtypes")
		Model<BindingChildNode> repeatableModel = model
				.configure(loader)
				.id("repeatable")
				.baseModel(nodeModel)
				.dataClass(BindingChildNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("occurances")
								.type(base.derivedTypes().rangeType())).create();
		modelSet.add(repeatableModel);

		@SuppressWarnings("rawtypes")
		Model<AbstractModel> abstractModelModel = model
				.configure(loader)
				.id("abstractModel")
				.baseModel(branchModel)
				.dataClass(AbstractModel.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("abstract")
								.type(base.primitiveType(DataType.BOOLEAN)).optional(true))
				.addChild(
						n -> n
								.data()
								.format(Format.PROPERTY)
								.id("baseModel")
								.optional(true)
								.type(base.derivedTypes().listType())
								.addChild(
										o -> o
												.data()
												.id("element")
												.type(base.derivedTypes().referenceType())
												.dataClass(Model.class)
												.addChild(
														p -> p
																.data()
																.id("id")
																.provideValue(
																		new BufferingDataTarget().put(
																				DataType.STRING, "id").buffer()))
												.addChild(
														p -> p
																.data()
																.id("targetDomain")
																.provideValue(
																		new BufferingDataTarget().put(
																				DataType.STRING, "Model").buffer()))))
				.addChild(
						o -> o.data().format(Format.PROPERTY).id("dataClass")
								.type(base.derivedTypes().classType()).optional(true))
				.addChild(
						o -> o.data().format(Format.PROPERTY).id("bindingStrategy")
								.type(base.derivedTypes().enumType())
								.dataClass(BindingStrategy.class).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("bindingClass")
								.type(base.derivedTypes().classType()))
				.addChild(
						o -> o.data().format(Format.PROPERTY).id("unbindingStrategy")
								.type(base.derivedTypes().referenceType()).optional(true))
				.addChild(
						o -> o.data().format(Format.PROPERTY).id("unbindingMethod")
								.outMethod("getUnbindingMethodName")
								.type(base.primitiveType(DataType.STRING)).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("unbindingClass")
								.type(base.derivedTypes().classType()))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(abstractModelModel);

		@SuppressWarnings("rawtypes")
		Model<Model> modelModel = model
				.configure(loader)
				.id("model")
				.baseModel(abstractModelModel)
				.dataClass(Model.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("id").optional(false))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(modelModel);

		@SuppressWarnings("rawtypes")
		Model<ElementNode> elementModel = model.configure(loader).id("element")
				.dataClass(ElementNode.class)
				.bindingClass(ElementNodeConfigurator.class)
				.baseModel(dataModel, repeatableModel, abstractModelModel)
				.isAbstract(false).addChild(n -> n.data().id("id"))
				.addChild(o -> o.data().id("dataClass"))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(elementModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> typedDataModel = model
				.configure(loader)
				.baseModel(dataModel)
				.id("typedData")
				.dataClass(DataNode.class)
				.bindingClass(DataNodeConfigurator.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("type").type(typeType))
				.addChild(
						n -> n
								.data()
								.format(Format.PROPERTY)
								.id("format")
								.type(base.derivedTypes().enumType())
								.dataClass(Format.class)
								.addChild(
										o -> o
												.inputSequence()
												.id("valueOf")
												.addChild(
														p -> p
																.data()
																.id("enumType")
																.provideValue(
																		new BufferingDataTarget()
																				.put(DataType.STRING,
																						"uk.co.strangeskies.modabi.model.nodes.DataNode.Format")
																				.buffer()))
												.addChild(p -> p.data().id("name"))))
				.addChild(
						n -> n.data().format(Format.SIMPLE_ELEMENT).id("value")
								.optional(true).type(base.derivedTypes().bufferedDataType()))
				.create();
		modelSet.add(typedDataModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> optionalModel = model
				.configure(loader)
				.id("optional")
				.isAbstract(true)
				.baseModel(nodeModel)
				.dataClass(DataNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("optional")
								.type(base.primitiveType(DataType.BOOLEAN))).create();
		modelSet.add(optionalModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> contentModel = model
				.configure(loader)
				.id("content")
				.baseModel(typedDataModel, optionalModel)
				.isAbstract(false)
				.dataClass(DataNode.class)
				.addChild(
						n -> n
								.data()
								.id("format")
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING, "Content")
												.buffer())).addChild(n -> n.data().id("id")).create();
		modelSet.add(contentModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> propertyModel = model
				.configure(loader)
				.id("property")
				.isAbstract(false)
				.baseModel(typedDataModel, optionalModel)
				.dataClass(DataNode.class)
				.addChild(
						n -> n
								.data()
								.id("format")
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING, "Property")
												.buffer())).addChild(n -> n.data().id("id")).create();
		modelSet.add(propertyModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> simpleElementModel = model
				.configure(loader)
				.id("simpleElement")
				.isAbstract(false)
				.baseModel(typedDataModel, optionalModel)
				.dataClass(DataNode.class)
				.addChild(
						n -> n
								.data()
								.id("format")
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING,
												"Simple Element").buffer()))
				.addChild(n -> n.data().id("id")).create();
		modelSet.add(simpleElementModel);

		/* Type Models */

		@SuppressWarnings("rawtypes")
		Model<DataBindingType> typeModel = model
				.configure(loader)
				.dataClass(DataBindingType.class)
				.id("type")
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("dataClass")
								.type(base.derivedTypes().classType()))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("bindingClass")
								.type(base.derivedTypes().classType()))
				.addChild(
						n -> n
								.element()
								.id("properties")
								.dataClass(List.class)
								.outMethod("getChildren")
								.addChild(
										o -> o.element().baseModel(propertyModel)
												.outMethodIterable(true).outMethod("this"))).create();
		modelSet.add(typeModel);

		/* Schema Models */

		@SuppressWarnings("rawtypes")
		Model<Set> modelsModel = model
				.configure(loader)
				.id("models")
				.dataClass(Set.class)
				.addChild(
						n -> n.element().baseModel(modelModel).outMethodIterable(true)
								.outMethod("this").occurances(Range.create(0, null))).create();
		modelSet.add(modelsModel);

		schemaModel = model
				.configure(loader)
				.id("schemaModel")
				.dataClass(Schema.class)
				.bindingClass(SchemaConfigurator.class)
				.addChild(
						n -> n
								.element()
								.id("dependencies")
								.occurances(Range.create(0, 1))
								.dataClass(Set.class)
								.addChild(
										o -> o.element().id("dependency")
												.baseModel(base.models().includeModel())
												.outMethodIterable(true).outMethod("this")
												.occurances(Range.create(0, null))))
				.addChild(
						n -> n
								.element()
								.id("types")
								.outMethod("getDataTypes")
								.occurances(Range.create(0, 1))
								.dataClass(Set.class)
								.addChild(
										o -> o.element().baseModel(typeModel).outMethod("this")
												.id("type").outMethodIterable(true)
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
