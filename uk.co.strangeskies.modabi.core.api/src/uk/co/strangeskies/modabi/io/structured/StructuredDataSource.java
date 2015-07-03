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

import java.util.Set;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.io.DataSource;

public interface StructuredDataSource {
	public StructuredDataState currentState();

	public Namespace getDefaultNamespaceHint();

	public Set<Namespace> getNamespaceHints();

	public Set<String> getComments();

	public QualifiedName startNextChild();

	public QualifiedName peekNextChild();

	public default void startNextChild(QualifiedName name) {
		QualifiedName nextName = startNextChild();
		if (!nextName.equals(name)) {
			throw new SchemaException("Next child '" + nextName
					+ "' does not match expected name '" + name + "'");
		}
	}

	public Set<QualifiedName> getProperties();

	public DataSource readProperty(QualifiedName name);

	public DataSource readContent();

	public boolean hasNextChild();

	public default boolean skipNextChild() {
		boolean hasNext = hasNextChild();
		if (hasNext) {
			startNextChild();
			skipChildren();
			endChild();
		}
		return hasNext;
	}

	public default void skipChildren() {
		while (skipNextChild())
			;
	}

	/**
	 * throws an exception if there are more children, so call skipChildren()
	 * first, or call endChildEarly, if you want to ignore them.
	 */
	public void endChild();

	public default void endChildEarly() {
		skipChildren();
		endChild();
	}

	public int depth();

	public int indexAtDepth();

	public default <T extends StructuredDataTarget> T pipeNextChild(T output) {
		pipeNamespaceHints(output);

		QualifiedName childElement;

		int depth = 0;
		do {
			while ((childElement = startNextChild()) != null) {
				output.nextChild(childElement);

				pipeDataAtChild(output);

				depth++;
			}

			if (depth-- > 0) {
				output.endChild();
				endChild();
			}
		} while (depth > 0);

		return output;
	}

	public default <T extends StructuredDataTarget> T pipeNamespaceHints(T output) {
		if (getDefaultNamespaceHint() != null)
			output.registerDefaultNamespaceHint(getDefaultNamespaceHint());
		for (Namespace hint : getNamespaceHints())
			output.registerNamespaceHint(hint);

		return output;
	}

	public default <T extends StructuredDataTarget> T pipeDataAtChild(T output) {
		pipeNamespaceHints(output);

		for (QualifiedName property : getProperties())
			readProperty(property).pipe(output.writeProperty(property)).terminate();

		DataSource content = readContent();
		if (content != null)
			content.pipe(output.writeContent()).terminate();

		return output;
	}

	public default BufferedStructuredDataSource bufferNextChild() {
		return pipeNextChild(new BufferingStructuredDataTarget()).buffer();
	}
}
