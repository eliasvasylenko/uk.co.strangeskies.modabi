/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.io.structured;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.utilities.IdentityComparator;

/**
 * It shouldn't matter in what order attributes are added to a child, or whether
 * they are added before, after, or between other children. Because of this,
 * {@link MultiBufferingStructuredDataTarget} does not produce a
 * {@link BufferedStructuredDataSource} which tries to match input order.
 * Instead, in an effort to make it easier for consumers to deal with stream
 * order, it adds a guarantee that buffered attributes will appear before any
 * other children types when piped. Similarly, it guarantees that all global
 * namespace hints will be piped before the rest of the document begins, and
 * non-global hints will be piped before any children of the child they occur
 * in.
 *
 * @author Elias N Vasylenko
 *
 */
public class MultiBufferingStructuredDataTarget extends
		StructuredDataTargetImpl<MultiBufferingStructuredDataTarget> {
	private final List<WeakReference<BufferedStructuredDataSourceImpl>> buffers;

	public MultiBufferingStructuredDataTarget() {
		buffers = new ArrayList<>();
	}

	private void forEachHead(Consumer<StructuredDataBuffer> perform) {
		if (buffers.isEmpty())
			return;
		else if (buffers.size() == 1) {
			BufferedStructuredDataSourceImpl buffer = buffers.iterator().next().get();
			if (buffer != null) {
				perform.accept(buffer.peekHead());
			} else {
				buffers.clear();
			}
		} else {
			Set<StructuredDataBuffer> bufferHeads = new TreeSet<>(
					new IdentityComparator<>());
			forEachBuffer(buffer -> {
				StructuredDataBuffer bufferHead = buffer.peekHead();
				if (bufferHeads.add(bufferHead)) {
					perform.accept(bufferHead);
				}
			});
		}
	}

	private void forEachBuffer(Consumer<BufferedStructuredDataSourceImpl> perform) {
		if (buffers.isEmpty())
			return;
		else {
			Iterator<WeakReference<BufferedStructuredDataSourceImpl>> bufferIterator = buffers
					.iterator();

			while (bufferIterator.hasNext()) {
				BufferedStructuredDataSourceImpl buffer = bufferIterator.next().get();
				if (buffer != null) {
					perform.accept(buffer);
				} else {
					bufferIterator.remove();
				}
			}
		}
	}

	@Override
	public void registerDefaultNamespaceHintImpl(Namespace namespace) {
		forEachHead(h -> h.setDefaultNamespaceHint(namespace));
	}

	@Override
	public void registerNamespaceHintImpl(Namespace namespace) {
		forEachHead(h -> h.addNamespaceHint(namespace));
	}

	@Override
	public DataTarget writePropertyImpl(QualifiedName name) {
		BufferingDataTarget target = new BufferingDataTarget();
		forEachHead(h -> h.addProperty(name, target));
		return target;
	}

	@Override
	public DataTarget writeContentImpl() {
		BufferingDataTarget target = new BufferingDataTarget();
		forEachHead(h -> h.addContent(target));
		return target;
	}

	@Override
	public void nextChildImpl(QualifiedName name) {
		StructuredDataBuffer child = new StructuredDataBuffer(name);
		forEachBuffer(b -> b.pushHead(child));
	}

	@Override
	public void endChildImpl() {
		forEachBuffer(b -> b.popHead());
	}

	private BufferedStructuredDataSource openBuffer(boolean consumable) {
		BufferedStructuredDataSourceImpl buffer = new BufferedStructuredDataSourceImpl(
				consumable);
		buffers.add(new WeakReference<>(buffer));
		return buffer;
	}

	public BufferedStructuredDataSource openBuffer() {
		return openBuffer(false);
	}

	public StructuredDataSource openConsumableBuffer() {
		return openBuffer(true);
	}

	@Override
	public void commentImpl(String comment) {
		forEachHead(h -> h.comment(comment));
	}
}
