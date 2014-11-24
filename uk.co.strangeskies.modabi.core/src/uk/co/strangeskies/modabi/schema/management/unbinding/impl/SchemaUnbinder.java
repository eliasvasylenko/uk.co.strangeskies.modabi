package uk.co.strangeskies.modabi.schema.management.unbinding.impl;

import java.util.List;

import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.management.providers.ImportReferenceTarget;
import uk.co.strangeskies.modabi.schema.management.providers.IncludeTarget;
import uk.co.strangeskies.modabi.schema.management.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.management.providers.TypeComposer;
import uk.co.strangeskies.modabi.schema.management.providers.impl.UnbindingProviders;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingContext;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingException;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public class SchemaUnbinder {
	private final UnbindingContextImpl context;

	public SchemaUnbinder(SchemaManager manager) {
		UnbindingProviders providers = new UnbindingProviders();

		context = new UnbindingContextImpl(manager)
				.withProvision(ReferenceTarget.class, providers.referenceTarget())
				.withProvision(ImportReferenceTarget.class, providers.importTarget())
				.withProvision(IncludeTarget.class, providers.includeTarget())
				.withProvision(TypeComposer.class, providers.typeComposer())
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
