package uk.co.strangeskies.modabi.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.functions.Function;
import uk.co.strangeskies.gears.mathematics.functions.collections.ListTransformationFunction;
import uk.co.strangeskies.gears.utilities.collections.HashSetMultiHashMap;
import uk.co.strangeskies.gears.utilities.collections.SetMultiMap;
import uk.co.strangeskies.modabi.BaseSchemaFactory;
import uk.co.strangeskies.modabi.MetaSchemaFactory;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.data.DataInput;
import uk.co.strangeskies.modabi.data.DataInputBuffer;
import uk.co.strangeskies.modabi.data.DataOutput;
import uk.co.strangeskies.modabi.data.impl.DataInputBufferImpl;
import uk.co.strangeskies.modabi.node.BindingNode;
import uk.co.strangeskies.modabi.node.BranchingNode;
import uk.co.strangeskies.modabi.node.ChoiceNode;
import uk.co.strangeskies.modabi.node.DataNode;
import uk.co.strangeskies.modabi.node.InputNode;
import uk.co.strangeskies.modabi.node.PropertyNode;
import uk.co.strangeskies.modabi.node.SchemaNode;
import uk.co.strangeskies.modabi.node.SequenceNode;
import uk.co.strangeskies.modabi.node.builder.SchemaNodeBuilderFactory;
import uk.co.strangeskies.modabi.processing.QualifiedName;
import uk.co.strangeskies.modabi.processing.SchemaBinder;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class SchemaBinderImpl implements SchemaBinder {
	private class SchemaSavingContext<T> implements SchemaProcessingContext {
		private final T data;
		private final Schema<T> schema;
		private final DataOutput output;

		public SchemaSavingContext(T data, DataOutput output) {
			this.data = data;
			this.output = output;
		}

		public SchemaSavingContext(T data, Schema<T> schema, DataOutput output) {
			this.data = data;
			this.schema = schema;
			this.output = output;
		}

		protected void processChildren(BranchingNode node) {
			for (SchemaNode child : node.getChildren()) {
				child.process(this);
			}
		}

		protected void save() {
			schema.getRoot();
		}

		@Override
		public <U> void accept(DataNode<U> node) {
			// TODO Auto-generated method stub

		}

		@Override
		public <U> void accept(PropertyNode<U> node) {
			// TODO Auto-generated method stub

		}

		@Override
		public void accept(ChoiceNode node) {
			processChildren(node);
		}

		@Override
		public void accept(SequenceNode node) {
			processChildren(node);
		}

		@Override
		public <U> void accept(BindingNode<U> node) {
			processChildren(node);
		}
	}

	private class SchemaLoadingContext<T> implements SchemaProcessingContext {
		private final Schema<T> schema;
		private final DataInputBuffer input;
		private final Deque<Object> bindingStack;

		public SchemaLoadingContext() {
			bindingStack = new ArrayDeque<>();
		}

		public SchemaLoadingContext(DataInput input) {
			this();
			this.input = new DataInputBufferImpl(input);
		}

		public SchemaLoadingContext(Schema<T> schema, DataInput input) {
			this.schema = schema;
			this.input = new DataInputBufferImpl(input);
		}

		protected T load() {
			Set<? extends BindingNode<?>> models = schema.getModelSet();

			return bind(schema.getRoot());
		}

		@Override
		public <U> void accept(DataNode<U> node) {
			invokeInMethod(node, input.getData(node.getType()));
		}

		@Override
		public <U> void accept(PropertyNode<U> node) {
			invokeInMethod(node, input.getProperty(node.getName(), node.getType()));
		}

		@Override
		public void accept(ChoiceNode node) {

		}

		@Override
		public void accept(SequenceNode node) {
			processChildren(node);
		}

		public <U> U bind(BindingNode<U> node) {
			String name = input.nextChild();
			String namespace = input.getProperty("xmlns", null);

			bindingStack.push(provideInstance(node.getBuilderClass()));
			processChildren(node);
			@SuppressWarnings("unchecked")
			U boundObject = (U) bindingStack.pop();
			return boundObject;
		}

		@Override
		public <U> void accept(BindingNode<U> node) {
			invokeInMethod(node, bind(node));
		}

		private void invokeInMethod(InputNode node, Object... parameters) {
			try {
				Object object = bindingStack
						.peek()
						.getClass()
						.getMethod(
								node.getInMethod(),
								ListTransformationFunction.<Class<?>, Object> applyTo(
										parameters, new Function<Class<?>, Object>() {
											@Override
											public Class<?> applyTo(Object input) {
												return input.getClass();
											}
										})).invoke(bindingStack.peek(), parameters);
				if (node.isInMethodChained()) {
					bindingStack.pop();
					bindingStack.push(object);
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				e.printStackTrace();
			}
		}

		private <U> U provideInstance(Class<U> builderClass) {
			return null;
		}

		protected void processChildren(BranchingNode node) {
			for (SchemaNode child : node.getChildren()) {
				child.process(this);
			}
		}
	}

	private final Schema<Void> baseSchema;
	private final Schema<Schema<?>> metaSchema;

	private final SetMultiMap<Schema<?>, QualifiedName> unmetDependencies;
	private final Map<QualifiedName, MockedSchema<?>> mockedDependencies;
	private final Map<QualifiedName, Schema<?>> registeredSchema;

	public SchemaBinderImpl(MetaSchemaFactory metaSchemaFactory,
			BaseSchemaFactory baseSchemaFactory,
			SchemaNodeBuilderFactory schemaNodeBuilderFactory) {
		unmetDependencies = new HashSetMultiHashMap<>();
		mockedDependencies = new HashMap<>();
		registeredSchema = new HashMap<>();

		baseSchema = baseSchemaFactory.create();
		metaSchema = metaSchemaFactory.create();
		registerSchema(baseSchema);
		registerSchema(metaSchema);
	}

	private void registerSchema(Schema<?> schema) {
		registeredSchema.put(schema.getQualifiedName(), schema);
	}

	public Schema<Schema<?>> getMetaSchema() {
		return metaSchema;
	}

	@Override
	public <T> T processInput(Schema<T> schema, DataInput input) {
		return new SchemaLoadingContext<>(schema, input).load();
	}

	@Override
	public <T> void processOutput(T data, Schema<T> schema, DataOutput output) {
		new SchemaSavingContext<>(data, schema, output).save();
	}

	@Override
	public Object processInput(DataInput input) {
		return new SchemaLoadingContext<>(input).load();
	}

	@Override
	public void processOutput(Object data, DataOutput output) {
		new SchemaSavingContext<>(data, output).save();
	}
}
