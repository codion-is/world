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
 * Copyright (c) 2023 - 2024, Björn Darri Sigurðsson.
 */
package is.codion.demos.world.model;

import is.codion.common.value.Value;
import is.codion.common.value.ValueObserver;
import is.codion.demos.world.domain.api.World.City;
import is.codion.demos.world.domain.api.World.Country;
import is.codion.framework.db.EntityConnectionProvider;
import is.codion.framework.domain.entity.Entity;
import is.codion.framework.domain.entity.attribute.ForeignKey;
import is.codion.swing.framework.model.SwingEntityEditModel;
import is.codion.swing.framework.model.component.EntityComboBoxModel;

import java.util.Objects;

public final class CountryEditModel extends SwingEntityEditModel {

	private final Value<Double> averageCityPopulation = Value.value();

	CountryEditModel(EntityConnectionProvider connectionProvider) {
		super(Country.TYPE, connectionProvider);
		initializeComboBoxModels(Country.CAPITAL_FK);
		entity().addConsumer(country ->
						averageCityPopulation.set(averageCityPopulation(country)));
	}

	@Override
	public EntityComboBoxModel createForeignKeyComboBoxModel(ForeignKey foreignKey) {
		EntityComboBoxModel comboBoxModel = super.createForeignKeyComboBoxModel(foreignKey);
		if (foreignKey.equals(Country.CAPITAL_FK)) {
			//only show cities for currently selected country
			entity().addConsumer(country ->
							comboBoxModel.filter().predicate().set(city ->
											country != null && Objects.equals(city.get(City.COUNTRY_FK), country)));
		}

		return comboBoxModel;
	}

	public ValueObserver<Double> averageCityPopulation() {
		return averageCityPopulation.observer();
	}

	private Double averageCityPopulation(Entity country) {
		if (country == null) {
			return null;
		}
		return connection().execute(Country.AVERAGE_CITY_POPULATION, country.get(Country.CODE));
	}
}
