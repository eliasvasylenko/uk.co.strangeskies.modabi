package uk.co.strangeskies.modabi.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.data.impl.BufferingDataTargetImpl;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.InputNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public class MetaSchemaImpl implements MetaSchema {
	private final Schema metaSchema;

	private final Model<Schema> schemaModel;

	@SuppressWarnings("unchecked")
	public MetaSchemaImpl(SchemaBuilder schema, ModelBuilder model,
			DataTypeBuilder dataType, BaseSchema base) {
		QualifiedName name = new QualifiedName(MetaSchema.class.getName(),
				new Namespace(BaseSchema.class.getPackage().getName()));

		/*
		 * Types
		 */
		Set<DataType<?>> typeSet = new HashSet<>();

		DataType<?> typeType = dataType.configure().name("type")
				.dataClass(DataType.class).create();
		typeSet.add(typeType);

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		/* Node Models */

		Model<SchemaNode> nodeModel = model
				.configure()
				.id("node")
				.isAbstract(true)
				.dataClass(SchemaNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY)
								.type(base.primitiveTypes().stringType()).id("id")
								.optional(true)).create();
		modelSet.add(nodeModel);

		Model<InputNode> inputModel = model
				.configure()
				.id("input")
				.baseModel(nodeModel)
				.dataClass(InputNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("inMethod")
								.outMethod("getInMethodName").optional(true)
								.type(base.primitiveTypes().stringType()))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("inMethodChained")
								.optional(true).type(base.primitiveTypes().booleanType()))
				.create();
		modelSet.add(inputModel);

		@SuppressWarnings("rawtypes")
		Model<BindingChildNode> dataModel = model
				.configure()
				.id("data")
				.baseModel(inputModel)
				.dataClass(BindingChildNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("dataClass")
								.type(base.derivedTypes().classType()).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("outMethod")
								.outMethod("getOutMethodName").optional(true)
								.type(base.primitiveTypes().stringType()))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("outMethodIterable")
								.optional(true).type(base.primitiveTypes().booleanType()))
				.create();
		modelSet.add(dataModel);

		Model<SchemaNode> branchModel = model
				.configure()
				.id("branch")
				.baseModel(nodeModel)
				.dataClass(SchemaNode.class)
				.addChild(
						n -> n.element().id("child").outMethod("getChildren")
								.baseModel(nodeModel).outMethodIterable(true)
								.occurances(Range.create(0, null))).create();
		modelSet.add(branchModel);

		Model<ChoiceNode> choiceModel = model
				.configure()
				.id("choice")
				.isAbstract(false)
				.dataClass(ChoiceNode.class)
				.bindingClass(ChoiceNodeConfigurator.class)
				.baseModel(branchModel)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("mandatory")
								.type(base.primitiveTypes().booleanType()))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(choiceModel);

		Model<SequenceNode> sequenceModel = model.configure().id("sequence")
				.isAbstract(false).dataClass(SequenceNode.class)
				.bindingClass(SequenceNodeConfigurator.class)
				.baseModel(inputModel, branchModel)
				.addChild(n -> n.data().format(Format.PROPERTY).id("id"))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(sequenceModel);

		@SuppressWarnings("rawtypes")
		Model<BindingChildNode> repeatableModel = model
				.configure()
				.id("repeatable")
				.baseModel(nodeModel)
				.dataClass(BindingChildNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("occurances")
								.type(base.derivedTypes().rangeType())).create();
		modelSet.add(repeatableModel);

		@SuppressWarnings("rawtypes")
		Model<AbstractModel> abstractModelModel = model
				.configure()
				.id("abstractModel")
				.baseModel(branchModel)
				.dataClass(AbstractModel.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("abstract")
								.type(base.primitiveTypes().booleanType()).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("baseModel")
								.type(base.builtInTypes().referenceType()).optional(true))
				.addChild(
						o -> o.data().format(Format.PROPERTY).id("dataClass")
								.type(base.derivedTypes().classType()).optional(true))
				.addChild(
						o -> o.data().format(Format.PROPERTY).id("bindingStrategy")
								.type(base.builtInTypes().referenceType()).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("bindingClass")
								.type(base.derivedTypes().classType()))
				.addChild(
						o -> o.data().format(Format.PROPERTY).id("unbindingStrategy")
								.type(base.builtInTypes().referenceType()).optional(true))
				.addChild(
						o -> o.data().format(Format.PROPERTY).id("unbindingMethod")
								.outMethod("getUnbindingMethodName")
								.type(base.primitiveTypes().stringType()).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("unbindingClass")
								.type(base.derivedTypes().classType()))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(abstractModelModel);

		@SuppressWarnings("rawtypes")
		Model<Model> modelModel = model
				.configure()
				.id("model")
				.baseModel(abstractModelModel)
				.dataClass(Model.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("id").optional(false))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(modelModel);

		@SuppressWarnings("rawtypes")
		Model<ElementNode> elementModel = model
				.configure()
				.id("element")
				.dataClass(ElementNode.class)
				.bindingClass(ElementNodeConfigurator.class)
				.baseModel(dataModel, repeatableModel, abstractModelModel)
				.isAbstract(false)
				.addChild(n -> n.data().format(Format.PROPERTY).id("id"))
				.addChild(
						o -> o.data().format(Format.PROPERTY).id("dataClass")
								.type(base.derivedTypes().classType()))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(elementModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> typedDataModel = model
				.configure()
				.baseModel(dataModel)
				.id("typedData")
				.dataClass(DataNode.class)
				.bindingClass(DataNodeConfigurator.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("type").type(typeType))
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("format")
								.type(base.derivedTypes().enumType()))
				.addChild(
						n -> n.data().format(Format.SIMPLE_ELEMENT).id("value")
								.optional(true).type(base.builtInTypes().bufferedDataType()))
				.create();
		modelSet.add(typedDataModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> optionalModel = model
				.configure()
				.id("optional")
				.isAbstract(true)
				.baseModel(nodeModel)
				.dataClass(DataNode.class)
				.addChild(
						n -> n.data().format(Format.PROPERTY).id("optional")
								.type(base.primitiveTypes().booleanType())).create();
		modelSet.add(optionalModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> contentModel = model
				.configure()
				.id("content")
				.baseModel(typedDataModel, optionalModel)
				.isAbstract(false)
				.dataClass(DataNode.class)
				.bindingClass(DataNodeConfigurator.class)
				.addChild(
						n -> n.data().id("format")
								.value(new BufferingDataTargetImpl().string("Content").buffer()))
				.addChild(n -> n.data().id("id")).create();
		modelSet.add(contentModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> propertyModel = model
				.configure()
				.id("property")
				.isAbstract(false)
				.baseModel(typedDataModel, optionalModel)
				.dataClass(DataNode.class)
				.bindingClass(DataNodeConfigurator.class)
				.addChild(
						n -> n.data().id("format")
								.value(new BufferingDataTargetImpl().string("Property").buffer()))
				.addChild(n -> n.data().id("id")).create();
		modelSet.add(propertyModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> simpleElementModel = model
				.configure()
				.id("simpleElement")
				.isAbstract(false)
				.baseModel(typedDataModel, optionalModel)
				.dataClass(DataNode.class)
				.bindingClass(DataNodeConfigurator.class)
				.addChild(
						n -> n
								.data()
								.id("format")
								.value(
										new BufferingDataTargetImpl().string("Simple Element").buffer()))
				.addChild(n -> n.data().id("id")).create();
		modelSet.add(simpleElementModel);

		/* Type Models */

		@SuppressWarnings("rawtypes")
		Model<DataType> typeModel = model
				.configure()
				.dataClass(DataType.class)
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
				.configure()
				.id("models")
				.dataClass(Set.class)
				.addChild(
						n -> n.element().baseModel(modelModel).outMethodIterable(true)
								.outMethod("this").occurances(Range.create(0, null))).create();
		modelSet.add(modelsModel);

		schemaModel = model
				.configure()
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
												.outMethodIterable(true).dataClass(DataType.class)
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
}
