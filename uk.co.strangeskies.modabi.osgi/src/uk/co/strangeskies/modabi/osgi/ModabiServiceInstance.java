package uk.co.strangeskies.modabi.osgi;

import java.net.URL;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.modabi.Binder;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class ModabiServiceInstance<T> {
	@Reference
	SchemaManager manager;

	private final TypeToken<T> type;
	private final URL location;
	private final QualifiedName schema;

	public ModabiServiceInstance(URL location) {
		this(location, null);
	}

	public ModabiServiceInstance(String name, String extension) {
		this(name, extension, null);
	}

	public ModabiServiceInstance(String name, String extension, QualifiedName schema) {
		String resourceLocation = getClass().getPackage().getName().replaceAll(".", "/") + '/';
		this.location = getClass().getResource(resourceLocation + name + '.' + extension);

		this.schema = schema;

		type = findType();
	}

	public ModabiServiceInstance(URL location, QualifiedName schema) {
		this.location = location;
		this.schema = schema;

		type = findType();
	}

	private TypeToken<T> findType() {
		TypeToken<T> type = TypeToken.over(getClass()).resolveSupertypeParameters(ModabiServiceInstance.class)
				.resolveType(new TypeParameter<T>() {});

		if (!type.isProper()) {
			throw new SchemaException(
					"Class " + getClass() + " cannot be bound with service, as type parameter of superclass "
							+ ModabiServiceInstance.class + " is not proper");
		}

		if (!type.isAssignableFrom(getClass())) {
			throw new SchemaException("Type " + type + " must be assignable from providing service class " + getClass());
		}

		return type;
	}

	@SuppressWarnings("unchecked")
	@Activate
	public void initialise() {
		SchemaManager manager = this.manager;
		this.manager = null;

		Binder<T> binder = (schema != null)

				? manager.bind(schema, type)

				: manager.bind(type);

		binder.withRoot((T) this).from(location).resolve();
	}
}
