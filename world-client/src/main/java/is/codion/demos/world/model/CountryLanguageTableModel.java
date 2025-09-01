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

import is.codion.demos.world.domain.api.World.CountryLanguage;
import is.codion.framework.db.EntityConnectionProvider;
import is.codion.framework.domain.entity.Entity;
import is.codion.swing.framework.model.SwingEntityTableModel;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.util.Collection;

public final class CountryLanguageTableModel extends SwingEntityTableModel {

	private final DefaultPieDataset<String> chartDataset = new DefaultPieDataset<>();

	CountryLanguageTableModel(EntityConnectionProvider connectionProvider) {
		super(CountryLanguage.TYPE, connectionProvider);
		editModel().initializeComboBoxModels(CountryLanguage.COUNTRY_FK);
		items().included().addConsumer(this::refreshChartDataset);
	}

	public PieDataset<String> chartDataset() {
		return chartDataset;
	}

	private void refreshChartDataset(Collection<Entity> countryLanguages) {
		chartDataset.clear();
		countryLanguages.forEach(countryLanguage ->
						chartDataset.setValue(countryLanguage.get(CountryLanguage.LANGUAGE),
										countryLanguage.get(CountryLanguage.NO_OF_SPEAKERS)));
	}
}
