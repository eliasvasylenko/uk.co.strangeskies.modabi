package uk.co.strangeskies.modabi.scripting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;

public class ScriptObjectBuilder<T> {
	private ScriptEngineManager manager;
	private String objectName;
	private String language;
	private String script;
	private String resource;
	private TypeToken<T> objectType;

	public static ScriptObjectBuilder<Object> build() {
		return new ScriptObjectBuilder<>();
	}

	@SuppressWarnings("unchecked")
	public static <T> ScriptObject<T> cast(T object) {
		return (ScriptObject<T>) object;
	}

	public ScriptObjectBuilder<T> setScriptEngineManager(ScriptEngineManager manager) {
		this.manager = manager;

		return this;
	}

	public ScriptObjectBuilder<T> setObjectName(String objectName) {
		this.objectName = objectName;

		return this;
	}

	public ScriptObjectBuilder<T> setScript(String script) {
		this.script = script;

		return this;
	}

	public ScriptObjectBuilder<T> setResource(String resource) {
		this.resource = resource;

		return this;
	}

	public ScriptObjectBuilder<T> setLanguage(String language) {
		this.language = language;

		return this;
	}

	@SuppressWarnings("unchecked")
	public <U extends T> ScriptObjectBuilder<U> setObjectType(TypeToken<U> objectType) {
		this.objectType = (TypeToken<T>) objectType;

		return (ScriptObjectBuilder<U>) this;
	}

	public ScriptObject<T> create() throws ScriptException {
		return create(Thread.currentThread().getContextClassLoader());
	}

	@SuppressWarnings("unchecked")
	public ScriptObject<T> create(ClassLoader classLoader) throws ScriptException {
		Class<?> rawType = objectType.getRawType();

		Property<ScriptObject<T>, ScriptObject<T>> proxy = new IdentityProperty<>();

		ScriptEngine engine;
		if (language != null) {
			engine = manager.getEngineByName(language);
		} else {
			engine = manager.getEngineByExtension(ManifestUtilities.getResourceExtension(resource));
		}
		engine.eval(script);
		Invocable invocable = (Invocable) engine;
		Object object = invocable.getInterface(rawType);

		ScriptObject<T> scriptObject = new ScriptObject<T>() {
			TypeToken<ScriptObject<T>> objectType = getScriptObjectType(ScriptObjectBuilder.this.objectType);
			String objectName = ScriptObjectBuilder.this.objectName;
			String language = ScriptObjectBuilder.this.language;
			String script = ScriptObjectBuilder.this.script;
			String resource = ScriptObjectBuilder.this.resource;

			@Override
			public TypeToken<ScriptObject<T>> getThisType() {
				return objectType;
			}

			private <U> TypeToken<ScriptObject<U>> getScriptObjectType(TypeToken<U> objectType) {
				return new TypeToken<ScriptObject<U>>() {}.withTypeArgument(new TypeParameter<U>() {}, objectType);
			}

			@Override
			public String getLanguage() {
				return language;
			}

			@Override
			public String getScript() {
				return script;
			}

			@Override
			public String getResource() {
				return resource;
			}

			@Override
			public String getObjectName() {
				return objectName;
			}

			@Override
			public ScriptObject<T> copy() {
				return proxy.get();
			}

			@Override
			public T cast() {
				return (T) proxy.get();
			}
		};

		proxy.set((ScriptObject<T>) Proxy.newProxyInstance(classLoader, new Class[] { rawType, ScriptObject.class },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						Object delegate;
						if (method.getDeclaringClass().equals(ScriptObject.class)
								|| method.getDeclaringClass().equals(Reified.class)) {
							delegate = scriptObject;
						} else {
							delegate = object;
						}

						return method.invoke(delegate, args);
					}
				}));

		return proxy.get();
	}
}
