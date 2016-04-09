package uk.co.strangeskies.modabi.osgi;

import java.net.URL;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.modabi.Binder;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class ModabiServiceProvider<T> {
	@Reference
	SchemaManager manager;

	private final TypeToken<T> type;
	private final URL location;
	private final QualifiedName schema;
	private final Hashtable<String, ?> serviceProperties;

	public ModabiServiceProvider(URL location) {
		this(location, null);
	}

	public ModabiServiceProvider(URL location, QualifiedName schema) {
		this(location, schema, new Hashtable<>());
	}

	public ModabiServiceProvider(URL location, QualifiedName schema, Hashtable<String, ?> serviceProperties) {
		this.location = location;
		this.schema = schema;
		this.serviceProperties = serviceProperties;

		type = TypeToken.over(getClass()).resolveSupertypeParameters(ModabiServiceInstance.class)
				.resolveType(new TypeParameter<T>() {});

		if (!type.isProper()) {
			throw new SchemaException(
					"Class " + getClass() + " cannot be bound with service, as type parameter of superclass "
							+ ModabiServiceInstance.class + " is not proper");
		}
	}

	@Activate
	public void initialise(BundleContext context) {
		SchemaManager manager = this.manager;
		this.manager = null;

		Binder<T> binder = (schema != null)

				? manager.bind(schema, type)

				: manager.bind(type);

		T service = binder.from(location).resolve();

		context.registerService(type.getRawType(), service, serviceProperties);
	}
}
