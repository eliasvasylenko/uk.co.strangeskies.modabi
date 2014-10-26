package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.function.Function;

import uk.co.strangeskies.utilities.collection.ComputingMap;

public class ProcessingContextImpl {
	public enum CacheScope {
		MANAGER_GLOBAL, SCHEMA_GLOBAL, PROCESS_GLOBAL, CHILD_NODES
	}

	/**
	 * Returns a unique buffer map for each key/value pair class requested. The
	 * same underlying data is accessed regardless of the computation function and
	 * scope requested.
	 * 
	 * @param key
	 *          The class of key from which to compute and reference buffered
	 *          values.
	 * @param value
	 *          The class of computed and buffered values.
	 * @param computation
	 *          This is the computation function to be applied to keys added to
	 *          the returned map view.
	 * @param scope
	 *          This is the scope values computed and buffered using the returned
	 *          map view should be retained within.
	 * @return
	 */
	public final <K, V> ComputingMap<K, V> getBufferMap(Class<K> key,
			Class<V> value, Function<K, V> computation, CacheScope scope) {
		return null;
	}
}
