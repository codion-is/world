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

import is.codion.demos.world.domain.api.World.Country;
import is.codion.demos.world.model.ContinentModel;
import is.codion.swing.framework.ui.EntityPanel;
import is.codion.swing.framework.ui.EntityTablePanel;

import org.jfree.chart.ChartPanel;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

import static is.codion.demos.world.ui.ChartPanels.createBarChartPanel;
import static is.codion.demos.world.ui.ChartPanels.createPieChartPanel;
import static is.codion.swing.common.ui.component.Components.*;
import static is.codion.swing.common.ui.component.Sizes.preferredHeight;
import static is.codion.swing.common.ui.layout.Layouts.borderLayout;
import static java.awt.event.KeyEvent.VK_1;
import static java.awt.event.KeyEvent.VK_2;

final class ContinentPanel extends EntityPanel {

	ContinentPanel(ContinentModel continentModel) {
		super(continentModel, new ContinentTablePanel(continentModel.tableModel()));
	}

	@Override
	protected void initializeUI() {
		ContinentModel model = (ContinentModel) model();

		ChartPanel populationChartPanel = createPieChartPanel(this, model.populationDataset(), "Population");
		ChartPanel surfaceAreaChartPanel = createPieChartPanel(this, model.surfaceAreaDataset(), "Surface area");
		ChartPanel gnpChartPanel = createPieChartPanel(this, model.gnpDataset(), "GNP");
		ChartPanel lifeExpectancyChartPanel = createBarChartPanel(this, model.lifeExpectancyDataset(), "Life expectancy", "Continent", "Years");
		preferredHeight(lifeExpectancyChartPanel, 120);

		Dimension pieChartSize = new Dimension(260, 260);
		populationChartPanel.setPreferredSize(pieChartSize);
		surfaceAreaChartPanel.setPreferredSize(pieChartSize);
		gnpChartPanel.setPreferredSize(pieChartSize);

		JPanel pieChartChartPanel = gridLayoutPanel(1, 3)
						.add(populationChartPanel)
						.add(surfaceAreaChartPanel)
						.add(gnpChartPanel)
						.build();

		JPanel chartPanel = borderLayoutPanel()
						.northComponent(lifeExpectancyChartPanel)
						.centerComponent(pieChartChartPanel)
						.build();

		EntityTablePanel countryTablePanel =
						new EntityTablePanel(model.detailModels().get(Country.TYPE).tableModel(),
										config -> config
														.includeConditions(false)
														.includeToolBar(false));
		preferredHeight(countryTablePanel, 300);

		JTabbedPane tabbedPane = tabbedPane()
						.tabBuilder("Charts", chartPanel)
						.mnemonic(VK_1)
						.add()
						.tabBuilder("Countries", countryTablePanel.initialize())
						.mnemonic(VK_2)
						.add()
						.build();

		setLayout(borderLayout());

		add(tablePanel().initialize(), BorderLayout.CENTER);
		add(tabbedPane, BorderLayout.SOUTH);

		setupKeyboardActions();
		setupNavigation();
	}
}
