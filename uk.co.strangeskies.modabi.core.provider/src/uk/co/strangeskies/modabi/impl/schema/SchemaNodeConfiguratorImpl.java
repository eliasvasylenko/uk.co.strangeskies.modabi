package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildBindingPointConfigurationContext;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.InputInitializerConfigurator;
import uk.co.strangeskies.modabi.schema.OutputInitializerConfigurator;
import uk.co.strangeskies.modabi.schema.StructuralNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;

public class SchemaNodeConfiguratorImpl implements SchemaNodeConfigurator {
	private final BindingPointConfiguratorImpl<?, ?> parent;

	public SchemaNodeConfiguratorImpl(BindingPointConfiguratorImpl<?, ?> parent) {
		this.parent = parent;
	}

	@Override
	public StructuralNode create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaNodeConfigurator copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputInitializerConfigurator initializeInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputInitializerConfigurator<?> initializeOutput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChildBindingPointConfigurator<?> addChildBindingPoint() {
		ChildBindingPointConfigurationContext context = new ChildBindingPointConfigurationContext() {
			@Override
			public Namespace namespace() {
				return parent.getName().get().getNamespace();
			}

			@Override
			public StructuralNode parentNode() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Stream<ChildBindingPoint<?>> overrideChild(QualifiedName id) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public TypeToken<?> outputSourceType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public TypeToken<?> inputTargetType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Imports imports() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public DataLoader dataLoader() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public BoundSet boundSet() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void addChildResult(ChildBindingPoint<?> result) {
				// TODO Auto-generated method stub

			}
		};

		return new ChildBindingPointConfiguratorImpl<>(context);
	}

	@Override
	public List<ChildBindingPointConfigurator<?>> getChildBindingPoints() {
		// TODO Auto-generated method stub
		return null;
	}
}
