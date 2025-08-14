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

import is.codion.swing.framework.model.SwingEntityTableModel;
import is.codion.swing.framework.ui.EntityTablePanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.general.PieDataset;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.util.function.Consumer;

import static is.codion.demos.world.ui.ChartPanels.createPieChartPanel;
import static is.codion.swing.common.ui.component.Components.borderLayoutPanel;
import static is.codion.swing.common.ui.component.Components.tabbedPane;
import static java.awt.event.KeyEvent.VK_1;
import static java.awt.event.KeyEvent.VK_2;

abstract class ChartTablePanel extends EntityTablePanel {

	private final ChartPanel chartPanel;

	protected ChartTablePanel(SwingEntityTableModel tableModel, PieDataset<String> chartDataset,
														String chartTitle) {
		this(tableModel, chartDataset, chartTitle, config -> {});
	}

	protected ChartTablePanel(SwingEntityTableModel tableModel, PieDataset<String> chartDataset,
														String chartTitle, Consumer<Config> config) {
		super(tableModel, config);
		setPreferredSize(new Dimension(200, 200));
		chartPanel = createPieChartPanel(chartDataset, chartTitle);
	}

	@Override
	protected final void layoutPanel(JComponent tableComponent, JPanel southPanel) {
		super.layoutPanel(tabbedPane()
						.tab("Table")
						.component(borderLayoutPanel()
										.center(tableComponent)
										.south(southPanel)
										.build())
						.mnemonic(VK_1)
						.add()
						.tab("Chart")
						.component(chartPanel)
						.mnemonic(VK_2)
						.add()
						.build(), southPanel);
	}
}
