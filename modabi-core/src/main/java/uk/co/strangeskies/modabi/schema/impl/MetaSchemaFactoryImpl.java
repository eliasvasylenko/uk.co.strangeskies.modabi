package uk.co.strangeskies.modabi.schema.impl;

import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.MetaSchemaFactory;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.data.DataType;
import uk.co.strangeskies.modabi.schema.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.schema.data.impl.DataTypeBuilderImpl;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.BranchingNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.PropertyNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.node.builder.BindingNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.BranchNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.BranchingNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.DataNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.PropertyNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.schema.node.builder.impl.SchemaNodeBuilderFactoryImpl;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class MetaSchemaFactoryImpl implements
		MetaSchemaFactory<SchemaProcessingContext<?>, SchemaProcessingContext<?>> {
	private SchemaNodeBuilderFactory node() {
		return new SchemaNodeBuilderFactoryImpl();
	}

	private SchemaBuilder<Object, ? extends SchemaProcessingContext<?>> schema() {
		return new BindingSchemaBuilderImpl<>();
	}

	private DataTypeBuilder<Object> dataType() {
		return new DataTypeBuilderImpl<>();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Schema<Schema<?, SchemaProcessingContext<?>>, SchemaProcessingContext<?>> create(
			SchemaNodeBuilderFactory factory) {
		/*
		 * Types
		 */
		Set<DataType<?>> typeSet = new HashSet<>();

		DataType<String> stringType = dataType().dataClass(String.class).create();
		typeSet.add(stringType);

		DataType<Boolean> booleanType = dataType().dataClass(Boolean.class)
				.create();
		typeSet.add(booleanType);

		DataType<Class> classType = null;
		typeSet.add(classType);

		DataType<Range> rangeType = null;
		typeSet.add(rangeType);

		DataType<BindingNode> referenceType = null;
		typeSet.add(referenceType);

		/*
		 * Models
		 */
		Set<BindingNode<?, SchemaProcessingContext<?>>> modelSet = new HashSet<>();

		BindingNode<Object, SchemaProcessingContext<?>> includeModel = node()
				.element().name("include").create();
		modelSet.add(includeModel);

		BindingNode<DataType, SchemaProcessingContext<?>> typeModel = node()
				.element().dataClass(DataType.class).name("type").create();
		modelSet.add(typeModel);

		BindingNode<SchemaNode, SchemaProcessingContext<?>> nodeModel = node()
				.element().name("node").dataClass(SchemaNode.class).create();
		modelSet.add(nodeModel);

		BindingNode<BranchingNode, SchemaProcessingContext<?>> branchModel = node()
				.element()
				.name("branch")
				.base(nodeModel)
				.dataClass(BranchingNode.class)
				.factoryClass(BranchingNodeBuilder.class)
				.addChild(
						node().element().name("children").inMethod("addChild")
								.occurances(Range.create(1, null)).create())
				.addChild(node().property().name("choice").type(booleanType).create())
				.addChild(node().property().name("inMethod").create()).create();
		modelSet.add(branchModel);

		BindingNode<SequenceNode, SchemaProcessingContext<?>> sequenceModel = node()
				.element().name("sequence").base(branchModel)
				.dataClass(SequenceNode.class).factoryClass(BranchNodeBuilder.class)
				.addChild(node().property().name("choice").data(false).create())
				.create();
		modelSet.add(sequenceModel);

		BindingNode<SequenceNode, SchemaProcessingContext<?>> choiceModel = node()
				.element().name("choice").base(sequenceModel)
				.addChild(node().property().name("choice").data(true).create())
				.create();
		modelSet.add(choiceModel);

		BindingNode<BindingNode, SchemaProcessingContext<?>> modelModel = node()
				.element()
				.name("model")
				.base(branchModel)
				.dataClass(BindingNode.class)
				.factoryClass(BindingNodeBuilder.class)
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

		BindingNode<BindingNode, SchemaProcessingContext<?>> elementModel = node()
				.element().name("element").base(modelModel).create();
		modelSet.add(elementModel);

		BindingNode<DataNode, SchemaProcessingContext<?>> dataModel = node()
				.element()
				.name("data")
				.base(nodeModel)
				.dataClass(DataNode.class)
				.factoryClass(DataNodeBuilder.class)
				.addChild(node().property().name("type").data(true).create())
				.addChild(
						node().property().name("optional").optional(true).type(booleanType)
								.create()).addChild(node().data().optional(true).create())
				.create();
		modelSet.add(dataModel);

		BindingNode<PropertyNode, SchemaProcessingContext<?>> propertyModel = node()
				.element().name("property").base(dataModel)
				.dataClass(PropertyNode.class).factoryClass(PropertyNodeBuilder.class)
				.addChild(node().property().name("name").create()).create();
		modelSet.add(propertyModel);

		BindingNode<Set, SchemaProcessingContext<?>> includesModel = node()
				.element()
				.name("includes")
				.occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(
						node().element().base(includeModel)
								.occurances(Range.create(0, null)).create()).create();

		BindingNode<Set, SchemaProcessingContext<?>> typesModel = node()
				.element()
				.name("types")
				.occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(
						node().element().base(typeModel).occurances(Range.create(0, null))
								.create()).create();

		BindingNode<Set, SchemaProcessingContext<?>> modelsModel = node()
				.element()
				.name("models")
				.occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(
						node().element().base(modelModel).occurances(Range.create(0, null))
								.create()).create();

		BindingNode<Set, SchemaProcessingContext<?>> rootModel = node().element()
				.name("models").occurances(Range.create(0, 1)).dataClass(Set.class)
				.addChild(node().element().occurances(Range.create(0, null)).create())
				.create();

		BindingNode<Schema, SchemaProcessingContext<?>> scehmaModel = node()
				.element().name("modelSchema").dataClass(Schema.class)
				.factoryClass(SchemaBuilder.class).addChild(includesModel)
				.addChild(typesModel).addChild(modelsModel).addChild(rootModel)
				.create();

		/*
		 * Schema
		 */

		Schema<? extends Schema<?, SchemaProcessingContext<?>>, SchemaProcessingContext<?>> schema = (Schema<? extends Schema<?, SchemaProcessingContext<?>>, SchemaProcessingContext<?>>) schema()
				.types(typeSet).models(modelSet).root(scehmaModel).create();

		return (Schema<Schema<?, SchemaProcessingContext<?>>, SchemaProcessingContext<?>>) schema;
	}
}
