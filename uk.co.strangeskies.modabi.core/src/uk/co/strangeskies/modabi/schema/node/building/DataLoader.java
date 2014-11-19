package uk.co.strangeskies.modabi.schema.node.building;

import java.util.List;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.node.DataNode;

public interface DataLoader {
	<T> List<T> loadData(DataNode<T> node, DataSource data);
}
