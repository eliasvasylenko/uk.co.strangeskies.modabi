package uk.co.strangeskies.modabi.benchmark;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

@Component(immediate = true)
public class ConsoleLog implements LogListener {
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addLogReader(LogReaderService service) {
		service.addLogListener(this);
	}

	public void removeLogReader(LogReaderService service) {
		service.removeLogListener(this);
	}

	@Override
	public void logged(LogEntry entry) {
		String bundle = entry.getBundle() != null ? "; " + entry.getBundle() : "";
		String service = entry.getServiceReference() != null
				? "; " + entry.getServiceReference() : "";

		System.out.println("[" + entry.getTime() + "; " + entry.getLevel() + bundle
				+ service + "] " + entry.getMessage());
	}
}
