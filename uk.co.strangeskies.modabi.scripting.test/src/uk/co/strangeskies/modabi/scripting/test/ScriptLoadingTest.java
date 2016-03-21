package uk.co.strangeskies.modabi.scripting.test;

import java.io.InputStream;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;

public class ScriptLoadingTest {
	protected static final String XML_POSTFIX = ".xml";

	private BundleContext getBundleContext() {
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	}

	private <T> T getService(Class<T> clazz) {
		try {
			BundleContext context = getBundleContext();

			ServiceTracker<T, T> serviceTracker = new ServiceTracker<>(context, clazz, null);
			serviceTracker.open();
			try {
				return serviceTracker.waitForService(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@SuppressWarnings("unchecked")
	private Model<Runnable> getRunnableScriptModel(SchemaManager manager) {
		return (Model<Runnable>) manager.registeredModels()
				.get(new QualifiedName("runnableScript", Schema.MODABI_NAMESPACE));
	}

	@SuppressWarnings("unchecked")
	private Model<Function<String, String>> getFunctionScriptModel(SchemaManager manager) {
		return (Model<Function<String, String>>) manager.registeredModels()
				.get(new QualifiedName("runnableScript", Schema.MODABI_NAMESPACE));
	}

	@SuppressWarnings("unchecked")
	private Model<IdentityProperty<Function<String, String>>> getFunctionScriptPropertyModel(SchemaManager manager) {
		return (Model<IdentityProperty<Function<String, String>>>) manager.registeredModels()
				.get(new QualifiedName("scriptProperty", Schema.MODABI_NAMESPACE));
	}

	@SuppressWarnings("unchecked")
	private Model<IdentityProperty<Function<String, String>>> getFunctionPropertyModel(SchemaManager manager) {
		return (Model<IdentityProperty<Function<String, String>>>) manager.registeredModels()
				.get(new QualifiedName("functionProperty", Schema.MODABI_NAMESPACE));
	}

	@Test
	public void loadScriptTestSchema() {
		SchemaManager manager = getService(SchemaManager.class);

		Assert.assertNotNull(getRunnableScriptModel(manager));
		Assert.assertNotNull(getFunctionScriptModel(manager));
	}

	@Test(timeout = 2000)
	public void loadRunnableScriptTest() {
		SchemaManager manager = getService(SchemaManager.class);

		manager.bind().from(() -> this.getResouce("RunnableScript")).resolve(1000);
	}

	@Test(timeout = 2000)
	public void executeRunnableScriptTest() {
		SchemaManager manager = getService(SchemaManager.class);

		Runnable runnable = manager.bind(getRunnableScriptModel(manager)).from(() -> this.getResouce("RunnableScript"))
				.resolve(1000);

		runnable.run();
	}

	@Test(timeout = 2000)
	public void loadFunctionScriptPropertyTest() {
		SchemaManager manager = getService(SchemaManager.class);

		manager.bind().from(() -> this.getResouce("RunnableScript")).resolve(1000);
	}

	@Test(timeout = 2000)
	public void executeFunctionScriptPropertyTest() {
		SchemaManager manager = getService(SchemaManager.class);

		Property<Function<String, String>, Function<String, String>> property = manager
				.bind(getFunctionScriptPropertyModel(manager)).from(() -> this.getResouce("FunctionScriptProperty"))
				.resolve(1000);

		String result = property.get().apply("capitalise me");

		Assert.assertEquals(result, "CAPITALISE ME");
	}

	@Test(timeout = 2000)
	public void loadFunctionPropertyTest() {
		SchemaManager manager = getService(SchemaManager.class);

		manager.bind().from(() -> this.getResouce("FunctionProperty")).resolve(1000);
	}

	@Test(timeout = 2000)
	public void executeFunctionPropertyTest() {
		SchemaManager manager = getService(SchemaManager.class);

		Property<Function<String, String>, Function<String, String>> property = manager
				.bind(getFunctionPropertyModel(manager)).from(() -> this.getResouce("FunctionProperty")).resolve(1000);

		String result = property.get().apply("UNCAPITALISE ME");

		Assert.assertEquals(result, "uncapitalise me");
	}

	private InputStream getResouce(String resource) {
		return getClass().getResourceAsStream(resource + XML_POSTFIX);
	}
}
