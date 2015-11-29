package uk.co.strangeskies.modabi.mapping;

import java.util.List;

import uk.co.strangeskies.modabi.schema.Model;

/*
 * Ways of doing this:
 * 
 * 1) Unbind to structured data, map structured data to other structured data, rebind
 * 
 * 2) Unbind to objects at specified nodes, rebind objects to certain nodes of another schema.
 */
public interface Mapping<F, T> {
	Model<F> fromModel();

	Model<T> toModel();

	T map(F from);

	List<NodeMapping<?, ?>> children();
}
