package uk.co.strangeskies.modabi.processing.impl;

import java.util.Set;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.data.DataInput;
import uk.co.strangeskies.modabi.data.DataOutput;
import uk.co.strangeskies.modabi.node.BindingNode;
import uk.co.strangeskies.modabi.node.BranchingNode;
import uk.co.strangeskies.modabi.node.ChoiceNode;
import uk.co.strangeskies.modabi.node.DataNode;
import uk.co.strangeskies.modabi.node.PropertyNode;
import uk.co.strangeskies.modabi.node.SchemaNode;
import uk.co.strangeskies.modabi.node.SequenceNode;
import uk.co.strangeskies.modabi.processing.SchemaBinder;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

class SchemaLoadingContext implements
		SchemaProcessingContext<SchemaLoadingContext> {
	protected void processChildren(
			BranchingNode<? super SchemaLoadingContext> node) {
		for (SchemaNode<? super SchemaLoadingContext> child : node.getChildren()) {
			child.process(this);
		}
	}

	protected <T> T load(Schema<T, ? super SchemaLoadingContext> schema,
			DataInput input) {
		Set<? extends BindingNode<?, ? super SchemaLoadingContext>> models = schema
				.getModelSet();

		BindingNode<T, ? super SchemaLoadingContext> root = schema.getRoot();

		return null;
	}

	@Override
	public void accept(DataNode<?> node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(PropertyNode<?> node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(ChoiceNode<? super SchemaLoadingContext> node) {
		processChildren(node);
	}

	@Override
	public void accept(SequenceNode<? super SchemaLoadingContext> node) {
		processChildren(node);
	}

	@Override
	public void accept(BindingNode<?, ? super SchemaLoadingContext> node) {
		processChildren(node);
	}
}

class SchemaSavingContext implements
		SchemaProcessingContext<SchemaSavingContext> {
	protected void processChildren(BranchingNode<? super SchemaSavingContext> node) {
		for (SchemaNode<? super SchemaSavingContext> child : node.getChildren()) {
			child.process(this);
		}
	}

	protected <T> void save(T data,
			Schema<T, ? super SchemaLoadingContext> schema, DataOutput input) {
		schema.getRoot();
	}

	@Override
	public void accept(DataNode<?> node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(PropertyNode<?> node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(ChoiceNode<? super SchemaSavingContext> node) {
		processChildren(node);
	}

	@Override
	public void accept(SequenceNode<? super SchemaSavingContext> node) {
		processChildren(node);
	}

	@Override
	public void accept(BindingNode<?, ? super SchemaSavingContext> node) {
		processChildren(node);
	}
}

public class SchemaBinderImpl implements
		SchemaBinder<SchemaProcessingContext<?>> {
	@Override
	public <T> T processInput(
			Schema<T, ? super SchemaProcessingContext<?>> schema, DataInput input) {
		return new SchemaLoadingContext().load(schema, input);
	}

	@Override
	public <T> void processOutput(T data,
			Schema<T, ? super SchemaProcessingContext<?>> schema, DataOutput output) {
		new SchemaSavingContext().save(data, schema, output);
	}
}
