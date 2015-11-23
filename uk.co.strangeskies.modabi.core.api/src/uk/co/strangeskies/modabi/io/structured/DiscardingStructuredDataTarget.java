package uk.co.strangeskies.modabi.io.structured;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.DiscardingDataTarget;

public class DiscardingStructuredDataTarget
		extends StructuredDataTargetImpl<DiscardingStructuredDataTarget> {
	@Override
	protected void registerDefaultNamespaceHintImpl(Namespace namespace) {}

	@Override
	protected void registerNamespaceHintImpl(Namespace namespace) {}

	@Override
	protected void commentImpl(String comment) {}

	@Override
	protected void nextChildImpl(QualifiedName name) {}

	@Override
	protected DataTarget writePropertyImpl(QualifiedName name) {
		return new DiscardingDataTarget();
	}

	@Override
	protected DataTarget writeContentImpl() {
		return new DiscardingDataTarget();
	}

	@Override
	protected void endChildImpl() {}
}
