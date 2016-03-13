package uk.co.strangeskies.modabi.scripting;

import javax.script.ScriptEngineManager;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.modabi.SchemaManager;

@Component
public class ScriptEngineManagerRegistration {
	@Reference
	ScriptEngineManager scriptEngineManager;

	@Reference
	SchemaManager schemaManager;

	@Activate
	public void activate() {
		schemaManager.provisions().registerProvider(ScriptEngineManager.class, () -> scriptEngineManager);
	}
}
