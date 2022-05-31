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
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

@NoArgsConstructor
@Getter @Setter
public class FxmlControllerTlkToXml {

    public File inputTlkFile;
    public File outputXmlFile;
    public App app;

    @FXML
    private Button buttonToSelectInputTlkFile;
    @FXML
    private Button buttonToSelectOutputXmlFile;
    @FXML
    private Button startTlkToXml;
    @FXML
    private TextField textFieldInputPathTlkToXml;
    @FXML
    private TextField textFieldIOutputPathTlkToXml;
    @FXML
    private TextField textFieldStatusTlkToXml;
    @FXML
    public ProgressBar progressBarTlkToXml;
    boolean textFieldOutputChosenTlkToXml = false;

    @FXML
    private void selectInputTlkFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TLK files", "*.tlk"));
        File f = fc.showOpenDialog(app.getPrimaryStage());
        textFieldInputPathTlkToXml.setText(f.getAbsolutePath());
        if (!textFieldOutputChosenTlkToXml) {
            textFieldIOutputPathTlkToXml.setText(FilenameUtils.removeExtension(f.getAbsolutePath())+".xml");
        }
    }

    @FXML
    private void selectOutputXmlFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File f = fc.showOpenDialog(null);
        textFieldIOutputPathTlkToXml.setText(f.getAbsolutePath());
        textFieldOutputChosenTlkToXml = true;
    }

    @FXML
    private void startExportTlkToXml(ActionEvent event) throws IOException, XMLStreamException {
        progressBarTlkToXml.setProgress(0.0);
        TlkFile tf = new TlkFile();
        tf.loadTlkData(textFieldInputPathTlkToXml.getText(), true);
        tf.storeToFile(textFieldIOutputPathTlkToXml.getText(), FileFormat.XML, this);
    }

    public File inputXmlFile;
    public File outputTlkFile;

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
    private TextField textFieldStatusXmlToTlk;
    @FXML
    public ProgressBar progressBarXmlToTlk;
    boolean textFieldOutputChosenXmlToTlk = false;

    @FXML
    private void selectInputXmlFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File f = fc.showOpenDialog(app.getPrimaryStage());
        textFieldInputPathXmlToTlk.setText(f.getAbsolutePath());
        if (!textFieldOutputChosenXmlToTlk) {
            textFieldIOutputPathXmlToTlk.setText(FilenameUtils.removeExtension(f.getAbsolutePath())+".tlk");
        }
    }

    @FXML
    private void selectOutputTlkFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TLK files", "*.tlk"));
        File f = fc.showOpenDialog(null);
        textFieldIOutputPathXmlToTlk.setText(f.getAbsolutePath());
        textFieldOutputChosenXmlToTlk = true;
    }

    @FXML
    private void startExportXmlToTlk(ActionEvent event) throws IOException, ParserConfigurationException, SAXException {
        progressBarXmlToTlk.setProgress(0.0);
        HuffmanCompression hc = new HuffmanCompression();
        hc.loadInputData(textFieldInputPathXmlToTlk.getText(), FileFormat.XML, true);
        hc.saveToTlkFile(textFieldIOutputPathXmlToTlk.getText(), true);
    }

}
