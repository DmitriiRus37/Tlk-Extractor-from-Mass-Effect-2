package ru;

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
public class FxmlController {

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
    private TextField statusTlkToXml;
    @FXML
    private TextField textFieldStatusTlkToXml;
    @FXML
    public ProgressBar progressBarTlkToXml;
    boolean textFieldOutputChosenTlkToXml = false;

    @FXML
    private void selectInputTlkFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TLK files", "*.tlk"));
        File f = fc.showOpenDialog(app.getPrimaryStage());
        if (f != null) {
            textFieldInputPathTlkToXml.setText(f.getAbsolutePath());
            if (!textFieldOutputChosenTlkToXml) {
                textFieldIOutputPathTlkToXml.setText(FilenameUtils.removeExtension(f.getAbsolutePath())+".xml");
            }
        }
    }

    @FXML
    private void selectOutputXmlFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File f = fc.showOpenDialog(null);
        if (f!=null) {
            textFieldIOutputPathTlkToXml.setText(f.getAbsolutePath());
            textFieldOutputChosenTlkToXml = true;
        }
    }

    @FXML
    private void startExportTlkToXml() throws IOException, XMLStreamException {
        statusTlkToXml.setText("in progress");
        progressBarTlkToXml.setProgress(0.0);
        TlkFile tf = new TlkFile();
        tf.loadTlkData(textFieldInputPathTlkToXml.getText(), true);
        tf.storeToFile(textFieldIOutputPathTlkToXml.getText(), FileFormat.XML, this);
        statusTlkToXml.setText("DONE");
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
    private TextField statusXmlToTlk;
    @FXML
    public ProgressBar progressBarXmlToTlk;
    boolean textFieldOutputChosenXmlToTlk = false;

    @FXML
    private void selectInputXmlFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File f = fc.showOpenDialog(app.getPrimaryStage());
        textFieldInputPathXmlToTlk.setText(f.getAbsolutePath());
        if (!textFieldOutputChosenXmlToTlk) {
            textFieldIOutputPathXmlToTlk.setText(FilenameUtils.removeExtension(f.getAbsolutePath())+".tlk");
        }
    }

    @FXML
    private void selectOutputTlkFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TLK files", "*.tlk"));
        File f = fc.showOpenDialog(null);
        textFieldIOutputPathXmlToTlk.setText(f.getAbsolutePath());
        textFieldOutputChosenXmlToTlk = true;
    }

    @FXML
    private void startExportXmlToTlk() throws IOException, ParserConfigurationException, SAXException {
        statusXmlToTlk.setText("in progress");
        progressBarXmlToTlk.setProgress(0.0);
        HuffmanCompression hc = new HuffmanCompression();
        hc.loadInputData(textFieldInputPathXmlToTlk.getText(), FileFormat.XML, true);
        hc.saveToTlkFile(textFieldIOutputPathXmlToTlk.getText(), true);
        statusXmlToTlk.setText("DONE");
    }

}
