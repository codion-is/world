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

import is.codion.framework.db.EntityConnectionProvider;
import is.codion.framework.demos.world.domain.api.World.Lookup;
import is.codion.framework.domain.entity.Entity;
import is.codion.framework.json.domain.EntityObjectMapper;
import is.codion.swing.framework.model.SwingEntityTableModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

import static is.codion.common.Text.textFileContents;
import static is.codion.framework.json.domain.EntityObjectMapper.entityObjectMapper;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public final class LookupTableModel extends SwingEntityTableModel {

	public enum ExportFormat {
		CSV {
			@Override
			public String defaultFileName() {
				return "export.csv";
			}
		},
		JSON {
			@Override
			public String defaultFileName() {
				return "export.json";
			}
		};

		public abstract String defaultFileName();
	}

	private final EntityObjectMapper objectMapper = entityObjectMapper(entities());

	LookupTableModel(EntityConnectionProvider connectionProvider) {
		super(Lookup.TYPE, connectionProvider);
		objectMapper.setIncludeNullValues(false);
	}

	public void export(File file, ExportFormat format) throws IOException {
		requireNonNull(file);
		requireNonNull(format);
		switch (format) {
			case CSV -> exportCSV(file);
			case JSON -> exportJSON(file);
			default -> throw new IllegalArgumentException("Unknown export format: " + format);
		}
	}

	public void importJSON(File file) throws IOException {
		List<Entity> entities = objectMapper.deserializeEntities(textFileContents(file, UTF_8));
		clear();
		conditionModel().clear();
		addItemsAtSorted(0, entities);
	}

	private void exportCSV(File file) throws IOException {
		Files.writeString(file.toPath(), rowsAsDelimitedString(','));
	}

	private void exportJSON(File file) throws IOException {
		Collection<Entity> entities = selectionModel().isSelectionEmpty() ? items() : selectionModel().getSelectedItems();
		Files.writeString(file.toPath(), objectMapper.serializeEntities(entities));
	}
}
