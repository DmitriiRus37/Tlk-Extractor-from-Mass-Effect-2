package ru;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TlkFile  {
    TlkHeader header;
    public List<TlkStringRef> stringRefs;
    List<HuffmanNode> characterTree;
    BitArray bits;
    ByteOrder byteOrder = ByteOrder.nativeOrder();

    /** <summary>
     *      Loads a TLK file into memory.
     * </summary>
     * <param name="fileName"></param>
     */
    public void loadTlkData(String fileName, boolean isPC) throws IOException {
        /** **************** STEP ONE ****************
         *          -- load TLK file header --
         *        reading first 28 (4 * 7) bytes
         */

        /** using LittleEndian for PC architecture and BigEndian for Xbox360 */
        PositionInputStream r = baseStreamSeek( 0, fileName);

        header = new TlkHeader(r);
        /** read possibly correct ME2 TLK file, but from another platfrom */
        if (header.magic == 1416391424) {
            throw new RuntimeException();
        }
        /** read definitely NOT a ME2 TLK ile */
        if (header.magic != 7040084) {
            throw new RuntimeException();
        }
        //DebugTools.PrintHeader(Header);

        /** **************** STEP TWO ****************
         *  -- read and store Huffman Tree nodes --
         */
        /** jumping to the beginning of Huffmann Tree stored in TLK file */
        long pos = r.getPosition();
        r = baseStreamSeek( pos + (header.entry1Count + header.entry2Count)*8L, fileName);

        characterTree = new LinkedList<>();
        for (int i = 0; i < header.treeNodeCount; i++) {
            characterTree.add(new HuffmanNode(r));
        }

        /** **************** STEP THREE ****************
         *  -- read all of coded data into memory --
         */
        byte[] data = new byte[header.dataLen];
        r.read(data,0, data.length);
        /** and store it as raw bits for further processing */
        bits = new BitArray(data);
        /** rewind BinaryReader just after the Header
         * at the beginning of TLK Entries data
         * */
        r = baseStreamSeek(pos, fileName);

        /** **************** STEP FOUR ****************
         * -- decode (basing on Huffman Tree) raw bits data into actual strings --
         * and store them in a Dictionary<int, string> where:
         *   int: bit offset of the beginning of data (offset starting at 0 and counted for Bits array)
         *        so offset == 0 means the first bit in Bits array
         *   string: actual decoded String
         */
        Map<Integer, String> rawStrings = new HashMap<>();
        int offset = 0;
        Wrap offsetWrap = new Wrap(offset);
        while (offsetWrap.getValue() < bits.length()) {
            int key = offsetWrap.getValue();
            /** read the String and update 'offset' variable to store NEXT String offset */
            String s = GetString(offsetWrap);
            rawStrings.put(key, s);
        }

        /** **************** STEP FIVE ****************
         *         -- bind data to String IDs --
         * go through Entries in TLK file and read it's String ID and offset
         * then check if offset is a key in rawStrings and if it is, then bind data.
         * Sometimes there's no such key, in that case, our String ID is probably a substring
         * of another String present in rawStrings.
         */
        stringRefs = new LinkedList<>();
        for (int i = 0; i < header.entry1Count + header.entry2Count; i++) {
            TlkStringRef sref = new TlkStringRef(r);
            sref.position = i;
            if (sref.bitOffset >= 0) {
                /** actually, it should store the fullString and subStringOffset,
                 * but as we don't have to use this compression feature,
                 * we will store only the part of String we need
                 * */
                /** int key = rawStrings.Keys.Last(c => c < sref.BitOffset);
                 * String fullString = rawStrings[key];
                 * int subStringOffset = fullString.LastIndexOf(partString);
                 * sref.StartOfString = subStringOffset;
                 * sref.Data = fullString;
                 */
                sref.Data = rawStrings.containsKey(sref.bitOffset) ?
                        rawStrings.get(sref.bitOffset) :
                        GetString(new Wrap(sref.bitOffset));
            }
            stringRefs.add(sref);
        }
        r.close();
    }

    /** <summary>
     *      Writes data stored in memory to an appriopriate text format.
     *  </summary>
     *  <param name="fileName"></param>
     *  <param name="ff"></param>
     */
    public void dumpToFile(String fileName, FileFormat ff) throws XMLStreamException, IOException, TransformerConfigurationException {

        Files.deleteIfExists(Paths.get(fileName));
        /** for now, it's better not to sort, to preserve original order */
        // StringRefs.Sort(CompareTlkStringRef);

        if (ff.equals(FileFormat.xml)) {
            saveToXmlFile(fileName);
            prettyXmlFile(fileName);
        } else {
            saveToTextFile(fileName);
        }
    }

    /** <summary>
     *      Starts reading 'Bits' array at position 'bitOffset'. Read data is
     *      used on a Huffman Tree to decode read bits into real strings.
     *      'bitOffset' variable is updated with last read bit PLUS ONE (first unread bit).
     *  </summary>
     *  <param name="bitOffset"></param>
     *  <returns>
     *      decoded String or null if there's an error (last string's bit code is incomplete)
     *  </returns>
     *  <remarks>
     *      Global variables used:
     *      List(of HuffmanNodes) CharacterTree
     *      main.java.ru.BitArray Bits
     *  </remarks>
     */
    private String GetString(Wrap bitOffsetWrap) {
        HuffmanNode root = characterTree.get(0);
        HuffmanNode curNode = root;
        String curString = "";
        int i;
        for (i = bitOffsetWrap.getValue(); i < bits.length(); i++) {
            /** reading bits' sequence and decoding it to Strings while traversing Huffman Tree */
            int nextNodeID = bits.getRev(i) ? curNode.rightNodeId : curNode.leftNodeId;

            if (nextNodeID >= 0) {
                /** it's an internal node - keep looking for a leaf */
                curNode = characterTree.get(nextNodeID);
            } else {
                /** it's a leaf! */
                char c = 0;
                try {
                    c = BitConverter.toCharRev(BitConverter.GetBytes(0xffff - nextNodeID), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (c != '\0') {
                    /** it's not NULL */
                    curString += c;
                    curNode = root;
                } else {
                    /** it's a NULL terminating processed string, we're done */
                    i++;
                    bitOffsetWrap.setValue(i);
                    return curString;
                }
            }
        }
        i++;
        bitOffsetWrap.setValue(i);
        return null;
    }

    /** <summary>
     *       Writing data in an XML format.
     *  </summary>
     * <param name="fileName"></param>
     * */
    private void saveToXmlFile(String fileName)
            throws XMLStreamException, IOException, TransformerConfigurationException {

        // Creating FileWriter object
        Writer fileWriter = new FileWriter(fileName);

        // Getting the XMLOutputFactory instance
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        // Creating XMLStreamWriter object from xmlOutputFactory.
        XMLStreamWriter xr = xmlOutputFactory.createXMLStreamWriter(fileWriter);

        xr.writeStartDocument("utf-8", "1.0");
        xr.writeStartElement("tlkFile");
        xr.writeAttribute("TLKToolVersion", "how to get version???");
        xr.writeComment("Male entries section begin (ends at position " + (header.entry1Count - 1) + ")");

        for (TlkStringRef s : stringRefs) {
            if (s.position == header.entry1Count) {
                xr.writeComment("Male entries section end");
                xr.writeComment("Female entries section begin (ends at position " + (header.entry1Count + header.entry2Count - 1) + ")");
            }
            xr.writeStartElement("string");

            xr.writeStartElement("id");
            xr.writeCharacters(String.valueOf(s.stringId));
            xr.writeEndElement(); // </id>

            xr.writeStartElement("position");
            xr.writeCharacters(String.valueOf(s.position));
            xr.writeEndElement(); // </position>

            xr.writeStartElement("data");// </data>
            xr.writeCharacters(s.bitOffset < 0 ? "-1" : s.Data);
            xr.writeEndElement(); // </data>

            xr.writeEndElement(); // </string>
        }
        xr.writeComment("Female entries section end");
        xr.writeEndElement(); // </tlkFile>
        xr.flush();
        xr.close();
    }

    private void prettyXmlFile(String fileName) throws IOException {
        String xmlString;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            xmlString = stringBuilder.toString();
        }

        try {
            InputSource src = new InputSource(new StringReader(xmlString));
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, true ? "yes" : "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            PrintWriter writer = new PrintWriter(fileName, StandardCharsets.UTF_8);
            transformer.transform(new DOMSource(document), new StreamResult(writer));

        } catch (IOException | IllegalArgumentException |
                ParserConfigurationException | TransformerException | SAXException e) {
            throw new RuntimeException("Error occurs when pretty-printing xml:\n" + xmlString, e);
        }
    }

    /** <summary>
     *      Writing data in a normal text format.
     * </summary>
     * <remarks>
     *      Currently not used by main application, but it works ok.
     * </remarks>
     * <param name="fileName"></param>
     */
    private void saveToTextFile(String fileName)
    {
        int totalCount = stringRefs.size();
        int count = 0;
        int lastProgress = -1;

        for (TlkStringRef s : stringRefs) {
            String line = s.stringId + ": " + s.Data + "\r\n";

            try (FileWriter fw = new FileWriter(fileName, true)) {
                fw.write(line);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int progress = (++count * 100) / totalCount;
            if (progress > lastProgress) {
                lastProgress = progress;
            }
        }
    }

    public static int readInt32(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1));
    }

    public static PositionInputStream baseStreamSeek(long pos, String fileName) throws IOException {
        PositionInputStream r = new PositionInputStream(new FileInputStream(fileName));
        r.skipNBytes(pos);
        return r;
    }

//        /** for sorting */
//        private static int CompareTlkStringRef(ru.TlkHeader.ru.TlkStringRef strRef1, ru.TlkHeader.ru.TlkStringRef strRef2) {
//            int result = strRef1.stringId.compareTo(strRef2.stringId);
//            return result;
//        }
}

