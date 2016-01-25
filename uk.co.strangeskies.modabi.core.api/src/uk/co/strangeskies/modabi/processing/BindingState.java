/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.processing;

import java.util.List;

import uk.co.strangeskies.modabi.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.TypedObject;

public interface BindingState {
	List<SchemaNode.Effective<?, ?>> bindingNodeStack();

	default SchemaNode.Effective<?, ?> bindingNode() {
		return bindingNode(0);
	}

	default SchemaNode.Effective<?, ?> bindingNode(int parent) {
		return bindingNodeStack().get(bindingNodeStack().size() - (1 + parent));
	}

	List<TypedObject<?>> bindingTargetStack();

	default TypedObject<?> bindingTarget() {
		return bindingTarget(0);
	}

	default TypedObject<?> bindingTarget(int parent) {
		return bindingTargetStack().get(bindingTargetStack().size() - (1 + parent));
	}

	Bindings bindings();
}
