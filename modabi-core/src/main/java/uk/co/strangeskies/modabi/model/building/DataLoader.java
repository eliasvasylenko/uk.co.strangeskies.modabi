package uk.co.strangeskies.modabi.model.building;

import java.util.List;

import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.nodes.DataNode;

public interface DataLoader {
	<T> List<T> loadData(DataNode<T> node, BufferedDataSource data);
}
