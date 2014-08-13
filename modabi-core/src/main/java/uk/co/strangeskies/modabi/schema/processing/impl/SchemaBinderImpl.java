package uk.co.strangeskies.modabi.schema.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.gears.utilities.function.collection.ListTransformationFunction;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.data.DataBindingTypes;
import uk.co.strangeskies.modabi.data.impl.DataBindingTypeBuilderImpl;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.building.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.InputNode;
import uk.co.strangeskies.modabi.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.schema.processing.SchemaBinder;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class SchemaBinderImpl implements SchemaBinder {
	private static class SchemaLoadingContext<T> implements
			SchemaProcessingContext {
		private final Model<T> model;

		private final Deque<Object> bindingStack;

		public SchemaLoadingContext(Model<T> model, StructuredDataSource input) {
			bindingStack = new ArrayDeque<>();

			this.model = model;
		}

		protected Binding<T> load() {
			return new Binding<T>(model, bind(model.effective()));
		}

		@Override
		public void accept(ChoiceNode.Effective node) {
		}

		@Override
		public void accept(InputSequenceNode.Effective node) {
			processChildren(node);
		}

		public <U> U bind(AbstractModel.Effective<U, ?, ?> node) {
			// String name = input.nextChild();
			// String namespace = input.getProperty("xmlns", null);

			bindingStack.push(provideInstance(node.getBindingClass()));
			processChildren(node);
			@SuppressWarnings("unchecked")
			U boundObject = (U) bindingStack.pop();
			return boundObject;
		}

		@Override
		public <U> void accept(ElementNode.Effective<U> node) {
			invokeInMethod(node, (Object) bind(node));
		}

		private void invokeInMethod(InputNode.Effective<?, ?> node,
				Object... parameters) {
			try {
				Object object = bindingStack
						.peek()
						.getClass()
						.getMethod(node.getInMethodName(),
								ListTransformationFunction.apply(parameters, Object::getClass))
						.invoke(bindingStack.peek(), parameters);
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

		protected void processChildren(SchemaNode.Effective<?, ?> node) {
			for (ChildNode.Effective<?, ?> child : node.children())
				child.process(this);
		}

		@Override
		public <U> void accept(DataNode.Effective<U> node) {
			// invokeInMethod(node, (Object) input.getData(node.type()));
		}

		@Override
		public void accept(SequenceNode.Effective node) {

		}
	}

	private final BaseSchema baseSchema;
	private final MetaSchema metaSchema;

	private final List<Function<Class<?>, Object>> providers;

	final Models registeredModels; // TODO obvs
	private final DataBindingTypes registeredTypes;
	private final Schemata registeredSchema;

	public SchemaBinderImpl() {
		this(new SchemaBuilderImpl(), new ModelBuilderImpl(),
				new DataBindingTypeBuilderImpl());
	}

	public SchemaBinderImpl(SchemaBuilder schemaBuilder,
			ModelBuilder modelBuilder, DataBindingTypeBuilder dataTypeBuilder) {
		providers = new ArrayList<>();

		DataLoader loader = new DataLoader() {
			@Override
			public <T> T loadData(DataNode<T> node, BufferedDataSource data) {
				return null;
			}
		};
		baseSchema = new BaseSchemaImpl(schemaBuilder, modelBuilder,
				dataTypeBuilder, loader);
		metaSchema = new MetaSchemaImpl(schemaBuilder, modelBuilder,
				dataTypeBuilder, loader, baseSchema);

		registeredSchema = new Schemata();
		Namespace namespace = metaSchema.getQualifiedName().getNamespace();
		registeredModels = new Models(namespace);
		registeredTypes = new DataBindingTypes(namespace);

		registerSchema(baseSchema);
		registerSchema(metaSchema);

		registerProvider(DataBindingTypeBuilder.class, () -> dataTypeBuilder);
		registerProvider(ModelBuilder.class, () -> modelBuilder);
		registerProvider(SchemaBuilder.class, () -> schemaBuilder);
	}

	@Override
	public void registerSchema(Schema schema) {
		if (registeredSchema.add(schema)) {
			for (Schema dependency : schema.getDependencies())
				registerSchema(dependency);

			for (Model<?> model : schema.getModels())
				registerModel(model, schema.getQualifiedName().getNamespace());

			for (DataBindingType<?> type : schema.getDataTypes())
				registerDataType(type, schema.getQualifiedName().getNamespace());
		}
	}

	private void registerModel(Model<?> model, Namespace namespace) {
		registeredModels.add(model, namespace);
	}

	private void registerDataType(DataBindingType<?> type, Namespace namespace) {
		registeredTypes.add(type, namespace);
	}

	@Override
	public MetaSchema getMetaSchema() {
		return metaSchema;
	}

	@Override
	public BaseSchema getBaseSchema() {
		return baseSchema;
	}

	@Override
	public <T> T processInput(Model<T> model, StructuredDataSource input) {
		return new SchemaLoadingContext<>(model, input).load().getData();
	}

	@Override
	public Binding<?> processInput(StructuredDataSource input) {
		Model<?> model = null;
		// input.peekNext(model);
		return new SchemaLoadingContext<>(model, input).load();
	}

	@Override
	public <T> void processOutput(Model<T> model, StructuredDataTarget output,
			T data) {
		new SchemaSavingContext<>(this, model, output, data);
	}

	@Override
	public <T> void registerProvider(Class<T> providedClass, Supplier<T> provider) {
		registerProvider(c -> c.equals(providedClass) ? provider.get() : null);
	}

	@Override
	public void registerProvider(Function<Class<?>, ?> provider) {
		providers.add(c -> {
			Object provided = provider.apply(c);
			if (!c.isInstance(provided))
				throw new SchemaException("Invalid object provided for the class [" + c
						+ "] by provider [" + provider + "]");
			return provided;
		});
	}

	@SuppressWarnings("unchecked")
	protected <T> T provide(Class<T> clazz) {
		return (T) providers
				.stream()
				.map(p -> p.apply(clazz))
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow(
						() -> new SchemaException("No provider exists for the class "
								+ clazz));
	}
}
