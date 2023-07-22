/*
 * This file is part of Codion World Demo.
 *
 * Codion World Demo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Codion World Demo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Codion World Demo.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2023, Björn Darri Sigurðsson.
 */
package is.codion.framework.demos.world.model;

import is.codion.common.db.exception.DatabaseException;
import is.codion.common.user.User;
import is.codion.framework.db.EntityConnectionProvider;
import is.codion.framework.db.local.LocalEntityConnectionProvider;
import is.codion.framework.demos.world.domain.WorldImpl;
import is.codion.framework.demos.world.domain.api.World.Country;

import org.junit.jupiter.api.Test;

import static is.codion.framework.db.condition.Condition.where;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CountryEditModelTest {

  private static final User UNIT_TEST_USER =
          User.parse(System.getProperty("codion.test.user", "scott:tiger"));

  @Test
  void averageCityPopulation() throws DatabaseException {
    try (EntityConnectionProvider connectionProvider = createConnectionProvider()) {
      CountryEditModel countryEditModel = new CountryEditModel(connectionProvider);
      countryEditModel.setEntity(connectionProvider.connection()
              .selectSingle(where(Country.NAME).equalTo("Afghanistan")));
      assertEquals(583_025, countryEditModel.averageCityPopulationObserver().get());
      countryEditModel.setEntity(null);
      assertNull(countryEditModel.averageCityPopulationObserver().get());
    }
  }

  private EntityConnectionProvider createConnectionProvider() {
    return LocalEntityConnectionProvider.builder()
            .domain(new WorldImpl())
            .user(UNIT_TEST_USER)
            .build();
  }
}
