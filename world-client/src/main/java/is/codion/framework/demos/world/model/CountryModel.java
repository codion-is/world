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

import is.codion.common.value.Value;
import is.codion.framework.db.EntityConnectionProvider;
import is.codion.framework.demos.world.domain.api.World.City;
import is.codion.framework.domain.entity.Entity;
import is.codion.swing.framework.model.SwingEntityModel;
import is.codion.swing.framework.model.SwingEntityTableModel;

public final class CountryModel extends SwingEntityModel {

  private final Value<Double> averageCityPopulationValue = Value.value();

  CountryModel(EntityConnectionProvider connectionProvider) {
    super(new CountryTableModel(connectionProvider));
    SwingEntityModel cityModel = new SwingEntityModel(new CityTableModel(connectionProvider));
    SwingEntityModel countryLanguageModel = new SwingEntityModel(new CountryLanguageTableModel(connectionProvider));
    addDetailModels(cityModel, countryLanguageModel);

    cityModel.tableModel().refresher().addRefreshListener(() ->
            averageCityPopulationValue.set(averageCityPopulation()));
    CountryEditModel countryEditModel = editModel();
    countryEditModel.setAverageCityPopulationObserver(averageCityPopulationValue.observer());
  }

  private Double averageCityPopulation() {
    if (editModel().isEntityNew()) {
      return null;
    }

    SwingEntityTableModel cityTableModel = detailModel(City.TYPE).tableModel();
    Entity country = editModel().entity();

    return Entity.castTo(City.class, cityTableModel.items()).stream()
            .filter(city -> city.isInCountry(country))
            .map(City::population)
            .mapToInt(Integer::valueOf)
            .average()
            .orElse(0d);
  }
}
