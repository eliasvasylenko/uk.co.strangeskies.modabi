package uk.co.strangeskies.modabi.extender;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import aQute.bnd.annotation.headers.ProvideCapability;
import uk.co.strangeskies.utilities.Log;

@ProvideCapability(ns = "osgi.extender", name = "uk.co.strangeskies.modabi", version = "1.0.0")
@Component(immediate = true)
public class Activator implements BundleListener {
	private BundleContext context;
	private BundleCapability capability;
	private final Registry registry = new Registry();

	@Activate
	protected void activate(ComponentContext cc) {
		this.context = cc.getBundleContext();
		context.addBundleListener(this);

		capability = context.getBundle().adapt(BundleWiring.class)
				.getCapabilities("osgi.extender").get(0);

		for (Bundle bundle : context.getBundles()) {
			if ((bundle.getState() & (Bundle.STARTING | Bundle.ACTIVE)) != 0
					&& isRegisterable(bundle)) {
				registry.register(bundle);
			}
		}
	}

	@Deactivate
	protected void deactivate(ComponentContext context) throws Exception {
		this.context.removeBundleListener(this);
		registry.close();
	}

	public void bundleChanged(BundleEvent event) {
		if (isRegisterable(event.getBundle())) {
			switch (event.getType()) {
			case BundleEvent.STARTED:
				registry.register(event.getBundle());
				break;

			case BundleEvent.STOPPED:
				registry.unregister(event.getBundle());
				break;
			}
		}
	}

	private boolean isRegisterable(Bundle bundle) {
		boolean registerable = bundle.adapt(BundleWiring.class)
				.getRequirements("osgi.extender").stream()
				.anyMatch(r -> r.matches(capability));

		return registerable;
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	public void setLog(Log log) {
		registry.setLog(log);
	}
}
