package uk.co.strangeskies.modabi.eclipse;

import org.eclipse.osgi.compatibility.state.PlatformAdminImpl;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.service.component.annotations.Component;

@SuppressWarnings("restriction")
@Component(service = PlatformAdmin.class)
public class PlatformAdminImplComponent extends PlatformAdminImpl {}
