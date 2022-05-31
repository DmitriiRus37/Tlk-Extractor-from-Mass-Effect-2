package ru;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

@NoArgsConstructor
@Getter @Setter
public class FxmlControllerXmlToTlk {

    public File inputXmlFile;
    public File outputTlkFile;
    public App app;

    @FXML
    private Button buttonToSelectInputXmlFile;
    @FXML
    private Button buttonToSelectOutputTlkFile;
    @FXML
    private Button startXmlToTlk;
    @FXML
    private TextField textFieldInputPathXmlToTlk;
    @FXML
    private TextField textFieldIOutputPathXmlToTlk;
    @FXML
    private TextField textFieldIOStatus;
    @FXML
    public ProgressBar progressBarXmlToTlk;
    boolean textFieldOutputChoosen = false;

    @FXML
    private void selectInputXmlFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File f = fc.showOpenDialog(app.getPrimaryStage());
        textFieldInputPathXmlToTlk.setText(f.getAbsolutePath());
        if (!textFieldOutputChoosen) {
            textFieldIOutputPathXmlToTlk.setText(FilenameUtils.removeExtension(f.getAbsolutePath())+".tlk");
        }
    }

    @FXML
    private void selectOutputTlkFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TLK files", "*.tlk"));
        File f = fc.showOpenDialog(null);
        textFieldIOutputPathXmlToTlk.setText(f.getAbsolutePath());
        textFieldOutputChoosen = true;
    }

    @FXML
    private void startExportXmlToTlk(ActionEvent event) throws IOException, ParserConfigurationException, SAXException {
        progressBarXmlToTlk.setProgress(0.0);
        HuffmanCompression hc = new HuffmanCompression();
        hc.loadInputData(inputXmlFile.getAbsolutePath(), FileFormat.XML, true);
        hc.saveToTlkFile(outputTlkFile.getAbsolutePath(), true);
    }

}
