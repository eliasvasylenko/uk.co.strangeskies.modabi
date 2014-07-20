package uk.co.strangeskies.modabi.model.building;

import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.nodes.DataNode;

public interface DataLoader {
	<T> T loadData(DataNode<T> node, BufferedDataSource data);
}
