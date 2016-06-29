/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.benchmarks.
 *
 * uk.co.strangeskies.modabi.benchmarks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.benchmarks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.benchmarks.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.profile;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.structured.DiscardingStructuredDataTarget;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;

@Component
public class Profiler {
	private static final int OUTPUT_RESOLUTION = 10;
	private static final int WARMUP_ROUNDS = 120;
	private static final int PROFILE_ROUNDS = 30;

	@Reference
	private SchemaManager manager;
	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	private Log log;

	private void log(Level level, String message) {
		if (log != null) {
			log.log(level, message);
		} else {
			System.out.println(level + ": " + message);
		}
	}

	@Activate
	public void profile(BundleContext context) throws BundleException {
		ClassLoader classLoader = context.getBundle().adapt(BundleWiring.class).getClassLoader();

		try {
			log(Level.INFO,
					manager.bindOutput((Schema) manager.getMetaSchema()).with(manager.getMetaSchema().getSchemaModel())
							.withClassLoader(classLoader).to(new DiscardingStructuredDataTarget()).toString());

			warmUpProfiling(WARMUP_ROUNDS, classLoader);

			long totalTimeUnbinding = unbindingProfile(PROFILE_ROUNDS, classLoader);
			long totalTimeBinding = bindingProfile(PROFILE_ROUNDS, classLoader);

			log(Level.INFO, "Time per unbind: " + (double) totalTimeUnbinding / (PROFILE_ROUNDS * 1000) + " seconds");
			log(Level.INFO, "Time per bind: " + (double) totalTimeBinding / (PROFILE_ROUNDS * 1000) + " seconds");
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		} finally {
			context.getBundle(0).stop();
		}
	}

	private long bindingProfile(int profileRounds, ClassLoader classLoader) {
		StructuredDataBuffer.Navigable buffer = StructuredDataBuffer.singleBuffer();
		manager.bindOutput((Schema) manager.getMetaSchema()).with(manager.getMetaSchema().getSchemaModel())
				.withClassLoader(classLoader).to(buffer);

		log(Level.INFO, "Binding Profiling");

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < profileRounds; i++) {
			if (i % OUTPUT_RESOLUTION == 0)
				log(Level.INFO, "  working... (" + i + " / " + profileRounds + ")");

			buffer.getBuffer().reset();
			manager.bindInput().with(manager.getMetaSchema().getSchemaModel()).withClassLoader(classLoader)
					.from(buffer.getBuffer()).resolve();
		}
		long elapsedTime = System.currentTimeMillis() - startTime;

		log(Level.INFO, "  done       (" + profileRounds + " / " + profileRounds + ")");

		return elapsedTime;
	}

	private long unbindingProfile(int profileRounds, ClassLoader classLoader) {
		log(Level.INFO, "Unbinding Profiling");

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < profileRounds; i++) {
			if (i % OUTPUT_RESOLUTION == 0)
				log(Level.INFO, "  working... (" + i + " / " + profileRounds + ")");

			manager.bindOutput((Schema) manager.getMetaSchema()).with(manager.getMetaSchema().getSchemaModel())
					.withClassLoader(classLoader).to(StructuredDataBuffer.singleBuffer());
		}
		long elapsedTime = System.currentTimeMillis() - startTime;

		log(Level.INFO, "  done       (" + profileRounds + " / " + profileRounds + ")");

		return elapsedTime;
	}

	private void warmUpProfiling(int warmupRounds, ClassLoader classLoader) {
		log(Level.INFO, "Profiling Warmup");

		for (int i = 0; i < warmupRounds; i++) {
			if (i % OUTPUT_RESOLUTION == 0)
				log(Level.INFO, "  working... (" + i + " / " + warmupRounds + ")");

			StructuredDataBuffer.Navigable buffer = StructuredDataBuffer.singleBuffer();
			manager.bindOutput((Schema) manager.getMetaSchema()).with(manager.getMetaSchema().getSchemaModel())
					.withClassLoader(classLoader).to(buffer);

			buffer.getBuffer().reset();
			manager.bindInput().with(manager.getMetaSchema().getSchemaModel()).withClassLoader(classLoader)
					.from(buffer.getBuffer()).resolve();
		}

		log(Level.INFO, "  done       (" + warmupRounds + " / " + warmupRounds + ")");
	}
}
