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
package uk.co.strangeskies.modabi.io;

import java.net.URI;

import uk.co.strangeskies.modabi.ModabiProperties;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.structured.StructuredDataState;
import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;
import uk.co.strangeskies.text.properties.PropertyConfiguration;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;

@PropertyConfiguration(keyCase = KeyCase.LOWER, keySplitString = ".", key = "%3$s")
public interface IoProperties extends Properties<IoProperties> {
	ModabiProperties modabiProperties();

	Localized<String> nextChildDoesNotExist();

	Localized<String> overlappingDefaultNamespaceHints();

	Localized<String> unexpectedInputItem(QualifiedName nextName, QualifiedName name);

	Localized<String> illegalState(StructuredDataState state);

	Localized<String> illegalState(DataStreamState state);

	Localized<String> illegalStateTransition(StructuredDataState exitState, StructuredDataState entryState);

	Localized<String> illegalStateTransition(DataStreamState exitState, DataStreamState entryState);

	Localized<String> invalidOperationOnProperty(QualifiedName name);

	Localized<String> invalidOperationOnContent();

	Localized<String> invalidLocation(URI location);
}
