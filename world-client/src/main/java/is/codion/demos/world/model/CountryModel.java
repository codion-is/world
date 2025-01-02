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
 * Copyright (c) 2023 - 2025, Björn Darri Sigurðsson.
 */
package is.codion.demos.world.model;

import is.codion.framework.db.EntityConnectionProvider;
import is.codion.swing.framework.model.SwingEntityModel;

public final class CountryModel extends SwingEntityModel {

	CountryModel(EntityConnectionProvider connectionProvider) {
		super(new CountryTableModel(connectionProvider));
		SwingEntityModel cityModel = new SwingEntityModel(new CityTableModel(connectionProvider));
		SwingEntityModel countryLanguageModel = new SwingEntityModel(new CountryLanguageTableModel(connectionProvider));
		detailModels().add(cityModel, countryLanguageModel);
	}
}
