package uk.co.strangeskies.modabi.impl.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.schema.BindingSchema;
import uk.co.strangeskies.modabi.schema.node.BranchSchemaNode;
import uk.co.strangeskies.modabi.schema.node.BranchingSchemaNode;
import uk.co.strangeskies.modabi.schema.node.DataSchemaNode;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.PropertySchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessor;

class XMLSchemaLoadingContext implements
		SchemaProcessingContext<XMLSchemaLoadingContext> {
	@Override
	public void data(DataSchemaNode<?> node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void property(PropertySchemaNode<?> node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void branch(BranchSchemaNode<? super XMLSchemaLoadingContext> node) {
		processChildren(node);
	}

	@Override
	public void element(ElementSchemaNode<?, ? super XMLSchemaLoadingContext> node) {
		processChildren(node);
	}

	protected void processChildren(
			BranchingSchemaNode<? super XMLSchemaLoadingContext> node) {
		for (SchemaNode<? super XMLSchemaLoadingContext> child : node.getChildren()) {
			child.process(this);
		}
	}

	protected <T> T load(
			BindingSchema<T, ? super XMLSchemaLoadingContext> schema,
			InputStream input) {
		Set<? extends ElementSchemaNode<?, ? super XMLSchemaLoadingContext>> models = schema
				.getModelSet();

		ElementSchemaNode<T, ? super XMLSchemaLoadingContext> root = schema
				.getRoot();

		return null;
	}
}

class XMLSchemaSavingContext implements
		SchemaProcessingContext<XMLSchemaSavingContext> {
	@Override
	public void data(DataSchemaNode<?> node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void property(PropertySchemaNode<?> node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void branch(BranchSchemaNode<? super XMLSchemaSavingContext> node) {
		processChildren(node);
	}

	@Override
	public void element(ElementSchemaNode<?, ? super XMLSchemaSavingContext> node) {
		processChildren(node);
	}

	protected void processChildren(
			BranchingSchemaNode<? super XMLSchemaSavingContext> node) {
		for (SchemaNode<? super XMLSchemaSavingContext> child : node.getChildren()) {
			child.process(this);
		}
	}

	protected <T> void save(T data,
			BindingSchema<T, ? super XMLSchemaLoadingContext> schema,
			OutputStream input) {
		schema.getRoot()
	}
}

public class XMLSchemaProcessor implements
		SchemaProcessor<SchemaProcessingContext<?>> {
	@Override
	public String getFormatName() {
		return "XML";
	}

	@Override
	public List<String> getFileExtentions() {
		return Arrays.asList("xml");
	}

	@Override
	public <T> T processInput(
			BindingSchema<T, ? super SchemaProcessingContext<?>> schema,
			InputStream input) {
		return new XMLSchemaLoadingContext().load(schema, input);
	}

	@Override
	public <T> void processOutput(T data,
			BindingSchema<T, ? super SchemaProcessingContext<?>> schema,
			OutputStream output) {
		new XMLSchemaSavingContext().save(data, schema, output);
	}
}