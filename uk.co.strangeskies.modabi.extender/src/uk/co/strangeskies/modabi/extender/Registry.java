/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.extender.
 *
 * uk.co.strangeskies.modabi.extender is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.extender is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.extender.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.extender;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.utilities.Log;
import uk.co.strangeskies.utilities.Log.Level;

public class Registry {
	private Set<Bundle> attempted = new HashSet<>();
	private Log log;

	public void register(Bundle bundle) {
		synchronized (attempted) {
			if (!attempted.add(bundle))
				return;
		}

		Set<Object> clauses = getClauses(bundle);

		for (Object clause : clauses) {
			try {
				System.out.println(clause);

				// registerServlet(bundle, alias, className); TODO
			} catch (Throwable t) {
				if (log != null) {
					log.log(Level.ERROR,
							"[extender] Activating servlet from " + bundle.getLocation(), t);
				}
			}
		}
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public void unregister(Bundle bundle) {
		synchronized (attempted) {
			if (!attempted.remove(bundle))
				return;
		}

		// TODO
	}

	private Set<Object> getClauses(Bundle bundle) {
		return bundle.adapt(BundleWiring.class)
				.getCapabilities(Schema.class.getPackage().getName()).stream()
				.map(BundleCapability::getAttributes).map(a -> a.get("schema"))
				.collect(Collectors.toSet());
	}

	void close() {
		synchronized (attempted) {
			for (Bundle bundle : attempted) {
				unregister(bundle);
			}
		}
	}
}
