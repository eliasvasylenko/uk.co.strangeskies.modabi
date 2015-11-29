package uk.co.strangeskies.modabi.mapping;

import java.util.List;

import uk.co.strangeskies.modabi.schema.Model;

public interface MappingBuilder<F, T> {
	Model<F> fromModel();

	Model<T> toModel();

	T map(F from);

	List<NodeMapping<?, ?>> children();
}
