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
import is.codion.demos.world.model.CountryTableModel;
import is.codion.swing.common.model.worker.ProgressWorker.ProgressReporter;
import is.codion.swing.common.ui.control.Control;
import is.codion.swing.common.ui.dialog.Dialogs;
import is.codion.swing.framework.model.SwingEntityTableModel;
import is.codion.swing.framework.ui.EntityTablePanel;
import is.codion.swing.framework.ui.icon.FrameworkIcons;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import java.awt.Dimension;

import static is.codion.swing.framework.ui.EntityTablePanel.ControlKeys.PRINT;

final class CountryTablePanel extends EntityTablePanel {

	CountryTablePanel(SwingEntityTableModel tableModel) {
		super(tableModel, config -> config
						.editable(attributes -> attributes.remove(Country.CAPITAL_FK)));
	}

	@Override
	protected void setupControls() {
		control(PRINT).set(Control.builder()
						.command(this::viewCountryReport)
						.caption("Country report")
						.smallIcon(FrameworkIcons.instance().print())
						.enabled(tableModel().selection().empty().not())
						.build());
	}

	private void viewCountryReport() {
		Dialogs.progressWorkerDialog(this::fillCountryReport)
						.owner(this)
						.maximumProgress(tableModel().selection().count())
						.stringPainted(true)
						.onResult(this::viewReport)
						.execute();
	}

	private JasperPrint fillCountryReport(ProgressReporter<String> progressReporter) {
		CountryTableModel countryTableModel = (CountryTableModel) tableModel();

		return countryTableModel.fillCountryReport(progressReporter);
	}

	private void viewReport(JasperPrint countryReport) {
		Dialogs.componentDialog(new JRViewer(countryReport))
						.owner(this)
						.modal(false)
						.title("Country report")
						.size(new Dimension(800, 600))
						.show();
	}
}
