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
