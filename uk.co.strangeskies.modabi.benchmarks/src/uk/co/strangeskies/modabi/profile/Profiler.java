package uk.co.strangeskies.modabi.profile;

import java.io.ByteArrayOutputStream;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer;
import uk.co.strangeskies.utilities.ContextClassLoaderRunner;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;

@Component
public class Profiler {
	private SchemaManager manager;
	private Log log;

	@Reference
	public void setSchemaManager(SchemaManager manager) {
		this.manager = manager;
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	public void setlog(Log log) {
		this.log = log;
	}

	private void log(Level level, String message) {
		if (log != null)
			log.log(level, message);
	}

	@Activate
	public void profile(BundleContext context) throws BundleException {
		ClassLoader classLoader = context.getBundle().adapt(BundleWiring.class)
				.getClassLoader();

		new ContextClassLoaderRunner(classLoader).run(() -> {
			try {
				log(Level.INFO,
						manager
								.unbind(manager.getMetaSchema().getSchemaModel(),
										manager.getMetaSchema())
								.to("xml", new ByteArrayOutputStream()).toString());

				warmUpProfiling(60);

				int profileRounds = 20;
				long totalTimeUnbinding = unbindingProfile(profileRounds);
				long totalTimeBinding = bindingProfile(profileRounds);

				log(Level.INFO,
						"Time per unbind: "
								+ (double) totalTimeUnbinding / (profileRounds * 1000)
								+ " seconds");
				log(Level.INFO, "Time per bind: "
						+ (double) totalTimeBinding / (profileRounds * 1000) + " seconds");
			} catch (Throwable t) {
				throw t;
			} finally {
				// context.getBundle(0).stop();
			}
		});
	}

	private long bindingProfile(int profileRounds) {
		NavigableStructuredDataSource buffer = manager
				.unbind(manager.getMetaSchema().getSchemaModel(),
						manager.getMetaSchema())
				.to(StructuredDataBuffer.singleBuffer()).getBuffer();

		log(Level.INFO, "Binding Profiling");
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < profileRounds; i++) {
			if (i % 10 == 0)
				log(Level.INFO, "  working... (" + i + " / " + profileRounds + ")");

			buffer.reset();
			manager.bind(manager.getMetaSchema().getSchemaModel()).from(buffer)
					.resolve();
		}
		return System.currentTimeMillis() - startTime;
	}

	private long unbindingProfile(int profileRounds) {
		log(Level.INFO, "Unbinding Profiling");
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < profileRounds; i++) {
			if (i % 10 == 0)
				log(Level.INFO, "  working... (" + i + " / " + profileRounds + ")");

			manager.unbind(manager.getMetaSchema().getSchemaModel(),
					manager.getMetaSchema()).to(StructuredDataBuffer.singleBuffer());
		}
		return System.currentTimeMillis() - startTime;
	}

	private void warmUpProfiling(int warmupRounds) {
		log(Level.INFO, "Profiling Warmup");
		for (int i = 0; i < warmupRounds; i++) {
			if (i % 10 == 0)
				log(Level.INFO, "  working... (" + i + " / " + warmupRounds + ")");

			NavigableStructuredDataSource buffer = manager
					.unbind(manager.getMetaSchema().getSchemaModel(),
							manager.getMetaSchema())
					.to(StructuredDataBuffer.singleBuffer()).getBuffer();

			buffer.reset();
			manager.bind(manager.getMetaSchema().getSchemaModel()).from(buffer)
					.resolve();
		}
	}
}
