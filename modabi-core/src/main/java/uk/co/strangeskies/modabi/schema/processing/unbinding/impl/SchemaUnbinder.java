package uk.co.strangeskies.modabi.schema.processing.unbinding.impl;

import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportReferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.IncludeTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingContext;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingException;

public class SchemaUnbinder {
	private final UnbindingContextImpl context;

	public SchemaUnbinder(SchemaManager manager) {
		Function<UnbindingContextImpl, IncludeTarget> includeTarget = context -> new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				context.bindings().add(model, object);

				context.output().registerNamespaceHint(model.getName().getNamespace());
			}
		};

		Function<UnbindingContextImpl, ImportReferenceTarget> importTarget = context -> new ImportReferenceTarget() {
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
				UnbindingContextImpl finalContext = context.withUnbindingSource(source);
				return new DataNodeUnbinder(finalContext).unbindToDataBuffer(node,
						BindingNodeUnbinder.getData(node, finalContext));
			}
		};

		Function<UnbindingContextImpl, ReferenceTarget> referenceTarget = context -> new ReferenceTarget() {
			@Override
			public <U> DataSource dereference(Model<U> model, QualifiedName idDomain,
					U object) {
				if (!context.bindings().get(model).contains(object))
					throw new SchemaException("Cannot find any instance '" + object
							+ "' bound to model '" + model.getName() + "'.");

				return importTarget.apply(context).dereferenceImport(model, idDomain,
						object);
			}
		};

		context = new UnbindingContextImpl(manager)
				.withProvision(ReferenceTarget.class, referenceTarget)
				.withProvision(ImportReferenceTarget.class, importTarget)
				.withProvision(IncludeTarget.class, includeTarget)
				.withProvision(UnbindingContext.class, c -> c);
	}

	public <T> void unbind(Model.Effective<T> model, StructuredDataTarget output,
			T data) {
		UnbindingContextImpl context = this.context.withOutput(output);

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
		UnbindingContextImpl context = this.context.withOutput(output);

		List<? extends Model.Effective<U>> models = context
				.getMatchingModels(dataClass);

		context.attemptUnbindingUntilSuccessful(models, (c, m) -> {
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
