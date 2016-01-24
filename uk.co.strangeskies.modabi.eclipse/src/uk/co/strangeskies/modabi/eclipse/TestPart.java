package uk.co.strangeskies.modabi.eclipse;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.fx.core.di.LocalInstance;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import uk.co.strangeskies.fx.FXUtilities;

public class TestPart {
	@Inject
	IEclipseContext context;

	@FXML
	private Pane chartPane;

	@FXML
	private Label noSelectionLabel;

	@PostConstruct
	void initialise(BorderPane container, @LocalInstance FXMLLoader loader) {
		container.setCenter(FXUtilities.loadIntoController(loader, this));
	}
}
