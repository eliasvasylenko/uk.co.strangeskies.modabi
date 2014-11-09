package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.utilities.collection.ComputingMap;
import uk.co.strangeskies.utilities.collection.LRUCacheComputingMap;

public class ProcessingContextImpl {
	public enum CacheScope {
		MANAGER_GLOBAL, PROCESS_LOCAL
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

	private final List<SchemaNode.Effective<?, ?>> bindingNodeStack;
	private final Bindings bindings;// TODO erase bindings in failed sections

	private final Map<MetaKey<?, ?>, ComputingMap<?, ?>> metaMap;

	public ProcessingContextImpl() {
		bindingNodeStack = Collections.emptyList();
		this.bindings = new Bindings();
		metaMap = new HashMap<>();
	}

	public ProcessingContextImpl(ProcessingContextImpl parentContext) {
		bindingNodeStack = parentContext.bindingNodeStack;
		bindings = parentContext.bindings;
	}

	public ProcessingContextImpl(ProcessingContextImpl parentContext,
			SchemaNode.Effective<?, ?> bindingNode) {
		List<SchemaNode.Effective<?, ?>> bindingNodeStack = new ArrayList<>(
				parentContext.bindingNodeStack);
		bindingNodeStack.add(bindingNode);
		this.bindingNodeStack = Collections.unmodifiableList(bindingNodeStack);
		bindings = parentContext.bindings;
	}

	public List<SchemaNode.Effective<?, ?>> bindingNodeStack() {
		return bindingNodeStack;
	}

	public Bindings bindings() {
		return bindings;
	}

	public final <K, V> ComputingMap<K, V> getBufferMap(Class<K> key,
			Class<V> value) {
		return getBufferMap(new MetaKey<>(key, value));
	}

	@SuppressWarnings("unchecked")
	private final <K, V> ComputingMap<K, V> getBufferMap(MetaKey<K, V> metaKey) {
		return (ComputingMap<K, V>) metaMap.get(metaKey);
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
		MetaKey<K, V> metaKey = new MetaKey<>(key, value);
		ComputingMap<K, V> cache = getBufferMap(metaKey);

		if (cache == null)
			metaMap.put(metaKey, cache = new LRUCacheComputingMap<>(computation, 50));

		return cache;
	}
}
