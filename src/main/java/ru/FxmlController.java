package ru;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

@NoArgsConstructor
@Getter @Setter
public class FxmlController {

    public File inputTlkFile;
    public File outputXmlFile;
    public App app;

    @FXML
    private Button buttonToSelectInputTlkFile;
    @FXML
    private Button buttonToSelectOutputXmlFile;
    @FXML
    private TextField textFieldInputPath;
    @FXML
    private TextField textFieldIOutputPath;
    @FXML
    private TextField textFieldIOStatus;

    @FXML
    private void selectInputTlkFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TLK files", "*.tlk"));
        File f = fc.showOpenDialog(app.getPrimaryStage());
    }

    @FXML
    private void selectOutputXmlFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File f = fc.showOpenDialog(null);
    }



}
