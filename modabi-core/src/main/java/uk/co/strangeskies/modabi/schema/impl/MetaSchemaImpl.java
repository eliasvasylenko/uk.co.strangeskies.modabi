package uk.co.strangeskies.modabi.schema.impl;

import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.schema.SchemaGraph;
import uk.co.strangeskies.modabi.schema.SchemaGraphBuilder;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.schema.node.builder.impl.SchemaNodeBuilderFactoryImpl;

public class MetaSchemaImpl implements SchemaGraph<SchemaGraph<?>> {
	private final Set<ElementSchemaNode<?>> modelSet;

	public MetaSchemaImpl() {
		modelSet = new HashSet<>();
	}

	private SchemaNodeBuilderFactory build() {
		return new SchemaNodeBuilderFactoryImpl();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ElementSchemaNode<SchemaGraph<?>> getRoot() {
		return (ElementSchemaNode<SchemaGraph<?>>) build().element()
				.name("modelSchema").dataClass(SchemaGraph.class)
				.factoryClass(SchemaGraphBuilder.class).addChild(getModelNode())
				.addChild(getRootElementNode()).create();
	}

	private SchemaNode getRootElementNode() {
		// TODO Auto-generated method stub
		return null;
	}

	private SchemaNode getModelNode() {
		return build().element().name("model").dataClass(ElementSchemaNode.class)
				.occurances(0, null).create();
	}

	@Override
	public Set<ElementSchemaNode<?>> getModelSet() {
		return modelSet;
	}
}
