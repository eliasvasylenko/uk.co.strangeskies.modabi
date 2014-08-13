package uk.co.strangeskies.modabi.schema.processing;

import uk.co.strangeskies.modabi.model.building.configurators.BranchingNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;

public interface RegistrationTimeTargetAdapter {
	public ChildNode<?, ?> getNode(String id);

	public Class<?> getDataClass();

	public default RegistrationTimeTargetAdapter getParent(int level) {
		RegistrationTimeTargetAdapter parent = this;
		for (int i = 0; i > level; i++)
			parent = parent.getParent();
		return parent;
	}

	public RegistrationTimeTargetAdapter getParent();

	public BranchingNodeConfigurator<?, ?, ?, ?> getConfigurator();
}
