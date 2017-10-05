/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.test.
 *
 * uk.co.strangeskies.modabi.core.test is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.test.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.core.test;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.modabi.testing.TestBase;

public class ReferenceTest extends TestBase {
  private static final String NAMED_VALUE_MODEL = "namedValue";
  private static final String STRING_REFERENCS_MODEL = "stringReferences";

  @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
  public void loadReferenceTestSchema() {
    Assert.assertNotNull(getModel(NAMED_VALUE_MODEL));
    Assert.assertNotNull(getModel(STRING_REFERENCS_MODEL));
  }
}
