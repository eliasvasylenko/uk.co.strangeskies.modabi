package uk.co.strangeskies.modabi.schema.impl;

import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.BindingSchema;
import uk.co.strangeskies.modabi.schema.BindingSchemaBuilder;
import uk.co.strangeskies.modabi.schema.MetaSchemaFactory;
import uk.co.strangeskies.modabi.schema.node.BranchSchemaNode;
import uk.co.strangeskies.modabi.schema.node.BranchingSchemaNode;
import uk.co.strangeskies.modabi.schema.node.DataSchemaNode;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.PropertySchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.builder.BranchSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.BranchingSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.DataSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.ElementSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.PropertySchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.schema.node.builder.impl.SchemaNodeBuilderFactoryImpl;
import uk.co.strangeskies.modabi.schema.node.data.SchemaDataType;
import uk.co.strangeskies.modabi.schema.node.data.SchemaDataTypeBuilder;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class MetaSchemaFactoryImpl implements
		MetaSchemaFactory<SchemaProcessingContext<?>, SchemaProcessingContext<?>> {
	private SchemaNodeBuilderFactory node() {
		return new SchemaNodeBuilderFactoryImpl();
	}

	private BindingSchemaBuilder<Object, ? extends SchemaProcessingContext<?>> schema() {
		return new BindingSchemaBuilderImpl<>();
	}

	private SchemaDataTypeBuilder<Object> dataType() {
		return new SchemaDataTypeBuilderImpl<>();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public BindingSchema<BindingSchema<?, SchemaProcessingContext<?>>, SchemaProcessingContext<?>> create(
			SchemaNodeBuilderFactory factory) {
		/*
		 * Types
		 */
		Set<SchemaDataType<?>> typeSet = new HashSet<>();

		/*
		 * Models
		 */
		Set<ElementSchemaNode<?, SchemaProcessingContext<?>>> modelSet = new HashSet<>();

		ElementSchemaNode<Object, SchemaProcessingContext<?>> includeModel = node()
				.element().name("include").create();
		modelSet.add(includeModel);

		ElementSchemaNode<SchemaDataType, SchemaProcessingContext<?>> typeModel = node()
				.element().dataClass(SchemaDataType.class).name("type").create();
		modelSet.add(typeModel);

		ElementSchemaNode<SchemaNode, SchemaProcessingContext<?>> nodeModel = node()
				.element().name("node").dataClass(SchemaNode.class).create();
		modelSet.add(nodeModel);

		SchemaDataType<Boolean> booleanType = new SchemaDataTypeBuilder();
		SchemaDataType<Class> classType = null;
		SchemaDataType<Range> rangeType = null;
		SchemaDataType<ElementSchemaNode> referenceType = null;

		ElementSchemaNode<BranchingSchemaNode, SchemaProcessingContext<?>> branchModel = node()
				.element()
				.name("branch")
				.base(nodeModel)
				.dataClass(BranchingSchemaNode.class)
				.factoryClass(BranchingSchemaNodeBuilder.class)
				.addChild(
						node().element().name("children").inMethod("addChild")
								.occurances(Range.create(1, null)).create())
				.addChild(node().property().name("choice").type(booleanType).create())
				.addChild(node().property().name("inMethod").create()).create();
		modelSet.add(branchModel);

		ElementSchemaNode<BranchSchemaNode, SchemaProcessingContext<?>> sequenceModel = node()
				.element().name("sequence").base(branchModel)
				.dataClass(BranchSchemaNode.class)
				.factoryClass(BranchSchemaNodeBuilder.class)
				.addChild(node().property().name("choice").data(false).create())
				.create();
		modelSet.add(sequenceModel);

		ElementSchemaNode<BranchSchemaNode, SchemaProcessingContext<?>> choiceModel = node()
				.element().name("choice").base(sequenceModel)
				.addChild(node().property().name("choice").data(true).create())
				.create();
		modelSet.add(choiceModel);

		ElementSchemaNode<ElementSchemaNode, SchemaProcessingContext<?>> modelModel = node()
				.element()
				.name("model")
				.base(branchModel)
				.dataClass(ElementSchemaNode.class)
				.factoryClass(ElementSchemaNodeBuilder.class)
				.addChild(node().property().name("choice").data(false).create())
				.addChild(node().property().name("name").optional(true).create())
				.addChild(
						node().property().name("occurances").type(rangeType).optional(true)
								.create())
				.addChild(
						node().property().name("dataClass").type(classType).optional(true)
								.create())
				.addChild(
						node().property().name("factoryClass").type(classType)
								.optional(true).create())
				.addChild(node().property().name("outMethod").optional(true).create())
				.addChild(
						node().property().name("iterable").type(booleanType).optional(true)
								.create())
				.addChild(node().property().name("buildMethod").optional(true).create())
				.addChild(
						node().property().name("base").type(referenceType).optional(true)
								.create()).create();
		modelSet.add(modelModel);

		ElementSchemaNode<ElementSchemaNode, SchemaProcessingContext<?>> elementModel = node()
				.element().name("element").base(modelModel).create();
		modelSet.add(elementModel);

		ElementSchemaNode<DataSchemaNode, SchemaProcessingContext<?>> dataModel = node()
				.element()
				.name("data")
				.base(nodeModel)
				.dataClass(DataSchemaNode.class)
				.factoryClass(DataSchemaNodeBuilder.class)
				.addChild(node().property().name("type").data(true).create())
				.addChild(
						node().property().name("optional").optional(true).type(booleanType)
								.create()).addChild(node().data().optional(true).create())
				.create();
		modelSet.add(dataModel);

		ElementSchemaNode<PropertySchemaNode, SchemaProcessingContext<?>> propertyModel = node()
				.element().name("property").base(dataModel)
				.dataClass(PropertySchemaNode.class)
				.factoryClass(PropertySchemaNodeBuilder.class)
				.addChild(node().property().name("name").create()).create();
		modelSet.add(propertyModel);

		ElementSchemaNode<Set, SchemaProcessingContext<?>> includesModel = node()
				.element()
				.name("includes")
				.occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(
						node().element().base(includeModel)
								.occurances(Range.create(0, null)).create()).create();

		ElementSchemaNode<Set, SchemaProcessingContext<?>> typesModel = node()
				.element()
				.name("types")
				.occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(
						node().element().base(typeModel).occurances(Range.create(0, null))
								.create()).create();

		ElementSchemaNode<Set, SchemaProcessingContext<?>> modelsModel = node()
				.element()
				.name("models")
				.occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(
						node().element().base(modelModel).occurances(Range.create(0, null))
								.create()).create();

		ElementSchemaNode<Set, SchemaProcessingContext<?>> rootModel = node()
				.element().name("models").occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(node().element().occurances(Range.create(0, null)).create())
				.create();

		ElementSchemaNode<BindingSchema, SchemaProcessingContext<?>> scehmaModel = node()
				.element().name("modelSchema").dataClass(BindingSchema.class)
				.factoryClass(BindingSchemaBuilder.class).addChild(includesModel)
				.addChild(typesModel).addChild(modelsModel).addChild(rootModel)
				.create();

		/*
		 * Schema
		 */

		BindingSchema<? extends BindingSchema<?, SchemaProcessingContext<?>>, SchemaProcessingContext<?>> schema = (BindingSchema<? extends BindingSchema<?, SchemaProcessingContext<?>>, SchemaProcessingContext<?>>) schema()
				.types(typeSet).models(modelSet).root(scehmaModel).create();

		return (BindingSchema<BindingSchema<?, SchemaProcessingContext<?>>, SchemaProcessingContext<?>>) schema;
	}
}
