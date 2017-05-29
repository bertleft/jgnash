/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2017 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.uifx.dialog;

import java.util.Collections;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;

import jgnash.convert.imports.ImportFilter;
import jgnash.uifx.skin.StyleClass;
import jgnash.uifx.util.InjectFXML;
import jgnash.uifx.util.JavaFXUtils;
import jgnash.uifx.util.TableViewManager;

import static jgnash.util.LogUtil.logSevere;

/**
 * Controller for managing import scripts
 *
 * @author Craig Cavanaugh
 */
public class ImportScriptsDialogController {

    private static final String PREF_NODE = "/jgnash/uifx/dialog/ImportScriptsDialogController";

    private static final double[] PREF_COLUMN_WEIGHTS = {0, 50, 50};

    private static final String DEFAULT = "default";

    @InjectFXML
    private final ObjectProperty<Scene> parent = new SimpleObjectProperty<>();

    @FXML
    private Button upButton;

    @FXML
    private Button downButton;

    @FXML
    private ResourceBundle resources;

    @FXML
    private TableView<Script> tableView;

    private final IntegerProperty scriptCountProperty = new SimpleIntegerProperty();

    private final IntegerProperty selectedIndexProperty = new SimpleIntegerProperty();

    @SuppressWarnings("FieldCanBeLocal")
    private TableViewManager<Script> tableViewManager;

    @FXML
    private void initialize() {

        // simplify binding process
        scriptCountProperty.bind(Bindings.size(tableView.getItems()));
        selectedIndexProperty.bind(tableView.selectionModelProperty().get().selectedIndexProperty());

        final TableColumn<Script, Boolean> enabledColumn = new TableColumn<>(resources.getString("Column.Enabled"));
        enabledColumn.setCellValueFactory(param -> param.getValue().enabledProperty);
        enabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(enabledColumn));

        enabledColumn.setEditable(true);
        tableView.getColumns().add(enabledColumn);

        final TableColumn<Script, String> descriptionColumn = new TableColumn<>(resources.getString("Column.Description"));
        descriptionColumn.setCellValueFactory(param -> param.getValue().descriptionProperty);
        tableView.getColumns().add(descriptionColumn);

        final TableColumn<Script, String> scriptColumn = new TableColumn<>(resources.getString("Column.Script"));
        scriptColumn.setCellValueFactory(param -> param.getValue().pathProperty);
        tableView.getColumns().add(scriptColumn);

        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getStylesheets().addAll(StyleClass.HIDE_HORIZONTAL_CSS);

        upButton.disableProperty().bind(scriptCountProperty.lessThan(2)
                .or(selectedIndexProperty.lessThan(1)));

        downButton.disableProperty().bind(scriptCountProperty.lessThan(2)
                .or(selectedIndexProperty.greaterThan(scriptCountProperty.subtract(1)))
                .or(selectedIndexProperty.lessThan(0)));


        tableViewManager = new TableViewManager<>(tableView, PREF_NODE);
        tableViewManager.setColumnWeightFactory(column -> PREF_COLUMN_WEIGHTS[column]);
        tableViewManager.setPreferenceKeyFactory(() -> DEFAULT);

        JavaFXUtils.runLater(this::loadScripts);

        JavaFXUtils.runLater(tableViewManager::restoreLayout);
    }

    private void loadScripts() {
        for (String filter : ImportFilter.KNOWN_SCRIPTS) {
            try {

                final Script script = new Script();

                ImportFilter importFilter = new ImportFilter(filter);

                script.descriptionProperty.setValue(importFilter.getDescription());
                script.pathProperty.setValue(filter);
                //script.enabledProperty.setValue(true);

                tableView.getItems().add(script);
            } catch (final Exception e) {
                logSevere(ImportScriptsDialogController.class, e);
            }
        }
    }

    @FXML
    private void handleCloseAction() {
        ((Stage) parent.get().getWindow()).close();
    }

    @FXML
    private void handleUpAction() {
        Collections.swap(tableView.getItems(), selectedIndexProperty.get(), selectedIndexProperty.get() - 1);
    }

    @FXML
    private void handleDownAction() {
        Collections.swap(tableView.getItems(), selectedIndexProperty.get(), selectedIndexProperty.get() + 1);
    }

    private class Script {
        final BooleanProperty enabledProperty = new SimpleBooleanProperty(false);

        final StringProperty descriptionProperty = new SimpleStringProperty();

        final StringProperty pathProperty = new SimpleStringProperty();
    }
}