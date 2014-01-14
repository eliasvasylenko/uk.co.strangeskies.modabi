package uk.co.strangeskies.modabi.impl;

import java.util.HashSet;
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
import uk.co.strangeskies.modabi.data.DataTypeConfigurator;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.model.BranchingNode;
import uk.co.strangeskies.modabi.model.ChoiceNode;
import uk.co.strangeskies.modabi.model.ContentNode;
import uk.co.strangeskies.modabi.model.DataNode;
import uk.co.strangeskies.modabi.model.ElementNode;
import uk.co.strangeskies.modabi.model.InputNode;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.OptionalNode;
import uk.co.strangeskies.modabi.model.PropertyNode;
import uk.co.strangeskies.modabi.model.RepeatableNode;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.TypedDataNode;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ContentNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.building.ModelConfigurator;
import uk.co.strangeskies.modabi.model.building.OptionalNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.TypedDataNodeConfigurator;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public class MetaSchemaImpl implements MetaSchema {
	private Schema component;

	private ModelBuilder modelBuilder;
	private SchemaBuilder schemaBuilder;
	private DataTypeBuilder dataTypeBuilder;

	private Model<Schema> schemaModel;

	public void setModelBuilder(ModelBuilder modelBuilder) {
		this.modelBuilder = modelBuilder;
	}

	public void setSchemaBuilder(SchemaBuilder schemaBuilder) {
		this.schemaBuilder = schemaBuilder;
	}

	@Override
	public void setDataTypeBuilder(DataTypeBuilder dataTypeBuilder) {
		this.dataTypeBuilder = dataTypeBuilder;
	}

	@Override
	public QualifiedName getQualifiedName() {
		return component.getQualifiedName();
	}

	@Override
	public Schemata getDependencies() {
		return component.getDependencies();
	}

	@Override
	public DataTypes getDataTypes() {
		return component.getDataTypes();
	}

	@Override
	public final Models getModels() {
		return component.getModels();
	}

	@Override
	public Model<Schema> getSchemaModel() {
		return schemaModel;
	}

	private ModelConfigurator<Object> model() {
		return modelBuilder.configure();
	}

	private SchemaConfigurator schema() {
		return schemaBuilder.configure();
	}

	private DataTypeConfigurator<Object> dataType() {
		return dataTypeBuilder.configure();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialise() {
		QualifiedName name = new QualifiedName(MetaSchema.class.getName(),
				new Namespace(MetaSchema.class.getPackage().getName()));

		/*
		 * Types
		 */
		Set<DataType<?>> typeSet = new HashSet<>();

		DataType<?> stringType = dataType().name("string").dataClass(String.class)
				.create();
		typeSet.add(stringType);

		DataType<?> booleanType = dataType().name("boolean")
				.dataClass(Boolean.class).create();
		typeSet.add(booleanType);

		DataType<?> classType = dataType().name("class").dataClass(Class.class)
				.create();
		typeSet.add(classType);

		DataType<?> rangeType = dataType().name("range").dataClass(Range.class)
				.create();
		typeSet.add(rangeType);

		DataType<?> referenceType = dataType().name("reference").create();
		typeSet.add(referenceType);

		DataType<?> typeType = dataType().name("type").dataClass(DataType.class)
				.create();
		typeSet.add(referenceType);

		/*
		 * Models
		 */
		Set<Model<?>> modelSet = new HashSet<>();

		Model<Object> includeModel = model().id("include")
				.builderClass(ModelLoader.class).isAbstract(true).create();
		modelSet.add(includeModel);

		@SuppressWarnings("rawtypes")
		Model<DataType> typeModel = model().dataClass(DataType.class).id("type")
				.create();
		modelSet.add(typeModel);

		/*
		 * Node Models
		 */

		Model<SchemaNode> nodeModel = model().id("node").isAbstract(true)
				.dataClass(SchemaNode.class)
				.addChild(n -> n.property().id("id").optional(true)).create();
		modelSet.add(nodeModel);

		Model<OptionalNode> optionalModel = model().id("optional").isAbstract(true)
				.dataClass(OptionalNode.class)
				.builderClass(OptionalNodeConfigurator.class)
				.addChild(n -> n.property().id("optional").type(booleanType)).create();

		Model<InputNode> inputModel = model()
				.id("input")
				.baseModel(nodeModel)
				.dataClass(InputNode.class)
				.addChild(
						n -> n.property().id("inMethod").optional(true).type(stringType))
				.addChild(
						n -> n.property().id("inMethodChained").optional(true)
								.type(booleanType)).create();
		modelSet.add(inputModel);

		@SuppressWarnings("rawtypes")
		Model<DataNode> dataModel = model()
				.id("data")
				.baseModel(inputModel)
				.dataClass(DataNode.class)
				.addChild(
						n -> n.property().id("dataClass").type(classType).optional(true))
				.addChild(
						n -> n.property().id("outMethod").optional(true).type(stringType))
				.addChild(
						n -> n.property().id("outMethodIterable").optional(true)
								.type(booleanType)).create();
		modelSet.add(dataModel);

		Model<BranchingNode> branchModel = model()
				.id("branch")
				.baseModel(nodeModel)
				.dataClass(BranchingNode.class)
				.addChild(
						n -> n.element().id("child").outMethod("getChildren")
								.baseModel(nodeModel).outMethodIterable(true)
								.occurances(Range.create(0, null))).create();
		modelSet.add(branchModel);

		Model<ChoiceNode> choiceModel = model()
				.id("choice")
				.isAbstract(false)
				.dataClass(ChoiceNode.class)
				.builderClass(ChoiceNodeConfigurator.class)
				.baseModel(branchModel)
				.addChild(n -> n.property().id("mandatory").type(booleanType))
				.addChild(
						n -> n.element().id("child").occurances(Range.create(2, null)))
				.create();
		modelSet.add(choiceModel);

		Model<SequenceNode> sequenceModel = model().id("sequence")
				.isAbstract(false).dataClass(SequenceNode.class)
				.builderClass(SequenceNodeConfigurator.class)
				.baseModel(inputModel, branchModel).create();
		modelSet.add(sequenceModel);

		Model<RepeatableNode> repeatableModel = model().id("repeatable")
				.isAbstract(false).baseModel(nodeModel).dataClass(RepeatableNode.class)
				.create();
		modelSet.add(repeatableModel);

		@SuppressWarnings("rawtypes")
		Model<Model> modelModel = model()
				.id("model")
				.baseModel(branchModel)
				.dataClass(Model.class)
				.builderClass(ModelConfigurator.class)
				.addChild(n -> n.property().id("id").optional(false))
				.addChild(
						n -> n.property().id("abstract").type(booleanType).optional(true))
				.addChild(
						n -> n.choice().id("model").mandatory(false)
								.addChild(o -> o.property().id("dataClass").type(classType))
								.addChild(o -> o.property().id("bindingStyle").type(null) // TODO
										.optional(true)))
				.addChild(n -> n.property().id("builderClass").type(classType))
				.addChild(
						n -> n.property().id("builderMethod").type(stringType)
								.optional(true)).create();
		modelSet.add(modelModel);

		@SuppressWarnings("rawtypes")
		Model<ElementNode> elementModel = model().id("element")
				.dataClass(ElementNode.class)
				.builderClass(ElementNodeConfigurator.class)
				.baseModel(modelModel, dataModel, repeatableModel).isAbstract(false)
				.create();
		modelSet.add(elementModel);

		@SuppressWarnings("rawtypes")
		Model<TypedDataNode> typedDataModel = model().baseModel(dataModel)
				.id("typedData").dataClass(TypedDataNode.class)
				.builderClass(TypedDataNodeConfigurator.class)
				.addChild(n -> n.property().id("type").type(typeType))
				.addChild(n -> n.simpleElement().id("value").type(null) // TODO
				).create();

		@SuppressWarnings("rawtypes")
		Model<ContentNode> contentModel = model().id("content")
				.dataClass(ContentNode.class).baseModel(typedDataModel, optionalModel)
				.builderClass(ContentNodeConfigurator.class).create();

		@SuppressWarnings("rawtypes")
		Model<PropertyNode> propertyModel = model().id("property")
				.baseModel(contentModel).dataClass(PropertyNode.class)
				.builderClass(PropertyNodeConfigurator.class)
				.addChild(n -> n.property().id("id")).create();
		modelSet.add(propertyModel);

		/*
		 * Schema Models
		 */
		@SuppressWarnings("rawtypes")
		Model<Set> modelsModel = model()
				.id("models")
				.dataClass(Set.class)
				.addChild(
						n -> n.element().baseModel(modelModel)
								.occurances(Range.create(0, null))).create();
		modelSet.add(modelsModel);

		schemaModel = model()
				.id("modelSchema")
				.dataClass(Schema.class)
				.builderClass(SchemaConfigurator.class)
				.addChild(
						n -> n
								.element()
								.id("dependencies")
								.occurances(Range.create(0, 1))
								.addChild(
										o -> o.element().id("dependency").baseModel(includeModel)
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

		component = schema().qualifiedName(name).types(typeSet).models(modelSet)
				.create();
	}
}
