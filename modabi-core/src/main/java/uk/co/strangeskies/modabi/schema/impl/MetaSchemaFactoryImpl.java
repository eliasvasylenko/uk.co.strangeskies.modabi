package uk.co.strangeskies.modabi.schema.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.modabi.schema.BindingSchema;
import uk.co.strangeskies.modabi.schema.BindingSchemaBuilder;
import uk.co.strangeskies.modabi.schema.MetaSchemaFactory;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.schema.node.builder.impl.SchemaNodeBuilderFactoryImpl;
import uk.co.strangeskies.modabi.schema.node.data.DataNodeType;

public class MetaSchemaFactoryImpl implements MetaSchemaFactory {
	private SchemaNodeBuilderFactory node() {
		return new SchemaNodeBuilderFactoryImpl();
	}

	private BindingSchemaBuilder<Object> graph() {
		return new BindingSchemaBuilderFactoryImpl().create();
	}

	private SchemaNode getRootElementNode() {
		// TODO Auto-generated method stub
		return null;
	}

	private Collection<? extends DataNodeType<?>> createTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	private Set<ElementSchemaNode<?>> createModels() {
		Set<ElementSchemaNode<?>> modelSet = new HashSet<>();

		return modelSet;
	}

	@SuppressWarnings("unchecked")
	private ElementSchemaNode<BindingSchema<?>> createRoot() {
		return (ElementSchemaNode<BindingSchema<?>>) node().element()
				.name("modelSchema").dataClass(BindingSchema.class)
				.factoryClass(BindingSchemaBuilder.class).addChild(getModelNode())
				.addChild(getRootElementNode()).create();
	}

	@Override
	public BindingSchema<BindingSchema<?>> create() {
		return graph().types(createTypes()).models(createModels())
				.root(createRoot()).create();
	}
}
