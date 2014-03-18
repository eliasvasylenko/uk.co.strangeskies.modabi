package uk.co.strangeskies.modabi.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.ModelLoader;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ContentNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.building.OptionalNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.TypedDataNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BranchingNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.ContentNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.InputNode;
import uk.co.strangeskies.modabi.model.nodes.OptionalNode;
import uk.co.strangeskies.modabi.model.nodes.PropertyNode;
import uk.co.strangeskies.modabi.model.nodes.RepeatableNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.model.nodes.TypedDataNode;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public class MetaSchemaImpl implements MetaSchema {
	private final Schema metaSchema;

	private final Model<Schema> schemaModel;

	@SuppressWarnings("unchecked")
	public MetaSchemaImpl(SchemaBuilder schema, ModelBuilder model,
			DataTypeBuilder dataType) {
		QualifiedName name = new QualifiedName(MetaSchemaImpl.class.getName(),
				new Namespace(MetaSchemaImpl.class.getPackage().getName()));

		/*
		 * Types
		 */
		Set<DataType<?>> typeSet = new HashSet<>();

		DataType<?> stringType = dataType.configure().name("string")
				.dataClass(String.class).create();
		typeSet.add(stringType);

		DataType<?> booleanType = dataType.configure().name("boolean")
				.dataClass(Boolean.class).create();
		typeSet.add(booleanType);

		DataType<?> classType = dataType.configure().name("class")
				.dataClass(Class.class).create();
		typeSet.add(classType);

		DataType<?> enumType = dataType.configure().name("enum")
				.dataClass(Enum.class).create();
		typeSet.add(enumType);

		DataType<?> rangeType = dataType.configure().name("range")
				.dataClass(Range.class).create();
		typeSet.add(rangeType);

		DataType<?> referenceType = dataType.configure().name("reference")
				.dataClass(Object.class).create();
		typeSet.add(referenceType);

		DataType<?> typeType = dataType.configure().name("type")
				.dataClass(DataType.class).create();
		typeSet.add(typeType);

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new LinkedHashSet<>();

		Model<Object> includeModel = model.configure().id("include")
				.builderClass(ModelLoader.class).dataClass(Object.class).create();
		modelSet.add(includeModel);

		@SuppressWarnings("rawtypes")
		Model<DataType> typeModel = model.configure().dataClass(DataType.class)
				.id("type").create();
		modelSet.add(typeModel);

		/*
		 * Node Models
		 */

		Model<SchemaNode> nodeModel = model.configure().id("node").isAbstract(true)
				.dataClass(SchemaNode.class)
				.addChild(n -> n.property().type(stringType).id("id").optional(true))
				.create();
		modelSet.add(nodeModel);

		Model<OptionalNode> optionalModel = model.configure().id("optional")
				.isAbstract(true).dataClass(OptionalNode.class)
				.builderClass(OptionalNodeConfigurator.class)
				.addChild(n -> n.property().id("optional").type(booleanType)).create();

		Model<InputNode> inputModel = model
				.configure()
				.id("input")
				.baseModel(nodeModel)
				.dataClass(InputNode.class)
				.addChild(
						n -> n.property().id("inMethod").outMethod("getInMethodName")
								.optional(true).type(stringType))
				.addChild(
						n -> n.property().id("inMethodChained").optional(true)
								.type(booleanType)).create();
		modelSet.add(inputModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> dataModel = model
				.configure()
				.id("data")
				.baseModel(inputModel)
				.dataClass(DataNode.class)
				.addChild(
						n -> n.property().id("dataClass").type(classType).optional(true))
				.addChild(
						n -> n.property().id("outMethod").outMethod("getOutMethodName")
								.optional(true).type(stringType))
				.addChild(
						n -> n.property().id("outMethodIterable").optional(true)
								.type(booleanType)).create();
		modelSet.add(dataModel);

		Model<BranchingNode> branchModel = model
				.configure()
				.id("branch")
				.baseModel(nodeModel)
				.dataClass(BranchingNode.class)
				.addChild(
						n -> n.element().id("child").outMethod("getChildren")
								.baseModel(nodeModel).outMethodIterable(true)
								.occurances(Range.create(0, null))).create();
		modelSet.add(branchModel);

		Model<ChoiceNode> choiceModel = model.configure().id("choice")
				.isAbstract(false).dataClass(ChoiceNode.class)
				.builderClass(ChoiceNodeConfigurator.class).baseModel(branchModel)
				.addChild(n -> n.property().id("mandatory").type(booleanType))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(choiceModel);

		Model<SequenceNode> sequenceModel = model.configure().id("sequence")
				.isAbstract(false).dataClass(SequenceNode.class)
				.builderClass(SequenceNodeConfigurator.class)
				.baseModel(inputModel, branchModel)
				.addChild(n -> n.property().id("id"))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(sequenceModel);

		Model<RepeatableNode> repeatableModel = model.configure().id("repeatable")
				.baseModel(nodeModel).dataClass(RepeatableNode.class)
				.addChild(n -> n.property().id("occurances").type(rangeType)).create();
		modelSet.add(repeatableModel);

		@SuppressWarnings("rawtypes")
		Model<AbstractModel> abstractModelModel = model
				.configure()
				.id("abstractModel")
				.baseModel(branchModel)
				.dataClass(AbstractModel.class)
				.addChild(
						n -> n.property().id("abstract").type(booleanType).optional(true))
				.addChild(
						n -> n.property().id("baseModel").type(referenceType)
								.optional(true))
				.addChild(
						o -> o.property().id("dataClass").type(classType).optional(true))
				.addChild(
						o -> o.property().id("implementationStrategy").type(enumType)
								.optional(true))
				.addChild(n -> n.property().id("builderClass").type(classType))
				.addChild(
						n -> n.property().id("builderMethod").type(stringType)
								.optional(true)).addChild(n -> n.element().id("child"))
				.create();
		modelSet.add(abstractModelModel);

		@SuppressWarnings("rawtypes")
		Model<Model> modelModel = model.configure().id("model")
				.baseModel(abstractModelModel).dataClass(Model.class)
				.addChild(n -> n.property().id("id").optional(false))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(abstractModelModel);

		@SuppressWarnings("rawtypes")
		Model<ElementNode> elementModel = model.configure().id("element")
				.dataClass(ElementNode.class)
				.builderClass(ElementNodeConfigurator.class)
				.baseModel(dataModel, repeatableModel, abstractModelModel)
				.isAbstract(false).addChild(n -> n.property().id("id"))
				.addChild(o -> o.property().id("dataClass").type(classType))
				.addChild(n -> n.element().id("child")).create();
		modelSet.add(elementModel);

		@SuppressWarnings("rawtypes")
		Model<TypedDataNode> typedDataModel = model.configure()
				.baseModel(dataModel).id("typedData").dataClass(TypedDataNode.class)
				.builderClass(TypedDataNodeConfigurator.class)
				.addChild(n -> n.property().id("type").type(typeType))
				.addChild(n -> n.simpleElement().id("value").type(referenceType))
				.create();

		@SuppressWarnings("rawtypes")
		Model<ContentNode> contentModel = model.configure().id("content")
				.dataClass(ContentNode.class).baseModel(typedDataModel, optionalModel)
				.builderClass(ContentNodeConfigurator.class).create();
		modelSet.add(contentModel);

		@SuppressWarnings("rawtypes")
		Model<PropertyNode> propertyModel = model.configure().id("property")
				.isAbstract(false).dataClass(PropertyNode.class)
				.baseModel(typedDataModel, optionalModel)
				.builderClass(PropertyNodeConfigurator.class)
				.addChild(n -> n.property().id("id")).create();
		modelSet.add(propertyModel);

		/*
		 * Schema Models
		 */
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
				.builderClass(SchemaConfigurator.class)
				.addChild(
						n -> n
								.element()
								.id("dependencies")
								.occurances(Range.create(0, 1))
								.dataClass(Set.class)
								.addChild(
										o -> o.element().id("dependency").baseModel(includeModel)
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
