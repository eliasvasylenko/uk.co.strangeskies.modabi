package uk.co.strangeskies.modabi.schema.processing.impl.schemata;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.data.DataBindingTypeConfigurator;
import uk.co.strangeskies.modabi.data.DataBindingTypes;
import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.SchemaConfigurator;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.model.AbstractModel;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.Models;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.schema.model.building.configurators.AbstractModelConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.InputNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;
import uk.co.strangeskies.modabi.schema.requirement.Requirement;
import uk.co.strangeskies.modabi.schema.requirement.Requirements;
import uk.co.strangeskies.modabi.schema.requirement.impl.RequirementBuilderImpl;

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
		 * Requirements
		 */
		Set<Requirement<?>> requirementSet = new LinkedHashSet<>();

		requirementSet.add(new RequirementBuilderImpl()
				.configure(new QualifiedName("schemaConfigurator", namespace),
						SchemaBuilder.class)
				.addProvision(c -> c, SchemaConfigurator.class, "configure").create());

		requirementSet.add(new RequirementBuilderImpl()
				.configure(new QualifiedName("dataTypeConfigurator", namespace),
						DataBindingTypeBuilder.class)
				.addProvision(c -> c, DataBindingTypeConfigurator.class, "configure",
						DataLoader.class).create());

		requirementSet.add(new RequirementBuilderImpl()
				.configure(new QualifiedName("modelBuilder", namespace),
						ModelBuilder.class)
				.addProvision(c -> c, ModelConfigurator.class, "configure",
						DataLoader.class).create());

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
								.postInputClass(SchemaNodeConfigurator.class))
				.addChild(
						n -> n.data().format(Format.PROPERTY)
								.type(base.primitiveType(DataType.QUALIFIED_NAME)).name("name")
								.optional(true)).create();
		modelSet.add(nodeModel);

		Model<SchemaNode> branchModel = model
				.configure(loader)
				.name("branch", namespace)
				.isAbstract(true)
				.baseModel(nodeModel)
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.element().name("child").outMethod("children")
								.inMethod("null").extensible(true).baseModel(nodeModel)
								.bindingStrategy(BindingStrategy.TARGET_ADAPTOR)
								.bindingClass(SchemaNodeConfigurator.class)
								.dataClass(ChildNode.class).outMethodIterable(true)
								.occurances(Range.create(0, null)))
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
				.bindingClass(SchemaNodeConfigurator.class)
				.addChild(c -> c.inputSequence().name("addChild").inMethodChained(true))
				.addChild(
						c -> c.inputSequence().name("configure").isAbstract(true)
								.inMethodChained(true)
								.postInputClass(ChildNodeConfigurator.class)).create();

		Model<InputNode> inputModel = model
				.configure(loader)
				.name("input", namespace)
				.isAbstract(true)
				.baseModel(childModel)
				.dataClass(InputNode.class)
				.addChild(
						c -> c.inputSequence().name("configure").isAbstract(true)
								.postInputClass(InputNodeConfigurator.class))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethod")
								.outMethod("getInMethodName").optional(true)
								.type(base.primitiveType(DataType.STRING)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("inMethodChained")
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
								.postInputClass(BindingNodeConfigurator.class))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("abstract")
								.inMethod("isAbstract")
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
								.postInputClass(BindingChildNodeConfigurator.class))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("extensible")
								.type(base.primitiveType(DataType.BOOLEAN)).optional(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("ordered")
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

		Model<AbstractModel> abstractModelModel = model
				.configure(loader)
				.name("abstractModel", namespace)
				.baseModel(bindingNodeModel)
				.isAbstract(true)
				.dataClass(AbstractModel.class)
				.addChild(
						c -> c.inputSequence().name("configure").isAbstract(true)
								.postInputClass(AbstractModelConfigurator.class))
				.addChild(n -> n.data().name("name"))
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
				.bindingClass(ModelBuilder.class)
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

		Model<ElementNode> elementModel = model
				.configure(loader)
				.name("element", namespace)
				.dataClass(ElementNode.class)
				.baseModel(bindingChildNodeModel, abstractModelModel)
				.addChild(c -> c.inputSequence().name("addChild"))
				.addChild(
						c -> c.inputSequence().name("configure").inMethod("element")
								.inMethodChained(true))
				.addChild(n -> n.data().name("name"))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("occurances")
								.type(base.derivedTypes().rangeType()))
				.addChild(o -> o.data().name("dataClass")).create();
		modelSet.add(elementModel);

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
												.optional(true)
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
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("optional")
								.optional(true).type(base.primitiveType(DataType.BOOLEAN)))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("format").optional(true)
								.valueResolution(ValueResolution.REGISTRATION_TIME)
								.inMethodChained(false).isAbstract(true)
								.type(base.derivedTypes().enumType()).dataClass(Format.class))
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
								.provideValue(
										new BufferingDataTarget().put(DataType.STRING,
												"SIMPLE_ELEMENT").buffer())).create();
		modelSet.add(simpleElementModel);

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
				.bindingClass(DataBindingTypeBuilder.class)
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
				.name("schema", namespace)
				.dataClass(Schema.class)
				.bindingClass(SchemaBuilder.class)
				.addChild(
						c -> c.inputSequence().name("configure").inMethodChained(true))
				.addChild(
						n -> n.data().format(Format.PROPERTY).name("name")
								.inMethod("qualifiedName").outMethod("getQualifiedName")
								.type(base.primitiveType(DataType.QUALIFIED_NAME)))
				.addChild(
						n -> n
								.element()
								.name("dependencies")
								.occurances(Range.create(0, 1))
								.dataClass(Set.class)
								.addChild(
										o -> o
												.data()
												.format(Format.SIMPLE_ELEMENT)
												.inMethod("add")
												.name("dependency")
												.type(base.derivedTypes().importType())
												.dataClass(Schema.class)
												.outMethodIterable(true)
												.outMethod("this")
												.occurances(Range.create(0, null))
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
																.outMethodIterable(true)
																.inMethod("null")
																.type(base.derivedTypes().includeType())
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
																.outMethodIterable(true)
																.inMethod("null")
																.type(base.derivedTypes().includeType())
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
						n -> n
								.element()
								.name("models")
								.occurances(Range.create(0, 1))
								.dataClass(Set.class)
								.addChild(
										o -> o.element().baseModel(modelModel)
												.outMethodIterable(true).outMethod("this")
												.occurances(Range.create(0, null))))
				.addChild(n -> n.inputSequence().name("create").inMethodChained(true))
				.create();
		modelSet.add(schemaModel);

		/*
		 * Schema
		 */
		metaSchema = schema.configure().qualifiedName(name)
				.requirements(requirementSet).dependencies(Arrays.asList(base))
				.types(typeSet).models(modelSet).create();
	}

	@Override
	public QualifiedName getQualifiedName() {
		return metaSchema.getQualifiedName();
	}

	@Override
	public Requirements getRequirements() {
		return metaSchema.getRequirements();
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
