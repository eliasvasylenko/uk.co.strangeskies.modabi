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

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataTarget;

/**
 * Not entirely sure what the most sensible API is for this... Will have to work
 * at it from the other end to see what is needed. Eventually this should
 * provide means to update an existing data representation with an updated
 * object binding. The idea is to allow updates whilst retaining formatting
 * wherever possible.
 * 
 * There may turn out to be limits to the feasibility of this feature...
 * 
 * @author eli
 *
 */
public interface RewritableStructuredDataSource extends
		NavigableStructuredDataSource {
	DataTarget overwriteProperty(QualifiedName name);

	@Override
	public void endChild();

	public boolean deleteChild();

	public StructuredDataTarget editAtLocation();

	public Object anchorLocation();

	public void navigateToAnchor(Object key);

	public default StructuredDataTarget editAtAnchor(Object key) {
		navigateToAnchor(key);
		return editAtLocation();
	}
}
