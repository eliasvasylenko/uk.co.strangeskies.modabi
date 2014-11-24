package uk.co.strangeskies.modabi.schema.management.providers.impl;

import java.lang.reflect.ParameterizedType;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.providers.ImportReferenceTarget;
import uk.co.strangeskies.modabi.schema.management.providers.IncludeTarget;
import uk.co.strangeskies.modabi.schema.management.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.management.providers.TypeComposer;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingContext;
import uk.co.strangeskies.modabi.schema.management.unbinding.impl.BindingNodeUnbinder;
import uk.co.strangeskies.modabi.schema.management.unbinding.impl.DataNodeUnbinder;
import uk.co.strangeskies.modabi.schema.management.unbinding.impl.UnbindingContextImpl;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public class UnbindingProviders {
	public Function<UnbindingContext, IncludeTarget> includeTarget() {
		return context -> new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				context.bindings().add(model, object);

				context.output().registerNamespaceHint(model.getName().getNamespace());
			}
		};
	}

	public Function<UnbindingContext, ImportReferenceTarget> importTarget() {
		return context -> new ImportReferenceTarget() {
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
	}

	public Function<UnbindingContext, ReferenceTarget> referenceTarget() {
		return context -> new ReferenceTarget() {
			@Override
			public <U> DataSource dereference(Model<U> model, QualifiedName idDomain,
					U object) {
				if (!context.bindings().get(model).contains(object))
					throw new SchemaException("Cannot find any instance '" + object
							+ "' bound to model '" + model.getName() + "'.");

				return importTarget().apply(context).dereferenceImport(model, idDomain,
						object);
			}
		};
	}

	public Function<UnbindingContext, TypeComposer> typeComposer() {
		return context -> Object::toString;
	}
}
