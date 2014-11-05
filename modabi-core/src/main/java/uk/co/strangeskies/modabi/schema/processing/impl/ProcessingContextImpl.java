package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.function.Function;

import uk.co.strangeskies.utilities.collection.ComputingMap;

public class ProcessingContextImpl {
	public enum CacheScope {
		MANAGER, PROCESS, STACK
	}

	private static class MetaKey<K, V> {
		private final Class<K> key;
		private final Class<V> value;
		private final int hash;

		public MetaKey(Class<K> key, Class<V> value) {
			this.key = key;
			this.value = value;
			hash = key.hashCode() ^ value.hashCode();
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof MetaKey))
				return false;

			MetaKey<?, ?> thatMetaKey = (MetaKey<?, ?>) that;

			return key.equals(thatMetaKey.key) && value.equals(thatMetaKey.value);
		}

		@Override
		public int hashCode() {
			return hash;
		}
	}

	private final ComputingMap<MetaKey<?, ?>, ComputingMap<?, ?>> metaMap;

	public ProcessingContextImpl() {
		metaMap = null;
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
	@SuppressWarnings("unchecked")
	public final <K, V> ComputingMap<K, V> getBufferMap(Class<K> key,
			Class<V> value, Function<K, V> computation, CacheScope scope) {
		return (ComputingMap<K, V>) metaMap.get(new MetaKey<>(key, value));
	}
}
