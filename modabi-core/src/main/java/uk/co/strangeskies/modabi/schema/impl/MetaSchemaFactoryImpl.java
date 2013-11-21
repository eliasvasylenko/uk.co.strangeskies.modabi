package uk.co.strangeskies.modabi.schema.impl;

import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.MetaSchemaFactory;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.node.BranchingNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.PropertyNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.builder.BranchNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.BranchingNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.DataNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.BindingNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.PropertyNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.schema.node.builder.impl.SchemaNodeBuilderFactoryImpl;
import uk.co.strangeskies.modabi.schema.node.data.DataType;
import uk.co.strangeskies.modabi.schema.node.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.schema.processing.DataInput;

public class MetaSchemaFactoryImpl implements
		MetaSchemaFactory<DataInput<?>, DataInput<?>> {
	private SchemaNodeBuilderFactory node() {
		return new SchemaNodeBuilderFactoryImpl();
	}

	private SchemaBuilder<Object, ? extends DataInput<?>> schema() {
		return new BindingSchemaBuilderImpl<>();
	}

	private DataTypeBuilder<Object> dataType() {
		return new SchemaDataTypeBuilderImpl<>();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Schema<Schema<?, DataInput<?>>, DataInput<?>> create(
			SchemaNodeBuilderFactory factory) {
		/*
		 * Types
		 */
		Set<DataType<?>> typeSet = new HashSet<>();

		/*
		 * Models
		 */
		Set<BindingNode<?, DataInput<?>>> modelSet = new HashSet<>();

		BindingNode<Object, DataInput<?>> includeModel = node()
				.element().name("include").create();
		modelSet.add(includeModel);

		BindingNode<DataType, DataInput<?>> typeModel = node()
				.element().dataClass(DataType.class).name("type").create();
		modelSet.add(typeModel);

		BindingNode<SchemaNode, DataInput<?>> nodeModel = node()
				.element().name("node").dataClass(SchemaNode.class).create();
		modelSet.add(nodeModel);

		DataType<Boolean> booleanType = new DataTypeBuilder();
		DataType<Class> classType = null;
		DataType<Range> rangeType = null;
		DataType<BindingNode> referenceType = null;

		BindingNode<BranchingNode, DataInput<?>> branchModel = node()
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

		BindingNode<SequenceNode, DataInput<?>> sequenceModel = node()
				.element().name("sequence").base(branchModel)
				.dataClass(SequenceNode.class)
				.factoryClass(BranchNodeBuilder.class)
				.addChild(node().property().name("choice").data(false).create())
				.create();
		modelSet.add(sequenceModel);

		BindingNode<SequenceNode, DataInput<?>> choiceModel = node()
				.element().name("choice").base(sequenceModel)
				.addChild(node().property().name("choice").data(true).create())
				.create();
		modelSet.add(choiceModel);

		BindingNode<BindingNode, DataInput<?>> modelModel = node()
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

		BindingNode<BindingNode, DataInput<?>> elementModel = node()
				.element().name("element").base(modelModel).create();
		modelSet.add(elementModel);

		BindingNode<DataNode, DataInput<?>> dataModel = node()
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

		BindingNode<PropertyNode, DataInput<?>> propertyModel = node()
				.element().name("property").base(dataModel)
				.dataClass(PropertyNode.class)
				.factoryClass(PropertyNodeBuilder.class)
				.addChild(node().property().name("name").create()).create();
		modelSet.add(propertyModel);

		BindingNode<Set, DataInput<?>> includesModel = node()
				.element()
				.name("includes")
				.occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(
						node().element().base(includeModel)
								.occurances(Range.create(0, null)).create()).create();

		BindingNode<Set, DataInput<?>> typesModel = node()
				.element()
				.name("types")
				.occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(
						node().element().base(typeModel).occurances(Range.create(0, null))
								.create()).create();

		BindingNode<Set, DataInput<?>> modelsModel = node()
				.element()
				.name("models")
				.occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(
						node().element().base(modelModel).occurances(Range.create(0, null))
								.create()).create();

		BindingNode<Set, DataInput<?>> rootModel = node()
				.element().name("models").occurances(Range.create(0, 1))
				.dataClass(Set.class)
				.addChild(node().element().occurances(Range.create(0, null)).create())
				.create();

		BindingNode<Schema, DataInput<?>> scehmaModel = node()
				.element().name("modelSchema").dataClass(Schema.class)
				.factoryClass(SchemaBuilder.class).addChild(includesModel)
				.addChild(typesModel).addChild(modelsModel).addChild(rootModel)
				.create();

		/*
		 * Schema
		 */

		Schema<? extends Schema<?, DataInput<?>>, DataInput<?>> schema = (Schema<? extends Schema<?, DataInput<?>>, DataInput<?>>) schema()
				.types(typeSet).models(modelSet).root(scehmaModel).create();

		return (Schema<Schema<?, DataInput<?>>, DataInput<?>>) schema;
	}
}
