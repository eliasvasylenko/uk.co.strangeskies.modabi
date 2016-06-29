/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.eclipse.
 *
 * uk.co.strangeskies.modabi.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.eclipse.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.eclipse;

import static uk.co.strangeskies.fx.FXMLLoadBuilder.buildWith;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.fx.core.di.LocalInstance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class TestPart {
	@Inject
	IEclipseContext context;

	@FXML
	private Pane chartPane;

	@FXML
	private Label noSelectionLabel;

	@PostConstruct
	void initialise(BorderPane container, @LocalInstance FXMLLoader loader) {
		container.setCenter(buildWith(loader).controller(this).loadRoot());
	}
}
