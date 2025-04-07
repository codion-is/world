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
package is.codion.demos.world.ui;

import is.codion.demos.world.domain.api.World.CountryLanguage;
import is.codion.swing.framework.model.SwingEntityEditModel;
import is.codion.swing.framework.ui.EntityEditPanel;

import javax.swing.JPanel;

import static is.codion.swing.common.ui.component.Components.gridLayoutPanel;
import static is.codion.swing.common.ui.layout.Layouts.gridLayout;

final class CountryLanguageEditPanel extends EntityEditPanel {

	CountryLanguageEditPanel(SwingEntityEditModel editModel) {
		super(editModel);
		// Perform an update each time the IS_OFFICIAL
		// value is edited, bypassing the update confirmation
		editModel.editor().value(CountryLanguage.IS_OFFICIAL).edited().addListener(editModel()::update);
	}

	@Override
	protected void initializeUI() {
		focus().initial().set(CountryLanguage.COUNTRY_FK);

		createComboBox(CountryLanguage.COUNTRY_FK)
						.preferredWidth(120);
		createTextField(CountryLanguage.LANGUAGE);
		createCheckBox(CountryLanguage.IS_OFFICIAL);
		createTextField(CountryLanguage.PERCENTAGE)
						.columns(4);

		JPanel percentageOfficialPanel = gridLayoutPanel(1, 2)
						.add(createInputPanel(CountryLanguage.PERCENTAGE))
						.add(createInputPanel(CountryLanguage.IS_OFFICIAL))
						.build();

		setLayout(gridLayout(0, 1));

		addInputPanel(CountryLanguage.COUNTRY_FK);
		addInputPanel(CountryLanguage.LANGUAGE);
		add(percentageOfficialPanel);
	}
}
