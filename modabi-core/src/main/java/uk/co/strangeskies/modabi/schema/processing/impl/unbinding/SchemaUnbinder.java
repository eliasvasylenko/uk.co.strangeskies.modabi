package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.reference.DereferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportDereferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.IncludeTarget;

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

		Function<UnbindingContext, ImportDereferenceTarget> importTarget = context -> new ImportDereferenceTarget() {
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

		Function<UnbindingContext, DereferenceTarget> dereferenceTarget = context -> new DereferenceTarget() {
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
			@Override
			public Object unbindingSource() {
				return null;
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> unbindingNodeStack() {
				return Collections.emptyList();
			}

			@SuppressWarnings("unchecked")
			@Override
			public <U> U provide(Class<U> clazz, UnbindingContext context) {
				if (clazz.equals(DereferenceTarget.class))
					return (U) dereferenceTarget.apply(context);
				if (clazz.equals(ImportDereferenceTarget.class))
					return (U) importTarget.apply(context);
				if (clazz.equals(IncludeTarget.class))
					return (U) includeTarget.apply(context);

				return manager.provide(clazz);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return clazz.equals(DereferenceTarget.class)
						|| clazz.equals(ImportDereferenceTarget.class)
						|| clazz.equals(IncludeTarget.class) || manager.isProvided(clazz);
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
			public <T> List<Model<? extends T>> getMatchingModels(
					ElementNode.Effective<T> element, Class<?> dataClass) {
				return manager.registeredModels().getMatchingModels(element, dataClass);
			}

			@Override
			public <T> List<DataBindingType<? extends T>> getMatchingTypes(
					DataNode.Effective<T> node, Class<?> dataClass) {
				return manager.registeredTypes().getMatchingTypes(node, dataClass);
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
		} catch (SchemaException e) {
			throw e;
		} catch (Exception e) {
			throw context.exception("Unexpected problem during uninding.", e);
		}
	}
}
