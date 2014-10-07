package uk.co.strangeskies.modabi.schema.processing.unbinding.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.ElementNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportReferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.IncludeTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingException;

public class SchemaUnbinder {
	private final UnbindingContext context;

	public SchemaUnbinder(SchemaManager manager) {
		Bindings bindings = new Bindings();

		Function<UnbindingContext, IncludeTarget> includeTarget = context -> new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				context.bindings().add(model, object);

				context.output().registerNamespaceHint(model.getName().getNamespace());
			}
		};

		Function<UnbindingContext, ImportReferenceTarget> importTarget = context -> new ImportReferenceTarget() {
			@Override
			public <U> DataSource dereferenceImport(Model<U> model,
					QualifiedName idDomain, U object) {
				DataNode.Effective<?> node = (DataNode.Effective<?>) model
						.effective()
						.children()
						.stream()
						.filter(
								c -> c.getName().equals(idDomain)
										&& c instanceof DataNode.Effective<?>)
						.findAny()
						.orElseThrow(
								() -> new SchemaException("Can't fine child '" + idDomain
										+ "' to target for model '" + model + "'."));

				return unbindDataNode(node, object);
			}

			private <V> DataSource unbindDataNode(DataNode.Effective<V> node,
					Object source) {
				UnbindingContext finalContext = context.withUnbindingSource(source);
				return new DataNodeUnbinder(finalContext).unbindToDataBuffer(node,
						BindingNodeUnbinder.getData(node, finalContext));
			}
		};

		Function<UnbindingContext, ReferenceTarget> dereferenceTarget = context -> new ReferenceTarget() {
			@Override
			public <U> DataSource dereference(Model<U> model, QualifiedName idDomain,
					U object) {
				if (!bindings.get(model).contains(object))
					throw new SchemaException("Cannot find any instance '" + object
							+ "' bound to model '" + model.getName() + "'.");

				return importTarget.apply(context).dereferenceImport(model, idDomain,
						object);
			}
		};

		context = new UnbindingContext() {
			private final Map<Class<?>, List<? extends Model.Effective<?>>> attemptedMatchingModels = new HashMap<>();
			private final Map<Class<?>, List<? extends DataBindingType.Effective<?>>> attemptedMatchingTypes = new HashMap<>();

			@Override
			public List<Object> unbindingSourceStack() {
				return Collections.emptyList();
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> unbindingNodeStack() {
				return Collections.emptyList();
			}

			@SuppressWarnings("unchecked")
			@Override
			public <U> U provide(Class<U> clazz, UnbindingContext context) {
				if (clazz.equals(ReferenceTarget.class))
					return (U) dereferenceTarget.apply(context);
				if (clazz.equals(ImportReferenceTarget.class))
					return (U) importTarget.apply(context);
				if (clazz.equals(IncludeTarget.class))
					return (U) includeTarget.apply(context);
				if (clazz.equals(UnbindingContext.class))
					return (U) context;

				return manager.provide(clazz);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return clazz.equals(ReferenceTarget.class)
						|| clazz.equals(ImportReferenceTarget.class)
						|| clazz.equals(IncludeTarget.class)
						|| clazz.equals(UnbindingContext.class)
						|| manager.isProvided(clazz);
			}

			@Override
			public StructuredDataTarget output() {
				return null;
			}

			@Override
			public Bindings bindings() {
				return bindings;
			}

			@Override
			public <U> List<Model.Effective<U>> getMatchingModels(Class<U> dataClass) {
				return manager.registeredModels().getMatchingModels(dataClass).stream()
						.map(n -> n.effective())
						.collect(Collectors.toCollection(ArrayList::new));
			}

			@Override
			public <T> List<Model.Effective<? extends T>> getMatchingModels(
					ElementNode.Effective<T> element, Class<? extends T> dataClass) {
				@SuppressWarnings("unchecked")
				List<Model.Effective<? extends T>> cached = (List<Model.Effective<? extends T>>) attemptedMatchingModels
						.get(dataClass);

				if (cached == null) {
					cached = manager.registeredModels()
							.getMatchingModels(element, dataClass).stream()
							.map(n -> n.effective())
							.collect(Collectors.toCollection(ArrayList::new));
					attemptedMatchingModels.put(dataClass, cached);
				}

				return cached;
			}

			@Override
			public <T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
					DataNode.Effective<T> node, Class<?> dataClass) {
				@SuppressWarnings("unchecked")
				List<DataBindingType.Effective<? extends T>> cached = (List<DataBindingType.Effective<? extends T>>) attemptedMatchingTypes
						.get(dataClass);

				if (cached == null) {
					cached = manager.registeredTypes().getMatchingTypes(node, dataClass)
							.stream().map(n -> n.effective())
							.collect(Collectors.toCollection(ArrayList::new));
					attemptedMatchingTypes.put(dataClass, cached);
				}

				return cached;
			}
		};
	}

	public <T> void unbind(Model.Effective<T> model, StructuredDataTarget output,
			T data) {
		UnbindingContext context = this.context.withOutput(output);

		output.registerDefaultNamespaceHint(model.getName().getNamespace());

		try {
			context.output().nextChild(model.getName());
			new BindingNodeUnbinder(context).unbind(model, data);
			context.output().endChild();
		} catch (UnbindingException e) {
			throw e;
		} catch (Exception e) {
			throw new UnbindingException("Unexpected problem during uninding.",
					context, e);
		}
	}

	public <T> void unbind(StructuredDataTarget output,
			Class<? extends T> dataClass, T data) {
		castingUnbind(output, dataClass, data);
	}

	@SuppressWarnings("unchecked")
	private <T, U extends T> void castingUnbind(StructuredDataTarget output,
			Class<U> dataClass, T data) {
		UnbindingContext context = this.context.withOutput(output);

		List<? extends Model.Effective<U>> models = context
				.getMatchingModels(dataClass);

		new UnbindingAttempter(context).tryForEach(models, (c, m) -> {
			c.output().registerDefaultNamespaceHint(m.getName().getNamespace());

			try {
				c.output().nextChild(m.getName());

				U castData = (U) data;

				new BindingNodeUnbinder(c).unbind(m, castData);
				c.output().endChild();
			} catch (UnbindingException e) {
				throw e;
			} catch (Exception e) {
				throw new UnbindingException("Unexpected problem during uninding.", c,
						e);
			}
		}, e -> new UnbindingException("Cannot unbind data '" + data
				+ "' of class '" + dataClass + "' with models '" + models + "'.",
				context, e));
	}

	public <T> void unbind(StructuredDataTarget output, T data) {
		unbind(output, data.getClass(), data);
	}
}
