import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class MainClass {

    public static void main(String[] args) throws IOException, XMLStreamException, ParserConfigurationException, SAXException {
       args = new String[]{"load", "/home/gur/Downloads/dir3/Mass Effect 3 International Language Pack/BIOGame_RUS.tlk"};

        if (!args[0].equals("create") && !args[0].equals("load")) {
            throw new RuntimeException();
        }
        Action action = Action.valueOf(args[0]);
        String inputPath = args[1];
        String outputPath;
        if (args.length == 3) {
            outputPath = args[2];
        } else {
            if (action.equals(Action.load)) {
                outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + ".xml";
            } else if (action.equals(Action.create)) {
                outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + ".tlk";
            } else {
                throw new RuntimeException();
            }
        }

        if (action.equals(Action.load)) {
            TlkFile tf = new TlkFile();
            tf.loadTlkData(inputPath, true);
            tf.dumpToFile(outputPath, FileFormat.xml);
            // debug
            // tf.PrintHuffmanTree();
        } else {
            HuffmanCompression hc = new HuffmanCompression();
            hc.loadInputData(inputPath, FileFormat.xml, true);
            hc.saveToTlkFile(outputPath, true);
        }
    }

    private enum Action {
        load,
        create
    }
}
