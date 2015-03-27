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
package uk.co.strangeskies.modabi.schema.management.binding;

import java.util.List;

import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public interface BindingState {
	List<SchemaNode.Effective<?, ?>> bindingNodeStack();

	default SchemaNode.Effective<?, ?> bindingNode() {
		return bindingNode(0);
	}

	default SchemaNode.Effective<?, ?> bindingNode(int parent) {
		return bindingNodeStack().get(bindingNodeStack().size() - (1 + parent));
	}

	List<Object> bindingTargetStack();

	default Object bindingTarget() {
		return bindingTarget(0);
	}

	default Object bindingTarget(int parent) {
		return bindingTargetStack().get(bindingTargetStack().size() - (1 + parent));
	}

	Bindings bindings();
}
