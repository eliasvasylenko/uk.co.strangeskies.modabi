package uk.co.strangeskies.modabi.osgi;

import java.net.URL;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.modabi.Binder;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.Self;

public abstract class ModabiServiceInstance<S extends ModabiServiceInstance<S>> implements Self<S> {
	@Reference
	SchemaManager manager;

	private final TypeToken<S> type;
	private final URL location;
	private final QualifiedName schema;

	public ModabiServiceInstance(URL location) {
		this(location, null);
	}

	public ModabiServiceInstance(URL location, QualifiedName schema) {
		this.location = location;
		this.schema = schema;

		type = TypeToken.over(getClass()).resolveSupertypeParameters(ModabiServiceInstance.class)
				.resolveType(new TypeParameter<S>() {});

		if (!type.isProper()) {
			throw new SchemaException(
					"Class " + getClass() + " cannot be bound with service, as type parameter of superclass "
							+ ModabiServiceInstance.class + " is not proper");
		}

		if (!type.isAssignableFrom(getClass())) {
			throw new SchemaException("Type " + type + " must be assignable from providing service class " + getClass());
		}
	}

	@Activate
	public void initialise() {
		SchemaManager manager = this.manager;
		this.manager = null;

		Binder<S> binder;

		if (schema != null) {
			Model<S> model = manager.registeredModels().get(schema, type);

			if (!model.getBindingType().isAssignableFrom(type)) {
				throw new SchemaException("Cannot bind type " + type + " with model " + model);
			}

			binder = manager.bind(model);
		} else {
			binder = manager.bind(type);
		}

		binder.withRoot(getThis()).from(location).resolve();
	}
}
